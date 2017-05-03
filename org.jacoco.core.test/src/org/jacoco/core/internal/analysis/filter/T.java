package org.jacoco.core.internal.analysis.filter;

import com.sonar.sslr.api.typed.ActionParser;
import org.jacoco.core.internal.Java9Support;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.sonar.java.ast.parser.JavaParser;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.BlockTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.TryStatementTree;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class T {

	private static final IFilter filter = new FinallyFilter();

	public static void main(String[] args) throws IOException {
		new T().run();
	}

	private void run() throws IOException {
		if (false) {
			// process JDK
			final String home = "/Users/evgeny.mandrikov";
			process(new File(home + "/.java-select/versions/1.7/src.zip"),
					new File(home
							+ "/.java-select/versions/1.7/jre/lib/rt.jar"));

		}

		if (true) {
			// process central
			scan(new File("/Users/evgeny.mandrikov/tmp/central/"));
		}
	}

	private long bytes;

	private void scan(File dir) throws IOException {
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		for (File f : files) {
			if (f.isDirectory()) {
				scan(f);
			} else {
				final String name = f.getAbsolutePath();
				if (name.endsWith("-sources.jar")) {
					System.out.println(
							"after " + (bytes / 1024 / 1024) + " Mb " + name);
					final File classesJar = new File(
							name.substring(0, name.lastIndexOf("-sources.jar"))
									+ ".jar");
					if (classesJar.exists()) {
//						process(f, classesJar);
						verifyLines(classesJar);
					}
				}
			}
		}
	}

	private void verifyLines(final File classes) throws IOException {
		bytes += classes.length();

		final ZipFile jar = new ZipFile(classes);
		final Enumeration e = jar.entries();
		while (e.hasMoreElements()) {
			final ZipEntry entry = (ZipEntry) e.nextElement();
			if (!entry.getName().endsWith(".class")) {
				continue;
			}
			final InputStream inputStream = jar.getInputStream(entry);

			try {
				getFinallyLinesFromBytecode(inputStream);
			} catch (RuntimeException ex) {
				throw new RuntimeException(classes.getAbsolutePath() + " " + entry.getName(), ex);
			}
		}
		jar.close();
	}

	private static final ActionParser<Tree> parser = JavaParser.createParser();

	private void process(final File source, final File classes)
			throws IOException {

		bytes += classes.length();

		final Map<String, Output> outputs = new HashMap<String, Output>();

		{
			final ZipFile jar = new ZipFile(source);
			final Enumeration e = jar.entries();
			while (e.hasMoreElements()) {
				final ZipEntry entry = (ZipEntry) e.nextElement();
				if (!entry.getName().endsWith(".java")) {
					continue;
				}
				final InputStream inputStream = jar.getInputStream(entry);

				String name = entry.getName();
				name = name.substring(0, name.lastIndexOf(".java"));
				if (!outputs.containsKey(name)) {
					outputs.put(name, new Output());
				}
				final Output output = outputs.get(name);
				output.finallySourceLines
						.addAll(getFinallyLinesFromSource(inputStream));
			}
			jar.close();
		}

		{
			final ZipFile jar = new ZipFile(classes);
			final Enumeration e = jar.entries();
			while (e.hasMoreElements()) {
				final ZipEntry entry = (ZipEntry) e.nextElement();
				if (!entry.getName().endsWith(".class")) {
					continue;
				}
				final InputStream inputStream = jar.getInputStream(entry);

				String name = entry.getName();
				name = name.substring(0, name.lastIndexOf(".class"));
				if (name.indexOf('$') > 0) {
					name = name.substring(0, name.indexOf('$'));
				}
				final Output output = outputs.get(name);
				if (output == null) {
					// System.err.println("no sources for " + name);
					continue;
				}

				output.hasBytecode = true;
				try {
					output.finallyBytecodeLines
							.addAll(getFinallyLinesFromBytecode(inputStream));
				} catch (RuntimeException ex) {
					System.err.println("Exception in " + entry.getName());
					ex.printStackTrace(System.err);
					// throw ex;
				}
			}
			jar.close();
		}

		for (Map.Entry<String, Output> entry : outputs.entrySet()) {
			Output o = entry.getValue();
			if (!o.hasBytecode) {
				continue;
			}

			// TODO this only checks that all found lines in bytecode are within
			// finally blocks, but not that all finally blocks are found
			Set<Integer> miss = new HashSet<Integer>();
			boolean match = true;
			for (int line : o.finallyBytecodeLines) {
				boolean inside = false;
				for (Range range : o.finallySourceLines) {
					if (range.from <= line && line <= range.to) {
						inside = true;
						break;
					}
				}
				if (!inside) {
					miss.add(line);
					match = false;
				}
			}
			if (!match) {
				System.err.println(source);
				System.err.println(entry.getKey());
				System.err.println("s: " + o.finallySourceLines);
				System.err.println("b: " + o.finallyBytecodeLines);
				System.err.println("m: " + miss);
			}
		}
	}

	private static Set<Integer> getFinallyLinesFromBytecode(
			final InputStream inputStream) throws IOException {
		final Set<Integer> result = new HashSet<Integer>();
		ClassReader classReader;
		try {
			classReader = new ClassReader(Java9Support.downgradeIfRequired(Java9Support.readFully(inputStream)));
		} catch (Exception e) {
			// corrupted inputStream
			e.printStackTrace(System.err);
			return result;
		}
		final ClassNode classNode = new ClassNode();
		classReader.accept(classNode, 0);
		for (MethodNode methodNode : classNode.methods) {
			try {
				filter.filter("", "", methodNode, new IFilterOutput() {
					public void ignore(AbstractInsnNode fromInclusive,
							AbstractInsnNode toInclusive) {
					}

					public void merge(AbstractInsnNode i1,
							AbstractInsnNode i2) {
						int line1 = getLine(i1);
						int line2 = getLine(i1);
						if (line1 != line2) {
							throw new UnsupportedOperationException();
						}
						if (line1 == -1) {
//							System.err.println("NO LINE INFORMATION in " + methodNode.name + " " + methodNode.desc);
						}
						result.add(line1);
					}
				});
			} catch (Exception e) {
				throw new RuntimeException(
						"Exception during analysis of method "
								+ methodNode.name,
						e);
			}

			if (false)
			new SynchronizedFilter().filter("", "", methodNode,
					new IFilterOutput() {
						public void ignore(AbstractInsnNode fromInclusive,
								AbstractInsnNode toInclusive) {
							for (AbstractInsnNode i = fromInclusive; i != toInclusive; i = i
									.getNext()) {
								result.remove(getLine(i));
							}
							result.remove(getLine(toInclusive));
						}

						public void merge(AbstractInsnNode i1,
								AbstractInsnNode i2) {
						}
					});
		}
		return result;
	}

	private static int getLine(AbstractInsnNode i) {
		while (i != null && i.getType() != AbstractInsnNode.LINE) {
			i = i.getPrevious();
		}
		if (i == null) {
			return -1;
		}
		return ((LineNumberNode) i).line;
	}

	private static List<Range> getFinallyLinesFromSource(
			final InputStream inputStream) throws IOException {
		final List<Range> result = new ArrayList<Range>();
		final byte[] sourceBytes = Java9Support.readFully(inputStream);
		parser.parse(new String(sourceBytes)).accept(new BaseTreeVisitor() {
			@Override
			public void visitTryStatement(TryStatementTree tree) {
				super.visitTryStatement(tree);

				if (!tree.resources().isEmpty()) {
					// try-with-resources
					int line = tree.block().closeBraceToken().line();
					result.add(new Range(line, line));
				}

				BlockTree f = tree.finallyBlock();
				if (f == null) {
					return;
				}
				result.add(
						new Range(f.firstToken().line(), f.lastToken().line()));
			}
		});
		return result;
	}

	static class Range {
		final int from;
		final int to;

		Range(int from, int to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public String toString() {
			return "{" + from + "-" + to + "}";
		}
	}

	static class Output {
		final List<Range> finallySourceLines = new ArrayList<Range>();

		boolean hasBytecode;
		final Set<Integer> finallyBytecodeLines = new HashSet<Integer>();
	}

}

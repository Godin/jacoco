package org.jacoco.core.internal.analysis.filter;

import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.typed.ActionParser;
import org.jacoco.core.internal.Java9Support;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.sonar.java.ast.parser.JavaParser;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.TryStatementTree;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class T {

	public static void main(String[] args) throws IOException {
		new T().run();
	}

	private void run() throws IOException {
//		 scan(new File("/home/godin/.m2/repository"));

//		 scan(new
//		 File("/home/godin/.m2/repository/org/sonarsource/orchestrator/sonar-orchestrator/3.11"));

//		 scan(new
//		 File("/home/godin/.m2/repository/org/sonarsource/java/java-frontend/4.7.1.9272"));

//		 scan(new
//		 File("/home/godin/.m2/repository/org/eclipse/jgit/org.eclipse.jgit/4.1.2.201602141800-r/"));

//		scan(new
//		File("/home/godin/.m2/repository/org/eclipse/jdt/org.eclipse.jdt.core/3.12.2/"));

//		process(new File("/home/godin/.java-select/versions/1.7/src.zip"),
//				new File("/home/godin/.java-select/versions/1.7/jre/lib/rt.jar"));
//
//		process(new File("/home/godin/.java-select/versions/1.8/src.zip"),
//				new File("/home/godin/.java-select/versions/1.8/jre/lib/rt.jar"));

		try {
			scan(new File("/Users/evgeny.mandrikov/tmp/central"));
		} finally {
			System.out.println("Scanned " + (bytes / 1024 / 1024) + " Mb");
			System.out.println("try statements: " + tryStatements);
			System.out.println(
					"try-with-resources statements: " + tryWithResourcesStatements);
		}
	}

	private long bytes;
	private long tryStatements;
	private long tryWithResourcesStatements;

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
					System.out.println("after " + (bytes / 1024 / 1024) + " Mb " + name);
					final File classesJar = new File(
							name.substring(0, name.lastIndexOf("-sources.jar"))
									+ ".jar");
					if (classesJar.exists()) {
						process(f, classesJar);
					}
				}
			}
		}
	}

	private final ActionParser<Tree> parser = JavaParser.createParser();

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

				byte[] sourceBytes = Java9Support.readFully(inputStream);
				try {
					parser.parse(new String(sourceBytes))
							.accept(new BaseTreeVisitor() {
								@Override
								public void visitTryStatement(
										TryStatementTree node) {
									if (!node.resources().isEmpty()) {
										output.trySourceLines
												.add(node.tryKeyword().line());
										output.curlySourceLines.add(node.block()
												.closeBraceToken().line());
										tryWithResourcesStatements++;
									} else {
										tryStatements++;
									}
									super.visitTryStatement(node);
								}
							});
				} catch (RecognitionException ex) {
					ex.printStackTrace();
					return;
				}
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
//					System.err.println("no sources for " + name);
					continue;
				}

				final ClassReader classReader = new ClassReader(inputStream);
				final ClassNode classNode = new ClassNode();
				classReader.accept(classNode, 0);
				for (MethodNode methodNode : classNode.methods) {
					javacFilter.filter(methodNode, new JavacOutput(output));
					ecjFilter.filter(methodNode, new EcjOutput(output));
				}
			}
			jar.close();
		}

		for (Map.Entry<String, Output> entry : outputs.entrySet()) {
			Output o = entry.getValue();
			boolean match = (o.trySourceLines.isEmpty()
					&& o.javacBytecodeLines.isEmpty()
					&& o.ecjBytecodeLines.isEmpty())
					|| (o.ecjBytecodeLines.isEmpty()
							&& o.javacBytecodeLines.equals(o.trySourceLines))
					|| (o.javacBytecodeLines.isEmpty()
							&& o.ecjBytecodeLines.equals(o.curlySourceLines));
			if (!match) {
				System.err.println(source);
				System.err.println(entry.getKey());
				System.err.println("s: " + o.trySourceLines);
				System.err.println("b: " + o.javacBytecodeLines);
				System.err.println("s: " + o.curlySourceLines);
				System.err.println("b: " + o.ecjBytecodeLines);
			}
		}
	}

	private final IFilter javacFilter = new TryWithResourcesJavacFilter();
	private final IFilter ecjFilter = new TryWithResourcesEcjFilter();

	static class JavacOutput implements IFilterOutput {
		final Output delegate;
		int count;

		JavacOutput(Output delegate) {
			this.delegate = delegate;
		}

		public void ignore(final AbstractInsnNode fromInclusive,
				final AbstractInsnNode toInclusive) {
			count++;
			if (count % 2 == 0) {
				int line = ((LineNumberNode) fromInclusive.getNext()).line;
				delegate.javacBytecodeLines.add(line);
			}
		}
	}

	static class EcjOutput implements IFilterOutput {
		final Output delegate;

		EcjOutput(Output delegate) {
			this.delegate = delegate;
		}

		public void ignore(final AbstractInsnNode fromInclusive,
				final AbstractInsnNode toInclusive) {
			AbstractInsnNode i = fromInclusive;
			while (i.getType() != AbstractInsnNode.LINE) {
				i = i.getPrevious();
			}
			int line = ((LineNumberNode) i).line;
			delegate.ecjBytecodeLines.add(line);
//			throw new AssertionError();
			System.err.println("ECJ filter triggered");
		}
	}

	static class Output {
		final Set<Integer> trySourceLines = new HashSet<Integer>();
		final Set<Integer> curlySourceLines = new HashSet<Integer>();

		final Set<Integer> javacBytecodeLines = new HashSet<Integer>();
		final Set<Integer> ecjBytecodeLines = new HashSet<Integer>();
	}

}

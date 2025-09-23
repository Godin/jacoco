/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin;

import org.jacoco.core.internal.InputStreams;
import org.jacoco.core.internal.analysis.filter.IFilter;
import org.jacoco.core.internal.analysis.filter.IFilterContext;
import org.jacoco.core.internal.analysis.filter.IFilterOutput;
import org.jacoco.core.internal.analysis.filter.KotlinSafeCallOperatorFilter;
import org.jacoco.core.internal.analysis.filter.Replacements;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FilterScanner {

	private static BitSet apply(final ClassNode classNode, IFilter filter) {
		final IFilterContext context = new IFilterContext() {
			public String getClassName() {
				return classNode.name;
			}

			public String getSuperClassName() {
				return classNode.superName;
			}

			public Set<String> getClassAnnotations() {
				final HashSet<String> annotations = new HashSet<String>();
				if (classNode.visibleAnnotations != null) {
					for (AnnotationNode annotationNode : classNode.visibleAnnotations) {
						annotations.add(annotationNode.desc);
					}
				}
				return annotations;
			}

			public Set<String> getClassAttributes() {
				return Collections.emptySet();
			}

			public String getSourceFileName() {
				return classNode.sourceFile;
			}

			public String getSourceDebugExtension() {
				return classNode.sourceDebug;
			}
		};
		final BitSet lines = new BitSet();
		for (final MethodNode m : classNode.methods) {
			final HashSet<AbstractInsnNode> instructions = new HashSet<AbstractInsnNode>();
			final IFilterOutput output = new IFilterOutput() {
				public void ignore(final AbstractInsnNode fromInclusive,
						final AbstractInsnNode toInclusive) {
					for (AbstractInsnNode i = fromInclusive; i != toInclusive; i = i
							.getNext()) {
						instructions.add(i);
					}
				}

				public void merge(final AbstractInsnNode i1,
						final AbstractInsnNode i2) {
					instructions.add(i1);
					instructions.add(i2);
				}

				public void replaceBranches(AbstractInsnNode source,
						Replacements replacements) {
					instructions.add(source);
				}
			};
			filter.filter(m, context, output);
			int line = -1;
			for (AbstractInsnNode i : m.instructions) {
				if (i.getType() == AbstractInsnNode.LINE) {
					line = ((LineNumberNode) i).line;
				}
				if (instructions.contains(i)) {
					lines.set(line);
				}
			}
		}
		return lines;
	}

	private static void scan(final File file, final Map<String, BitSet> result)
			throws IOException {
		final File[] files = file.listFiles();
		if (files != null) {
			for (final File f : files) {
				scan(f, result);
			}
			return;
		}
		if (!file.getName().endsWith(".class")) {
			return;
		}
		final FileInputStream fileInputStream = new FileInputStream(file);
		final ClassNode classNode = classNode(
				InputStreams.readFully(fileInputStream));
		fileInputStream.close();
		final BitSet lines = apply(classNode,
				new KotlinSafeCallOperatorFilter());
		if (!lines.isEmpty()) {
			final String packageName = classNode.name.substring(0,
					Math.max(0, classNode.name.lastIndexOf('/')));
			final String sourceName = packageName + "/" + classNode.sourceFile;
			final BitSet bitSet = result.get(sourceName);
			if (bitSet != null) {
				lines.or(bitSet);
			}
			result.put(sourceName, lines);
		}
	}

	private static void printSources(final File file,
			final Map<String, BitSet> i) throws IOException {
		final File[] files = file.listFiles();
		if (files != null) {
			for (final File f : files) {
				printSources(f, i);
			}
			return;
		}
		for (Map.Entry<String, BitSet> entry : i.entrySet()) {
			final String sourceName = entry.getKey();
			if (file.getAbsolutePath().endsWith(sourceName)) {
				System.out.println(file.getAbsolutePath());
				final BufferedReader bufferedReader = new BufferedReader(
						new FileReader(file));
				int lineNr = 0;
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					lineNr++;
					System.out.println("|" + line);
					if (entry.getValue().get(lineNr)) {
						System.out.println("Filtered");
					}
				}
				bufferedReader.close();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		args = new String[] { "/Users/godin/projects/jacoco/kotlin/" };
		// args = new String[] {
		// "/Users/godin/projects/sonarsource/sonar-kotlin/" };
		File dir = new File(args[0]);
		HashMap<String, BitSet> map = new HashMap<String, BitSet>();
		scan(dir, map);
		printSources(dir, map);
	}

	private static ClassNode classNode(final byte[] bytes) {
		final ClassNode classNode = new ClassNode();
		new ClassReader(bytes).accept(classNode, 0);
		return classNode;
	}

}

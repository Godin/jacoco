/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Filters duplicates of finally blocks that compiler generates.
 */
public final class FinallyFilter implements IFilter {

	public static void main(String[] args) throws IOException {
		// new Analyzer(null, null).analyzeAll(new
		// File("/tmp/jacoco/colt-1.0.3/edu/oswego/cs/dl/util/concurrent/Rendezvous.class"));

		String filename = "/tmp/jacoco/colt-1.0.3/edu/oswego/cs/dl/util/concurrent/Rendezvous.class";
//		String filename = "/tmp/jacoco/Fun.class";
		ClassReader classReader = new ClassReader(
				new FileInputStream(filename));

		final ClassNode classNode = new ClassNode();
		classReader.accept(classNode, 0);

		for (MethodNode node : classNode.methods) {
			MethodNode nodeWithoutJsr = new MethodNode(Opcodes.ASM5,
					node.access, node.name, node.desc, node.signature,
					node.exceptions.toArray(new String[0]));
			JSRInlinerAdapter adapter = new JSRInlinerAdapter(nodeWithoutJsr,
					node.access, node.name, node.desc, node.signature,
					node.exceptions.toArray(new String[0]));
			node.accept(adapter);

			System.out.println(InsnPrinter.prettyprint(nodeWithoutJsr));

			new FinallyFilter().filter("", "", nodeWithoutJsr,
					new IFilterOutput() {
						public void ignore(AbstractInsnNode fromInclusive,
								AbstractInsnNode toInclusive) {

						}

						public void merge(AbstractInsnNode i1,
								AbstractInsnNode i2) {

						}
					});
		}
	}

	public static class InsnPrinter {
		private static final Printer printer = new Textifier();
		private static final TraceMethodVisitor methodPrinter = new TraceMethodVisitor(printer);

		public static String prettyprint(MethodNode m) {
			StringWriter sw = new StringWriter();
			sw.append(m.name + "\n");
			for (TryCatchBlockNode t : m.tryCatchBlocks) {
				t.accept(methodPrinter);
				printer.print(new PrintWriter(sw));
				printer.getText().clear();
			}
			for (AbstractInsnNode i = m.instructions.getFirst(); i != null; i = i.getNext()) {
				i.accept(methodPrinter);
				printer.print(new PrintWriter(sw));
				printer.getText().clear();
			}
			return sw.toString();
		}
	}

	public void filter(final String className, final String superClassName,
			final MethodNode methodNode, final IFilterOutput output) {
		for (TryCatchBlockNode tryCatchBlock : methodNode.tryCatchBlocks) {
			if (tryCatchBlock.type == null) {
				filter(tryCatchBlock, output);
				filterEcj(tryCatchBlock, output);
			}
		}
	}

	private void filter(final TryCatchBlockNode tryCatchBlock,
			final IFilterOutput output) {
		final AbstractInsnNode n = next(tryCatchBlock.end);
		final AbstractInsnNode e = next(tryCatchBlock.handler);

		final int size = size(e);
		if (size == 0) {
			return;
		}
		if (!isSame(size, next(e), n)) {
			return;
		}
		filter(output, size, e, n);
	}

	private static void filter(final IFilterOutput output, final int size,
			AbstractInsnNode e, AbstractInsnNode n) {
		output.ignore(e, e);
		e = next(e);
		for (int i = 0; i < size; i++) {
			output.merge(e, n);
			n = next(n);
			e = next(e);
		}
		output.ignore(e, next(e));
		if (n != null && n.getOpcode() == Opcodes.GOTO) {
			output.ignore(n, n);
		}
	}

	private static int size(AbstractInsnNode e) {
		if (e.getOpcode() != Opcodes.ASTORE) {
			return 0;
		}
		final int var = ((VarInsnNode) e).var;
		int size = 0;
		while (true) {
			e = next(e);
			if (e == null) {
				return 0;
			}
			if (Opcodes.ALOAD == e.getOpcode()
					&& ((VarInsnNode) e).var == var) {
				break;
			}
			size++;
		}
		e = next(e);
		if (Opcodes.ATHROW != e.getOpcode()) {
			return 0;
		}
		return size;
	}

	private static boolean isSame(final int size, AbstractInsnNode e,
			AbstractInsnNode n) {
		for (int i = 0; i < size; i++) {
			if (e.getOpcode() != n.getOpcode()) {
				return false;
			}
			e = next(e);
			n = next(n);
		}
		return true;
	}

	private void filterEcj(final TryCatchBlockNode tryCatch,
			final IFilterOutput output) {
		AbstractInsnNode e = next(tryCatch.handler);

		final int size = size(e);
		if (size == 0) {
			return;
		}

		e = next(e);
		for (int i = 0; i < size; i++) {
			e = next(e);
		}
		final AbstractInsnNode n = next(next(e));
		if (n == null) {
			return;
		}
		e = next(tryCatch.handler);
		if (!isSame(size, next(e), n)) {
			return;
		}

		for (AbstractInsnNode i = next(tryCatch.end); i != tryCatch.start; i = i
				.getPrevious()) {
			if (Opcodes.GOTO == i.getOpcode()
					&& n == next(((JumpInsnNode) i).label)) {
				filter(output, size, e, n);
				break;
			}
		}
	}

	private static AbstractInsnNode next(AbstractInsnNode node) {
		do {
			node = node.getNext();
		} while (node != null && (node.getType() == AbstractInsnNode.FRAME
				|| node.getType() == AbstractInsnNode.LABEL
				|| node.getType() == AbstractInsnNode.LINE));
		return node;
	}

}

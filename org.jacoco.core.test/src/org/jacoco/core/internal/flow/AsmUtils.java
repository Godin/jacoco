/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.internal.flow;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class AsmUtils {

	private AsmUtils() {
	}

	public static ClassNode classBytesToClassNode(final byte[] classBytes) {
		final ClassNode classNode = new ClassNode();
		new ClassReader(classBytes).accept(classNode, ClassReader.SKIP_FRAMES);
		return classNode;
	}

	private static MethodProbesAdapter createMethodProbesAdapter() {
		final TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(
				new Textifier());
		final MethodProbesVisitor methodProbesVisitor = new MethodProbesVisitor(
				traceMethodVisitor) {
			@Override
			public void visitProbe(final int probeId) {
				traceMethodVisitor.p.text
						.add("    // Probe[" + probeId + "]\n");
			}

			@Override
			public void visitJumpInsnWithProbe(final int opcode,
					final Label label, final int probeId, final IFrame frame) {
				traceMethodVisitor.p.text
						.add("    // JumpInsnWithProbe[" + probeId + "]:\n");
				visitJumpInsn(opcode, label);
			}

			@Override
			public void visitInsnWithProbe(final int opcode,
					final int probeId) {
				traceMethodVisitor.p.text
						.add("    // InsnWithProbe[" + probeId + "]:\n");
				visitInsn(opcode);
			}

			@Override
			public void visitTableSwitchInsnWithProbes(final int min,
					final int max, final Label dflt, final Label[] labels,
					final IFrame frame) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void visitLookupSwitchInsnWithProbes(final Label dflt,
					final int[] keys, final Label[] labels,
					final IFrame frame) {
				throw new UnsupportedOperationException();
			}
		};
		final MethodProbesAdapter methodProbesAdapter = new MethodProbesAdapter(
				methodProbesVisitor, new IProbeIdGenerator() {
					private int id;

					public int nextId() {
						return id++;
					}
				});
		return methodProbesAdapter;
	}

	public static String instrumentToString(final MethodNode methodNode) {
		final MethodProbesAdapter methodProbesAdapter = createMethodProbesAdapter();
		final TraceMethodVisitor traceMethodVisitor = (TraceMethodVisitor) methodProbesAdapter
				.getDelegate().getDelegate();
		LabelFlowAnalyzer.markLabels(methodNode);
		methodNode.accept(methodProbesAdapter);
		final StringWriter stringWriter = new StringWriter();
		traceMethodVisitor.p.print(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}

	public static String toString(final byte[] classBytes) {
		ClassNode classNode = classBytesToClassNode(classBytes);
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter);
		classNode.accept(new TraceClassVisitor(printWriter));
		return stringWriter.toString();
	}

	/**
	 * @deprecated this method doesn't print modifications done by
	 *             {@link org.jacoco.core.internal.instr.MethodInstrumenter}
	 */
	@Deprecated
	public static String instrumentToString(final ClassNode classNode) {
		final MethodProbesAdapter methodProbesAdapter = createMethodProbesAdapter();
		final TraceMethodVisitor traceMethodVisitor = (TraceMethodVisitor) methodProbesAdapter
				.getDelegate().getDelegate();
		traceMethodVisitor.p.visit(classNode.version, classNode.access,
				classNode.name, classNode.signature, classNode.superName, null);
		for (final MethodNode methodNode : classNode.methods) {
			traceMethodVisitor.p.visitMethod(methodNode.access, methodNode.name,
					methodNode.desc, methodNode.signature, null);
			LabelFlowAnalyzer.markLabels(methodNode);
			methodNode.accept(methodProbesAdapter);
		}
		final StringWriter stringWriter = new StringWriter();
		traceMethodVisitor.p.print(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}

}

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
package org.jacoco.core.internal.instr;

import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.internal.flow.IFrame;
import org.jacoco.core.internal.flow.IProbeIdGenerator;
import org.jacoco.core.internal.flow.LabelFlowAnalyzer;
import org.jacoco.core.internal.flow.MethodProbesAdapter;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.jacoco.core.test.JvmProcess;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * Unit test for {@link MethodInstrumenter}.
 *
 * Unlike {@link MethodInstrumenterTest} uses
 * {@link MethodInstrumenter#accept(MethodNode, MethodVisitor)} as entry point.
 *
 * TODO And so not really unit, because requires {@link MethodProbesAdapter}.
 */
public class MethodInstrumenterMonitorsTest {

	/**
	 * {@link MethodInstrumenter#visitProbe(int)}
	 */
	@Test
	public void test_visitProbe() {
		final MethodBuilder m = new MethodBuilder();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITORENTER);
		final Label label = m.label();
		// visitProbe
		// probe 0 skipped
		m.visitLabel(label);
		m.visitLineNumber(42, label);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(MONITOREXIT);
		m.visitMethodInsn(INVOKESTATIC, "", "m", "()V", false);
		m.expected.p.text.add("// probe 1\n");
		m.visitInsn(RETURN);
		m.visitMaxs(0, 0);
		Assert.assertEquals(toString(m.expected), instrument(m.original));
	}

	/**
	 * {@link MethodInstrumenter#visitJumpInsnWithProbe(int, Label, int, IFrame)}
	 */
	@Test
	public void test_visitJumpInsnWithProbe() {
		final MethodBuilder m = new MethodBuilder();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		final Label target = new Label();
		// visitJumpInsnWithProbe
		// probe 0 skipped
		m.visitJumpInsn(Opcodes.IFNULL, target);
		// visitProbe
		// probe 1 skipped
		m.visitLabel(target);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.expected.p.text.add("// probe 2\n");
		m.visitInsn(Opcodes.RETURN);
		m.visitMaxs(0, 0);
		Assert.assertEquals(toString(m.expected), instrument(m.original));
	}

	/**
	 * This test demonstrates that
	 * {@link org.objectweb.asm.tree.TryCatchBlockNode#end} should be exclusive
	 */
	@Test
	public void test() {
		final MethodBuilder m = new MethodBuilder();
		Label start = m.label();
		Label end = m.label();
		Label handler = m.label();
		m.visitTryCatchBlock(start, end, handler, null);
		m.visitLabel(start);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitLabel(end);
		// we need visitProbe here
		// successor of previous, method invocation line
		m.expected.p.text.add("// probe 0\n");
		Label lineNumber = m.label();
		m.expected.visitLabel(lineNumber);
		m.expected.visitLineNumber(42, lineNumber);
		m.original.visitLineNumber(42, end);
		m.visitMethodInsn(INVOKESTATIC, "", "m", "()V", false);
		m.expected.p.text.add("// probe 1\n");
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(handler);
		m.visitInsn(ATHROW);
		m.visitMaxs(0, 0);
		Assert.assertEquals(toString(m.expected), instrument(m.original));
	}

	/**
	 * {@link MethodInstrumenter#visitInsnWithProbe(int, int)}
	 */
	@Test
	public void test_visitInsnWithProbe() {
		final MethodBuilder m = new MethodBuilder();
		final Label start = new Label();
		final Label end = new Label();
		m.visitTryCatchBlock(start, end, end, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitLabel(start);
		m.expected.p.text.add("// probe 0\n");
		m.expected.visitLabel(m.label());
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "", "nop", "()V", false);
		m.visitInsn(Opcodes.MONITOREXIT);
		// visitInsnWithProbe
		// probe 1 skipped
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(end);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.expected.p.text.add("// probe 2\n");
		m.visitInsn(Opcodes.ATHROW);
		m.visitMaxs(0, 0);
		Assert.assertEquals(toString(m.expected), instrument(m.original));
	}

	/**
	 * {@link MethodInstrumenter#visitTableSwitchInsnWithProbes(int, int, Label, Label[], IFrame)}
	 */
	@Test
	public void test_visitTableSwitchInsnWithProbes() {
		final MethodBuilder m = new MethodBuilder();
		final Label target1 = new Label();
		final Label target2 = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITORENTER);
		// visitTableSwitchInsnWithProbes
		m.visitTableSwitchInsn(0, 0, target1, target2);
		m.visitLabel(target1);
		// probe 0 skipped
		m.visitInsn(Opcodes.NOP);
		m.visitLabel(target2);
		// probe 1 skipped
		m.visitInsn(Opcodes.MONITOREXIT);
		m.expected.p.text.add("// probe 2\n");
		m.visitInsn(Opcodes.RETURN);
		m.visitMaxs(0, 0);
		Assert.assertEquals(toString(m.expected), instrument(m.original));
	}

	/**
	 * {@link MethodInstrumenter#visitLookupSwitchInsnWithProbes(Label, int[], Label[], IFrame)}
	 */
	@Test
	public void test_visitLookupSwitchInsnWithProbes() {
		final MethodBuilder m = new MethodBuilder();
		final Label target1 = new Label();
		final Label target2 = new Label();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITORENTER);
		// visitLookupSwitchInsnWithProbes
		m.visitLookupSwitchInsn(target1, new int[] { 0 },
				new Label[] { target2 });
		m.visitLabel(target1);
		// probe 0 skipped
		m.visitInsn(Opcodes.NOP);
		m.visitLabel(target2);
		// probe 1 skipped
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.expected.p.text.add("// probe 2\n");
		m.visitInsn(Opcodes.RETURN);
		m.visitMaxs(0, 0);
		Assert.assertEquals(toString(m.expected), instrument(m.original));
	}

	/**
	 * All handlers must have same monitor stacks.
	 * 
	 * @see MonitorsJvmTest#test4()
	 */
	@Test
	public void test1() throws Exception {
		final MethodBuilder m = new MethodBuilder();
		final Label l0 = m.label();
		final Label l1 = m.label();
		final Label l2 = m.label();
		final Label l3 = m.label();
		m.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");
		m.visitTryCatchBlock(l0, l3, l3, null);
		m.visitInsn(NOP);
		m.visitLabel(l0);
		// probe 0 skipped
		m.expected.visitLabel(m.label());
		m.visitInsn(NOP);
		m.visitLabel(l1);
		m.visitInsn(ACONST_NULL);
		m.expected.p.text.add("// probe 1\n");
		m.visitInsn(ATHROW);
		m.visitLabel(l3);
		m.expected.p.text.add("// probe 2\n");
		m.visitInsn(RETURN);
		m.visitLabel(l2);
		m.visitVarInsn(ALOAD, 0);
		m.visitInsn(MONITOREXIT);
		m.visitInsn(ATHROW);
		m.visitMaxs(0, 0);
		Assert.assertEquals(toString(m.expected), instrument(m.original));

		final ClassWriter classWriter = new ClassWriter(
				ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classWriter.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, "Main", null,
				"java/lang/Object", null);
		m.original.accept(classWriter);
		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();
		Assert.assertEquals("instrumented", "",
				execute(instrument(classBytes)));
	}

	/**
	 * All handlers must have same monitor stacks.
	 *
	 * TODO {@link #test1()} seems better
	 */
	@Test
	public void all_handlers() throws Exception {
		final MethodBuilder m = new MethodBuilder();
		final Label start = new Label();
		final Label end = new Label();
		final Label handler = new Label();
		m.visitTryCatchBlock(start, handler, handler,
				"java/lang/ClassCastException");
		m.visitTryCatchBlock(start, end, handler, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitLabel(start);
		m.expected.p.text.add("// probe 0\n");
		m.expected.visitLabel(m.label());
		m.visitVarInsn(ALOAD, 0);
		m.visitInsn(ARRAYLENGTH);
		m.visitInsn(POP);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitLabel(end);
		// visitInsnWithProbe
		// probe 1 skipped
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(handler);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.expected.p.text.add("// probe 2\n");
		m.visitInsn(Opcodes.ATHROW);
		m.visitMaxs(0, 0);
		Assert.assertEquals(toString(m.expected), instrument(m.original));

		final ClassWriter classWriter = new ClassWriter(
				ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classWriter.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, "Main", null,
				"java/lang/Object", null);
		m.original.accept(classWriter);
		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();
		Assert.assertEquals("instrumented", "",
				execute(instrument(classBytes)));
	}

	/**
	 * TODO {@link #test2()} seems better
	 */
	@Test
	public void catch_all() {
		final MethodBuilder m = new MethodBuilder();
		final Label l0 = new Label();
		final Label l1 = new Label();
		final Label l2 = new Label();
		final Label handler = new Label();
		m.visitTryCatchBlock(l0, l1, handler, "java/lang/ClassCastException");
		m.visitTryCatchBlock(l1, l2, handler, null);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitLabel(l0);
		// probe 0 skipped
		m.expected.visitLabel(m.label());
		m.visitInsn(NOP);
		m.visitLabel(l1);
		m.expected.p.text.add("// probe 1\n");
		m.expected.visitLabel(m.label());
		m.visitVarInsn(ALOAD, 0);
		m.visitInsn(ARRAYLENGTH);
		m.visitInsn(POP);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.visitLabel(l2);
		m.expected.p.text.add("// probe 2\n");
		m.visitInsn(Opcodes.RETURN);
		m.visitLabel(handler);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.MONITOREXIT);
		m.expected.p.text.add("// probe 3\n");
		m.visitInsn(Opcodes.ATHROW);
		m.visitMaxs(0, 0);
		Assert.assertEquals(toString(m.expected), instrument(m.original));
	}

	/**
	 * Probe inserted in position with non-empty monitors stack must be guarded
	 * by catch-all handler.
	 * 
	 * @see MonitorsJvmTest#test3()
	 */
	@Test
	public void test2() throws Exception {
		final MethodBuilder m = new MethodBuilder();
		final Label l0 = m.label();
		final Label l1 = m.label();
		final Label l2 = m.label();
		m.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
		m.visitTryCatchBlock(l1, l2, l2, null);
		m.visitVarInsn(ALOAD, 0);
		m.visitInsn(MONITORENTER);
		m.visitLabel(l0);
		// probe 0 skipped
		m.expected.visitLabel(m.label());
		m.visitInsn(NOP);
		m.visitLabel(l1);
		m.expected.p.text.add("// probe 1\n");
		m.expected.visitLabel(m.label());
		m.visitInsn(ACONST_NULL);
		m.expected.p.text.add("// probe 2\n");
		m.visitInsn(ATHROW);
		m.visitLabel(l2);
		m.visitVarInsn(ALOAD, 0);
		m.visitInsn(MONITOREXIT);
		m.expected.p.text.add("// probe 3\n");
		m.visitInsn(RETURN);
		m.visitMaxs(0, 0);
		Assert.assertEquals(toString(m.expected), instrument(m.original));

		final ClassWriter classWriter = new ClassWriter(
				ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classWriter.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, "Main", null,
				"java/lang/Object", null);
		m.original.accept(classWriter);
		classWriter.visitEnd();
		final byte[] classBytes = classWriter.toByteArray();
		Assert.assertEquals("instrumented", "",
				execute(instrument(classBytes)));
	}

	@Test
	public void repro() {
		final MethodBuilder m = new MethodBuilder();
		final Label label0 = m.label();
		final Label label1 = m.label();
		final Label label2 = m.label();
		m.visitTryCatchBlock(label0, label1, label2, "java/lang/Exception");
		final Label label3 = m.label();
		final Label label4 = m.label();
		m.visitTryCatchBlock(label1, label3, label4, null);
		final Label label5 = m.label();
		m.visitTryCatchBlock(label5, label4, label2, "java/lang/Exception");
		final Label label6 = m.label();
		m.visitTryCatchBlock(label6, label2, label2, "java/lang/Exception");
		// LabelNode label7 = method.namedLabel();
		// method.visitLabel(label7);
		// method.visitLineNumber(2, label7);
		m.visitTypeInsn(NEW, "java/lang/Object");
		m.visitInsn(DUP);
		m.visitInsn(DUP2);
		m.visitVarInsn(ASTORE, 0);
		m.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V",
				false);
		m.visitLabel(label0);
		// Probe[0]
		m.expected.p.text.add("// probe 0\n");
		m.expected.visitLabel(m.label());
		// method.visitLineNumber(4, label0);
		m.visitInsn(MONITORENTER);
		m.visitLabel(label1);
		// Probe[1]
		m.expected.p.text.add("// probe 1\n");
		m.expected.visitLabel(m.label());
		// method.visitLineNumber(5, label1); // Line 5
		m.visitMethodInsn(INVOKESTATIC, "ExampleKt", "nop", "()V", false);
		// Label label8 = new Label();
		// method.visitLabel(label8);
		// method.visitLineNumber(6, label8); // Line 6
		m.visitFieldInsn(GETSTATIC, "kotlin/Unit", "INSTANCE", "Lkotlin/Unit;");
		m.visitLabel(label3);
		m.visitInsn(POP);
		m.visitLabel(label5);
		// Probe[2] - skipped
		m.expected.visitLabel(m.label());
		// method.visitLineNumber(4, label5);
		m.visitInsn(MONITOREXIT);
		Label label9 = m.label();
		// JumpInsnWithProbe[3]:
		m.expected.p.text.add("// probe 3\n");
		m.visitJumpInsn(GOTO, label9);
		m.visitLabel(label4);
		m.visitVarInsn(ASTORE, 1);
		m.visitLabel(label6);
		// Probe[4] - skipped
		m.expected.visitLabel(m.label());
		m.visitVarInsn(ALOAD, 1);
		m.visitVarInsn(ALOAD, 0);
		m.visitInsn(MONITOREXIT);
		// InsnWithProbe[5]:
		m.expected.p.text.add("// probe 5\n");
		m.visitInsn(ATHROW);
		m.visitLabel(label2);
		m.visitInsn(POP);
		// Probe[6]
		// Label label10 = new Label();
		// method.visitLabel(label10);
		// method.visitLineNumber(8, label10);
		m.visitMethodInsn(INVOKESTATIC, "ExampleKt", "nop", "()V", false);
		// Probe[7]
		m.expected.p.text.add("// probe 6\n");
		m.visitLabel(label9);

		// InsnWithProbe[8]:
		m.expected.p.text.add("// probe 7\n");
		m.visitInsn(RETURN);

		m.visitMaxs(0, 0);
		m.visitEnd();

		Assert.assertEquals(toString(m.expected), instrument(m.original));
	}

	private static String instrument(final MethodNode methodNode) {
		final Textifier textifier = new Textifier();
		final TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(
				textifier);
		final MethodInstrumenter methodInstrumenter = new MethodInstrumenter(
				traceMethodVisitor, new IProbeInserter() {
					public void insertProbe(final int id) {
						textifier.text.add("// probe " + id + "\n");
					}
				});
		final MethodProbesAdapter probesAdapter = new MethodProbesAdapter(
				methodInstrumenter, new IProbeIdGenerator() {
					int id = 0;

					public int nextId() {
						return id++;
					}
				});
		LabelFlowAnalyzer.markLabels(methodNode);
		methodInstrumenter.accept(methodNode, probesAdapter);
		return toString(traceMethodVisitor);
	}

	private static String toString(final TraceMethodVisitor m) {
		final StringWriter buffer = new StringWriter();
		m.p.print(new PrintWriter(buffer));
		return buffer.toString();
	}

	private static class MethodBuilder extends MethodVisitor {
		private final MethodNode original;
		private final TraceMethodVisitor expected;
		private final Map<Label, String> labels = new HashMap<Label, String>();

		public MethodBuilder() {
			super(InstrSupport.ASM_API_VERSION);
			expected = new TraceMethodVisitor(
					new Textifier(InstrSupport.ASM_API_VERSION) {
						{
							this.labelNames = labels;
						}
					});
			original = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
					"main", "([Ljava/lang/String;)V", null, null);
			this.mv = expected;
		}

		public Label label() {
			final Label label = new Label();
			final String name = "L" + labels.size();
			labels.put(label, name);
			return label;
		}

		@Override
		public void visitFrame(final int type, final int numLocal,
				final Object[] local, final int numStack,
				final Object[] stack) {
			super.visitFrame(type, numLocal, local, numStack, stack);
			original.visitFrame(type, numLocal, local, numStack, stack);
		}

		@Override
		public void visitInsn(final int opcode) {
			super.visitInsn(opcode);
			original.visitInsn(opcode);
		}

		@Override
		public void visitIntInsn(final int opcode, final int operand) {
			super.visitIntInsn(opcode, operand);
			original.visitIntInsn(opcode, operand);
		}

		@Override
		public void visitVarInsn(final int opcode, final int varIndex) {
			super.visitVarInsn(opcode, varIndex);
			original.visitVarInsn(opcode, varIndex);
		}

		@Override
		public void visitTypeInsn(final int opcode, final String type) {
			super.visitTypeInsn(opcode, type);
			original.visitTypeInsn(opcode, type);
		}

		@Override
		public void visitFieldInsn(final int opcode, final String owner,
				final String name, final String descriptor) {
			super.visitFieldInsn(opcode, owner, name, descriptor);
			original.visitFieldInsn(opcode, owner, name, descriptor);
		}

		@Override
		public void visitMethodInsn(final int opcode, final String owner,
				final String name, final String descriptor) {
			super.visitMethodInsn(opcode, owner, name, descriptor);
			original.visitMethodInsn(opcode, owner, name, descriptor);
		}

		@Override
		public void visitMethodInsn(final int opcode, final String owner,
				final String name, final String descriptor,
				final boolean isInterface) {
			super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
			original.visitMethodInsn(opcode, owner, name, descriptor,
					isInterface);
		}

		@Override
		public void visitInvokeDynamicInsn(final String name,
				final String descriptor, final Handle bootstrapMethodHandle,
				final Object... bootstrapMethodArguments) {
			super.visitInvokeDynamicInsn(name, descriptor,
					bootstrapMethodHandle, bootstrapMethodArguments);
			original.visitInvokeDynamicInsn(name, descriptor,
					bootstrapMethodHandle, bootstrapMethodArguments);
		}

		@Override
		public void visitJumpInsn(final int opcode, final Label label) {
			super.visitJumpInsn(opcode, label);
			original.visitJumpInsn(opcode, label);
		}

		@Override
		public void visitLabel(final Label label) {
			super.visitLabel(label);
			original.visitLabel(label);
		}

		@Override
		public void visitLdcInsn(final Object value) {
			super.visitLdcInsn(value);
			original.visitLdcInsn(value);
		}

		@Override
		public void visitIincInsn(final int varIndex, final int increment) {
			super.visitIincInsn(varIndex, increment);
			original.visitIincInsn(varIndex, increment);
		}

		@Override
		public void visitTableSwitchInsn(final int min, final int max,
				final Label dflt, final Label... labels) {
			super.visitTableSwitchInsn(min, max, dflt, labels);
			original.visitTableSwitchInsn(min, max, dflt, labels);
		}

		@Override
		public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
				final Label[] labels) {
			super.visitLookupSwitchInsn(dflt, keys, labels);
			original.visitLookupSwitchInsn(dflt, keys, labels);
		}

		@Override
		public void visitMultiANewArrayInsn(final String descriptor,
				final int numDimensions) {
			super.visitMultiANewArrayInsn(descriptor, numDimensions);
			original.visitMultiANewArrayInsn(descriptor, numDimensions);
		}

		@Override
		public void visitTryCatchBlock(final Label start, final Label end,
				final Label handler, final String type) {
			super.visitTryCatchBlock(start, end, handler, type);
			original.visitTryCatchBlock(start, end, handler, type);
		}

		@Override
		public void visitLineNumber(final int line, final Label start) {
			super.visitLineNumber(line, start);
			original.visitLineNumber(line, start);
		}

		@Override
		public void visitMaxs(final int maxStack, final int maxLocals) {
			super.visitMaxs(maxStack, maxLocals);
			original.visitMaxs(maxStack, maxLocals);
		}

	}

	/**
	 * @return instrumented classBytes
	 */
	private static byte[] instrument(final byte[] classBytes)
			throws IOException {
		return new Instrumenter(new IExecutionDataAccessorGenerator() {
			public int generateDataAccessor(final long classId,
					final String className, final int probeCount,
					final MethodVisitor mv) {
				InstrSupport.push(mv, probeCount);
				mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
				return 1;
			}
		}).instrument(classBytes, null);
	}

	private static String execute(byte[] classBytes) throws Exception {
		return new JvmProcess() //
				.addOption("-Xcomp") //
				.addOption("-XX:CompileCommand=quiet") //
				.addOption("-XX:CompileCommand=compileonly Main::*") //
				.execute("Main", classBytes);
	}

}

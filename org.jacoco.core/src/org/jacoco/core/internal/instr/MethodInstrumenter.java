/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import org.jacoco.core.internal.flow.IFrame;
import org.jacoco.core.internal.flow.LabelInfo;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.jacoco.core.internal.flow.MonitorsAnalyzer;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This method adapter inserts probes as requested by the
 * {@link MethodProbesVisitor} events.
 */
class MethodInstrumenter extends MethodProbesVisitor {

	private final IProbeInserter probeInserter;

	/**
	 * Create a new instrumenter instance for the given method.
	 *
	 * @param mv
	 *            next method visitor in the chain
	 * @param probeInserter
	 *            call-back to insert probes where required
	 */
	public MethodInstrumenter(final MethodVisitor mv,
			final IProbeInserter probeInserter) {
		super(mv);
		this.probeInserter = probeInserter;
	}

	// === IMethodProbesVisitor ===

	@Override
	public void visitProbe(final int probeId) {
		// TODO explain labels, ie that originalInstructionIndex points to
		// original label and start/end labels of try-catch will be before probe
		if (MonitorsAnalyzer.EVALUATE_IDEA) {
			final TryCatchBlockNode tryCatch = handlers[originalInstructionIndex];
			if (tryCatch != null) {
				mv.visitLabel(tryCatch.start.getLabel());
				probeInserter.insertProbe(probeId);
				mv.visitLabel(tryCatch.end.getLabel());
				return;
			}
		}
		if (canInsertProbe()) {
			probeInserter.insertProbe(probeId);
		}
	}

	@Override
	public void visitInsnWithProbe(final int opcode, final int probeId) {
		if (canInsertProbe()) {
			probeInserter.insertProbe(probeId);
		}
		mv.visitInsn(opcode);
	}

	@Override
	public void visitJumpInsnWithProbe(final int opcode, final Label label,
			final int probeId, final IFrame frame) {
		if (!canInsertProbe()) {
			mv.visitJumpInsn(opcode, label);
			return;
		}
		if (opcode == Opcodes.GOTO) {
			probeInserter.insertProbe(probeId);
			mv.visitJumpInsn(Opcodes.GOTO, label);
		} else {
			final Label intermediate = new Label();
			mv.visitJumpInsn(getInverted(opcode), intermediate);
			probeInserter.insertProbe(probeId);
			mv.visitJumpInsn(Opcodes.GOTO, label);
			mv.visitLabel(intermediate);
			frame.accept(mv);
		}
	}

	private int getInverted(final int opcode) {
		switch (opcode) {
		case Opcodes.IFEQ:
			return Opcodes.IFNE;
		case Opcodes.IFNE:
			return Opcodes.IFEQ;
		case Opcodes.IFLT:
			return Opcodes.IFGE;
		case Opcodes.IFGE:
			return Opcodes.IFLT;
		case Opcodes.IFGT:
			return Opcodes.IFLE;
		case Opcodes.IFLE:
			return Opcodes.IFGT;
		case Opcodes.IF_ICMPEQ:
			return Opcodes.IF_ICMPNE;
		case Opcodes.IF_ICMPNE:
			return Opcodes.IF_ICMPEQ;
		case Opcodes.IF_ICMPLT:
			return Opcodes.IF_ICMPGE;
		case Opcodes.IF_ICMPGE:
			return Opcodes.IF_ICMPLT;
		case Opcodes.IF_ICMPGT:
			return Opcodes.IF_ICMPLE;
		case Opcodes.IF_ICMPLE:
			return Opcodes.IF_ICMPGT;
		case Opcodes.IF_ACMPEQ:
			return Opcodes.IF_ACMPNE;
		case Opcodes.IF_ACMPNE:
			return Opcodes.IF_ACMPEQ;
		case Opcodes.IFNULL:
			return Opcodes.IFNONNULL;
		case Opcodes.IFNONNULL:
			return Opcodes.IFNULL;
		}
		throw new IllegalArgumentException();
	}

	@Override
	public void visitTableSwitchInsnWithProbes(final int min, final int max,
			final Label dflt, final Label[] labels, final IFrame frame) {
		if (!canInsertProbe()) {
			mv.visitTableSwitchInsn(min, max, dflt, labels);
			return;
		}

		// 1. Calculate intermediate labels:
		LabelInfo.resetDone(dflt);
		LabelInfo.resetDone(labels);
		final Label newDflt = createIntermediate(dflt);
		final Label[] newLabels = createIntermediates(labels);
		mv.visitTableSwitchInsn(min, max, newDflt, newLabels);

		// 2. Insert probes:
		insertIntermediateProbes(dflt, labels, frame);
	}

	@Override
	public void visitLookupSwitchInsnWithProbes(final Label dflt,
			final int[] keys, final Label[] labels, final IFrame frame) {
		if (!canInsertProbe()) {
			mv.visitLookupSwitchInsn(dflt, keys, labels);
			return;
		}

		// 1. Calculate intermediate labels:
		LabelInfo.resetDone(dflt);
		LabelInfo.resetDone(labels);
		final Label newDflt = createIntermediate(dflt);
		final Label[] newLabels = createIntermediates(labels);
		mv.visitLookupSwitchInsn(newDflt, keys, newLabels);

		// 2. Insert probes:
		insertIntermediateProbes(dflt, labels, frame);
	}

	private Label[] createIntermediates(final Label[] labels) {
		final Label[] intermediates = new Label[labels.length];
		for (int i = 0; i < labels.length; i++) {
			intermediates[i] = createIntermediate(labels[i]);
		}
		return intermediates;
	}

	private Label createIntermediate(final Label label) {
		final Label intermediate;
		if (LabelInfo.getProbeId(label) == LabelInfo.NO_PROBE) {
			intermediate = label;
		} else {
			if (LabelInfo.isDone(label)) {
				intermediate = LabelInfo.getIntermediateLabel(label);
			} else {
				intermediate = new Label();
				LabelInfo.setIntermediateLabel(label, intermediate);
				LabelInfo.setDone(label);
			}
		}
		return intermediate;
	}

	private void insertIntermediateProbe(final Label label,
			final IFrame frame) {
		final int probeId = LabelInfo.getProbeId(label);
		if (probeId != LabelInfo.NO_PROBE && !LabelInfo.isDone(label)) {
			mv.visitLabel(LabelInfo.getIntermediateLabel(label));
			frame.accept(mv);
			probeInserter.insertProbe(probeId);
			mv.visitJumpInsn(Opcodes.GOTO, label);
			LabelInfo.setDone(label);
		}
	}

	private void insertIntermediateProbes(final Label dflt,
			final Label[] labels, final IFrame frame) {
		LabelInfo.resetDone(dflt);
		LabelInfo.resetDone(labels);
		insertIntermediateProbe(dflt, frame);
		for (final Label l : labels) {
			insertIntermediateProbe(l, frame);
		}
	}

	private boolean canInsertProbe() {
		if (monitors == null) {
			// TODO MethodInstrumenterTest
			return true;
		}
		// Probe adds CFG edge from current instruction into catch-all handler
		// or method exit
		final MonitorsAnalyzer.MonitorsStack monitorsStack = monitors
				.monitorsStack(originalInstructionIndex);
		// TODO what to do with error states?
		if (MonitorsAnalyzer.UNREACHABLE == monitorsStack) {
			// TODO ? see Groovy
			return true;
		}
		return monitorsStack == //
				monitors.monitorsStackAtCatchAny(originalInstructionIndex);
	}

	private MonitorsAnalyzer monitors;

	/**
	 * Index of original non-instrumented method instruction that is currently
	 * being processed by this visitor.
	 */
	private int originalInstructionIndex;

	Label start = new Label();
	Label end = new Label();
	Label handler = new Label();

	/**
	 * @param methodNode
	 *            original non-instrumented method
	 */
	@Override
	public void accept(final MethodNode methodNode,
			final MethodVisitor methodVisitor) {
		// TODO idea: LabelFlowAnalyzer was already executed, so we should be
		// able to know probes that require handler and hence can pre-allocate
		// them
		monitors = new MonitorsAnalyzer(methodNode);
		if (MonitorsAnalyzer.EVALUATE_IDEA) {
			process(methodNode, methodVisitor);
		}
		final MethodVisitor mv = new MethodVisitor(InstrSupport.ASM_API_VERSION,
				methodVisitor) {
			@Override
			public void visitFrame(int type, int numLocal, Object[] local,
					int numStack, Object[] stack) {
				super.visitFrame(type, numLocal, local, numStack, stack);
				originalInstructionIndex++;
			}

			@Override
			public void visitInsn(int opcode) {
				super.visitInsn(opcode);
				originalInstructionIndex++;
			}

			@Override
			public void visitIntInsn(int opcode, int operand) {
				super.visitIntInsn(opcode, operand);
				originalInstructionIndex++;
			}

			@Override
			public void visitVarInsn(int opcode, int varIndex) {
				super.visitVarInsn(opcode, varIndex);
				originalInstructionIndex++;
			}

			@Override
			public void visitTypeInsn(int opcode, String type) {
				super.visitTypeInsn(opcode, type);
				originalInstructionIndex++;
			}

			@Override
			public void visitFieldInsn(int opcode, String owner, String name,
					String descriptor) {
				super.visitFieldInsn(opcode, owner, name, descriptor);
				originalInstructionIndex++;
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name,
					String descriptor) {
				super.visitMethodInsn(opcode, owner, name, descriptor);
				originalInstructionIndex++;
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name,
					String descriptor, boolean isInterface) {
				super.visitMethodInsn(opcode, owner, name, descriptor,
						isInterface);
				originalInstructionIndex++;
			}

			@Override
			public void visitInvokeDynamicInsn(String name, String descriptor,
					Handle bootstrapMethodHandle,
					Object... bootstrapMethodArguments) {
				super.visitInvokeDynamicInsn(name, descriptor,
						bootstrapMethodHandle, bootstrapMethodArguments);
				originalInstructionIndex++;
			}

			@Override
			public void visitJumpInsn(int opcode, Label label) {
				super.visitJumpInsn(opcode, label);
				originalInstructionIndex++;
			}

			@Override
			public void visitLabel(final Label label) {
				assert ((LabelNode) methodNode.instructions
						.get(originalInstructionIndex)).getLabel() == label;
				super.visitLabel(label);
				originalInstructionIndex++;
			}

			@Override
			public void visitLdcInsn(Object value) {
				super.visitLdcInsn(value);
				originalInstructionIndex++;
			}

			@Override
			public void visitIincInsn(int varIndex, int increment) {
				super.visitIincInsn(varIndex, increment);
				originalInstructionIndex++;
			}

			@Override
			public void visitTableSwitchInsn(int min, int max, Label dflt,
					Label... labels) {
				super.visitTableSwitchInsn(min, max, dflt, labels);
				originalInstructionIndex++;
			}

			@Override
			public void visitLookupSwitchInsn(Label dflt, int[] keys,
					Label[] labels) {
				super.visitLookupSwitchInsn(dflt, keys, labels);
				originalInstructionIndex++;
			}

			@Override
			public void visitMultiANewArrayInsn(String descriptor,
					int numDimensions) {
				super.visitMultiANewArrayInsn(descriptor, numDimensions);
				originalInstructionIndex++;
			}

			@Override
			public void visitLineNumber(int line, Label start) {
				super.visitLineNumber(line, start);
				originalInstructionIndex++;
			}
		};
		methodNode.accept(mv);
	}

	private TryCatchBlockNode[] handlers;

	public void process(final MethodNode methodNode,
			final MethodVisitor methodVisitor) {
		handlers = new TryCatchBlockNode[methodNode.instructions.size()];
		for (int i = 0; i < methodNode.instructions.size(); i++) {
			AbstractInsnNode instruction = methodNode.instructions.get(i);
			if (instruction.getType() == AbstractInsnNode.LABEL) {
				Label label = ((LabelNode) instruction).getLabel();
				if (LabelInfo.needsProbe(label)) {
					if (monitors.monitorsStack(i) != monitors
							.monitorsStackAtCatchAny(i)) {
						// TODO requires protection
						final Label start = new Label();
						final Label end = new Label();
						final Label handler = new Label();
						handlers[i] = new TryCatchBlockNode(
								new LabelNode(start), new LabelNode(end),
								new LabelNode(handler), null);
						methodVisitor.visitTryCatchBlock(start, end, handler,
								null);
					}
				}
			}
		}
	}

	private int maxLocals;

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		this.maxLocals = maxLocals;
		super.visitMaxs(maxStack, maxLocals);
	}

	@Override
	public void visitEnd() {
		if (!MonitorsAnalyzer.EVALUATE_IDEA) {
			super.visitEnd();
			return;
		}
		for (int i = 0; i < handlers.length; i++) {
			final TryCatchBlockNode tryCatch = handlers[i];
			if (tryCatch != null) {
				mv.visitLabel(tryCatch.handler.getLabel());

				// https://stackoverflow.com/questions/66622530/what-determines-the-locals-of-a-stackmap-frame-of-a-java-bytecode-try-catch-hand
				Object[] locals = new Object[maxLocals];
				Arrays.fill(locals, Opcodes.TOP);
				MonitorsAnalyzer.MonitorsStack stack = monitors
						.monitorsStack(i);
				while (stack.previous != null) {
					locals[stack.var] = "java/lang/Object";
					stack = stack.previous;
				}
				mv.visitFrame(Opcodes.F_NEW, locals.length, locals, 1,
						new Object[] { "java/lang/Throwable" });

				stack = monitors.monitorsStack(i);
				while (stack.previous != null) {
					mv.visitVarInsn(Opcodes.ALOAD, stack.var);
					mv.visitInsn(Opcodes.MONITOREXIT);
					stack = stack.previous;
				}

				mv.visitInsn(Opcodes.ATHROW);
			}
		}
		super.visitEnd();
	}

}

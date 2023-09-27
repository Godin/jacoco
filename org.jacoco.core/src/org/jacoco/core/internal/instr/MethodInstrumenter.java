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
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

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
		// original label, start/end labels of try-catch will be before probe
		// (see MethodProbesAdapter), start is inclusive and end is exclusive
		// (see visitTryCatchBlock), so catch-any handler of probe is the same
		// as catch-any handler of original label
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

	/**
	 * @return <code>true</code> if insertion of probe will not break structured
	 *         locking
	 */
	private boolean canInsertProbe() {
		if (monitors == null) {
			// TODO MethodInstrumenterTest
			return true;
		}
		// Probe adds CFG edge from current instruction into catch-all handler
		// or method exit
		final Monitors.Stack monitorsStack = monitors
				.stackAt(originalInstructionIndex);
		// TODO what to do with error states?
		// TODO add test
		// if (MonitorsAnalyzer.UNREACHABLE == monitorsStack) {
		// // TODO ? see Groovy
		// return true;
		// }
		Monitors.Handler handler = monitors
				.handlersAt(originalInstructionIndex);
		if (false) {
			// FIXME all handlers must have same stack
			return monitorsStack == (handler == null || handler.type() != null
					? Monitors.EMPTY
					: monitors.stackAt(handler.index()));
		}
		if (handler == null) {
			return monitorsStack.equals(Monitors.EMPTY);
		}
		if (handler.type() != null && !monitorsStack.equals(Monitors.EMPTY)) {
			return false;
		}
		while (handler != null) {
			if (!monitorsStack.equals(monitors.stackAt(handler.index()))) {
				return false;
			}
			handler = handler.previous();
		}
		return true;
	}

	private Monitors monitors;

	/**
	 * Index of original non-instrumented method instruction that is currently
	 * being processed by this visitor.
	 */
	private int originalInstructionIndex;

	/**
	 * @param methodNode
	 *            original non-instrumented method
	 */
	@Override
	public void accept(final MethodNode methodNode,
			final MethodVisitor methodVisitor) {
		// TODO set to null in absence of monitorenter/monitorexit instructions,
		// this will allow to instrument non-throwing try-body in methods
		// without synchronized
		monitors = new Monitors(methodNode);
		final MethodVisitor mv = new MethodVisitor(InstrSupport.ASM_API_VERSION,
				methodVisitor) {
			@Override
			public void visitFrame(final int type, final int numLocal,
					final Object[] local, final int numStack,
					final Object[] stack) {
				super.visitFrame(type, numLocal, local, numStack, stack);
				originalInstructionIndex++;
			}

			@Override
			public void visitInsn(final int opcode) {
				super.visitInsn(opcode);
				originalInstructionIndex++;
			}

			@Override
			public void visitIntInsn(final int opcode, final int operand) {
				super.visitIntInsn(opcode, operand);
				originalInstructionIndex++;
			}

			@Override
			public void visitVarInsn(final int opcode, final int varIndex) {
				super.visitVarInsn(opcode, varIndex);
				originalInstructionIndex++;
			}

			@Override
			public void visitTypeInsn(final int opcode, final String type) {
				super.visitTypeInsn(opcode, type);
				originalInstructionIndex++;
			}

			@Override
			public void visitFieldInsn(final int opcode, final String owner,
					final String name, final String descriptor) {
				super.visitFieldInsn(opcode, owner, name, descriptor);
				originalInstructionIndex++;
			}

			@Override
			public void visitMethodInsn(final int opcode, final String owner,
					final String name, final String descriptor) {
				super.visitMethodInsn(opcode, owner, name, descriptor);
				originalInstructionIndex++;
			}

			@Override
			public void visitMethodInsn(final int opcode, final String owner,
					final String name, final String descriptor,
					final boolean isInterface) {
				super.visitMethodInsn(opcode, owner, name, descriptor,
						isInterface);
				originalInstructionIndex++;
			}

			@Override
			public void visitInvokeDynamicInsn(final String name,
					final String descriptor, final Handle bootstrapMethodHandle,
					final Object... bootstrapMethodArguments) {
				super.visitInvokeDynamicInsn(name, descriptor,
						bootstrapMethodHandle, bootstrapMethodArguments);
				originalInstructionIndex++;
			}

			@Override
			public void visitJumpInsn(final int opcode, final Label label) {
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
			public void visitLdcInsn(final Object value) {
				super.visitLdcInsn(value);
				originalInstructionIndex++;
			}

			@Override
			public void visitIincInsn(final int varIndex, final int increment) {
				super.visitIincInsn(varIndex, increment);
				originalInstructionIndex++;
			}

			@Override
			public void visitTableSwitchInsn(final int min, final int max,
					final Label dflt, final Label... labels) {
				super.visitTableSwitchInsn(min, max, dflt, labels);
				originalInstructionIndex++;
			}

			@Override
			public void visitLookupSwitchInsn(final Label dflt,
					final int[] keys, final Label[] labels) {
				super.visitLookupSwitchInsn(dflt, keys, labels);
				originalInstructionIndex++;
			}

			@Override
			public void visitMultiANewArrayInsn(final String descriptor,
					final int numDimensions) {
				super.visitMultiANewArrayInsn(descriptor, numDimensions);
				originalInstructionIndex++;
			}

			@Override
			public void visitLineNumber(final int line, final Label start) {
				super.visitLineNumber(line, start);
				originalInstructionIndex++;
			}
		};
		methodNode.accept(mv);
	}

}

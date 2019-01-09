/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import org.jacoco.core.internal.flow.IFrame;
import org.jacoco.core.internal.flow.LabelInfo;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.List;

/**
 * A {@link MethodProbesVisitor} that builds the {@link Instruction}s of a
 * method to calculate the detailed execution status.
 */
public class MethodAnalyzer extends MethodProbesVisitor {

	private final InstructionsBuilder builder;

	/** Current node of the ASM tree API */
	private AbstractInsnNode currentNode;

	/**
	 * New instance that uses the given builder.
	 */
	MethodAnalyzer(final InstructionsBuilder builder) {
		this.builder = builder;
	}

	@Override
	public void accept(final MethodNode methodNode,
			final MethodVisitor methodVisitor) {
		methodVisitor.visitCode();
		for (final TryCatchBlockNode n : methodNode.tryCatchBlocks) {
			n.accept(methodVisitor);
		}
		currentNode = methodNode.instructions.getFirst();
		while (currentNode != null) {
			currentNode.accept(methodVisitor);
			currentNode = currentNode.getNext();
		}
		methodVisitor.visitEnd();
	}

	@Override
	public void visitLabel(final Label label) {
		builder.addLabel(label);
	}

	@Override
	public void visitLineNumber(final int line, final Label start) {
		builder.setCurrentLine(line);
	}

	@Override
	public void visitInsn(final int opcode) {
		builder.addInstruction(currentNode);
	}

	@Override
	public void visitIntInsn(final int opcode, final int operand) {
		builder.addInstruction(currentNode);
	}

	@Override
	public void visitVarInsn(final int opcode, final int var) {
		builder.addInstruction(currentNode);
	}

	@Override
	public void visitTypeInsn(final int opcode, final String type) {
		builder.addInstruction(currentNode);
	}

	@Override
	public void visitFieldInsn(final int opcode, final String owner,
			final String name, final String desc) {
		builder.addInstruction(currentNode);
	}

	@Override
	public void visitMethodInsn(final int opcode, final String owner,
			final String name, final String desc, final boolean itf) {
		builder.addInstruction(currentNode);
	}

	@Override
	public void visitInvokeDynamicInsn(final String name, final String desc,
			final Handle bsm, final Object... bsmArgs) {
		builder.addInstruction(currentNode);
	}

	@Override
	public void visitJumpInsn(final int opcode, final Label label) {
		builder.addInstruction(currentNode);
		builder.addJump(label, 1);
	}

	@Override
	public void visitLdcInsn(final Object cst) {
		builder.addInstruction(currentNode);
	}

	@Override
	public void visitIincInsn(final int var, final int increment) {
		builder.addInstruction(currentNode);
	}

	@Override
	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label... labels) {
		visitSwitchInsn(dflt, labels);
	}

	@Override
	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
		visitSwitchInsn(dflt, labels);
	}

	private void visitSwitchInsn(final Label dflt, final Label[] labels) {
		builder.addInstruction(currentNode);
		LabelInfo.resetDone(labels);
		int branch = 0;
		builder.addJump(dflt, branch);
		LabelInfo.setDone(dflt);
		for (final Label l : labels) {
			if (!LabelInfo.isDone(l)) {
				branch++;
				builder.addJump(l, branch);
				LabelInfo.setDone(l);
			}
		}
	}

	@Override
	public void visitMultiANewArrayInsn(final String desc, final int dims) {
		builder.addInstruction(currentNode);
	}

	@Override
	public void visitProbe(final int probeId) {
		builder.addProbe(probeId, 0, null);
		builder.noSuccessor();
	}

	@Override
	public void visitJumpInsnWithProbe(final int opcode, final Label label,
			final int probeId, final IFrame frame) {
		builder.addInstruction(currentNode);
		builder.addProbe(probeId, 1, ((JumpInsnNode) currentNode).label);
	}

	@Override
	public void visitInsnWithProbe(final int opcode, final int probeId) {
		builder.addInstruction(currentNode);
		builder.addProbe(probeId, 0, currentNode);
	}

	@Override
	public void visitTableSwitchInsnWithProbes(final int min, final int max,
			final Label dflt, final Label[] labels, final IFrame frame) {
		final TableSwitchInsnNode node = (TableSwitchInsnNode) currentNode;
		visitSwitchInsnWithProbes(node.dflt, node.labels);
	}

	@Override
	public void visitLookupSwitchInsnWithProbes(final Label dflt,
			final int[] keys, final Label[] labels, final IFrame frame) {
		final LookupSwitchInsnNode node = (LookupSwitchInsnNode) currentNode;
		visitSwitchInsnWithProbes(node.dflt, node.labels);
	}

	private void visitSwitchInsnWithProbes(final LabelNode dflt,
			final List<LabelNode> labels) {
		builder.addInstruction(currentNode);
		LabelInfo.resetDone(dflt.getLabel());
		for (LabelNode l : labels) {
			LabelInfo.resetDone(l.getLabel());
		}
		int branch = 0;
		visitSwitchTarget(dflt, branch);
		for (final LabelNode l : labels) {
			branch++;
			visitSwitchTarget(l, branch);
		}
	}

	private void visitSwitchTarget(final LabelNode label, final int branch) {
		final int id = LabelInfo.getProbeId(label.getLabel());
		if (!LabelInfo.isDone(label.getLabel())) {
			if (id == LabelInfo.NO_PROBE) {
				builder.addJump(label.getLabel(), branch);
			} else {
				builder.addProbe(id, branch, label);
			}
			LabelInfo.setDone(label.getLabel());
		}
	}

}

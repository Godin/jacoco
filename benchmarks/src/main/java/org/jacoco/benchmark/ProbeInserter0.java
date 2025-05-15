package org.jacoco.benchmark;

import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * ALOAD + BASTORE
 */
public class ProbeInserter0 implements InstrumentationBenchmark.ProbeStrategy {

	public int storeInstance(MethodVisitor mv, int variable) {
		InstrSupport.push(mv, 10);
		mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
		mv.visitVarInsn(Opcodes.ASTORE, variable);
		return 3;
	}

	public void insertProbe(MethodVisitor mv, String owner, int variable, int id) {
		mv.visitVarInsn(Opcodes.ALOAD, variable);
		InstrSupport.push(mv, id);
		mv.visitInsn(Opcodes.ICONST_1);
		mv.visitInsn(Opcodes.BASTORE);
	}

}

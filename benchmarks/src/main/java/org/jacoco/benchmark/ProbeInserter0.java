package org.jacoco.benchmark;

import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ProbeInserter0 implements InstrumentationBenchmark.ProbeStrategy {

	public void insertProbe(MethodVisitor mv, int variable, int id) {
		mv.visitVarInsn(Opcodes.ALOAD, variable);
		InstrSupport.push(mv, id);
		mv.visitInsn(Opcodes.ICONST_1);
		mv.visitInsn(Opcodes.BASTORE);
	}

}

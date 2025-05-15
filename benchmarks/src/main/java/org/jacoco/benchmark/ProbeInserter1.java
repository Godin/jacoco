package org.jacoco.benchmark;

import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * GETSTATIC + BASTORE
 */
public class ProbeInserter1 implements InstrumentationBenchmark.ProbeStrategy {

	public int storeInstance(MethodVisitor mv, int variable) {
		// nothing to do
		return 0;
	}

	public void insertProbe(MethodVisitor mv, String owner, int variable, int id) {
		mv.visitFieldInsn(Opcodes.GETSTATIC, owner, "$jacocoData", "[Z");
		InstrSupport.push(mv, id);
		mv.visitInsn(Opcodes.ICONST_1);
		mv.visitInsn(Opcodes.BASTORE);
	}

}

package org.jacoco.benchmark;

import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * GETSTATIC + INVOKESTATIC
 */
public class ProbeInserter3 implements InstrumentationBenchmark.ProbeStrategy {

	public static void hit(boolean[] probes, int index) {
		if (!probes[index])
			probes[index] = true;
	}

	public int storeInstance(MethodVisitor mv, int variable) {
		// nothing to do
		return 0;
	}

	public void insertProbe(MethodVisitor mv, String owner, int variable, int id) {
		mv.visitFieldInsn(Opcodes.GETSTATIC, owner, "$jacocoData", "[Z");
		InstrSupport.push(mv, id);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				getClass().getName().replace(".", "/"), "hit", "([ZI)V", false);
	}

}

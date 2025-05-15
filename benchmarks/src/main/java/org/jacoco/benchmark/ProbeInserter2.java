package org.jacoco.benchmark;

import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * ALOAD + INVOKESTATIC
 */
public class ProbeInserter2 implements InstrumentationBenchmark.ProbeStrategy {

	public static void hit(boolean[] probes, int index) {
		if (!probes[index])
			probes[index] = true;
	}

	public int storeInstance(MethodVisitor mv, int variable) {
		InstrSupport.push(mv, 10);
		mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
		mv.visitVarInsn(Opcodes.ASTORE, variable);
		return 2;
	}

	public void insertProbe(MethodVisitor mv, String owner, int variable, int id) {
		mv.visitVarInsn(Opcodes.ALOAD, variable);
		InstrSupport.push(mv, id);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				getClass().getName().replace(".", "/"), "hit", "([ZI)V", false);
	}

}

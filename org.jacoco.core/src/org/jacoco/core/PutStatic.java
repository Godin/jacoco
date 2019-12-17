package org.jacoco.core;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class PutStatic {

	public static void main(String[] args) throws Exception {
		byte[] bytes = create();
		MemoryClassLoader classLoader = new MemoryClassLoader();
		classLoader.add(bytes);

		Class<?> cls = classLoader.findClass("E");
		cls.newInstance();
	}

	private static byte[] create() {
		ClassNode c = new ClassNode(Opcodes.ASM7);
		c.version = Opcodes.V9; // succeeds with V1_8
		c.name = "E";
		c.superName = "java/lang/Object";
		c.access = Opcodes.ACC_PUBLIC;

		c.visitField(Opcodes.ACC_FINAL | Opcodes.ACC_STATIC, "f",
				"Ljava/lang/Object;", null, null);

		MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null,
				null);
		c.methods.add(m);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>",
				"()V", false);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitFieldInsn(Opcodes.PUTSTATIC, "E", "f", "Ljava/lang/Object;");
		m.visitInsn(Opcodes.RETURN);

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		c.accept(cw);
		return cw.toByteArray();
	}

}

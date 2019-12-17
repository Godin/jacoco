package org.jacoco.core;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class StaticInit {

	public static void main(String[] args) throws Exception {
		MemoryClassLoader classLoader = new MemoryClassLoader();
		classLoader.add(create());
		classLoader.loadClass("Constants");
	}

	static byte[] create() {
		ClassWriter cw = new ClassWriter(0);
		create(cw);
		return cw.toByteArray();
	}

	static void create(ClassVisitor cv) {
		cv.visit(V1_7, ACC_PUBLIC, "Constants", null, "java/lang/Object", null);

		cv.visitField(ACC_PUBLIC | ACC_STATIC | ACC_FINAL, "CONST",
				"Ljava/lang/Object;", null, null);

		{
			MethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC,
					"<clinit>", "()V", null, null);
			mv.visitCode();
			mv.visitTypeInsn(NEW, "java/lang/Object");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V", false);
			mv.visitFieldInsn(PUTSTATIC, "Constants", "CONST",
					"Ljava/lang/Object;");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 0);
			mv.visitEnd();
		}
		{
			MethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC,
					"<clinit>", "()V", null, null);
			mv.visitCode();
			mv.visitTypeInsn(NEW, "java/lang/Object");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V", false);
			mv.visitFieldInsn(PUTSTATIC, "Constants", "CONST",
					"Ljava/lang/Object;");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 0);
			mv.visitEnd();
		}

		cv.visitEnd();
	}

}

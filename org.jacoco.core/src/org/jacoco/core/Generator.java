package org.jacoco.core;

import org.objectweb.asm.ClassTooLargeException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodTooLargeException;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * javac 11.0.3
 * 
 * <pre>
 *     static {};
 *     descriptor: ()V
 *     flags: (0x0008) ACC_STATIC
 *     Code:
 *       stack=4, locals=0, args_size=0
 *          0: new           #4                  // class E
 *          3: dup
 *          4: ldc           #7                  // String C1
 *          6: iconst_0
 *          7: invokespecial #8                  // Method "<init>":(Ljava/lang/String;I)V
 *         10: putstatic     #9                  // Field C1:LE;
 *         13: new           #4                  // class E
 *         16: dup
 *         17: ldc           #10                 // String C2
 *         19: iconst_1
 *         20: invokespecial #8                  // Method "<init>":(Ljava/lang/String;I)V
 *         23: putstatic     #11                 // Field C2:LE;
 *         26: iconst_2
 *         27: anewarray     #4                  // class E
 *         30: dup
 *         31: iconst_0
 *         32: getstatic     #9                  // Field C1:LE;
 *         35: aastore
 *         36: dup
 *         37: iconst_1
 *         38: getstatic     #11                 // Field C2:LE;
 *         41: aastore
 *         42: putstatic     #1                  // Field $VALUES:[LE;
 *         45: return
 * </pre>
 *
 * kotlinc 1.3.61
 * 
 * <pre>
 *     static {};
 *     descriptor: ()V
 *     flags: (0x0008) ACC_STATIC
 *     Code:
 *       stack=8, locals=0, args_size=0
 *          0: iconst_2
 *          1: anewarray     #2                  // class E
 *          4: dup
 *          5: dup
 *          6: iconst_0
 *          7: new           #2                  // class E
 *         10: dup
 *         11: ldc           #48                 // String C1
 *         13: iconst_0
 *         14: invokespecial #49                 // Method "<init>":(Ljava/lang/String;I)V
 *         17: dup
 *         18: putstatic     #51                 // Field C1:LE;
 *         21: aastore
 *         22: dup
 *         23: iconst_1
 *         24: new           #2                  // class E
 *         27: dup
 *         28: ldc           #52                 // String C2
 *         30: iconst_1
 *         31: invokespecial #49                 // Method "<init>":(Ljava/lang/String;I)V
 *         34: dup
 *         35: putstatic     #54                 // Field C2:LE;
 *         38: aastore
 *         39: putstatic     #25                 // Field $VALUES:[LE;
 *         42: return
 * </pre>
 */
public class Generator {

	static final boolean SEPARATE_VALUES = true;
	static final boolean CONDY = true;

	static int VERSION = Opcodes.V11;

	public static void main(String[] args) throws Exception {
		save(create(2));

		for (int i = 10000; i > 0; i--) {
			try {
				byte[] bytes = create(i);
				MemoryClassLoader classLoader = new MemoryClassLoader();
				classLoader.add(bytes);

				Class<?> cls = classLoader.findClass("E");
				System.out.println(cls.getFields()[0].get(null));

				System.out.println("Created with " + i + " constants");
				break;
			} catch (MethodTooLargeException ignore) {
			} catch (ClassTooLargeException ignore) {
			}
		}
	}

	private static void save(byte[] bytes) throws IOException {
		final FileOutputStream stream = new FileOutputStream("/tmp/E.class");
		stream.write(bytes);
		stream.close();
	}

	private static final String B_DESC = "(" + //
			"Ljava/lang/invoke/MethodHandles$Lookup;" + //
			"Ljava/lang/String;" + "Ljava/lang/Class;" + //
			"Ljava/lang/String;" + // name of constant
			"I" + // ordinal
			")LE;";

	private static byte[] create(int n) {
		ClassNode c = new ClassNode(Opcodes.ASM7);
		c.version = VERSION;
		c.name = "E";
		c.superName = "java/lang/Object";
		c.access = Opcodes.ACC_PUBLIC;

		{ // constructor
			MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "<init>",
					"(Ljava/lang/String;I)V", null, null);
			c.methods.add(m);
			m.visitVarInsn(Opcodes.ALOAD, 0);
			m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object",
					"<init>", "()V", false);
			m.visitInsn(Opcodes.RETURN);
			m.visitMaxs(0, 0);
			m.visitEnd();
		}
		{ // fields
			for (int i = 0; i < n; i++) {
				c.visitField(
						Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC
								| Opcodes.ACC_FINAL,
						"C" + i, "LE;", null, null);
			}
			c.visitField(
					Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
					"VALUES", "[LE;", null, null);
		}
		{ // static initializer
			MethodNode m = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V",
					null, null);
			c.methods.add(m);
			for (int i = 0; i < n; i++) {
				createEnumConstant(m, i);
				// 3 bytes
				m.visitFieldInsn(Opcodes.PUTSTATIC, "E", "C" + i, "LE;");
			}
			if (SEPARATE_VALUES) {
				// 3 bytes
				m.visitMethodInsn(Opcodes.INVOKESTATIC, "E", "createValues",
						"()[LE;", false);
			} else {
				createValues(m, n);
			}
			// 3 bytes
			m.visitFieldInsn(Opcodes.PUTSTATIC, "E", "VALUES", "[LE;");
			// 1 byte
			m.visitInsn(Opcodes.RETURN); // 1 byte
			m.visitMaxs(0, 0);
			m.visitEnd();
		}

		//
		if (SEPARATE_VALUES) {
			MethodNode m = new MethodNode(Opcodes.ACC_STATIC, "createValues",
					"()[LE;", null, null);
			c.methods.add(m);
			createValues(m, n);
			m.visitInsn(Opcodes.ARETURN);
			m.visitMaxs(0, 0);
			m.visitEnd();
		}

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		c.accept(cw);
		return cw.toByteArray();
	}

	private static void createEnumConstant(MethodVisitor mv, int i) {
		// ldc - 2 bytes, ldc_w - 3 bytes
		if (CONDY) {
			Handle h1 = new Handle(Opcodes.H_NEWINVOKESPECIAL, "E", "<init>",
					"(Ljava/lang/String;I)V", false);
			Handle h2 = new Handle(Opcodes.H_INVOKESTATIC,
					"java/lang/invoke/ConstantBootstraps", "invoke", //
					"(" //
							+ "Ljava/lang/invoke/MethodHandles$Lookup;" //
							+ "Ljava/lang/String;" //
							+ "Ljava/lang/Class;" //
							+ "Ljava/lang/invoke/MethodHandle;" //
							+ "[Ljava/lang/Object;" //
							+ ")" + //
							"Ljava/lang/Object;", //
					false);
			ConstantDynamic c = new ConstantDynamic("C" + i, "LE;", h2, h1,
					"C" + i, // name
					i // ordinal
			);
			mv.visitLdcInsn(c);
			return;
		}
		// 3 bytes
		mv.visitTypeInsn(Opcodes.NEW, "E");
		// 1 byte
		mv.visitInsn(Opcodes.DUP);
		mv.visitLdcInsn("C" + i); // name
		mv.visitLdcInsn(i); // ordinal
		// 3 bytes
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "E", "<init>",
				"(Ljava/lang/String;I)V", false);
	}

	private static void createValues(MethodVisitor mv, int n) {
		mv.visitLdcInsn(n);
		mv.visitTypeInsn(Opcodes.ANEWARRAY, "E");
		for (int i = 0; i < n; i++) {
			mv.visitInsn(Opcodes.DUP);
			mv.visitLdcInsn(i);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "E", "C" + i,
					"LE;");
			mv.visitInsn(Opcodes.AASTORE);
		}
	}

}

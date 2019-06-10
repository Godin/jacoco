/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class CondyBenchmark {

	public interface Target {
		boolean[] run();
	}

	private static Class<?> generateClass(int type) {
		final ClassNode c = new ClassNode();
		c.version = Opcodes.V11;
		c.access = Opcodes.ACC_PUBLIC;
		c.name = "Condy";
		c.superName = "java/lang/Object";
		c.interfaces.add("org/jacoco/core/CondyBenchmark$Target");

		{
			MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V",
					null, null);
			m.visitVarInsn(Opcodes.ALOAD, 0);
			m.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object",
					"<init>", "()V", false);
			m.visitInsn(Opcodes.RETURN);
			c.methods.add(m);
		}

		if (type == 0) {
			MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "run", "()[Z",
					null, null);
			Handle handle = new Handle(Opcodes.H_INVOKESTATIC, c.name,
					"bootstrap",
					"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)[Z",
					false);
			m.visitLdcInsn(
					new ConstantDynamic("foo", "Ljava/lang/Object;", handle));
			m.visitTypeInsn(Opcodes.CHECKCAST, "[Z");
			m.visitInsn(Opcodes.ARETURN);
			c.methods.add(m);

			m = new MethodNode(Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC,
					"bootstrap",
					"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)[Z",
					null, null);
			m.visitInsn(Opcodes.ICONST_0);
			m.visitInsn(Opcodes.ICONST_0);
			m.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
			m.visitInsn(Opcodes.ARETURN);
			c.methods.add(m);
		}

		FieldNode f = new FieldNode(Opcodes.ACC_STATIC, "field", "[Z", null,
				null);
		c.fields.add(f);

		{
			// init
			MethodNode m = new MethodNode(
					Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, "init", "()[Z",
					null, null);
			m.visitFieldInsn(Opcodes.GETSTATIC, c.name, f.name, f.desc);

			m.visitInsn(Opcodes.DUP);
			Label alreadyInitialized = new Label();
			m.visitJumpInsn(Opcodes.IFNONNULL, alreadyInitialized);
			m.visitInsn(Opcodes.POP);
			m.visitInsn(Opcodes.ICONST_0);
			m.visitInsn(Opcodes.ICONST_0);
			m.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);

			m.visitInsn(Opcodes.DUP);
			m.visitFieldInsn(Opcodes.PUTSTATIC, c.name, f.name, f.desc);
			m.visitInsn(Opcodes.ARETURN);

			m.visitLabel(alreadyInitialized);

			m.visitInsn(Opcodes.ARETURN);
			c.methods.add(m);
		}

		if (type == 1) {
			MethodNode m = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V",
					null, null);
			m.visitMethodInsn(Opcodes.INVOKESTATIC, c.name, "init", "()[Z",
					false);
			m.visitInsn(Opcodes.RETURN);

			m = new MethodNode(Opcodes.ACC_PUBLIC, "run", "()[Z", null, null);
			m.visitFieldInsn(Opcodes.GETSTATIC, c.name, f.name, f.desc);
			m.visitInsn(Opcodes.ARETURN);
			c.methods.add(m);
		}

		if (type == 2) {
			MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "run", "()[Z",
					null, null);
			m.visitMethodInsn(Opcodes.INVOKESTATIC, c.name, "init", "()[Z",
					false);
			m.visitInsn(Opcodes.ARETURN);
			c.methods.add(m);
		}

		ClassWriter classWriter = new ClassWriter(
				ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		c.accept(classWriter);
		final byte[] bytes = classWriter.toByteArray();
		return new ClassLoader() {
			Class<?> load() {
				return defineClass(c.name, bytes, 0, bytes.length);
			}
		}.load();
	}

	private Target condy;
	private Target field;
	private Target init;

	@Setup
	public void setup() throws Exception {
		condy = (Target) generateClass(0).newInstance();
		field = (Target) generateClass(1).newInstance();
        init = (Target) generateClass(2).newInstance();
	}

	@Benchmark
	public Object condy() {
		return condy.run();
	}

	@Benchmark
	public Object field() {
		return field.run();
	}

	@Benchmark
	public Object init() {
		return init.run();
	}

	public static void main(String[] args) throws RunnerException {
		Options options = new OptionsBuilder() //
				.include(CondyBenchmark.class.getName()) //
				// .jvmArgsAppend("-XX:CompileCommand=dontinline,Condy::init")
				// .jvmArgsAppend("-XX:CompileCommand=exclude,Condy::*")
				.forks(1) //
				.warmupIterations(5) //
				.measurementIterations(5) //
				.build(); //
		new Runner(options).run();
	}

}

/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import static org.objectweb.asm.Opcodes.*;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinComposeFilter}.
 */
public class KotlinComposeFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinComposeFilter();

	/**
	 * <pre>
	 * &#064;androidx.compose.runtime.Composable
	 * fun example(x: Int) {
	 *   if (x < 0)
	 *     return
	 *   example(x - 1)
	 * }
	 * </pre>
	 *
	 * transformed by <a href=
	 * "https://github.com/JetBrains/kotlin/tree/v2.0.0-RC2/plugins/compose">Compose
	 * Kotlin compiler plugin version 2.0.0</a> into
	 *
	 * <pre>
	 * fun example(x: Int, $composer: Composer?, $changed: Int) {
	 *   $composer = $composer.startRestartGroup(...)
	 *   sourceInformation($composer, ...)
	 *   val $dirty = $changed
	 *   if ($changed and 0b0110 == 0) {
	 *     $dirty = $dirty or if ($composer.changed(x)) 0b0100 else 0b0010
	 *   }
	 *   if ($dirty and 0b0011 != 0b0010 || !$composer.skipping) {
	 *     if (isTraceInProgress()) {
	 *       traceEventStart(...)
	 *     }
	 *
	 *     if (x < 0) {
	 *       if (isTraceInProgress()) {
	 *         traceEventEnd()
	 *       }
	 *       $composer.endRestartGroup()?.updateScope { ... }
	 *       return
	 *     }
	 *
	 *     example(x, $composer, 0)
	 *
	 *     if (isTraceInProgress()) {
	 *       traceEventEnd()
	 *     }
	 *   } else {
	 *     $composer.skipToGroupEnd()
	 *   }
	 *   $composer.endRestartGroup()?.updateScope { ... }
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"f", "(Landroidx/compose/runtime/Composer;I)V", null, null);
		m.visitAnnotation("Landroidx/compose/runtime/Composable;", false);

		final Range range1 = new Range();
		final Range range2 = new Range();
		final Range range3 = new Range();
		final Range range4 = new Range();
		final Range range5 = new Range();
		final Range range6 = new Range();
		final Range range7 = new Range();

		m.visitVarInsn(ALOAD, 1);
		range1.fromInclusive = m.instructions.getLast();
		m.visitLdcInsn(Integer.valueOf(-974630231));
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"startRestartGroup", "(I)Landroidx/compose/runtime/Composer;",
				true);
		m.visitVarInsn(ASTORE, 1);
		m.visitVarInsn(ILOAD, 2);
		m.visitVarInsn(ISTORE, 3);
		Label label1 = new Label();
		m.visitLabel(label1);
		m.visitVarInsn(ILOAD, 2);
		m.visitIntInsn(BIPUSH, 14);
		m.visitInsn(IAND);
		Label label2 = new Label();
		m.visitJumpInsn(IFNE, label2);
		m.visitVarInsn(ILOAD, 3);
		m.visitVarInsn(ALOAD, 1);
		m.visitVarInsn(ILOAD, 0);
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"changed", "(I)Z", true);
		Label label3 = new Label();
		m.visitJumpInsn(IFEQ, label3);
		m.visitInsn(ICONST_4);
		Label label4 = new Label();
		m.visitJumpInsn(GOTO, label4);
		m.visitLabel(label3);
		m.visitInsn(ICONST_2);
		m.visitLabel(label4);
		m.visitInsn(IOR);
		m.visitVarInsn(ISTORE, 3);
		m.visitLabel(label2);
		m.visitVarInsn(ILOAD, 3);
		m.visitIntInsn(BIPUSH, 11);
		m.visitInsn(IAND);
		m.visitInsn(ICONST_2);
		Label label5 = new Label();
		m.visitJumpInsn(IF_ICMPNE, label5);
		m.visitVarInsn(ALOAD, 1);
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"getSkipping", "()Z", true);
		Label label6 = new Label();
		m.visitJumpInsn(IFNE, label6);
		range1.toInclusive = m.instructions.getLast();

		m.visitLabel(label5);
		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"isTraceInProgress", "()Z", false);
		Label label7 = new Label();
		m.visitJumpInsn(IFEQ, label7);
		range3.fromInclusive = m.instructions.getLast();
		m.visitLdcInsn(Integer.valueOf(-974630231));
		m.visitVarInsn(ILOAD, 3);
		m.visitInsn(ICONST_M1);
		m.visitLdcInsn("org.example.example (Example.kt:19)");
		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"traceEventStart", "(IIILjava/lang/String;)V", false);
		m.visitLabel(label7);
		range3.toInclusive = m.instructions.getLast();

		m.visitVarInsn(ILOAD, 0);
		Label label8 = new Label();
		m.visitJumpInsn(IFGE, label8);
		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"isTraceInProgress", "()Z", false);
		Label label9 = new Label();
		m.visitJumpInsn(IFEQ, label9);
		range4.fromInclusive = m.instructions.getLast();
		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"traceEventEnd", "()V", false);
		m.visitLabel(label9);
		range4.toInclusive = m.instructions.getLast();

		m.visitVarInsn(ALOAD, 1);
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"endRestartGroup",
				"()Landroidx/compose/runtime/ScopeUpdateScope;", true);
		m.visitInsn(DUP);
		Label label10 = new Label();
		m.visitJumpInsn(IFNULL, label10);
		range5.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(NEW, "org/example/ExampleKt$example$4");
		m.visitInsn(DUP);
		m.visitVarInsn(ILOAD, 0);
		m.visitVarInsn(ILOAD, 2);
		m.visitMethodInsn(INVOKESPECIAL, "org/example/ExampleKt$example$4",
				"<init>", "(II)V", false);
		m.visitTypeInsn(CHECKCAST, "kotlin/jvm/functions/Function2");
		m.visitMethodInsn(INVOKEINTERFACE,
				"androidx/compose/runtime/ScopeUpdateScope", "updateScope",
				"(Lkotlin/jvm/functions/Function2;)V", true);
		Label label11 = new Label();
		m.visitJumpInsn(GOTO, label11);
		m.visitLabel(label10);
		m.visitFrame(Opcodes.F_SAME1, 0, null, 1,
				new Object[] { "androidx/compose/runtime/ScopeUpdateScope" });
		m.visitInsn(POP);
		range5.toInclusive = m.instructions.getLast();

		m.visitLabel(label11);
		m.visitInsn(Opcodes.RETURN);

		m.visitLabel(label8);
		m.visitVarInsn(ILOAD, 0);
		m.visitInsn(ICONST_1);
		m.visitInsn(ISUB);
		m.visitVarInsn(ALOAD, 1);
		m.visitInsn(ICONST_0);
		m.visitMethodInsn(INVOKESTATIC, "org/example/ExampleKt", "example",
				"(ILandroidx/compose/runtime/Composer;I)V", false);

		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"isTraceInProgress", "()Z", false);
		Label label12 = new Label();
		m.visitJumpInsn(IFEQ, label12);
		range6.fromInclusive = m.instructions.getLast();
		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"traceEventEnd", "()V", false);
		m.visitJumpInsn(GOTO, label12);
		m.visitLabel(label6);
		range2.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(ALOAD, 1);
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"skipToGroupEnd", "()V", true);
		m.visitLabel(label12);
		range6.toInclusive = m.instructions.getLast();

		m.visitVarInsn(ALOAD, 1);
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"endRestartGroup",
				"()Landroidx/compose/runtime/ScopeUpdateScope;", true);
		m.visitInsn(DUP);
		Label label13 = new Label();
		m.visitJumpInsn(IFNULL, label13);
		range7.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(NEW, "org/example/ExampleKt$example$5");
		m.visitInsn(DUP);
		m.visitVarInsn(ILOAD, 0);
		m.visitVarInsn(ILOAD, 2);
		m.visitMethodInsn(INVOKESPECIAL, "org/example/ExampleKt$example$5",
				"<init>", "(II)V", false);
		m.visitTypeInsn(CHECKCAST, "kotlin/jvm/functions/Function2");
		m.visitMethodInsn(INVOKEINTERFACE,
				"androidx/compose/runtime/ScopeUpdateScope", "updateScope",
				"(Lkotlin/jvm/functions/Function2;)V", true);
		Label label14 = new Label();
		m.visitJumpInsn(GOTO, label14);
		m.visitLabel(label13);
		m.visitFrame(Opcodes.F_SAME1, 0, null, 1,
				new Object[] { "androidx/compose/runtime/ScopeUpdateScope" });
		m.visitInsn(POP);
		range7.toInclusive = m.instructions.getLast();

		m.visitLabel(label14);
		m.visitInsn(RETURN);
		range2.toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(range1, range2, range3, range4, range5, range6, range7);
	}

	/**
	 * TODO test non-static default value
	 * TODO replace int by String
	 *
	 * <pre>
	 * fun example(x: Int = 2) {
	 *   if (x < 0)
	 *     return
	 *   example(x - 1)
	 * }
	 * </pre>
	 *
	 * transformed by <a href=
	 * "https://github.com/JetBrains/kotlin/tree/v2.0.0/plugins/compose">Compose
	 * Kotlin compiler plugin version 2.0.0</a> into
	 *
	 * TODO
	 * https://github.com/JetBrains/kotlin/blob/v2.0.0/plugins/compose/compiler-hosted/src/main/java/androidx/compose/compiler/plugins/kotlin/lower/ComposableFunctionBodyTransformer.kt#L336-L357
	 *
	 * <pre>
	 * fun example(x: Int, $composer: Composer?, $changed: Int) {
	 *   $composer = $composer.startRestartGroup(...)
	 *   sourceInformation($composer, ...)
	 *   val $dirty = $changed
	 *   if ($changed and 0b0110 == 0) {
	 *     $dirty = $dirty or if ($composer.changed(x)) 0b0100 else 0b0010
	 *   }
	 *   if ($dirty and 0b0011 != 0b0010 || !$composer.skipping) {
	 *     x = if ($default and 0b1 != 0) 2 else x
	 *
	 *     if (isTraceInProgress()) {
	 *       traceEventStart(...)
	 *     }
	 *
	 *     if (x < 0) {
	 *       if (isTraceInProgress()) {
	 *         traceEventEnd()
	 *       }
	 *       $composer.endRestartGroup()?.updateScope { ... }
	 *       return
	 *     }
	 *
	 *     example(x, $composer, 0)
	 *
	 *     if (isTraceInProgress()) {
	 *       traceEventEnd()
	 *     }
	 *   } else {
	 *     $composer.skipToGroupEnd()
	 *   }
	 *   $composer.endRestartGroup()?.updateScope { ... }
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_TODO() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"f", "(ILandroidx/compose/runtime/Composer;II)V", null, null);
		m.visitAnnotation("Landroidx/compose/runtime/Composable;", false);

		final Range range1 = new Range();
		final Range range2 = new Range();
		final Range range3 = new Range();
		final Range range4 = new Range();
		final Range range5 = new Range();
		final Range range6 = new Range();
		final Range range7 = new Range();

		Label label0 = new Label();
		m.visitLabel(label0);
		range1.fromInclusive = m.instructions.getLast();
		m.visitLineNumber(6, label0);
		m.visitVarInsn(ALOAD, 1);
		m.visitLdcInsn(Integer.valueOf(-1061748588));
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"startRestartGroup", "(I)Landroidx/compose/runtime/Composer;",
				true);
		m.visitVarInsn(ASTORE, 1);
		m.visitVarInsn(ALOAD, 1);
		m.visitLdcInsn(
				"C(default_arguments)8@130L24:DefaultArguments.kt#p91eg0");
		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"sourceInformation",
				"(Landroidx/compose/runtime/Composer;Ljava/lang/String;)V",
				false);
		m.visitVarInsn(ILOAD, 2);
		m.visitVarInsn(ISTORE, 4);
		Label label1 = new Label();
		m.visitLabel(label1);
		m.visitVarInsn(ILOAD, 3);
		m.visitInsn(ICONST_1);
		m.visitInsn(IAND);
		Label label2 = new Label();
		m.visitJumpInsn(IFEQ, label2);
		m.visitVarInsn(ILOAD, 4);
		m.visitIntInsn(BIPUSH, 6);
		m.visitInsn(IOR);
		m.visitVarInsn(ISTORE, 4);
		Label label3 = new Label();
		m.visitJumpInsn(GOTO, label3);
		m.visitLabel(label2);
		m.visitFrame(Opcodes.F_APPEND, 1, new Object[] { Opcodes.INTEGER }, 0,
				null);
		m.visitVarInsn(ILOAD, 2);
		m.visitIntInsn(BIPUSH, 14);
		m.visitInsn(IAND);
		m.visitJumpInsn(IFNE, label3);
		m.visitVarInsn(ILOAD, 4);
		m.visitVarInsn(ALOAD, 1);
		m.visitVarInsn(ILOAD, 0);
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"changed", "(I)Z", true);
		Label label4 = new Label();
		m.visitJumpInsn(IFEQ, label4);
		m.visitInsn(ICONST_4);
		Label label5 = new Label();
		m.visitJumpInsn(GOTO, label5);
		m.visitLabel(label4);
		m.visitFrame(Opcodes.F_SAME1, 0, null, 1,
				new Object[] { Opcodes.INTEGER });
		m.visitInsn(ICONST_2);
		m.visitLabel(label5);
		m.visitFrame(Opcodes.F_FULL, 5,
				new Object[] { Opcodes.INTEGER,
						"androidx/compose/runtime/Composer", Opcodes.INTEGER,
						Opcodes.INTEGER, Opcodes.INTEGER },
				2, new Object[] { Opcodes.INTEGER, Opcodes.INTEGER });
		m.visitInsn(IOR);
		m.visitVarInsn(ISTORE, 4);
		m.visitLabel(label3);
		m.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		m.visitVarInsn(ILOAD, 4);
		m.visitIntInsn(BIPUSH, 11);
		m.visitInsn(IAND);
		m.visitInsn(ICONST_2);
		Label label6 = new Label();
		m.visitJumpInsn(IF_ICMPNE, label6);
		m.visitVarInsn(ALOAD, 1);
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"getSkipping", "()Z", true);
		Label label7 = new Label();
		m.visitJumpInsn(IFNE, label7);
		range1.toInclusive = m.instructions.getLast();
		m.visitLabel(label6);
		m.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		m.visitVarInsn(ILOAD, 3);
		m.visitInsn(ICONST_1);
		m.visitInsn(IAND);
		Label label8 = new Label();
		m.visitJumpInsn(IFEQ, label8); // TODO filter
		m.visitInsn(ICONST_2);
		m.visitVarInsn(ISTORE, 0);
		m.visitLabel(label8);
		m.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"isTraceInProgress", "()Z", false);
		Label label9 = new Label();
		m.visitJumpInsn(IFEQ, label9);
		range3.fromInclusive = m.instructions.getLast();
		m.visitLdcInsn(Integer.valueOf(-1061748588));
		m.visitVarInsn(ILOAD, 4);
		m.visitInsn(ICONST_M1);
		m.visitLdcInsn("org.example.default_arguments (DefaultArguments.kt:5)");
		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"traceEventStart", "(IIILjava/lang/String;)V", false);
		m.visitLabel(label9);
		range3.toInclusive = m.instructions.getLast();
		m.visitLineNumber(7, label9);
		m.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		m.visitVarInsn(ILOAD, 0);
		Label label10 = new Label();
		m.visitJumpInsn(IFGE, label10);
		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"isTraceInProgress", "()Z", false);
		Label label11 = new Label();
		m.visitJumpInsn(IFEQ, label11);
		range4.fromInclusive = m.instructions.getLast();
		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"traceEventEnd", "()V", false);
		m.visitLabel(label11);
		range4.toInclusive = m.instructions.getLast();
		m.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		m.visitVarInsn(ALOAD, 1);
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"endRestartGroup",
				"()Landroidx/compose/runtime/ScopeUpdateScope;", true);
		m.visitInsn(DUP);
		Label label12 = new Label();
		m.visitJumpInsn(IFNULL, label12);
		range5.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(ILOAD, 0);
		m.visitVarInsn(ILOAD, 2);
		m.visitVarInsn(ILOAD, 3);
		m.visitInvokeDynamicInsn("invoke",
				"(III)Lkotlin/jvm/functions/Function2;",
				new Handle(Opcodes.H_INVOKESTATIC,
						"java/lang/invoke/LambdaMetafactory", "metafactory",
						"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
						false),
				new Object[] { Type.getType(
						"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
						new Handle(Opcodes.H_INVOKESTATIC,
								"org/example/DefaultArgumentsKt",
								"default_arguments$lambda$0",
								"(IIILandroidx/compose/runtime/Composer;I)Lkotlin/Unit;",
								false),
						Type.getType(
								"(Landroidx/compose/runtime/Composer;Ljava/lang/Integer;)Lkotlin/Unit;") });
		m.visitMethodInsn(INVOKEINTERFACE,
				"androidx/compose/runtime/ScopeUpdateScope", "updateScope",
				"(Lkotlin/jvm/functions/Function2;)V", true);
		Label label13 = new Label();
		m.visitJumpInsn(GOTO, label13);
		m.visitLabel(label12);
		m.visitFrame(Opcodes.F_SAME1, 0, null, 1,
				new Object[] { "androidx/compose/runtime/ScopeUpdateScope" });
		m.visitInsn(POP);
		range5.toInclusive = m.instructions.getLast();
		m.visitLabel(label13);
		m.visitLineNumber(8, label13);
		m.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		m.visitInsn(RETURN);
		m.visitLabel(label10);
		m.visitLineNumber(9, label10);
		m.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		m.visitVarInsn(ILOAD, 0);
		m.visitInsn(ICONST_1);
		m.visitInsn(ISUB);
		m.visitVarInsn(ALOAD, 1);
		m.visitInsn(ICONST_0);
		m.visitInsn(ICONST_0);
		m.visitMethodInsn(INVOKESTATIC, "org/example/DefaultArgumentsKt",
				"default_arguments",
				"(ILandroidx/compose/runtime/Composer;II)V", false);
		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"isTraceInProgress", "()Z", false);
		Label label14 = new Label();
		m.visitJumpInsn(IFEQ, label14);
		range6.fromInclusive = m.instructions.getLast();
		m.visitMethodInsn(INVOKESTATIC, "androidx/compose/runtime/ComposerKt",
				"traceEventEnd", "()V", false);
		m.visitJumpInsn(GOTO, label14);
		m.visitLabel(label7);
		range2.fromInclusive = m.instructions.getLast();
		m.visitLineNumber(10, label7);
		m.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		m.visitVarInsn(ALOAD, 1);
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"skipToGroupEnd", "()V", true);
		m.visitLabel(label14);
		range6.toInclusive = m.instructions.getLast();
		m.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		m.visitVarInsn(ALOAD, 1);
		m.visitMethodInsn(INVOKEINTERFACE, "androidx/compose/runtime/Composer",
				"endRestartGroup",
				"()Landroidx/compose/runtime/ScopeUpdateScope;", true);
		m.visitInsn(DUP);
		Label label15 = new Label();
		m.visitJumpInsn(IFNULL, label15);
		range7.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(ILOAD, 0);
		m.visitVarInsn(ILOAD, 2);
		m.visitVarInsn(ILOAD, 3);
		m.visitInvokeDynamicInsn("invoke",
				"(III)Lkotlin/jvm/functions/Function2;",
				new Handle(Opcodes.H_INVOKESTATIC,
						"java/lang/invoke/LambdaMetafactory", "metafactory",
						"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
						false),
				new Object[] { Type.getType(
						"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
						new Handle(Opcodes.H_INVOKESTATIC,
								"org/example/DefaultArgumentsKt",
								"default_arguments$lambda$1",
								"(IIILandroidx/compose/runtime/Composer;I)Lkotlin/Unit;",
								false),
						Type.getType(
								"(Landroidx/compose/runtime/Composer;Ljava/lang/Integer;)Lkotlin/Unit;") });
		m.visitMethodInsn(INVOKEINTERFACE,
				"androidx/compose/runtime/ScopeUpdateScope", "updateScope",
				"(Lkotlin/jvm/functions/Function2;)V", true);
		Label label16 = new Label();
		m.visitJumpInsn(GOTO, label16);
		m.visitLabel(label15);
		m.visitFrame(Opcodes.F_SAME1, 0, null, 1,
				new Object[] { "androidx/compose/runtime/ScopeUpdateScope" });
		m.visitInsn(POP);
		range7.toInclusive = m.instructions.getLast();
		m.visitLabel(label16);
		m.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		m.visitInsn(RETURN);
		Label label17 = new Label();
		m.visitLabel(label17);
		m.visitLocalVariable("$dirty", "I", null, label1, label17, 4);
		m.visitLocalVariable("i", "I", null, label0, label17, 0);
		m.visitLocalVariable("$composer", "Landroidx/compose/runtime/Composer;",
				null, label0, label17, 1);
		m.visitLocalVariable("$changed", "I", null, label0, label17, 2);
		range2.toInclusive = m.instructions.getLast();
		m.visitMaxs(4, 5);
		m.visitEnd();

		filter.filter(m, context, output);

		assertIgnored(range1, range2, range3, range4, range5, range6, range7);
	}

}

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
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * TODO
 * https://github.com/scala/scala/blob/2.13.x/src/compiler/scala/tools/nsc/typechecker/SyntheticMethods.scala
 */
public final class ScalaCaseClassFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {

		if (!context.getClassName().endsWith("CaseClass")) {
			return;
		}
//		if (!isScalaClass(context)) {
//			return;
//		}

		final Matcher matcher = new Matcher();
		if (matcher.match(methodNode)) {
			output.ignore(methodNode.instructions.getFirst(),
					methodNode.instructions.getLast());
		}
	}

	private static boolean isScalaClass(final IFilterContext context) {
		return context.getClassAnnotations()
				.contains("Lscala/reflect/ScalaSignature;");
	}

	private static class Matcher extends AbstractMatcher {
		boolean match(final MethodNode methodNode) {
			if ("equals".equals(methodNode.name)) {
				return isDefaultEquals(methodNode);
			}
			if ("productIterator".equals(methodNode.name)) {
				return isDefaultProductIterator(methodNode);
			}
			if ("hashCode".equals(methodNode.name)) {
				return isDefaultHashCode(methodNode);
			}
			if ("toString".equals(methodNode.name)) {
				return isDefaultToString(methodNode);
			}
			return false;
		}

		private boolean isDefaultEquals(final MethodNode methodNode) {
			firstIsALoad0(methodNode);
			nextIsVar(Opcodes.ALOAD, "obj");
			nextIs(Opcodes.IF_ACMPEQ);
			nextIsVar(Opcodes.ALOAD, "obj");
			nextIs(Opcodes.ASTORE);
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.INSTANCEOF);
			nextIs(Opcodes.IFEQ);
			nextIs(Opcodes.ICONST_1);
			nextIs(Opcodes.ISTORE);
			nextIs(Opcodes.GOTO);
			nextIs(Opcodes.GOTO);
			nextIs(Opcodes.ICONST_0);
			nextIs(Opcodes.ISTORE);
			nextIs(Opcodes.GOTO);

			nextIs(Opcodes.ILOAD);
			nextIs(Opcodes.IFEQ);
			nextIsVar(Opcodes.ALOAD, "obj");
			nextIs(Opcodes.CHECKCAST);
			nextIsVar(Opcodes.ASTORE, "other");

			do {
				final AbstractInsnNode i = cursor;
				nextIsVar(Opcodes.ALOAD, "this");
				if (cursor == null) {
					cursor = i;
					break;
				}
				nextIs(Opcodes.INVOKEVIRTUAL); // accessor
				nextIsVar(Opcodes.ALOAD, "other");
				nextIs(Opcodes.INVOKEVIRTUAL); // accessor
				next();
				if (cursor == null) {
					return false;
				}
				if (cursor.getOpcode() == Opcodes.IF_ICMPNE) {
					continue;
				}
				if (cursor.getOpcode() != Opcodes.ASTORE) {
					return false;
				}

				nextIs(Opcodes.DUP);
				nextIs(Opcodes.IFNONNULL);
				nextIs(Opcodes.POP);
				nextIs(Opcodes.ALOAD);
				nextIs(Opcodes.IFNULL);
				nextIs(Opcodes.GOTO);
				nextIs(Opcodes.ALOAD);
				nextIs(Opcodes.INVOKEVIRTUAL);
				nextIs(Opcodes.IFEQ);
			} while (true);

			nextIsVar(Opcodes.ALOAD, "other");
			nextIsVar(Opcodes.ALOAD, "this");
			nextIs(Opcodes.INVOKEVIRTUAL); // canEqual
			nextIs(Opcodes.IFEQ);
			nextIs(Opcodes.ICONST_1);
			nextIs(Opcodes.GOTO);

			nextIs(Opcodes.ICONST_0);
			nextIs(Opcodes.IFEQ);
			nextIs(Opcodes.ICONST_1);
			nextIs(Opcodes.GOTO);
			nextIs(Opcodes.ICONST_0);
			nextIs(Opcodes.IRETURN);

			return cursor != null;
		}

		private boolean isDefaultProductIterator(final MethodNode methodNode) {
			cursor = methodNode.instructions.getFirst();
			nextIs(Opcodes.ALOAD);
			nextIsInvokeRuntime("typedProductIterator",
					"(Lscala/Product;)Lscala/collection/Iterator;");
			nextIs(Opcodes.ARETURN);
			return cursor != null;
		}

		private boolean isDefaultToString(final MethodNode methodNode) {
			cursor = methodNode.instructions.getFirst();
			nextIs(Opcodes.ALOAD);
			nextIsInvokeRuntime("_toString",
					"(Lscala/Product;)Ljava/lang/String;");
			nextIs(Opcodes.ARETURN);
			return cursor != null;
		}

		private boolean isDefaultHashCode(final MethodNode methodNode) {
			cursor = methodNode.instructions.getFirst();
			nextIs(Opcodes.ALOAD);
			nextIsInvokeRuntime("_hashCode", "(Lscala/Product;)I");
			nextIs(Opcodes.IRETURN);
			return cursor != null;
		}

		private void nextIsInvokeRuntime(final String name, final String desc) {
			nextIs(Opcodes.INVOKEVIRTUAL);
			if (cursor == null) {
				return;
			}
			final MethodInsnNode m = (MethodInsnNode) cursor;
			if ("scala/runtime/ScalaRunTime$".equals(m.owner)
					&& name.equals(m.name) && desc.equals(m.desc)) {
				return;
			}
			cursor = null;
		}

	}

}

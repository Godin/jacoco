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
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * TODO add description
 */
final class EnumStaticInitFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if ("java/lang/Enum".equals(context.getSuperClassName())
				&& new Matcher().match(context, methodNode)) {
			output.ignore(methodNode.instructions.getFirst(),
					methodNode.instructions.getLast());
		}
	}

	private static class Matcher extends AbstractMatcher {
		private boolean match(final IFilterContext context,
				final MethodNode methodNode) {
			cursor = methodNode.instructions.getFirst();
			nextIs(Opcodes.ICONST_0);
			nextIsType(Opcodes.ANEWARRAY, context.getClassName());
			nextIsPutStatic();
			nextIs(Opcodes.RETURN);
			return cursor != null;
		}

		final void nextIsPutStatic() {
			nextIs(Opcodes.PUTSTATIC);
			if (cursor == null) {
				return;
			}
			final FieldInsnNode i = (FieldInsnNode) cursor;
			if ("$VALUES".equals(i.name) || "ENUM$VALUES".equals(i.name)) {
				return;
			}
			cursor = null;
		}
	}

}

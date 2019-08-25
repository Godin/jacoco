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
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters bytecode that Scala compiler generates for lazy initializers.
 *
 * State of initialization stored in a field with prefix {@code bitmap$}, type
 * depends on number of initializers:
 * <ul>
 * <li>boolean for single initializer</li>
 * <li>byte for less than 9</li>
 * <li>int for less than 33</li>
 * <li>long otherwise</li>
 * </ul>
 */
public final class ScalaLazyInitializerFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (!SyntheticFilter.isScalaClass(context)) {
			return;
		}
		final Matcher matcher = new Matcher();
		matcher.matchGetter(methodNode, output);
		matcher.matchInit(methodNode, output);
	}

	private static class Matcher extends AbstractMatcher {
		void matchGetter(final MethodNode m, final IFilterOutput output) {
			firstIsALoad0(m);
			nextIsBitmap();
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.INVOKESPECIAL);
			nextIs(Opcodes.GOTO);
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.GETFIELD);
			if (cursor == null) {
				return;
			}
			output.ignore(m.instructions.getFirst(), m.instructions.getLast());
		}

		void matchInit(final MethodNode m, final IFilterOutput output) {
			firstIsALoad0(m);
			nextIs(Opcodes.DUP);
			nextIs(Opcodes.ASTORE);
			nextIs(Opcodes.MONITORENTER);
			nextIs(Opcodes.ALOAD);
			nextIsBitmap();
			if (cursor == null) {
				return;
			}
			output.ignore(cursor, cursor);
			output.ignore(((JumpInsnNode) cursor).label,
					m.instructions.getLast());
		}

		void nextIsBitmap() {
			nextIs(Opcodes.GETFIELD);
			if (cursor == null) {
				return;
			}
			final FieldInsnNode f = (FieldInsnNode) cursor;
			if (!f.name.startsWith("bitmap$")) {
				cursor = null;
				return;
			}
			if ("Z".equals(f.desc)) {
				nextIs(Opcodes.IFNE);
			} else if ("B".equals(f.desc)) {
				next(); // ICONST_1, ICONST_2, ICONST_4, BIPUSH, SIPUSH
				nextIs(Opcodes.IAND);
				nextIs(Opcodes.I2B);
				nextIs(Opcodes.ICONST_0);
				nextIs(Opcodes.IF_ICMPNE);
			} else if ("I".equals(f.desc)) {
				next(); // ICONST_1, ICONST_2, ICONST_4, BIPUSH, SIPUSH, LDC
				nextIs(Opcodes.IAND);
				nextIs(Opcodes.ICONST_0);
				nextIs(Opcodes.IF_ICMPNE);
			} else if ("J".equals(f.desc)) {
				next(); // LCONST_1, LDC
				nextIs(Opcodes.LAND);
				nextIs(Opcodes.LCONST_0);
				nextIs(Opcodes.LCMP);
				nextIs(Opcodes.IFNE);
			} else {
				cursor = null;
			}
		}
	}

}

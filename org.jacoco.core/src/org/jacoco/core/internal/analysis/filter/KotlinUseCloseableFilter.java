/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * TODO
 */
final class KotlinUseCloseableFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		// TODO

		final Matcher matcher = new Matcher();
		for (TryCatchBlockNode t : methodNode.tryCatchBlocks) {
			if ("java/lang/Throwable".equals(t.type)) {
				matcher.match(t.handler, output);
			}
		}

	}

	private static class Matcher extends AbstractMatcher {
		void match(final AbstractInsnNode start, final IFilterOutput output) {
			cursor = start.getPrevious();
			nextIsVar(Opcodes.ASTORE, "t1");
			nextIsVar(Opcodes.ALOAD, "t1");
			nextIsVar(Opcodes.ASTORE, "exception");
			nextIsVar(Opcodes.ALOAD, "t1");
			nextIs(Opcodes.ATHROW);

			nextIsVar(Opcodes.ASTORE, "t2");
			nextIsVar(Opcodes.ALOAD, "resource");
			nextIsVar(Opcodes.ALOAD, "exception");
			nextIsInvoke(Opcodes.INVOKESTATIC, "kotlin/io/CloseableKt",
				"closeFinally", "(Ljava/io/Closeable;Ljava/lang/Throwable;)V");
			nextIsVar(Opcodes.ALOAD, "t2");
			nextIs(Opcodes.ATHROW);

			if (cursor != null) {
				output.ignore(start, cursor);
			}
		}
	}

}

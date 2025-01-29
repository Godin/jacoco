/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
import org.objectweb.asm.tree.MethodNode;

/**
 * TODO
 */
final class KotlinCallableReferenceFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if ((context.getClassAccess() & Opcodes.ACC_SYNTHETIC) == 0) {
			return;
		}
		if (!context.getSuperClassName()
				.startsWith("kotlin/jvm/internal/FunctionReference")) {
			return;
		}
		output.ignore(methodNode.instructions.getFirst(),
				methodNode.instructions.getLast());
	}

}

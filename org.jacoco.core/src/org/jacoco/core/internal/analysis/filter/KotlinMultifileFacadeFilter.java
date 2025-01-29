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

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * TODO
 */
final class KotlinMultifileFacadeFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (isMultifileFacade(context)) {
			System.out.println("Ignoring " + context.getClassName());
			output.ignore(methodNode.instructions.getFirst(),
					methodNode.instructions.getLast());
		}
	}

	private static boolean isMultifileFacade(final IFilterContext context) {
		for (final AnnotationNode annotationNode : context
				.getClassAnnotationNodes()) {
			if (KotlinGeneratedFilter.KOTLIN_METADATA_DESC
					.equals(annotationNode.desc)) {
				for (int i = 0; i < annotationNode.values.size(); i += 2) {
					final String name = (String) annotationNode.values.get(i);
					final Object value = annotationNode.values.get(i + 1);
					if ("k".equals(name)) {
						return Integer.valueOf(4).equals(value)
								// TODO unfortunately filters too much:
								|| Integer.valueOf(3).equals(value);
					}
				}
			}
		}
		return false;
	}

}

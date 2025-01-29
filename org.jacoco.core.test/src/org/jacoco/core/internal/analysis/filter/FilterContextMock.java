/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.tree.AnnotationNode;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link IFilterContext} mock for unit tests.
 */
public class FilterContextMock implements IFilterContext {

	public int classAccess;
	public String className = "Foo";
	public String superClassName = "java/lang/Object";
	public Set<AnnotationNode> classAnnotationNodes = new HashSet<AnnotationNode>();
	/** @deprecated use {@link #classAnnotationNodes} instead */
	@Deprecated
	public Set<String> classAnnotations = new HashSet<String>();
	public Set<String> classAttributes = new HashSet<String>();
	public String sourceFileName = "Foo.java";
	public String sourceDebugExtension;

	public int getClassAccess() {
		return classAccess;
	}

	public String getClassName() {
		return className;
	}

	public String getSuperClassName() {
		return superClassName;
	}

	public Set<String> getClassAnnotations() {
		return classAnnotations;
	}

	public Set<AnnotationNode> getClassAnnotationNodes() {
		return classAnnotationNodes;
	}

	public Set<String> getClassAttributes() {
		return classAttributes;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public String getSourceDebugExtension() {
		return sourceDebugExtension;
	}

}

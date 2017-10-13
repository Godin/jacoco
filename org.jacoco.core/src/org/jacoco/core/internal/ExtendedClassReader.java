/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;

public class ExtendedClassReader extends ClassReader {

	/**
	 * TODO add comment
	 */
	private static final Label LABEL = new Label();

	public ExtendedClassReader(final byte[] b) {
		super(b);
	}

	@Override
	protected Label readLabel(final int offset, final Label[] labels) {
		if (offset >= labels.length) {
			return LABEL;
		}
		return super.readLabel(offset, labels);
	}

}

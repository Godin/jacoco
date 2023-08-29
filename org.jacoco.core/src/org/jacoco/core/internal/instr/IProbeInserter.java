/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.internal.instr;

import org.jacoco.core.internal.flow.IFrame;

/**
 * Internal interface for insertion of probes into in the instruction sequence
 * of a method.
 */
interface IProbeInserter {

	/**
	 * Inserts the probe with the given id.
	 *
	 * @param id
	 *            id of the probe to insert
	 */
	@Deprecated
	void insertProbe(int id);

	void insertProbe(int id, IFrame frame);

}

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
package org.jacoco.core.test.validation;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.validation.targets.Exceptions;
import org.junit.Test;

public class ExceptionsNewTest extends ValidationTestBase {

    public ExceptionsNewTest() {
        super(Exceptions.class);
    }

    @Test
    public void run() {
        assertLine("case-1", ICounter.FULLY_COVERED);
        assertLine("case-1_1", ICounter.NOT_COVERED);

        assertLine("case-1_2", ICounter.FULLY_COVERED);
        // TODO(Godin): wtf?
        assertLine("case-1_3", ICounter.NOT_COVERED);
        assertLine("case-1_4", ICounter.FULLY_COVERED);

        assertLine("case-2", ICounter.FULLY_COVERED);
        assertLine("case-2_1", ICounter.PARTLY_COVERED);
        assertLine("case-2_2", ICounter.NOT_COVERED);

        assertLine("case-3", ICounter.NOT_COVERED);

        assertLine("case-4_1", ICounter.NOT_COVERED);
        // TODO(Godin): affected by changes in javac
        assertLine("case-4_2", ICounter.EMPTY);

        assertLine("case-5", ICounter.FULLY_COVERED);

        assertLine("case-6_1", ICounter.FULLY_COVERED);
        // TODO(Godin): affected by changes in javac
        assertLine("case-6_2", ICounter.EMPTY);
    }

}
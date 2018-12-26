/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.ant;

import java.io.File;

import org.apache.ant.antunit.junit3.AntUnitSuite;
import org.apache.ant.antunit.junit4.AntUnitSuiteRunner;
import org.junit.runner.RunWith;

import junit.framework.TestSuite;

@RunWith(AntUnitSuiteRunner.class)
public class AgentTest {

	public static TestSuite suite() {
		System.setProperty("org.jacoco.ant.agentTest.classes.dir",
				TestTarget.getClassPath());
		final File file = new File("src/org/jacoco/ant/AgentTest.xml");
		return new AntUnitSuite(file, AgentTest.class);
	}

}
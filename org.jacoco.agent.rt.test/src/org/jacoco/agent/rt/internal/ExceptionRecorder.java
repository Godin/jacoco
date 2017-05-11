/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.agent.rt.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.jacoco.agent.rt.internal.IExceptionLogger;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * {@link IExceptionLogger} implementation for testing purposes.
 */
public class ExceptionRecorder implements IExceptionLogger {

	private Class<?> exceptionType;
	private String message;
	private Class<?> causeType;
	private String stackTrace;

	public void logExeption(Exception ex) {
		assertNull("multiple exeptions", exceptionType);
		exceptionType = ex.getClass();
		message = ex.getMessage();
		causeType = ex.getCause() == null ? null : ex.getCause().getClass();

		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));
		stackTrace = sw.toString();
	}

	public void clear() {
		exceptionType = null;
		message = null;
		causeType = null;
	}

	public void assertNoException() {
		assertNull(stackTrace, exceptionType);
	}

	public void assertException(final Class<? extends Throwable> exceptionType,
			final String message) {
		assertEquals(exceptionType, this.exceptionType);
		assertEquals(message, this.message);
	}

	public void assertException(final Class<? extends Throwable> exceptionType,
			final String message, final Class<? extends Throwable> causeType) {
		assertEquals(exceptionType, this.exceptionType);
		assertEquals(message, this.message);
		assertEquals(causeType, this.causeType);
	}

}

/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.targets;

import static org.junit.Assert.fail;
import static org.jacoco.core.test.validation.targets.Stubs.nop;

import java.lang.annotation.AnnotationFormatError;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This test target builds a constant with a lambda value in an annotation.
 */
public class LambdaInAnnotationTarget {

	@Retention(RetentionPolicy.RUNTIME)
	@interface LambdaInAnnotation {

		Runnable RUN = () -> {
			nop(); // $line-lambdabody$
		};

	}

	@LambdaInAnnotation
	void m1() {
	}

	@LambdaInAnnotation
	void m2() {
	}

	public static void main(String[] args) throws Exception {
		LambdaInAnnotation.RUN.run();

		LambdaInAnnotation a1 = LambdaInAnnotationTarget.class
				.getDeclaredMethod("m1")
				.getAnnotation(LambdaInAnnotation.class);
		LambdaInAnnotation a2 = LambdaInAnnotationTarget.class
				.getDeclaredMethod("m2")
				.getAnnotation(LambdaInAnnotation.class);
		// see JDK-8169629
		try {
			a1.equals(a2);
			fail("exception expected");
		} catch (AnnotationFormatError e) {
			// expected
		}
	}

}

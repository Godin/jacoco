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

/**
 * Experimental.
 *
 * <p>
 * (Godin): I believe that there is <i>happens-before</i> relationship (in terms
 * of Chapter 17 of The Java(tm) Language Specification) between loading of
 * class, its initialization and its execution. So that if we'll perform
 * initialization of probe array during instrumentation in case of online
 * instrumentation and during class initialization ("clinit") in case of offline
 * instrumentation, then we can greatly reduce overhead of synchronization
 * during retrieval of probe array in
 * {@link org.jacoco.core.runtime.RuntimeData#getProbes(java.lang.Object[])}
 * (see benchmarks), which allows to fully switch on usage of
 * LocalProbeArrayStrategy and so that additional members won't be required for
 * instrumentation.
 * </p>
 *
 * <p>
 * Idea of "companion classes" for instrumentation without addition of members
 * has some unanswered questions:
 * </p>
 * <ul>
 * <li>In case of online instrumentation of bootstrap classes - how to add class
 * into bootstrap classloader?</li>
 * <li>In case of offline instrumentation - how to persist class?</li>
 * <li>What will be the impact on PermGen/Metaspace?</li>
 * </ul>
 * <p>
 * This optimization supersedes idea of "companion classes" putting away the
 * need of answer on those questions.
 * </p>
 *
 * <p>
 * However this is a space-time tradeoff - currently probe array won't be
 * allocated for classes which are loaded, but not executed:
 * </p>
 * 
 * <pre>
 * public class Example {
 * 	// not executed:
 * 	public static final String $jacoco = $jacocoInit();
 *
 * 	static {
 * 		System.out.println("clinit");
 * 	}
 *
 * 	static boolean[] $jacocoInit();
 * }
 *
 * public class ExampleTest {
 * 	public static void main(String[] args) {
 * 		System.out.println(Example.class);
 * 		System.out.println(java.util.Arrays
 * 				.toString(Example.class.getDeclaredFields()));
 * 	}
 * }
 * </pre>
 *
 * <p>
 * But current experimental implementation of "companion classes" also
 * initializes probe array at time of instrumentation. And this will not affect
 * size of dump because of recent improvement - empty probe arrays are not
 * stored.
 * </p>
 * 
 * <p>
 * Also this optimization allows to remove need to pass class ID and class name
 * for retrieval of probe array - "slot id" allocated during initialization of
 * probe array can be used. In case of online instrumentation slots can be
 * sequential, so that array can be used as a storage -
 * {@link org.jacoco.core.internal.runtime.RuntimeData3}.
 * </p>
 */
package org.jacoco.core.internal.runtime;
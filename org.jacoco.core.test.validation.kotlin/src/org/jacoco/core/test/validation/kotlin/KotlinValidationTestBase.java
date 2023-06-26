/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.test.validation.kotlin;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.junit.runners.Parameterized;

import java.net.URLClassLoader;

public class KotlinValidationTestBase extends ValidationTestBase {

	private static URLClassLoader classLoader;

	protected final KotlinVersion kotlinVersion;

	protected static class KotlinVersion {
		private final int major;
		private final int minor;
		private final int micro;

		KotlinVersion(final String version) {
			final String[] s = version.split("[.-]");
			this.major = Integer.parseInt(s[0]);
			this.minor = Integer.parseInt(s[1]);
			this.micro = Integer.parseInt(s[2]);
		}

		public boolean isBefore(final String version) {
			final KotlinVersion other = new KotlinVersion(version);
			return this.major < other.major || (this.major == other.major
					&& this.minor < other.minor
					|| (this.minor == other.minor && this.micro < other.micro));
		}
	}

	@Parameterized.BeforeParam
	public static void beforeParam(final String kotlinVersion)
			throws Exception {
		classLoader = Kotlin.get(kotlinVersion).getClassLoader();
	}

	@Parameterized.AfterParam
	public static void afterParam() throws Exception {
		classLoader.close();
	}

	protected KotlinValidationTestBase(final String kotlinVersion,
			final Class<?> target) throws Exception {
		super(classLoader.loadClass(target.getName()));
		this.kotlinVersion = new KotlinVersion(kotlinVersion);
	}

}

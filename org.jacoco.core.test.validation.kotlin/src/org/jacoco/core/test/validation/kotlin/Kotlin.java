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

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

final class Kotlin {

	static List<String> SUPPORTED_VERSIONS = Arrays.asList( //
			"1.4.0", //
			"1.5.0", //
			"1.6.0", //
			"1.7.0", //
			"1.8.10" //
	);

	static final Map<String, String> CHECKSUMS = new HashMap<String, String>() {
		{
			put("1.2.0",
					"895d0f8286db3e4f43d67cd5e09b600af6e0a5017cb74072d1b09c78b697775a");
			put("1.3.0",
					"ff851cb84dd12df6078ae1f4a5424de9be6dcb4ac578b35455eeb7106dc52592");
			// With coroutines
			put("1.4.0",
					"590391d13b3c65ba52cba470f56efd5b14e2b1f5b9459f63aa12eb38ef52f161");
			put("1.5.0",
					"0343fc1f628fec1beccc9e534d2b8b7a0f8964b97e21563585d44d6d928ed1b7");
			put("1.6.0",
					"174c92e12a54c0901fd9f0badacf1514c28b5197a95654e4dab1775293dde1dc");
			put("1.7.0",
					"f5216644ad81571e5db62ec2322fe07468927bda40f51147ed626a2884b55f9a");
			put("1.8.10",
					"4c3fa7bc1bb9ef3058a2319d8bcc3b7196079f88e92fdcd8d304a46f4b6b5787");
		}
	};
	private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
	private static Map<String, Kotlin> map = new HashMap<String, Kotlin>();

	private final String kotlinVersion;

	public static Kotlin get(String version) throws Exception {
		Kotlin result = map.get(version);
		if (result == null) {
			result = new Kotlin(version);
			result.compile();
			map.put(version, result);
		}
		return result;
	}

	private Kotlin(final String version) {
		this.kotlinVersion = version;
	}

	public URLClassLoader getClassLoader() throws Exception {
		return new Kotlin.ChildFirstClassLoader(
				new URL[] { d.toURI().toURL() });
	}

	private File d;

	private void compile() throws Exception {
		final File baseDir = new File("/tmp/k/" + kotlinVersion);
		assertTrue(baseDir.isDirectory() || baseDir.mkdirs());

		// Download
		final File archive = new File(baseDir, "kotlin-compiler.zip");
		if (!archive.exists()) {
			System.err.println("Downloading into " + archive);
			Kotlin.download(
					"https://github.com/JetBrains/kotlin/releases/download/v"
							+ kotlinVersion + "/kotlin-compiler-"
							+ kotlinVersion + ".zip",
					archive);
		}
		if (!Kotlin.sha256(archive).equals(CHECKSUMS.get(kotlinVersion))
				&& false) {
			throw new IllegalStateException("sha256 " + archive);
		}

		// Unpack
		System.err.println("Unpacking into " + baseDir);
		Kotlin.unpack(archive, baseDir);
		File file = new File(baseDir, "kotlinc/bin/kotlinc");
		assertTrue(file.setExecutable(true));

		// TODO every test compiles entire directory
		final File d = new File(baseDir, "classes");
		System.err.println("Compiling into " + d);
		assertTrue(!d.exists() || Kotlin.deleteDirectory(d));
		int exitCode = new ProcessBuilder(file.getAbsolutePath(), //
				"src/org/jacoco/core/test/validation/kotlin/targets", //
				"-d", d.getAbsolutePath(), //
				// "-cp", System.getProperty("java.class.path") //
				"-cp", "../org.jacoco.core.test/target/classes"
						// 1.4.x and 1.5.x
						+ (":" + new File(baseDir,
								"kotlinc/lib/kotlinx-coroutines-core.jar"))
						// 1.6.x and upper
						+ (":" + new File(baseDir,
								"kotlinc/lib/kotlinx-coroutines-core-jvm.jar")))
				.inheritIO().start().waitFor();
		assertEquals("exit code", 0, exitCode);

		this.d = d;
	}

	private static boolean deleteDirectory(final File directoryToBeDeleted) {
		final File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (final File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

	private static void unpack(final File file, final File destinationDir)
			throws IOException {
		final ZipInputStream zis = new ZipInputStream(
				new FileInputStream(file));
		ZipEntry zipEntry = zis.getNextEntry();
		final byte[] buffer = new byte[1024];
		while (zipEntry != null) {
			final File newFile = new File(destinationDir, zipEntry.getName());
			if (zipEntry.isDirectory()) {
				newFile.mkdirs();
			} else {
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
			}
			zipEntry = zis.getNextEntry();
		}
	}

	private static void download(String url, File destination)
			throws IOException {
		InputStream inputStream = new URL(url).openConnection()
				.getInputStream();
		Files.copy(inputStream, destination.toPath());
	}

	private static String bytesToHex(byte[] bytes) {
		final char[] result = new char[bytes.length * 2];
		for (int i = 0; i < bytes.length; i++) {
			final int v = bytes[i] & 0xFF;
			result[i * 2] = HEX_ARRAY[v >>> 4];
			result[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(result);
	}

	private static String sha256(File file)
			throws IOException, NoSuchAlgorithmException {
		final byte[] buffer = new byte[8192];
		int count;
		final MessageDigest digest = MessageDigest.getInstance("SHA-256");
		final InputStream bis = new FileInputStream(file);
		while ((count = bis.read(buffer)) > 0) {
			digest.update(buffer, 0, count);
		}
		bis.close();
		return bytesToHex(digest.digest());
	}

	// TODO can be unit tested
	private static class ChildFirstClassLoader extends URLClassLoader {
		public ChildFirstClassLoader(final URL[] urls) {
			super(urls);
		}

		@Override
		protected Class<?> loadClass(final String name, final boolean resolve)
				throws ClassNotFoundException {
			final Class<?> result = findLoadedClass(name);
			if (result != null) {
				return result;
			}
			try {
				return findClass(name);
			} catch (final ClassNotFoundException e) {
				if (name.contains("target")) {
					throw e;
				}
				return super.loadClass(name, resolve);
			}
		}

		@Override
		public URL getResource(final String name) {
			final URL resource = findResource(name);
			if (resource != null) {
				return resource;
			}
			return super.getResource(name);
		}
	}
}

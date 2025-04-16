/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov -initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.internal.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;

/**
 * Report formatter that TODO.
 *
 * <pre>
 * find . -type f -name '*.exec' -exec cat {} \; > jacoco.exec
 *
 * java \
 *     -jar /Users/godin/projects/jacoco/jacoco/org.jacoco.cli/target/org.jacoco.cli-0.8.14-SNAPSHOT-nodeps.jar \
 *     report \
 *     ./sonar-kotlin-checks/build/jacoco/test.exec \
 *     --classfiles ./sonar-kotlin-checks/build/classes/kotlin/main \
 *     --sourcefiles ./sonar-kotlin-checks/src/main/java \
 *     --text jacoco.txt
 * </pre>
 */
public class TextFormatter {

	/**
	 * Creates a new visitor to write a report.
	 *
	 * @param output
	 *            output stream to write the report to
	 * @return visitor to emit the report data to
	 */
	public IReportVisitor createVisitor(final OutputStream output) {
		final PrintStream printStream = new PrintStream(output);
		class Visitor implements IReportVisitor {
			public void visitInfo(final List<SessionInfo> sessionInfos,
					final Collection<ExecutionData> executionData)
					throws IOException {
				// throw new UnsupportedOperationException();
			}

			public IReportGroupVisitor visitGroup(final String name)
					throws IOException {
				throw new UnsupportedOperationException();
			}

			public void visitBundle(final IBundleCoverage bundle,
					final ISourceFileLocator locator) throws IOException {
				for (IPackageCoverage aPackage : bundle.getPackages()) {
					for (IClassCoverage aClass : aPackage.getClasses()) {
						if (locator.getSourceFile(aClass.getPackageName(),
								aClass.getSourceFileName()) == null) {
							printStream.println(aClass.getSourceFileName());
							printStream.println(
									"NOT FOUND FOR class " + aClass.getName());
						}
					}
					for (ISourceFileCoverage aSourceFile : aPackage
							.getSourceFiles()) {
						renderSourceFile(printStream, aSourceFile, locator);
					}
				}
			}

			public void visitEnd() throws IOException {
				printStream.close();
			}
		}
		return new Visitor();
	}

	private void renderSourceFile(final PrintStream output,
			final ISourceFileCoverage sourceFileCoverage,
			final ISourceFileLocator sourceFileLocator) throws IOException {
		output.println(sourceFileCoverage.getPackageName() + "/"
				+ sourceFileCoverage.getName());
		final Reader sourceFile = sourceFileLocator.getSourceFile( //
				sourceFileCoverage.getPackageName(),
				sourceFileCoverage.getName());
		if (sourceFile == null) {
			// TODO not found
			output.println("NOT FOUND");
			return;
		}
		final BufferedReader sourceReader = new BufferedReader(sourceFile);
		int lineNr = 0;
		String lineText;
		while ((lineText = sourceReader.readLine()) != null) {
			lineNr++;
			final ILine lineCoverage = sourceFileCoverage.getLine(lineNr);
			final ICounter branchCounter = lineCoverage.getBranchCounter();
			output.printf("%5s %1s |%s%n", //
					branchCounter.getTotalCount() == 0 ? ""
							: String.format("%d/%d",
									branchCounter.getCoveredCount(),
									branchCounter.getTotalCount()),
					statusToString(lineCoverage), //
					lineText);
		}
		sourceReader.close();
	}

	private static String statusToString(final ILine lineCoverage) {
		switch (lineCoverage.getStatus()) {
		case ICounter.EMPTY:
			return "";
		case ICounter.NOT_COVERED:
			return "N";
		case ICounter.PARTLY_COVERED:
			return "P";
		case ICounter.FULLY_COVERED:
			return "F";
		default:
			throw new AssertionError();
		}
	}

}

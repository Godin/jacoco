/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;

/**
 * Report formatter that TODO.
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
					final Collection<ExecutionData> executionData) {
				// nothing to do
			}

			public IReportGroupVisitor visitGroup(final String name) {
				throw new UnsupportedOperationException();
			}

			public void visitBundle(final IBundleCoverage bundle,
					final ISourceFileLocator locator) throws IOException {
				writeBundleCoverage(printStream, bundle, locator);
			}

			public void visitEnd() {
				printStream.close();
			}
		}
		return new Visitor();
	}

	private static void writeBundleCoverage(final PrintStream output,
			final IBundleCoverage bundleCoverage,
			final ISourceFileLocator sourceFileLocator) throws IOException {
		output.println("Bundle");
		writeCounters(output, bundleCoverage);
		for (final IPackageCoverage packageCoverage : bundleCoverage
				.getPackages()) {
			for (final IClassCoverage classCoverage : packageCoverage
					.getClasses()) {
				// TODO
				if (sourceFileLocator.getSourceFile(
						classCoverage.getPackageName(),
						classCoverage.getSourceFileName()) == null) {
					output.println(classCoverage.getSourceFileName());
					output.println(
							"NOT FOUND FOR class " + classCoverage.getName());
				}
			}
			writePackageCoverage(output, packageCoverage, sourceFileLocator);
		}
	}

	private static void writePackageCoverage(final PrintStream output,
			final IPackageCoverage packageCoverage,
			final ISourceFileLocator sourceFileLocator) throws IOException {
		output.print("Package ");
		output.println(packageCoverage.getName());
		writeCounters(output, packageCoverage);
		for (final IClassCoverage classCoverage : packageCoverage
				.getClasses()) {
			writeClassCoverage(output, classCoverage);
		}
		for (final ISourceFileCoverage sourceFileCoverage : packageCoverage
				.getSourceFiles()) {
			writeSourceFileCoverage(output, sourceFileCoverage,
					sourceFileLocator);
		}
	}

	private static void writeClassCoverage(final PrintStream output,
			final IClassCoverage classCoverage) {
		// TODO indent classes inside package
		output.print("Class ");
		output.println(classCoverage.getName());
		writeCounters(output, classCoverage);
		for (final IMethodCoverage methodCoverage : classCoverage
				.getMethods()) {
			writeMethodCoverage(output, methodCoverage);
		}
	}

	private static void writeMethodCoverage(final PrintStream output,
			final IMethodCoverage methodCoverage) {
		// TODO indent methods inside class
		output.print("Method ");
		output.println(methodCoverage.getName() + methodCoverage.getDesc());
		writeCounters(output, methodCoverage);
	}

	private static void writeCounters(final PrintStream output,
			final ICoverageNode coverage) {
		output.print("  Classes:");
		output.println(coverage.getClassCounter());
		output.print("  Methods:");
		output.println(coverage.getMethodCounter());
		output.print("  Instructions: ");
		output.println(coverage.getInstructionCounter());
		output.print("  Branches: ");
		output.println(coverage.getBranchCounter());
	}

	private static void writeSourceFileCoverage(final PrintStream output,
			final ISourceFileCoverage sourceFileCoverage,
			final ISourceFileLocator sourceFileLocator) throws IOException {
		// TODO indent source inside package?
		output.println("Source: " + sourceFileCoverage.getPackageName() + "/"
				+ sourceFileCoverage.getName());
		writeCounters(output, sourceFileCoverage);
		final Reader sourceReader = sourceFileLocator.getSourceFile( //
				sourceFileCoverage.getPackageName(),
				sourceFileCoverage.getName());
		if (sourceReader == null) {
			// TODO not found
			output.println("NOT FOUND");
			return;
		}
		final BufferedReader sourceBufferedReader = new BufferedReader(
				sourceReader);
		int lineNumber = 0;
		String lineText;
		while ((lineText = sourceBufferedReader.readLine()) != null) {
			lineNumber++;
			final ILine lineCoverage = sourceFileCoverage.getLine(lineNumber);
			final ICounter branchCounter = lineCoverage.getBranchCounter();
			final ICounter instructionCounter = lineCoverage
					.getInstructionCounter();
			output.printf("%5s %5s %4d |%s%n", //
					branchCounter.getTotalCount() == 0 ? ""
							: String.format("%2d/%-2d",
									branchCounter.getCoveredCount(),
									branchCounter.getTotalCount()),
					instructionCounter.getTotalCount() == 0 ? ""
							: String.format("%2d/%-2d",
									instructionCounter.getCoveredCount(),
									instructionCounter.getTotalCount()),
					lineNumber, lineText);
		}
		sourceBufferedReader.close();
	}

}

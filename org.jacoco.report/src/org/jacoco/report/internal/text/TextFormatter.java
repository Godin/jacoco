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
import org.jacoco.report.IMultiReportOutput;
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
	 *            TODO
	 * @return visitor to emit the report data to
	 */
	public IReportVisitor createVisitor(final IMultiReportOutput output) {
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
				writeBundleCoverage(output, bundle, locator);
			}

			public void visitEnd() throws IOException {
				output.close();
			}
		}
		return new Visitor();
	}

	private static void writeBundleCoverage(final IMultiReportOutput output,
			final IBundleCoverage bundleCoverage,
			final ISourceFileLocator sourceFileLocator) throws IOException {
		// output.printf(" B I%n");
		// writeCounters(output, bundleCoverage, "", bundleCoverage.getName());
		for (final IPackageCoverage packageCoverage : bundleCoverage
				.getPackages()) {
			for (final IClassCoverage classCoverage : packageCoverage
					.getClasses()) {
				// TODO
				if (sourceFileLocator.getSourceFile(
						classCoverage.getPackageName(),
						classCoverage.getSourceFileName()) == null) {
					// output.println(classCoverage.getSourceFileName());
					// output.println(
					// "NOT FOUND FOR class " + classCoverage.getName());
				}
			}
			writePackageCoverage(output, packageCoverage, sourceFileLocator);
		}
	}

	private static void writePackageCoverage(IMultiReportOutput output,
			final IPackageCoverage packageCoverage,
			final ISourceFileLocator sourceFileLocator) throws IOException {
		// writeCounters(output, packageCoverage, " ",
		// packageCoverage.getName());
		for (final IClassCoverage classCoverage : packageCoverage
				.getClasses()) {
			writeClassCoverage(classCoverage);
		}
		for (final ISourceFileCoverage sourceFileCoverage : packageCoverage
				.getSourceFiles()) {
			writeSourceFileCoverage(output, sourceFileCoverage,
					sourceFileLocator);
		}
	}

	private static void writeClassCoverage(final IClassCoverage classCoverage) {
		// TODO indent classes inside package
		// writeCounters(output, classCoverage, " ", classCoverage.getName());
		for (final IMethodCoverage methodCoverage : classCoverage
				.getMethods()) {
			writeMethodCoverage(methodCoverage);
		}
	}

	private static void writeMethodCoverage(
			final IMethodCoverage methodCoverage) {
		// TODO indent methods inside class
		// writeCounters(output, methodCoverage, " ",
		// methodCoverage.getName() + methodCoverage.getDesc());
	}

	private static void writeCounters(final PrintStream output,
			final ICoverageNode coverage, final String indent,
			final String description) {
		final ICounter branchCounter = coverage.getBranchCounter();
		final ICounter instructionCounter = coverage.getInstructionCounter();
		String lineText = indent + coverage.getElementType() + " "
				+ description;
		output.println(lineText);
		lineText = "";
		output.printf("%s B:%5s I:%5s %s%n", //
				indent,
				branchCounter.getTotalCount() == 0 ? ""
						: String.format("%2d/%-2d",
								branchCounter.getCoveredCount(),
								branchCounter.getTotalCount()),
				instructionCounter.getTotalCount() == 0 ? ""
						: String.format("%2d/%-2d",
								instructionCounter.getCoveredCount(),
								instructionCounter.getTotalCount()),
				lineText);
	}

	private static void writeSourceFileCoverage(
			IMultiReportOutput fileMultiReportOutput,
			final ISourceFileCoverage sourceFileCoverage,
			final ISourceFileLocator sourceFileLocator) throws IOException {
		final PrintStream output = new PrintStream(fileMultiReportOutput
				.createFile(sourceFileCoverage.getPackageName() + "/"
						+ sourceFileCoverage.getName() + ".txt"));
		// TODO indent source inside package?
		final String indent = "    ";
		// FIXME write total counters at the end
		// writeCounters(output, sourceFileCoverage, " ",
		// sourceFileCoverage.getPackageName() + "/"
		// + sourceFileCoverage.getName());
		final Reader sourceReader = sourceFileLocator.getSourceFile( //
				sourceFileCoverage.getPackageName(),
				sourceFileCoverage.getName());
		if (sourceReader != null) {
			// TODO message when source not found?
			final BufferedReader sourceBufferedReader = new BufferedReader(
					sourceReader);
			int lineNumber = 0;
			String lineText;
			while ((lineText = sourceBufferedReader.readLine()) != null) {
				lineNumber++;
				final ILine lineCoverage = sourceFileCoverage
						.getLine(lineNumber);
				final ICounter branchCounter = lineCoverage.getBranchCounter();
				final ICounter instructionCounter = lineCoverage
						.getInstructionCounter();
				output.printf("%5s %5s |%s%n", //
						branchCounter.getTotalCount() == 0 ? ""
								: String.format("%2d/%-2d",
										branchCounter.getCoveredCount(),
										branchCounter.getTotalCount()),
						instructionCounter.getTotalCount() == 0 ? ""
								: String.format("%2d/%-2d",
										instructionCounter.getCoveredCount(),
										instructionCounter.getTotalCount()),
						lineText);
			}
			sourceBufferedReader.close();
		}

		output.printf("Total:%n");
		output.printf("  instructions: %d/%d%n",
				sourceFileCoverage.getInstructionCounter().getCoveredCount(),
				sourceFileCoverage.getInstructionCounter().getTotalCount());
		output.printf("  branches: %d/%d%n",
				sourceFileCoverage.getBranchCounter().getCoveredCount(),
				sourceFileCoverage.getBranchCounter().getTotalCount());
		output.printf("  lines: %d/%d%n",
				sourceFileCoverage.getLineCounter().getCoveredCount(),
				sourceFileCoverage.getLineCounter().getTotalCount());

		output.close();
	}

}

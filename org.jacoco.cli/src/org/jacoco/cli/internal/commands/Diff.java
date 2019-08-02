/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.cli.internal.commands;

import org.jacoco.cli.internal.Command;
import org.jacoco.core.analysis.CounterComparator;
import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.analysis.NodeComparator;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.internal.analysis.BundleCoverageImpl;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The <code>diff</code> command.
 */
public class Diff extends Command {

	@Argument(usage = "baseline", metaVar = "<baselineXML>", required = true)
	File baselineFile;

	@Argument(usage = "current", metaVar = "<currentXML>", required = true, index = 1)
	File currentFile;

	@Option(name = "--sourcefiles", usage = "location of the source files", metaVar = "<path>")
	File src;

	@Option(name = "--html", usage = "output directory for the HTML report", metaVar = "<dir>")
	File html;

	public String description() {
		return "";
	}

	public int execute(PrintWriter out, PrintWriter err) throws Exception {
		// TODO close streams
		final IBundleCoverage coverage = diff( //
				new FileInputStream(baselineFile), //
				new FileInputStream(currentFile) //
		);

		IReportVisitor reportVisitor = new HTMLFormatter()
				.createVisitor(new FileMultiReportOutput(html));
		reportVisitor.visitInfo(Collections.<SessionInfo> emptyList(),
				Collections.<ExecutionData> emptyList());
		// TODO multiple source directories
		reportVisitor.visitBundle(coverage,
				new DirectorySourceFileLocator(src, "UTF-8", 4));
		reportVisitor.visitEnd();

		return 0;
	}

	private static IBundleCoverage diff(final InputStream base,
			final InputStream current)
			throws IOException, SAXException, ParserConfigurationException {

		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final SAXParser parser = factory.newSAXParser();

		final ReportHandler baseHandler = new ReportHandler();
		parser.parse(base, baseHandler);

		final ReportHandler currentHandler = new ReportHandler();
		parser.parse(current, currentHandler);

		return diff(baseHandler.bundleCoverage, currentHandler.bundleCoverage);
	}

	private static final NodeComparator COMPARATOR = CounterComparator.TOTALITEMS
		.on(ICoverageNode.CounterEntity.METHOD)
		.second(CounterComparator.MISSEDITEMS
			.on(ICoverageNode.CounterEntity.METHOD))
		.second(CounterComparator.TOTALITEMS
			.on(ICoverageNode.CounterEntity.INSTRUCTION))
		.second(CounterComparator.MISSEDITEMS
			.on(ICoverageNode.CounterEntity.INSTRUCTION))
		.second(CounterComparator.TOTALITEMS
			.on(ICoverageNode.CounterEntity.BRANCH))
		.second(CounterComparator.MISSEDITEMS
			.on(ICoverageNode.CounterEntity.BRANCH))
		.second(CounterComparator.TOTALITEMS
			.on(ICoverageNode.CounterEntity.LINE))
		.second(CounterComparator.MISSEDITEMS
			.on(ICoverageNode.CounterEntity.LINE));

	/**
	 * TODO document
	 */
	private static IBundleCoverage diff(final IBundleCoverage base,
			final IBundleCoverage current) {
		final Map<String, IClassCoverage> baseClasses = new HashMap<String, IClassCoverage>();
		final Map<String, ISourceFileCoverage> baseSources = new HashMap<String, ISourceFileCoverage>();
		extractFromBundle(base, baseClasses, baseSources);
		final Map<String, IClassCoverage> currentClasses = new HashMap<String, IClassCoverage>();
		final Map<String, ISourceFileCoverage> currentSources = new HashMap<String, ISourceFileCoverage>();
		extractFromBundle(current, currentClasses, currentSources);

		final Set<IClassCoverage> classes = new HashSet<IClassCoverage>();
		final Set<ISourceFileCoverage> sources = new HashSet<ISourceFileCoverage>();
		for (Map.Entry<String, IClassCoverage> entry : currentClasses
				.entrySet()) {
			final IClassCoverage currentClassCoverage = entry.getValue();
			final IClassCoverage baseClassCoverage = baseClasses
					.get(entry.getKey());

			final String sourceName = currentClassCoverage.getPackageName()
					+ "/" + currentClassCoverage.getSourceFileName();
			final ISourceFileCoverage currentSourceCoverage = currentSources
					.get(sourceName);
			final ISourceFileCoverage baseSourceCoverage = baseSources
					.get(sourceName);

			if (baseClassCoverage == null // new class
					|| baseSourceCoverage == null // new source
					|| COMPARATOR.compare(baseClassCoverage,
							currentClassCoverage) != 0 // modified class
					|| COMPARATOR.compare(baseSourceCoverage,
							currentSourceCoverage) != 0 // modified source
			) {
				classes.add(currentClassCoverage);
				sources.add(currentSourceCoverage);
			}
		}

		return new BundleCoverageImpl("diff report", classes, sources);
	}

	private static void extractFromBundle(final IBundleCoverage bundle,
			final Map<String, IClassCoverage> classes,
			final Map<String, ISourceFileCoverage> sources) {
		for (IPackageCoverage p : bundle.getPackages()) {
			for (IClassCoverage c : p.getClasses()) {
				classes.put(c.getPackageName() + "/" + c.getName(), c);
			}
			for (ISourceFileCoverage s : p.getSourceFiles()) {
				sources.put(s.getPackageName() + "/" + s.getName(), s);
			}
		}
	}

	private static class Data {
		final Map<String, IClassCoverage> classes = new HashMap<String, IClassCoverage>();
		final Map<String, ISourceFileCoverage> sources = new HashMap<String, ISourceFileCoverage>();
	}

	private static class ReportHandler extends DefaultHandler {
		private final Data data = new Data();

		private BundleCoverageImpl bundleCoverage;
		private ClassCoverageImpl classCoverage;
		private MethodCoverageImpl methodCoverage;

		private ICounter instructions;
		private ICounter branches;
		private ICounter lines;

		private String packageName;
		private SourceData sourceFileCoverage;

		@Override
		public void startElement(final String uri, final String localName,
				final String qName, final Attributes attributes) {
			if ("report".equals(qName)) {
				attributes.getValue("name");

			} else if ("group".equals(qName)) {
				attributes.getValue("name");

			} else if ("package".equals(qName)) {
				packageName = attributes.getValue("name");

			} else if ("class".equals(qName)) {
				classCoverage = new ClassData(attributes.getValue("name"));
				classCoverage.setSourceFileName(
						attributes.getValue("sourcefilename"));
				data.classes.put(classCoverage.getPackageName() + "/"
						+ classCoverage.getName(), classCoverage);

			} else if ("method".equals(qName)) {
				instructions = null;
				branches = null;
				lines = null;

				attributes.getValue("line"); // first line
				methodCoverage = new MethodCoverageImpl( //
						attributes.getValue("name"), //
						attributes.getValue("desc"), //
						null // signature not available in XML
				);

			} else if ("sourcefile".equals(qName)) {
				sourceFileCoverage = new SourceData( //
						attributes.getValue("name"), //
						packageName //
				);
				data.sources.put(
						sourceFileCoverage.getPackageName() + "/"
								+ sourceFileCoverage.getName(),
						sourceFileCoverage);

			} else if ("line".equals(qName)) {
				final int lineNumber = Integer
						.parseInt(attributes.getValue("nr"));
				instructions = CounterImpl.getInstance( //
						parseAsOptionalInt(attributes, "mi"), //
						parseAsOptionalInt(attributes, "ci") //
				);
				branches = CounterImpl.getInstance( //
						parseAsOptionalInt(attributes, "mb"), //
						parseAsOptionalInt(attributes, "cb") //
				);
				sourceFileCoverage.increment(instructions, branches,
						lineNumber);

			} else if ("counter".equals(qName)) {
				final ICounter counter = CounterImpl.getInstance(
						Integer.parseInt(attributes.getValue("missed")),
						Integer.parseInt(attributes.getValue("covered")));
				switch (ICoverageNode.CounterEntity
						.valueOf(attributes.getValue("type"))) {
				case INSTRUCTION:
					instructions = counter;
					break;
				case BRANCH:
					branches = counter;
					break;
				case LINE:
					lines = counter;
					break;
				}
			}
		}

		@Override
		public void endElement(final String uri, final String localName,
				final String qName) {
			if ("method".equals(qName)) {
				if (branches == null) {
					branches = CounterImpl.COUNTER_0_0;
				}
				methodCoverage
						.increment(new LineData(instructions, branches, lines));
				methodCoverage.incrementMethodCounter();

				classCoverage.addMethod(methodCoverage);

			} else if ("report".equals(qName)) {
				for (IClassCoverage classCoverage : data.classes.values()) {
					sourceFileCoverage = (SourceData) data.sources
							.get(classCoverage.getPackageName() + "/"
									+ classCoverage.getSourceFileName());
					sourceFileCoverage.inc(classCoverage);
				}

				bundleCoverage = new BundleCoverageImpl("bundle",
						data.classes.values(), data.sources.values());
			}
		}

		@Override
		public InputSource resolveEntity(final String publicId,
				final String systemId) {
			// TODO
			if (publicId.startsWith("-//JACOCO//DTD Report")) {
				return new InputSource(new StringReader(""));
			}
			return null;
		}

		private static int parseAsOptionalInt(final Attributes attributes,
				final String name) {
			final String value = attributes.getValue(name);
			if (value == null) {
				return 0;
			}
			return Integer.parseInt(value);
		}

	}

	static class LineData extends CoverageNodeImpl {
		LineData(ICounter instructionCounter, ICounter branchCounter,
				ICounter lineCounter) {
			super(ICoverageNode.ElementType.GROUP, "");
			this.instructionCounter = (CounterImpl) instructionCounter;
			this.lineCounter = (CounterImpl) lineCounter;
			this.branchCounter = (CounterImpl) branchCounter;
		}
	}

	static class SourceData extends SourceFileCoverageImpl {
		SourceData(String name, String packageName) {
			super(name, packageName);
		}

		void inc(IClassCoverage classCoverage) {
			this.complexityCounter = this.complexityCounter.increment(classCoverage.getComplexityCounter());
			this.methodCounter = this.methodCounter.increment(classCoverage.getMethodCounter());
			this.classCounter = this.classCounter.increment(classCoverage.getClassCounter());
		}
	}

	static class ClassData extends ClassCoverageImpl {
		ClassData(String name) {
			super(name, 0, false);
		}

		@Override
		public void addMethod(IMethodCoverage method) {
			super.addMethod(method);
			this.lineCounter = this.lineCounter.increment(method.getLineCounter());
		}
	}

}

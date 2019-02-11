/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial implementation
 *
 *******************************************************************************/
package org.jacoco.cli.internal.commands;

import org.jacoco.cli.internal.Command;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * The <code>diffreport</code> command.
 */
final class DiffReport extends Command {

	@Option(name = "--previous", usage = "JaCoCo XML report to read", required = true)
	File xml;

	@Argument(usage = "JaCoCo exec file to read", metaVar = "<execfile>")
	File execFile;

	@Option(name = "--classfiles", usage = "location of Java class files", metaVar = "<path>", required = true)
	File classFiles;

	@Option(name = "--sourcefiles", usage = "location of the source files", metaVar = "<path>")
	File srcRoot;

	@Option(name = "--html", usage = "output directory for the HTML report", metaVar = "<dir>")
	File html;

	public String description() {
		return "EXPERIMENTAL";
	}

	@Override
	public int execute(final PrintWriter out, final PrintWriter err)
			throws Exception {
		final ExecFileLoader execFileLoader = new ExecFileLoader();
		execFileLoader.load(execFile);

		final FilteringCoverageBuilder builder = new FilteringCoverageBuilder(
				read(new FileInputStream(xml)));
		final Analyzer analyzer = new Analyzer(
				execFileLoader.getExecutionDataStore(), builder);
		analyzer.analyzeAll(classFiles);

		IBundleCoverage bundleCoverage = builder.delegate
				.getBundle("JaCoCo Diff Coverage Report");

		final HTMLFormatter formatter = new HTMLFormatter();
		final IReportVisitor visitor = formatter
				.createVisitor(new FileMultiReportOutput(html));
		visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),
				execFileLoader.getExecutionDataStore().getContents());

		visitor.visitBundle(bundleCoverage,
				new DirectorySourceFileLocator(srcRoot, "utf-8", 4));
		visitor.visitEnd();

		return 0;
	}

	private static Map<String, ICounter> read(final InputStream in)
			throws Exception {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final ReportHandler handler = new ReportHandler();
		factory.newSAXParser().parse(in, handler);
		return handler.result;
	}

	private static class ReportHandler extends DefaultHandler {

		private String className;

		private boolean insideClass;

		private Map<String, ICounter> result = new HashMap<String, ICounter>();

		@Override
		public void startElement(final String uri, final String localName,
				final String qName, final Attributes attributes) {

			if ("class".equals(qName)) {
				className = attributes.getValue("name");
				insideClass = true;

			} else if ("method".equals(qName)) {
				insideClass = false;

			} else if (insideClass && "counter".equals(qName)) {
				String type = attributes.getValue("type");
				int missed = Integer.parseInt(attributes.getValue("missed"));
				int covered = Integer.parseInt(attributes.getValue("covered"));
				if ("INSTRUCTION".equals(type)) {
					ICounter counter = CounterImpl.getInstance(missed, covered);
					result.put(className, counter);
				}
			}

		}

		@Override
		public void endElement(final String uri, final String localName,
				final String qName) {
			if ("method".equals(qName)) {
				insideClass = true;
			}
		}
	}

	private static class FilteringCoverageBuilder implements ICoverageVisitor {

		private final Map<String, ICounter> previous;

		private final CoverageBuilder delegate = new CoverageBuilder();

		private FilteringCoverageBuilder(final Map<String, ICounter> previous) {
			this.previous = previous;
		}

		public void visitCoverage(IClassCoverage coverage) {
			ICounter c = previous.get(coverage.getName());
			if (c == null) {
				delegate.visitCoverage(coverage);
			} else if (!c.equals(coverage.getInstructionCounter())) {
				delegate.visitCoverage(coverage);
			}
		}

	}

}

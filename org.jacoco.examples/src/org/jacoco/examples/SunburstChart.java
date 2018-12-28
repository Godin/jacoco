/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.examples;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class SunburstChart {

	private static final int WIDTH = 30;
	private static final int INITIAL_WIDTH = 5;

	public static void main(final String[] args) throws Exception {
		draw(read(new FileInputStream("/tmp/jacoco_zero_savings/1.xml")),
				new FileOutputStream("/tmp/test.svg"));
	}

	private static void draw(final Node root, final OutputStream output)
			throws IOException {
		final Writer writer = new OutputStreamWriter(output, "UTF-8");
		writer.append(
				"<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"-100 -100 200 200\">");

		draw(writer, root.children, 0, 0, Math.PI * 2);
		writer.append("</svg>");
		writer.close();
	}

	private static void draw(final Writer out, final List<Node> nodes,
			final int depth, final double startAngle, final double endAngle)
			throws IOException {
		if (!nodes.iterator().hasNext()) {
			return;
		}

		final double arcLength = Math.abs(startAngle - endAngle);

		Collections.sort(nodes, new Comparator<Node>() {
			public int compare(final Node o1, final Node o2) {
				return o2.size - o1.size;
			}
		});

		double totalSize = 0;
		for (Node node : nodes) {
			totalSize += node.size;
		}

		double nodeStartAngle = startAngle;
		for (Node node : nodes) {

			final double nodeEndAngle = nodeStartAngle
					+ arcLength * node.size / totalSize;
			final String color = color(node.size, totalSize);
			final String title = node.name + " " + node.size;

			append(out, title, color, INITIAL_WIDTH + depth * WIDTH,
					nodeStartAngle, nodeEndAngle);
			draw(out, node.children, depth + 1, nodeStartAngle, nodeEndAngle);

			nodeStartAngle = nodeEndAngle;
		}
	}

	private static String color(final double missed, final double size) {
		final int p = (int) Math.round(missed / size * 100);
		final int r = (255 * (p)) / 100;
		final int g = (255 * (100 - p)) / 100;
		final int b = 0;
		// return String.format("#%02x%02x%02x", r, g, b);
		return "#D3D3D3";
	}

	private static void append(final Writer out, final String title,
			final String fillColor, final int radius, final double startAngle,
			final double endAngle) throws IOException {
		out.append("<path ");
		out.append("fill='").append(fillColor).append("' ");
		out.append("stroke='white' ");
		out.append("stroke-width='0.1' ");
		out.append("d='").append(pieSlice(radius, startAngle, endAngle))
				.append("' ");
		out.append(">");
		out.append("<title>").append(title).append("</title>");
		out.append("</path>");
	}

	private static String pieSlice(final int radius, final double startAngle,
			final double endAngle) {

		final Segment inner = arcSegment(radius, startAngle, endAngle, 1);
		final Segment outer = arcSegment(radius + WIDTH, endAngle, startAngle,
				0);

		return inner.d + "L" + outer.startX + " " + outer.startY + outer.d + " "
				+ " L" + inner.startX + " " + inner.startY;
	}

	private static Segment arcSegment(int radius, double startAngle,
			double endAngle, int forward) {

		Segment segment = new Segment();

		segment.startX = radius * Math.cos(startAngle);
		segment.startY = radius * Math.sin(startAngle);

		segment.endX = radius * Math.cos(endAngle);
		segment.endY = radius * Math.sin(endAngle);

		double da = Math.abs(startAngle - endAngle);

		segment.d = "M " + segment.startX + " " + segment.startY + // move to
				" A " + radius + " " + radius + " 0 " + (da > Math.PI ? 1 : 0)
				+ " " + forward + " " + segment.endX + " " + segment.endY;

		return segment;
	}

	static class Segment {
		double startX;
		double startY;
		double endX;
		double endY;
		String d;
	}

	/**
	 * Reads JaCoCo XML report.
	 *
	 * @param in
	 *            stream to read XML report from
	 * @return parsed report
	 * @throws Exception
	 *             in case of errors
	 */
	private static Node read(final InputStream in) throws Exception {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final SAXParser parser = factory.newSAXParser();
		final ReportHandler handler = new ReportHandler();
		parser.parse(in, handler);
		return handler.root;
	}

	static class Node {

		final String name;
		final List<Node> children = new ArrayList<Node>();

		int size;

		Node(final String name) {
			this.name = name;
		}

		void addChild(final Node node) {
			children.add(node);
			size += node.size;
		}
	}

	private static class ReportHandler extends DefaultHandler {
		Node root = new Node(null);

		private Node grp;
		private Node pkg;
		private Node cls;

		@Override
		public void startElement(final String uri, final String localName,
				final String qName, final Attributes attributes) {
			if ("group".equals(qName)) {
				final String name = attributes.getValue("name");
				grp = new Node(name);

			} else if ("package".equals(qName)) {
				final String name = attributes.getValue("name");
				pkg = new Node(name);

			} else if ("class".equals(qName)) {
				final String name = attributes.getValue("name");
				cls = new Node(name);

			} else if ("counter".equals(qName) && cls != null) {
				final String type = attributes.getValue("type");
				final int missed = Integer
						.parseInt(attributes.getValue("missed"));
				if ("INSTRUCTION".equals(type) && missed != 0) {
					cls.size = missed;
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
			if ("group".equals(qName)) {
				root.addChild(grp);
				grp = null;
			} else if ("package".equals(qName)) {
				grp.addChild(pkg);
				pkg = null;
			} else if ("class".equals(qName)) {
				pkg.addChild(cls);
				cls = null;
			}
		}

		@Override
		public InputSource resolveEntity(final String publicId,
				final String systemId) {
			if (publicId.startsWith("-//JACOCO//DTD Report")) {
				return new InputSource(new StringReader(""));
			}
			return null;
		}
	}

}

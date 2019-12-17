package org.jacoco.core;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Gen {

	enum E {
		C0,
		C1,
		;

		public static void main(String[] args) {
			System.out.println(C0);
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		if (false) { // ecj 3.19
			PrintWriter out = new PrintWriter("/tmp/E.java");
			out.println("enum E {");
			for (int i = 0; i < 8205; i++) {
				out.println("C" + i + ",");
			}
			out.println(";");
			out.println("public static void main(String[] args) { System.out.println(C0); }");
			out.println("}");
			out.close();
		}
		if (true) { // kotlin 1.3.61
			PrintWriter out = new PrintWriter("/tmp/E.kt");
			out.println("enum class E {");
			for (int i = 0; i < 2990; i++) {
				out.println("C" + i + ",");
			}
			out.println("}");
			out.close();
		}
		if (false) { // groovy 2.5.8
			PrintWriter out = new PrintWriter("/tmp/E.groovy");
			out.println("enum E {");
			for (int i = 0; i < 1160; i++) {
				out.println("C" + i + ",");
			}
			out.println("}");
			out.close();
		}
	}

}

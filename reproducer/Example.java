public class Example {

	private record Point(Object component) {
	}

	private record R() {
	}

	private static void switchStatement(Object p) {
		switch (p) { // assertFullyCovered(0, 2)
		case Point(Object component) -> System.out.println("Point"); // assertSwitchStatementLastCase()
		default -> System.out.println("default"); // assertFullyCovered()
		} // assertEmpty()
	}

	private static void example(Object p) {
		switch(p) {
		case R() -> System.out.println("R");
		default -> System.out.println("default");
		}
	}

}

class Main {
    public static void main(String[] args) {
        Unmodified.sayHello();
        to_be_removed();
    }

    private static void to_be_removed() {
        new V1();
    }
}

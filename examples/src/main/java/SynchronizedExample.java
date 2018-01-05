class SynchronizedExample {

  private static final Object lock = new Object();

  public static void main(String[] args) {
    synchronized (lock) {
      System.out.println("synchronized");
    }
  }

}

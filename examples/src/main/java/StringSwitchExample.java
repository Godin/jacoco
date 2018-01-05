class StringSwitchExample {

  public static void main(String[] args) {
    System.out.println(typeOf("Monday"));
    System.out.println(typeOf("Sunday"));
  }

  private static String typeOf(String day) {
    switch (day) {
      case "Monday":
      case "Tuesday":
      case "Wednesday":
      case "Thursday":
      case "Friday":
        return "workday";
      case "Saturday":
      case "Sunday":
      default:
        return "weekend";
    }
  }

}

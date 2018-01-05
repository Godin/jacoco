import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

class TryWithResourcesExample {

  public static void main(String[] args) throws IOException {
    try (
      BufferedReader br = new BufferedReader(new FileReader("/dev/null"))
    ) {
      System.out.println(br.readLine());
    }
  }

}

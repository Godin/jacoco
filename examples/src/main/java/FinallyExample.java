import java.io.FileInputStream;
import java.io.IOException;

class FinallyExample {

  public static void main(String[] args) throws IOException {
    fun("/dev/null");
    fun("/");
  }

  public static void fun(String file) throws IOException {
    FileInputStream in = null;
    try {
      in = new FileInputStream(file);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (in != null) {
        in.close();
      }
    }
  }

}

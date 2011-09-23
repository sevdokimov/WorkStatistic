import java.io.File;

/**
 * @author Sergey Evdokimov
 */
public class Main {



  private void load(File file) {

  }

  public static void main(String[] args) {
    Main main = new Main();

    String logPath = System.getenv("WORK_STATISTIC_HOME");
    if (logPath == null) {
      logPath = System.getProperty("user.home") + "/workStatistic";
    }

    File dir = new File(logPath);
    if (!dir.isDirectory()) {
      throw new RuntimeException("Log dir donesn't a directory: " + logPath);
    }



  }

}

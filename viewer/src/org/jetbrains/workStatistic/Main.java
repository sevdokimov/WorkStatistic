package org.jetbrains.workStatistic;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sergey Evdokimov
 */
public class Main {

  private static final int SESSION_EXPIRED_TIME = 10 * 1000;

  private final List<Period> data = new ArrayList<Period>();

  private static final Pattern PATTERN = Pattern.compile("(\\S+ \\S+) (?:Startup|Shutdown|(?:work (\\d+\\.\\d+)))");

  private void load(File dir) throws IOException, ParseException {
    for (File file : dir.listFiles()) {
      if (!file.isFile() || !file.getName().endsWith(".log")) continue;

      Scanner sc = new Scanner(file);
      try {
        while (sc.hasNextLine()) {
          String s = sc.nextLine();
          Matcher matcher = PATTERN.matcher(s);

          if (!matcher.matches()) {
            throw new RuntimeException(s);
          }

          String sDuration = matcher.group(2);
          if (sDuration == null) continue;

          NumberFormat instance = NumberFormat.getInstance(Locale.ENGLISH);

          Period p = new Period(Period.FORMAT.parse(matcher.group(1)).getTime(), (long)(instance.parse(sDuration).doubleValue() * 1000));
          data.add(p);
        }
      }
      finally {
        sc.close();
      }
    }

    PeriodUtils.sortAndRemoveDuplicates(data, SESSION_EXPIRED_TIME);
  }


  private void printLines() {
    for (Period period : data) {
      System.out.println(period);
    }
  }

  private void printTotalTimePerDay() {

  }

  public static void main(String[] args) throws IOException, ParseException {
    String logPath = System.getenv("WORK_STATISTIC_HOME");
    if (logPath == null) {
      logPath = System.getProperty("user.home") + "/workStatistic";
    }

    File dir = new File(logPath);
    if (!dir.isDirectory()) {
      throw new RuntimeException("Log dir donesn't a directory: " + logPath);
    }

    Main main = new Main();
    main.load(dir);

    PeriodUtils.printStatistic(main.data, new PerHourClassifier());
  }

}

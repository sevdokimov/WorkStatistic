package org.jetbrains.workStatistic;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sergey Evdokimov
 */
public class Main {

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

    PeriodUtils.sortAndRemoveDuplicates(data, 2*60000);
  }


  private void printLines() {
    for (Period period : data) {
      System.out.println(period);
      System.out.println(Period.FORMAT.format(new Date(period.getEnd())));
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
    System.out.println();
    System.out.println("Total: " + PeriodUtils.toTime(PeriodUtils.sum(main.data)));
    System.out.println();
    main.printLines();
  }

}

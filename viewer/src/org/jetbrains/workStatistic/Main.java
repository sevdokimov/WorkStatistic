package org.jetbrains.workStatistic;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sergey Evdokimov
 */
public class Main {

  private final List<Period> data = new ArrayList<Period>();

  private static final Pattern PATTERN = Pattern.compile("(\\S+ \\S+) (?:Startup|Shutdown|(?:work (\\d+\\.\\d+)))");

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");
  private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH-mm-ss");

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

    PeriodUtils.sortAndRemoveDuplicates(data, 60000);
  }


  private void printLines() {
    for (int i = 0; i < data.size(); i++) {
      Period period = data.get(i);
      
      System.out.println(Period.FORMAT.format(new Date(period.getStart())) + " + " + PeriodUtils.toTime(period.getDuration()) );
      if (i + 1 < data.size()) {
        System.out.println(Period.FORMAT.format(new Date(period.getEnd())) + " - " + PeriodUtils.toTime(data.get(i + 1).getStart() - period.getEnd()));
      }
    }
  }

  private void printStartEnd() {
    Map<String, List<Period>> map = PeriodUtils.splitByDay(data);

    for (Map.Entry<String, List<Period>> entry : map.entrySet()) {
      List<Period> list = entry.getValue();

      Date startDate = new Date(list.get(0).getStart());
      
      String date = DATE_FORMAT.format(startDate);
      System.out.print(date + "   ");

      System.out.print(TIME_FORMAT.format(startDate));
      
      Date endDate = new Date(list.get(list.size() - 1).getEnd());

      System.out.print(" - ");
      System.out.println(TIME_FORMAT.format(endDate));
    }
    
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

    PeriodUtils.printStatistic(main.data, new PerDayClassifier());
    System.out.println();
    System.out.println("Total: " + PeriodUtils.toTime(PeriodUtils.sum(main.data)));
    System.out.println("Agv: " + PeriodUtils.toTime(PeriodUtils.getAgv(main.data, new PerDayClassifier())));
    System.out.println();
    main.printStartEnd();
  }

}

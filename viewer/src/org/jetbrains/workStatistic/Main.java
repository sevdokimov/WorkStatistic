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

import static org.jetbrains.workStatistic.PeriodUtils.*;

/**
 * @author Sergey Evdokimov
 */
public class Main {

  private final List<Period> workPeriods = new ArrayList<Period>();
  
  private final List<Period> openedIdeaPeriods = new ArrayList<Period>();

  private static final Pattern PATTERN = Pattern.compile("(\\S+ \\S+) (?:(Startup)|(Shutdown)|(?:work (\\d+\\.\\d+)))");

  private void loadSingleFile(File file) throws IOException, ParseException {
    Date openTime = null;

    Scanner sc = new Scanner(file);
    try {
      while (sc.hasNextLine()) {
        String s = sc.nextLine();
        Matcher matcher = PATTERN.matcher(s);

        if (!matcher.matches()) {
          throw new RuntimeException(s);
        }

        String sDuration = matcher.group(4);
        if (sDuration != null) {
          assert openTime != null;

          NumberFormat instance = NumberFormat.getInstance(Locale.ENGLISH);

          Period p = new Period(Period.FORMAT.parse(matcher.group(1)).getTime(), (long)(instance.parse(sDuration).doubleValue() * 1000));
          workPeriods.add(p);
        }
        else if (matcher.group(2) != null) {
          assert openTime == null;
          openTime = Period.FORMAT.parse(matcher.group(1));
        }
        else {
          assert matcher.group(3) != null;
          assert openTime != null;

          long closeTime = Period.FORMAT.parse(matcher.group(1)).getTime();
          openedIdeaPeriods.add(new Period(openTime.getTime(), closeTime - openTime.getTime()));
          openTime = null;
        }
      }
      if (openTime != null) {
        if (workPeriods.size() > 0) {
          long closeTime = workPeriods.get(workPeriods.size() - 1).getEnd();
          if (closeTime > openTime.getTime()) {
            openedIdeaPeriods.add(new Period(openTime.getTime(), closeTime - openTime.getTime()));
          }
        }
      }
    }
    finally {
      sc.close();
    }
  }

  private void load(File dir) throws IOException, ParseException {
    for (File file : dir.listFiles()) {
      if (!file.isFile() || !file.getName().endsWith(".log")) continue;
      loadSingleFile(file);
    }

    sortAndRemoveDuplicates(workPeriods, 60000);
    sortAndRemoveDuplicates(openedIdeaPeriods, 1);
  }


  private void printLines() {
    for (int i = 0; i < workPeriods.size(); i++) {
      Period period = workPeriods.get(i);
      
      System.out.println(Period.FORMAT.format(new Date(period.getStart())) + " + " + toTime(period.getDuration()) );
      if (i + 1 < workPeriods.size()) {
        System.out.println(Period.FORMAT.format(new Date(period.getEnd())) + " - " + toTime(workPeriods.get(i + 1).getStart() - period.getEnd()));
      }
    }
  }

  private void printStat() {
    printStatistic(workPeriods, openedIdeaPeriods);
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
    main.printStat();
    
    printStatistic(main.workPeriods, new PerWeakDayClassifier());
  }

}

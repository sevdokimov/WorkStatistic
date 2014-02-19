package org.jetbrains.workStatistic;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jetbrains.workStatistic.PeriodUtils.*;

/**
 * @author Sergey Evdokimov
 */
public class Main {

  private final List<Period> workPeriods = new ArrayList<Period>();
  
  private static final Pattern PATTERN = Pattern.compile("(\\S+ \\S+) (?:(Startup)|(Shutdown)|(?:work (\\d+\\.\\d+)))");

  private void loadSingleFile(File file) throws IOException, ParseException {
    System.out.println("reading: " + file);
    Period lastPeriod = null;

    int lineNumber = 0;

    Scanner sc = new Scanner(file);
    try {
      while (sc.hasNextLine()) {
        lineNumber++;

        String s = sc.nextLine();
        Matcher matcher = PATTERN.matcher(s);

        if (!matcher.matches()) {
          throw new RuntimeException(s);
        }

        String sDuration = matcher.group(4);
        if (sDuration != null) {
          NumberFormat instance = NumberFormat.getInstance(Locale.ENGLISH);

          lastPeriod = new Period(Period.FORMAT.parse(matcher.group(1)).getTime(), (long)(instance.parse(sDuration).doubleValue() * 1000));
          workPeriods.add(lastPeriod);
        }
        else if (matcher.group(2) != null) {
          // IDEA was open
        }
        else {
          // IDEA was closed
        }
      }
    }
    finally {
      sc.close();
    }
  }

  private void load(File dir) throws IOException, ParseException {
    System.out.println("Reading data...");
    File[] files = dir.listFiles();
    Arrays.sort(files);

    for (File file : files) {
      if (!file.isFile() || !file.getName().endsWith(".log")) continue;
      loadSingleFile(file);
    }

    System.out.println("Sorting...");

    sortAndRemoveDuplicates(workPeriods, 60000);
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
    printStatistic(workPeriods);
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
    
//    printStatistic(new PerWeakDayGrooper(), main.workPeriods, main.openedIdeaPeriods);
  }

}

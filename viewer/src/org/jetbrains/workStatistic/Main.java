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

  private static final int SESSION_EXPIRED_TIME = 10 * 1000;

  private static final Pattern PATTERN = Pattern.compile("(\\S+ \\S+) (?:Startup|Shutdown|(?:work (\\d+\\.\\d+)))");

  private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss(SSS)");

  private List<Period> data = new ArrayList<Period>();

  private void addPeriod(Period p) {
    int res = Collections.binarySearch(data, p, new Comparator<Period>() {
      public int compare(Period o1, Period o2) {
        return (int)(o1.getStart() - o2.getStart());
      }
    });

    if (res >= 0) {
      Period oldPeriod = data.get(res);
      assert oldPeriod.getStart() == p.getStart();
      if (p.getDuration() > oldPeriod.getDuration()) {
        oldPeriod.setDuration(p.getDuration());
      }
    }
    else {
      int position = (-res - 1);

      if (position > 0) {
        Period prev = data.get(position - 1);

        if (prev.getEnd() + SESSION_EXPIRED_TIME >= p.getStart()) {
          prev.extendTo(Math.max(p.getEnd(), prev.getEnd()));
          return;
        }
      }

      if (position < data.size()) {
        Period cur = data.get(position);

        if (p.getEnd() + SESSION_EXPIRED_TIME >= cur.getStart()) {
          long end = Math.max(cur.getEnd(), p.getEnd());
          cur.setStart(p.getStart());
          cur.extendTo(end);
          return;
        }
      }

      data.add(position, p);
    }
  }

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

          Period p = new Period(FORMAT.parse(matcher.group(1)).getTime(), (long)(instance.parse(sDuration).doubleValue() * 1000));
          addPeriod(p);
        }
      }
      finally {
        sc.close();
      }
    }
  }

  private void printLines() {
    for (Period period : data) {
      System.out.println(FORMAT.format(new Date(period.getStart())) + " " + period.getDuration());

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

    main.printLines();
  }

}

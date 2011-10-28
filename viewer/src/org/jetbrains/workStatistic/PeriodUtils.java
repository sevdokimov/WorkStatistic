package org.jetbrains.workStatistic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class PeriodUtils {

  private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH-mm-ss");
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");

  public static void sortAndRemoveDuplicates(List<Period> data, int t) {
    if (data.size() <= 1) return;

    Collections.sort(data);

    Iterator<Period> itr = data.iterator();
    Period prev = itr.next();

    for (; itr.hasNext(); ) {
      Period p = itr.next();
      if (prev.getEnd() + t < p.getStart()) {
        prev = p;
      }
      else {
        if (p.getEnd() > prev.getEnd()) {
          prev.setEnd(p.getEnd());
        }

        itr.remove();
      }
    }
  }

  private static <T> void inc(Map<T, Long> map, T key, long x) {
    Long oldValue = map.get(key);
    if (oldValue == null) {
      map.put(key, x);
    }
    else {
      map.put(key, oldValue + x);
    }
  }

  public static <T extends Comparable<T>> Map<T, List<Period>> splitByGrooper(Grooper<T> g, List<Period> data) {
    Map<T, List<Period>> res = new TreeMap<T, List<Period>>();

    for (Period p : data) {
      Grooper.Result<T> result = g.process(p.getStart());

      List<Period> list = res.get(result.id);
      if (list == null) {
        list = new ArrayList<Period>();
        res.put(result.id, list);
      }

      list.add(p);
    }

    return res;
  }
  
  public static <T extends Comparable<T>> Map<T, Long> sumByClassifier(List<Period> data, Grooper<T> grooper) {
    Map<T, Long> res = new HashMap<T, Long>();

    long totalSum = 0;

    for (Period p : data) {
      totalSum += p.getDuration();

      long x = p.getStart();

      while (x < p.getEnd()) {
        Grooper.Result<T> result = grooper.process(x);
        assert result.next > x;
        assert !result.id.equals(grooper.process(result.next));
        assert result.id.equals(grooper.process(result.next - 1).id);

        inc(res, result.id, Math.min(p.getEnd(), result.next) - x);

        x = result.next;
      }
    }

    for (Long t : res.values()) {
      totalSum -= t;
    }

    assert totalSum == 0;

    return res;
  }

  public static <T extends Comparable<T>> long getAgv(List<Period> data, Grooper<T> grooper) {
    Map<T, Long> map = sumByClassifier(data, grooper);
    return sum(data) / map.size();
  }

  public static <T extends Comparable<T>> void printStatistic(List<Period> data, Grooper<T> grooper) {
    Map<T, Long> map = sumByClassifier(data, grooper);

    Object[] keys = map.keySet().toArray(new Object[map.keySet().size()]);

    int maxKeySize = 0;
    for (Object key : keys) {
      maxKeySize = Math.max(maxKeySize, key.toString().length());
    }

    Arrays.sort(keys);

    for (Object key : keys) {
      StringBuilder sb = new StringBuilder();
      sb.append(key).append(": ");
      while (sb.length() < maxKeySize + 2) {
        sb.append(' ');
      }

      //noinspection SuspiciousMethodCalls
      sb.append(toTime(map.get(key)));

      System.out.println(sb);
    }
  }

  private static void append2Digital(StringBuilder sb, int x) {
    if (x < 10) {
      sb.append("0");
    }

    sb.append(x);
  }

  public static long sum(List<Period> data) {
    long res = 0;

    for (Period period : data) {
      res += period.getDuration();
    }

    return res;
  }

  public static String toTime(long period) {
    long sec = period / 1000;

    StringBuilder sb = new StringBuilder();

    append2Digital(sb, (int)(sec / (60*60)));
    sb.append(':');
    append2Digital(sb, (int)((sec / 60) % 60));
    sb.append(':');
    append2Digital(sb, (int) (sec % 60));

    return sb.toString();
  }

  public static int getWeakNumber(long date) {
    Calendar c = GregorianCalendar.getInstance();
    c.setTimeInMillis(date);
    return c.get(Calendar.WEEK_OF_YEAR);
  }

  public static <T extends Comparable<T>> void printStatistic(Grooper<T> grooper, List<Period> workPeriods, List<Period> openedIdeaPeriods) {
    Map<T, List<Period>> workMap = splitByGrooper(grooper, workPeriods);
    Map<T, List<Period>> openMap = splitByGrooper(grooper, openedIdeaPeriods);

    assert workMap.keySet().equals(openMap.keySet());

    for (T key : workMap.keySet()) {
      System.out.println(key + ":");
      printStatistic(workMap.get(key), openMap.get(key));
      System.out.println();
    }
  }

  public static void printStatistic(List<Period> workPeriods, List<Period> openedIdeaPeriods) {
    Map<String, List<Period>> workMap = splitByGrooper(PerDayGrooper.INSTANCE, workPeriods);
    Map<String, List<Period>> openMap = splitByGrooper(PerDayGrooper.INSTANCE, openedIdeaPeriods);

    assert workMap.keySet().equals(openMap.keySet());

    int weakNumber = -1;

    long totalWorkTime = 0;
    long totalOpenIdeaTime = 0;
    double totalEff = 0;

    for (String day : workMap.keySet()) {
      long work = sum(workMap.get(day));

      totalWorkTime += work;

      List<Period> openList = openMap.get(day);

      long open = sum(openList);
      totalOpenIdeaTime += open;

      int cw = getWeakNumber(openList.get(0).getStart());
      if (weakNumber != -1 && cw != weakNumber) {
        System.out.println();
      }
      weakNumber = cw;

      System.out.append(day).append("   ")
          .append(toTime(work))
          .append(", ")
          .append(toTime(open));

      double efficiency = (double) work / open;
      totalEff += efficiency;

      System.out.printf(", %.2f, ", efficiency);

      System.out.printf(" (%s - %s)\n", TIME_FORMAT.format(openList.get(0).getStart()), TIME_FORMAT.format(openList.get(openList.size() - 1).getEnd()));
    }

    int count = workMap.size();

    System.out.printf("\nTotal: %s, %s, %.2f\n", toTime(totalWorkTime / count), toTime(totalOpenIdeaTime / count), (totalEff / count));
  }
}

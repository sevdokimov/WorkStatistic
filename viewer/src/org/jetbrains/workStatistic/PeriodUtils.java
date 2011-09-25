package org.jetbrains.workStatistic;

import java.util.*;

public class PeriodUtils {

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

  public static <T extends Comparable<T>> Map<T, Long> sumByClassifier(List<Period> data, Classifier<T> classifier) {
    Map<T, Long> res = new HashMap<T, Long>();

    long totalSum = 0;

    for (Period p : data) {
      totalSum += p.getDuration();

      long x = p.getStart();

      while (x < p.getEnd()) {
        Classifier.Result<T> result = classifier.process(x);
        assert result.next > x;
        assert !result.id.equals(classifier.process(result.next));
        assert result.id.equals(classifier.process(result.next - 1).id);

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

  public static <T extends Comparable<T>> void printStatistic(List<Period> data, Classifier<T> classifier) {
    Map<T, Long> map = sumByClassifier(data, classifier);

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

}

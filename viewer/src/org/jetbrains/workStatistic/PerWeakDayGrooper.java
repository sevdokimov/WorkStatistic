package org.jetbrains.workStatistic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PerWeakDayGrooper extends Grooper<PerWeakDayGrooper.E> {

  private static final E[] RESULTS = {new E("Sun", 1), new E("Mon", 2), new E("Tue", 3), new E("Wed", 4), new E("Thu", 5), new E("Fri", 6), new E("Sat", 7)};
  
  @Override
  public Result<E> process(long date) {
    Calendar c = Calendar.getInstance();

    c.setTimeInMillis(date);

    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);

    return new Result<E>(RESULTS[c.get(Calendar.DAY_OF_WEEK) - 1], c.getTimeInMillis() + 24*60*60*1000);
  }
  
  public static class E implements Comparable<E> {
    private final String value;
    private final int index;

    public E(String value, int index) {
      this.value = value;
      this.index = index;
    }

    public int compareTo(E o) {
      return index - o.index;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;

      return index == ((E) o).index;
    }

    @Override
    public int hashCode() {
      return index;
    }

    @Override
    public String toString() {
      return value;
    }
  }
}

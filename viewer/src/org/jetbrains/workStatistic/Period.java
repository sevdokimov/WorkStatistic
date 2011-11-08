package org.jetbrains.workStatistic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Period implements Comparable<Period> {

  public static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss(SSS)");

  private long start;

  private long duration;

  public Period(long start, long duration) {
    assert duration >= 0;
    this.start = start;
    this.duration = duration;
  }

  public long getStart() {
    return start;
  }

  public void setStart(long start) {
    assert start > 0;
    this.start = start;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    assert duration > 0;
    this.duration = duration;
  }

  public long getEnd() {
    return start + duration;
  }

  public void setEnd(long endTime) {
    setDuration(endTime - start);
  }

  @Override
  public String toString() {
    return FORMAT.format(new Date(start)) + " " + PeriodUtils.toTime(duration);
  }

  public int compareTo(Period o) {
    if (start > o.getStart()) {
      return 1;
    }

    if (start < o.getStart()) {
      return -1;
    }
    return 0;
  }
}

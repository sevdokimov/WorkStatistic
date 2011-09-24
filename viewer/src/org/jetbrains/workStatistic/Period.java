package org.jetbrains.workStatistic;

public class Period {

  private long start;

  private long duration;

  public Period(long start, long duration) {
    this.start = start;
    this.duration = duration;
  }

  public long getStart() {
    return start;
  }

  public void setStart(long start) {
    this.start = start;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public long getEnd() {
    return start + duration;
  }

  public void extendTo(long endTime) {
    assert endTime > start;
    duration = endTime - start;
  }
}

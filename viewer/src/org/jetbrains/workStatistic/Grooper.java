package org.jetbrains.workStatistic;

public abstract class Grooper<T extends Comparable<T>> {

  public abstract Result<T> process(long date);

  public static class Result<T> {
    public final T id;
    public final long next;

    public Result(T id, long next) {
      this.id = id;
      this.next = next;
    }
  }
}

package org.jetbrains.workStatistic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PerDayClassifier extends Classifier<String> {

  public static final PerDayClassifier INSTANCE = new PerDayClassifier();

  private static final DateFormat FORMAT = new SimpleDateFormat("yyyy.MM.dd (EEE)");

  @Override
  public Result<String> process(long date) {
    Calendar c = Calendar.getInstance();

    c.setTimeInMillis(date);

    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);

    return new Result<String>(FORMAT.format(new Date(date)), c.getTimeInMillis() + 24*60*60*1000);
  }
}

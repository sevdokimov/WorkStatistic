package org.jetbrains.workStatistic;

import java.util.Calendar;

public class PerHourClassifier extends Classifier<String> {

  @Override
  public Result<String> process(long date) {
    Calendar c = Calendar.getInstance();

    c.setTimeInMillis(date);

    String res = c.get(Calendar.HOUR_OF_DAY) + ":00";

    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);

    return new Result<String>(res, c.getTimeInMillis() + 60*60*1000);
  }
}

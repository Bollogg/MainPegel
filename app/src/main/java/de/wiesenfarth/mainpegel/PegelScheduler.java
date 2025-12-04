package de.wiesenfarth.mainpegel;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.util.Log;

public class PegelScheduler {

  public static void schedule(Context context) {
    long trigger = nextQuarterHour();
    setAlarm(context, trigger);
  }

  public static void scheduleNext(Context context) {
    long trigger = nextQuarterHour();
    setAlarm(context, trigger);
  }

  private static void setAlarm(Context context, long triggerAt) {

    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    Intent intent = new Intent(context, AlarmReceiver.class);
    // Wird im Prg. AlarmReceiver abgefragt "RUN_FGS"
    intent.setAction("de.wiesenfarth.mainpegel.RUN_FGS");

    PendingIntent pi = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
    );

    am.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        triggerAt,
        pi
    );

    Log.i("PEGEL_SCHED", "⏰ Alarm gesetzt für: " + triggerAt);
  }

  /**
   * Nächste exakte Minute: 01, 16, 31, 46
   */
  private static long nextQuarterHour() {

    Calendar c = Calendar.getInstance();
    int minute = c.get(Calendar.MINUTE);
    int second = c.get(Calendar.SECOND);

    int[] marks = {1, 16, 31, 46};
    int next = -1;

    for (int t : marks) {
      if (minute < t || (minute == t && second == 0)) {
        next = t;
        break;
      }
    }

    // Falls vorbei → nächste Stunde Minute 1
    if (next == -1) {
      c.add(Calendar.HOUR_OF_DAY, 1);
      next = 1;
    }

    c.set(Calendar.MINUTE, next);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);

    long res = c.getTimeInMillis();
    Log.i("PEGEL_SCHED", "nextQuarterHour() → " + res);
    return res;
  }
}

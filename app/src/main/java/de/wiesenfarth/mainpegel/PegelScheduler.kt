package de.wiesenfarth.mainpegel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log

object PegelScheduler {
    fun schedule(context: Context) {
        val trigger = nextQuarterHour()
        setAlarm(context, trigger)
    }

    fun scheduleNext(context: Context) {
        val trigger = nextQuarterHour()
        setAlarm(context, trigger)
    }

    private fun setAlarm(context: Context, triggerAt: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)
        // Wird im Prg. AlarmReceiver abgefragt "RUN_FGS"
        intent.setAction("de.wiesenfarth.mainpegel.RUN_FGS")

        val pi = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pi
        )

        Log.i("PEGEL_SCHED", "⏰ Alarm gesetzt für: " + triggerAt)
    }

    /**
     * Nächste exakte Minute: 01, 16, 31, 46
     */
    private fun nextQuarterHour(): Long {
        val c = Calendar.getInstance()
        val minute = c.get(Calendar.MINUTE)
        val second = c.get(Calendar.SECOND)

        val marks = intArrayOf(1, 16, 31, 46)
        var next = -1

        for (t in marks) {
            if (minute < t || (minute == t && second == 0)) {
                next = t
                break
            }
        }

        // Falls vorbei → nächste Stunde Minute 1
        if (next == -1) {
            c.add(Calendar.HOUR_OF_DAY, 1)
            next = 1
        }

        c.set(Calendar.MINUTE, next)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)

        val res = c.getTimeInMillis()
        Log.i("PEGEL_SCHED", "nextQuarterHour() → " + res)
        return res
    }
}

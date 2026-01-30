package de.wiesenfarth.mainpegel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log

/*******************************************************
 * Objekt:     PegelScheduler
 *
 * Beschreibung:
 * Zentrale Klasse zur zeitgesteuerten Ausführung der
 * Pegelabfrage.
 *
 * Der Scheduler setzt einen exakten Alarm über den
 * AlarmManager, der den AlarmReceiver auslöst.
 * Dieser startet anschließend die Pegel-Logik
 * (Foreground Service / PegelLogic).
 *
 * Besonderheiten:
 * - Verwendung von setExactAndAllowWhileIdle(), damit
 *   der Alarm auch im Doze-Mode zuverlässig ausgelöst wird
 * - Ausführung zu festen Minuten:
 *   01, 16, 31, 46 (viertelstündlich versetzt)
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-20
 *******************************************************/
object PegelScheduler {

    /**
     * Plant den ersten Alarm.
     *
     * Wird typischerweise beim Start der App oder nach
     * einer Konfigurationsänderung aufgerufen.
     *
     * @param context Gültiger Application- oder Activity-Context
     */
    @JvmStatic
    fun schedule(context: Context) {
        val trigger = nextQuarterHour()
        setAlarm(context, trigger)
    }

    /**
     * Plant den nächsten Alarm nach einer erfolgreichen
     * Ausführung.
     *
     * Wird z. B. vom AlarmReceiver aufgerufen, nachdem
     * ein Alarm ausgelöst wurde.
     *
     * @param context Gültiger Application- oder Service-Context
     */
    fun scheduleNext(context: Context) {
        val trigger = nextQuarterHour()
        setAlarm(context, trigger)
    }

    /**
     * Setzt einen exakten Alarm beim AlarmManager.
     *
     * @param context   Context zum Zugriff auf Systemdienste
     * @param triggerAt Zeitpunkt (Millis seit Epoch),
     *                  zu dem der Alarm ausgelöst wird
     */
    private fun setAlarm(context: Context, triggerAt: Long) {

        // AlarmManager vom System holen
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Intent für den BroadcastReceiver
        val intent = Intent(context, AlarmReceiver::class.java)

        // Eindeutige Action, damit der Receiver nur
        // unsere eigenen Alarme verarbeitet
        intent.setAction("de.wiesenfarth.mainpegel.RUN_FGS")

        // PendingIntent, das vom AlarmManager ausgelöst wird
        val pi = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        // Exakten Alarm setzen, auch im Doze-Mode
        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pi
        )

        Log.i("PEGEL_SCHED", "⏰ Alarm gesetzt für: $triggerAt")
    }

    /**
     * Berechnet den nächsten festen Ausführungszeitpunkt.
     *
     * Zielzeiten (jede Stunde):
     *   Minute 01
     *   Minute 16
     *   Minute 31
     *   Minute 46
     *
     * Falls alle Zeitpunkte der aktuellen Stunde bereits
     * vorbei sind, wird Minute 01 der nächsten Stunde
     * gewählt.
     *
     * @return Zeitpunkt in Millisekunden seit Epoch
     */
    private fun nextQuarterHour(): Long {

        // Aktuelle Uhrzeit
        val c = Calendar.getInstance()
        val minute = c.get(Calendar.MINUTE)
        val second = c.get(Calendar.SECOND)

        // Feste Zielminuten
        val marks = intArrayOf(1, 16, 31, 46)
        var next = -1

        // Nächsten passenden Zeitpunkt suchen
        for (t in marks) {
            if (minute < t || (minute == t && second == 0)) {
                next = t
                break
            }
        }

        // Falls alle Marken der Stunde überschritten sind
        if (next == -1) {
            c.add(Calendar.HOUR_OF_DAY, 1)
            next = 1
        }

        // Zeitpunkt exakt setzen (Sekunden/Millisekunden = 0)
        c.set(Calendar.MINUTE, next)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)

        val result = c.timeInMillis
        Log.i("PEGEL_SCHED", "nextQuarterHour() → $result")

        return result
    }
}

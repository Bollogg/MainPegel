package de.net.wiesenfarth.mainpegel.AlarmManager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

/*******************************************************
 * Objekt: PegelScheduler
 *
 * Beschreibung:
 * -----------------------------------------------------
 * Zentrale Klasse zur Planung der automatischen
 * Pegelabfragen.
 *
 * Der Scheduler setzt einen Alarm über den Android
 * AlarmManager. Sobald dieser Alarm ausgelöst wird,
 * startet der AlarmReceiver, welcher wiederum den
 * PegelForegroundService startet.
 *
 * Architekturfluss:
 * -----------------------------------------------------
 *
 * PegelScheduler
 *      ↓
 * AlarmManager
 *      ↓
 * AlarmReceiver
 *      ↓
 * PegelForegroundService
 *      ↓
 * PegelLogic.run()
 *      ↓
 * API → Cache → Widget / UI
 *
 * Eigenschaften:
 * -----------------------------------------------------
 * • exakte Alarme (wenn erlaubt)
 * • funktioniert auch im Hintergrund
 * • überlebt App-Schließen
 * • Intervall konfigurierbar (SharedPreferences)
 *
 * Intervall-Einstellungen:
 * -----------------------------------------------------
 * Wird aus den App-Einstellungen geladen.
 *
 * Typische Werte:
 *  - 15 Minuten
 *  - 30 Minuten
 *  - 60 Minuten
 *
 * Besonderheit:
 * -----------------------------------------------------
 * Es wird immer ein +1 Minuten Puffer hinzugefügt.
 *
 * Grund:
 * Die Pegel-API liefert Daten meist leicht verzögert.
 * Dadurch wird sichergestellt, dass die neuen Werte
 * bereits verfügbar sind.
 *
 * Beispiel (Intervall 15 Minuten):
 *
 * API liefert:   00, 15, 30, 45
 * Scheduler:     01, 16, 31, 46
 *
 * Autor: Bollogg
 * Datum: 2026-03-15
 *******************************************************/
object PegelScheduler {

    /**
     * Startet die Planung eines neuen Alarms.
     *
     * Ablauf:
     * 1. Intervall aus Einstellungen lesen
     * 2. Nächsten Trigger berechnen
     * 3. Alarm beim System registrieren
     *
     * @param context ApplicationContext
     */
    @JvmStatic
    fun schedule(context: Context) {

        // Einstellungen laden
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

        // Intervall in Minuten (Standard: 15)
        val interval = prefs.getInt("interval_minutes", 15)

        // Zeitpunkt berechnen
        val trigger = calculateNextTrigger(interval)

        // Alarm setzen
        setAlarm(context, trigger)
    }

    /**
     * Alias für schedule().
     *
     * Wird typischerweise nach der Ausführung
     * eines Alarms erneut aufgerufen, um den
     * nächsten Alarm zu planen.
     */
    fun scheduleNext(context: Context) {
        schedule(context)
    }

    /**
     * Berechnet den nächsten Ausführungszeitpunkt.
     *
     * Logik:
     * ------------------------------------------------
     * 1. aktuelle Minute ermitteln
     * 2. nächstes Vielfaches des Intervalls berechnen
     * 3. +1 Minute Puffer hinzufügen
     *
     * Beispiel:
     *
     * aktuelle Zeit: 10:07
     * Intervall: 15
     *
     * nächste Zielminute: 15
     * +1 Minute Puffer: 16
     *
     * Ergebnis: 10:16
     *
     * @param interval Intervall in Minuten
     * @return Zeitpunkt in Millisekunden
     */
    private fun calculateNextTrigger(interval: Int): Long {

        val c = Calendar.getInstance()

        // aktuelle Minute
        val currentMin = c.get(Calendar.MINUTE)

        /**
         * Nächstes Vielfaches des Intervalls berechnen.
         *
         * Beispiel:
         * aktuelle Minute = 17
         * Intervall = 15
         *
         * 17 / 15 = 1
         * (1 + 1) * 15 = 30
         */
        var nextTargetMin = ((currentMin / interval) + 1) * interval

        /**
         * +1 Minute Puffer hinzufügen
         */
        var finalMin = nextTargetMin + 1

        /**
         * Wenn wir über 60 Minuten kommen,
         * muss die Stunde erhöht werden.
         */
        if (finalMin >= 60) {

            c.add(Calendar.HOUR_OF_DAY, 1)
            finalMin %= 60

        }

        // Zeit setzen
        c.set(Calendar.MINUTE, finalMin)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)

        val result = c.timeInMillis

        Log.i(
            "PEGEL_SCHED",
            "⏰ Nächster Abruf geplant für Minute $finalMin (Intervall: ${interval}m)"
        )

        return result
    }

    /**
     * Registriert den Alarm beim Android-System.
     *
     * Android-Versionen:
     * ------------------------------------------------
     * Android 12+ verlangt eine Berechtigung für
     * exakte Alarme.
     *
     * Wenn diese nicht vorhanden ist,
     * wird ein weniger genauer Alarm gesetzt.
     *
     * @param context ApplicationContext
     * @param triggerAt Zeitpunkt des nächsten Alarms
     */
    private fun setAlarm(context: Context, triggerAt: Long) {

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        /**
         * Intent für den BroadcastReceiver
         */
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "de.net.wiesenfarth.mainpegel.RUN_FGS"
        }

        /**
         * PendingIntent wird vom AlarmManager
         * zum Starten des Receivers verwendet.
         */
        val pi = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        /**
         * Ab Android 12 (API 31):
         * Berechtigung für exakte Alarme prüfen.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {

            Log.w(
                "PEGEL_SCHED",
                "Keine Berechtigung für exakte Alarme – nutze setAndAllowWhileIdle"
            )

            am.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pi
            )

        } else {

            /**
             * Exakter Alarm auch im Doze-Modus.
             */
            am.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pi
            )

        }
    }
}
package de.wiesenfarth.mainpegel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/*******************************************************
 * Klasse:      AlarmReceiver
 *
 * Beschreibung:
 * Empfängt Alarme (z. B. vom AlarmManager) und
 * startet anschließend den PegelForegroundService,
 * der im Hintergrund die Pegeldaten abruft.
 *
 * Verwendung:
 * Wird durch PegelScheduler / AlarmManager ausgelöst,
 * auch wenn die App im Hintergrund oder beendet ist.
 *
 * Besonderheiten:
 * Ab Android 8 (Oreo) müssen Foreground Services
 * über startForegroundService() gestartet werden.
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-21
 * @Version:   2025.11
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // --- ACTION PRÜFEN ---

        if ("de.wiesenfarth.mainpegel.RUN_FGS" != intent.getAction()) {
            return  // <-- ganz wichtig! Nur unsere Action ausführen!
        }
        Log.i("ALARM", "Alarm ausgelöst → PegelLogic.run()")

        // Pegel laden
        PegelLogic.run(context)


        // NÄCHSTEN ALARM SOFORT setzen (15min.)
        PegelScheduler.scheduleNext(context)
    }
}

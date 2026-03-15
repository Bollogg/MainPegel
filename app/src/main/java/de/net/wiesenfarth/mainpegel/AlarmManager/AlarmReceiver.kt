package de.net.wiesenfarth.mainpegel.AlarmManager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/*******************************************************
 * Klasse: AlarmReceiver
 *
 * Beschreibung:
 * -----------------------------------------------------
 * Dieser BroadcastReceiver empfängt Alarm-Events,
 * die vom AlarmManager ausgelöst werden.
 *
 * Wenn der Alarm ausgelöst wird:
 *
 * 1. Der Receiver wird vom System gestartet
 * 2. Der nächste Alarm wird geplant
 * 3. Der PegelForegroundService wird gestartet
 * 4. Der Service ruft anschließend Pegeldaten von der API ab
 *
 * Architekturfluss:
 * -----------------------------------------------------
 *
 * AlarmManager
 *      ↓
 * AlarmReceiver (BroadcastReceiver)
 *      ↓
 * PegelForegroundService
 *      ↓
 * PegelLogic.run()
 *      ↓
 * API → Cache → Widget / UI
 *
 * Besonderheiten:
 * -----------------------------------------------------
 * BroadcastReceiver dürfen keine langen Operationen
 * direkt ausführen, da sie vom System sehr schnell
 * beendet werden können.
 *
 * Deshalb startet der Receiver einen Foreground Service,
 * der den eigentlichen Netzwerkabruf übernimmt.
 *
 * Android Verhalten:
 * -----------------------------------------------------
 * Der Receiver kann ausgelöst werden, auch wenn:
 *
 * • die App nicht geöffnet ist
 * • die App im Hintergrund läuft
 * • das Gerät gerade gesperrt ist
 *
 * Voraussetzung:
 * AlarmManager muss vorher über PegelScheduler
 * konfiguriert worden sein.
 *
 * Autor: Bollogg
 * Datum: 2026-03-15
 *******************************************************/
class AlarmReceiver : BroadcastReceiver() {

    /**
     * Wird vom System aufgerufen, sobald der Alarm ausgelöst wird.
     *
     * @param context Kontext der Anwendung
     * @param intent  Intent mit der ausgelösten Action
     */
    override fun onReceive(context: Context, intent: Intent) {

        // -------------------------------------------------
        // ACTION PRÜFEN
        // -------------------------------------------------
        // Sicherheitsprüfung:
        // Nur unsere definierte Action darf verarbeitet werden.
        // Andere Broadcasts werden ignoriert.
        if ("de.net.wiesenfarth.mainpegel.RUN_FGS" != intent.action) {
            return
        }

        Log.i("ALARM", "Alarm ausgelöst → starte PegelForegroundService")

        /**
         * Nächsten Alarm planen.
         *
         * Dadurch entsteht eine wiederkehrende
         * Aktualisierung (z.B. alle 15 Minuten).
         */
        PegelScheduler.scheduleNext(context)

        /**
         * Foreground Service starten.
         *
         * Der Service übernimmt:
         * • API Abruf
         * • Datenverarbeitung
         * • Widget Update
         */
        val serviceIntent = Intent(context, PegelForegroundService::class.java)

        context.startService(serviceIntent)
    }
}
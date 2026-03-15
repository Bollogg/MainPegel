package de.net.wiesenfarth.mainpegel.AlarmManager

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import de.net.wiesenfarth.mainpegel.API.PegelLogic

/*******************************************************
 * Klasse: PegelForegroundService
 *
 * Beschreibung:
 * -----------------------------------------------------
 * Dieser Service führt einen Pegel-Datenabruf im
 * Vordergrund aus.
 *
 * Hintergrund:
 * Android erlaubt seit neueren Versionen keine
 * längeren Hintergrundprozesse ohne Foreground-Service.
 *
 * Deshalb wird der API-Abruf über diesen Service
 * gestartet, damit:
 *
 * • das System den Prozess nicht beendet
 * • Netzwerkoperationen zuverlässig laufen
 * • Widgets und Hintergrundupdates stabil bleiben
 *
 * Ablauf:
 * -----------------------------------------------------
 * 1. Service startet
 * 2. Foreground Notification wird angezeigt
 * 3. PegelLogic.run() wird gestartet
 * 4. Nach Abschluss → Service beendet sich selbst
 *
 * Eigenschaften:
 * -----------------------------------------------------
 * • läuft nur kurzzeitig
 * • startet keinen Dauerprozess
 * • wird nach API-Lauf automatisch beendet
 *
 * Verwendung:
 * -----------------------------------------------------
 * Wird typischerweise gestartet durch:
 *
 * - AlarmManager
 * - Hintergrund-Scheduler
 * - Widget-Update
 *
 * Autor: Bollogg
 * Stand:  2026-03-15
 *******************************************************/
class PegelForegroundService : Service() {

    /**
     * Wird aufgerufen, wenn der Service gestartet wird.
     *
     * Aufgaben:
     * 1. Service in Foreground-Modus versetzen
     * 2. PegelLogic.run() starten
     * 3. Nach Abschluss Service stoppen
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Service in Foreground starten (Pflicht für längere Tasks)
        startInForeground()

        Log.i("SERVICE", "Starte Pegel-Update im Vordergrund")

        /**
         * API-Logik ausführen.
         *
         * Der Callback wird aufgerufen, sobald
         * der API-Lauf vollständig abgeschlossen ist.
         */
        PegelLogic.run(applicationContext) {

            Log.i(
                "AlarmManager/PegelForegroundService",
                "API-Lauf beendet, stoppe Service"
            )

            // Service beenden
            stopSelf()
        }

        /**
         * START_NOT_STICKY:
         * Wenn der Service vom System beendet wird,
         * wird er NICHT automatisch neu gestartet.
         */
        return START_NOT_STICKY
    }

    /**
     * Startet den Service als Foreground-Service.
     *
     * Android verlangt hierfür eine sichtbare
     * Notification für den Benutzer.
     */
    private fun startInForeground() {

        val channelId = "pegel_channel"

        /**
         * Notification Channel erstellen (Android 8+ Pflicht)
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "Pegel Überwachung",
                NotificationManager.IMPORTANCE_LOW
            )

            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

        /**
         * Notification erstellen
         *
         * Wird angezeigt solange der Service läuft.
         */
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("Pegel-Service")
            .setContentText("Aktualisiere Daten...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        /**
         * Foreground Service starten
         *
         * Ab Android 14 muss zusätzlich der
         * Service-Typ angegeben werden.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {

            startForeground(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )

        } else {

            startForeground(1, notification)

        }
    }

    /**
     * Bind ist nicht erforderlich.
     * Der Service wird nur gestartet (startService).
     */
    override fun onBind(intent: Intent?): IBinder? = null
}
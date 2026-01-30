package de.net.wiesenfarth.mainpegel.AlarmManager

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import de.net.wiesenfarth.mainpegel.API.PegelLogic

/*******************************************************
 * Programm:  PegelForegroundService
 *
 * Beschreibung:
 * Foreground-Service, der im Hintergrund die Pegel-
 * Überwachung durchführt.
 *
 * Android (ab API 26) erlaubt keine dauerhaften
 * Hintergrunddienste mehr ohne Foreground-Modus.
 * Dieser Service:
 *
 * • startet mit einer sichtbaren Benachrichtigung
 * • führt die Pegellogik (API-Call) im Hintergrund aus
 * • beendet sich anschließend automatisch
 *
 * Der Service wird vom AlarmReceiver gestartet.
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-22
*******************************************************/
class PegelForegroundService : Service() {
  /**
  * Wird aufgerufen, wenn der Service gestartet wird.
  * Startet sofort die Foreground-Notification und dann
  * im Hintergrund einen Thread für die API-Logik.
  */
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    // Pflicht für Android 8+ → sonst Ausnahme

    startInForeground()

    // API-Aufruf oder Pegelverarbeitung in einer eigenen Thread
    Thread(Runnable { this.runTask() }).start()

    // START_NOT_STICKY: Service startet nicht neu,
    // falls System ihn beendet
    return START_NOT_STICKY
  }

  /**
   * Startet den Dienst im "Foreground-Modus"
   * mit einer permanent sichtbaren Notification.
   */
  private fun startInForeground() {
    val CHANNEL_ID = "pegel_channel"

    // Notwendig ab Android 8+ (API 26)
	  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		  val channel = NotificationChannel(
			  CHANNEL_ID,
			  "Pegel Überwachung",
			  NotificationManager.IMPORTANCE_LOW
		  )
		  channel.description = "Überwacht regelmäßig die Pegeldaten"

		  val nm =
			  getSystemService(NOTIFICATION_SERVICE) as NotificationManager

		  nm.createNotificationChannel(channel)
	  }

    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_menu_info_details)
        .setContentTitle("Pegel-Service aktiv")
        .setContentText("Überwache Wasserstand…")
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    try {
      startForeground(1, notification)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  /**
   * Führt die eigentliche Pegellogik aus (API, Auswertung usw.)
   * und beendet den Dienst nach Abschluss.
   */
  private fun runTask() {
    try {
      // Dezentralisierte Pegellogik
      //PegelLogic.run(this);
      PegelLogic.run(getApplicationContext())
      // Neuer Arlarm für den nächsten Durchlauf
      PegelScheduler.schedule(getApplicationContext())
    } catch (e: Exception) {
      e.printStackTrace()
    }

    // Service sauber beenden
    stopSelf()
  }

  /**
   * Wird für Bound-Services benötigt – hier nicht genutzt.
   */
  override fun onBind(intent: Intent?): IBinder? {
    return null
  }
}
package de.net.wiesenfarth.mainpegel.AlarmManager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import de.net.wiesenfarth.mainpegel.R

/*******************************************************
 * Programm:  NotificationHelper
 *
 * Beschreibung:
 * Zentrale Hilfsklasse zur Verwaltung und Anzeige von
 * Pegelwarnungen per Android-Benachrichtigung.
 *
 * Diese Klasse kapselt:
 * - das Erstellen des NotificationChannels (ab Android 8 / API 26)
 * - die Konfiguration von Sound und Vibration
 * - das Versenden von Pegelwarnungen mit erweiterten Texten
 *
 * Der NotificationChannel wird nur einmal pro App-
 * Installation erstellt und anschließend vom System
 * wiederverwendet.
 *
 * Autor:     Bollogg
 * Datum:     2025-11-20
 *******************************************************/
object NotificationHelper {
  /**
	* Feste ID des NotificationChannels.
	* Darf nach Veröffentlichung der App nicht mehr geändert werden,
	* da sonst bestehende Benachrichtigungseinstellungen verloren gehen.
	*/
  const val CHANNEL_ID: String = "pegel_alarm"

	/**
	* Erstellt (falls nötig) den NotificationChannel für Pegelwarnungen.
	*
	* Muss vor dem Versenden einer Notification aufgerufen werden.
	* Ab Android 8.0 (API 26) zwingend erforderlich.
	*
	* @param ctx Context der Anwendung oder des Services
	*/
	fun ensureChannel(ctx: Context) {
		// NotificationChannels existieren erst ab Android O
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val nm =
          ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

	    // Kanal definieren
      val ch =
	      NotificationChannel(
		      CHANNEL_ID,
		      "Pegelwarnungen",
		      NotificationManager.IMPORTANCE_HIGH
	      )

	    // ✔ Standard-Benachrichtigungston des Geräts
      val defaultSound =
				RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

      val attrs = AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_NOTIFICATION)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()

      ch.setSound(defaultSound, attrs)

	    // ✔ Vibration aktivieren
      ch.enableVibration(true)
      ch.setVibrationPattern(
        longArrayOf(
            0, 300, 200, 400
        )
      )

	    // Kanal beim System registrieren
      nm.createNotificationChannel(ch)
    }
  }

	/**
	* Sendet eine Pegelwarnung mit detaillierten Informationen.
	*
	* Die Benachrichtigung zeigt:
	* - den Pegelanstieg (Delta)
	* - den aktuellen Pegelwert
	* - den Zeitpunkt der Messung
	*
	* @param ctx   Context zum Zugriff auf NotificationManager
	* @param value Aktueller Pegelwert in cm
	* @param delta Pegeländerung in cm
	* @param time  Zeitpunkt der Messung (optional)
	*/
  @JvmStatic
  fun sendWaveAlert(ctx: Context,
                    value: Float,
                    delta: Float,
                    time: String?
	) {

		// Sicherstellen, dass der Channel existiert
    ensureChannel(ctx)

    val nm =
        ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

		// Erweiterter Text für BigTextStyle
		val bigText =
      ("Pegel gestiegen um " + delta + " cm\n"
              + "Neuer Pegel: " + value + " cm\n"
              + "Zeitpunkt: " + time + " Uhr")

    val builder =
      NotificationCompat.Builder(ctx, CHANNEL_ID)
          .setSmallIcon(R.drawable.ic_stat_pegel) // Statisches Notification-Icon
          .setContentTitle("Pegelwarnung")
          .setContentText("Pegelanstieg: +" + delta + " cm")
          .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .setAutoCancel(true)

		// Notification anzeigen
    nm.notify(1, builder.build())
  }
}
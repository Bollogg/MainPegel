package de.wiesenfarth.mainpegel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    // Kanal-ID muss konstant bleiben!
    const val CHANNEL_ID: String = "pegel_alarm"

    /**
     * Erstellt den NotificationChannel (nur 1× pro App-Installation)
     * Muss VOR jeder Notification aufgerufen werden.
     */
    fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm =
                ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val ch =
                NotificationChannel(
                    CHANNEL_ID,
                    "Pegelwarnungen",
                    NotificationManager.IMPORTANCE_HIGH
                )

            // ✔ Gerätesound verwenden
            val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            ch.setSound(defaultSound, attrs)

            // ✔ Vibration einschalten
            ch.enableVibration(true)
            ch.setVibrationPattern(
                longArrayOf(
                    0, 300, 200, 400
                )
            )

            nm.createNotificationChannel(ch)
        }
    }

    /**
     * Sendet eine erweiterte Pegelwarnung.
     */
    @JvmStatic
    fun sendWaveAlert(ctx: Context, value: Float, delta: Float, time: String?) {
        // Kanal sicherstellen (wichtig!)

        ensureChannel(ctx)

        val nm =
            ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val bigText =
            ("Pegel gestiegen um " + delta + " cm\n"
                    + "Neuer Pegel: " + value + " cm\n"
                    + "Zeitpunkt: " + time + " Uhr")

        val builder =
            NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_pegel) // Dein statisches Icon
                .setContentTitle("Pegelwarnung")
                .setContentText("Pegelanstieg: +" + delta + " cm")
                .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

        nm.notify(1, builder.build())
    }
}

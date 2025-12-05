package de.wiesenfarth.mainpegel;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

  // Kanal-ID muss konstant bleiben!
  public static final String CHANNEL_ID = "pegel_alarm";

  /**
   * Erstellt den NotificationChannel (nur 1× pro App-Installation)
   * Muss VOR jeder Notification aufgerufen werden.
   */
  public static void ensureChannel(Context ctx) {

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

      NotificationManager nm =
          (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

      NotificationChannel ch =
          new NotificationChannel(
              CHANNEL_ID,
              "Pegelwarnungen",
              NotificationManager.IMPORTANCE_HIGH
          );

      // ✔ Gerätesound verwenden
      Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

      AudioAttributes attrs = new AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_NOTIFICATION)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build();

      ch.setSound(defaultSound, attrs);

      // ✔ Vibration einschalten
      ch.enableVibration(true);
      ch.setVibrationPattern(new long[]{
          0, 300, 200, 400
      });

      nm.createNotificationChannel(ch);
    }
  }

  /**
   * Sendet eine erweiterte Pegelwarnung.
   */
  public static void sendWaveAlert(Context ctx, float value, float delta, String time) {

    // Kanal sicherstellen (wichtig!)
    ensureChannel(ctx);

    NotificationManager nm =
        (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

    String bigText =
        "Pegel gestiegen um " + delta + " cm\n"
            + "Neuer Pegel: " + value + " cm\n"
            + "Zeitpunkt: " + time + " Uhr";

    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_pegel)  // Dein statisches Icon
            .setContentTitle("Pegelwarnung")
            .setContentText("Pegelanstieg: +" + delta + " cm")
            .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true);

    nm.notify(1, builder.build());
  }
}

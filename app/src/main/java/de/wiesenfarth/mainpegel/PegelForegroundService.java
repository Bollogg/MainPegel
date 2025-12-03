package de.wiesenfarth.mainpegel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

/*******************************************************
 * Programm:  PegelForegroundService
 *
 * Beschreibung:
 *  Foreground-Service, der im Hintergrund die Pegel-
 *  Überwachung durchführt.
 *
 *  Android (ab API 26) erlaubt keine dauerhaften
 *  Hintergrunddienste mehr ohne Foreground-Modus.
 *  Dieser Service:
 *
 *    • startet mit einer sichtbaren Benachrichtigung
 *    • führt die Pegellogik (API-Call) im Hintergrund aus
 *    • beendet sich anschließend automatisch
 *
 *  Der Service wird vom AlarmReceiver gestartet.
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-22
 *******************************************************/
public class PegelForegroundService extends Service {

  /**
   * Wird aufgerufen, wenn der Service gestartet wird.
   * Startet sofort die Foreground-Notification und dann
   * im Hintergrund einen Thread für die API-Logik.
   */
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    // Pflicht für Android 8+ → sonst Ausnahme
    startInForeground();

    // API-Aufruf oder Pegelverarbeitung in einer eigenen Thread
    new Thread(this::runTask).start();

    // START_NOT_STICKY: Service startet nicht neu,
    // falls System ihn beendet
    return START_NOT_STICKY;
  }

  /**
   * Startet den Dienst im "Foreground-Modus"
   * mit einer permanent sichtbaren Notification.
   */
  private void startInForeground() {

    final String CHANNEL_ID ="pegel_channel";

    // Notwendig ab Android 8+ (API 26)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

      NotificationChannel channel = new NotificationChannel(
          CHANNEL_ID,
          "Pegel Überwachung",
          NotificationManager.IMPORTANCE_LOW
      );
      channel.setDescription("Überwacht regelmäßig die Pegeldaten");

      NotificationManager nm = getSystemService(NotificationManager.class);
      if (nm != null) {
        nm.createNotificationChannel(channel);
      }
    }

    Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_menu_info_details)
        .setContentTitle("Pegel-Service aktiv")
        .setContentText("Überwache Wasserstand…")
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build();

    try {
      startForeground(1, notification);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Führt die eigentliche Pegellogik aus (API, Auswertung usw.)
   * und beendet den Dienst nach Abschluss.
   */
  private void runTask() {
    try {
      // Dezentralisierte Pegellogik
      //PegelLogic.run(this);
      PegelLogic.run(getApplicationContext());
      // Neuer Arlarm für den nächsten Durchlauf
      PegelScheduler.schedule(getApplicationContext());

    } catch (Exception e) {
      e.printStackTrace();
    }

    // Service sauber beenden
    stopSelf();
  }

  /**
   * Wird für Bound-Services benötigt – hier nicht genutzt.
   */
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}

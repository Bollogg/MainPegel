package de.wiesenfarth.mainpegel;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import android.content.SharedPreferences;

public class WidgetUpdateService extends Service {
//ToDo: prüfen ob noch gebraucht? sonst löschen
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    Log.i("WIDGET", "WidgetUpdateService gestartet");
    updateWidget();

    stopSelf(startId);
    return START_NOT_STICKY;
  }

  private void updateWidget() {
    try {
      AppWidgetManager manager = AppWidgetManager.getInstance(this);
      ComponentName widget = new ComponentName(this, PegelWidget.class);

      // Pegelwerte aus cache lesen
      SharedPreferences prefs = getSharedPreferences("pegel_cache", MODE_PRIVATE);
      int value = prefs.getInt("last_value", -1);
      String time = prefs.getString("last_time", "--:--");

      // Widget-Layout laden
      RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_pegel);

      // ---- Bitmap erzeugen (z.B. Pegel-Grafik) ----
      float scale = getResources().getDisplayMetrics().density;
      int width = (int) (320 * scale);
      int height = (int) (110 * scale);
      Bitmap bitmap = PegelBitmapGenerator.makePegelBitmap(this,width,height);
      if (bitmap != null) {
        views.setImageViewBitmap(R.id.widgetChartImage, bitmap);
      }

      // Pegel anzeigen
      if (value >= 0) {
        views.setTextViewText(R.id.widget_pegelwert, value + " cm");
      } else {
        views.setTextViewText(R.id.widget_pegelwert, "-- cm");
      }

      views.setTextViewText(R.id.widget_timestamp, time);

      // Widget aktualisieren
      manager.updateAppWidget(widget, views);

      Log.i("WIDGET", "Widget aktualisiert: " + value + " cm");

    } catch (Exception e) {
      Log.e("WIDGET", "Fehler beim Widget-Update", e);
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}

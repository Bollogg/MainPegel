package de.wiesenfarth.mainpegel;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

public class PegelWidget extends AppWidgetProvider {

  public static final String UPDATE_ACTION = "de.wiesenfarth.mainpegel.UPDATE_WIDGET";

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

    for (int id : appWidgetIds) {
      updateWidget(context, appWidgetManager, id);
    }

    // Update-Zyklus starten
    scheduleNextUpdate(context);
  }

  private void updateWidget(Context context, AppWidgetManager mgr, int widgetId) {

    // WICHTIG → Pegel überprüfen, auch wenn App nicht läuft
    PegelLogic.run(context);

    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_pegel);

    // 🔥 Widget-Größe ermitteln
    Bundle options = mgr.getAppWidgetOptions(widgetId);

    int minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
    int maxW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
    int minH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
    int maxH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

    Log.i("WIDGET", "Größe dp → minW=" + minW + " maxW=" + maxW + " minH=" + minH + " maxH=" + maxH);

    // px berechnen (dp → px)
    float density = context.getResources().getDisplayMetrics().density;
    int widthPx  = (int) (maxW * density);
    int heightPx = (int) (maxH * density);

    if (widthPx < 100) widthPx = 300;   // falls System keine Daten liefert
    if (heightPx < 100) heightPx = 130;

    Log.i("WIDGET", "Gerenderte Bitmapgröße px: " + widthPx + " x " + heightPx);


    // Graph aus Cache erzeugen
    Bitmap bmp = PegelBitmapGenerator.makePegelBitmap(context, widthPx, heightPx);
    views.setImageViewBitmap(R.id.widgetChartImage, bmp);

    //Bitmap bmp = PegelBitmapGenerator.makePegelBitmap(context);
    //views.setImageViewBitmap(R.id.widgetChartImage, bmp);

    mgr.updateAppWidget(widgetId, views);
    // danach Widget-UI aktualisieren
    //ToDo: löschen WidgetUpdater.updateWidgetViews(context, mgr, appWidgetIds);
    ComponentName thisWidget = new ComponentName(context, PegelWidget.class);
    int[] ids = mgr.getAppWidgetIds(thisWidget);
    WidgetUpdater.updateWidgetViews(context, mgr, ids);
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);

    if (UPDATE_ACTION.equals(intent.getAction())) {

      Log.i("WIDGET", "⟳ Widget Update ausgelöst – lade Pegel…");

      // Erst API ausführen → speichert Daten → sendet Broadcast
      PegelLogic.run(context);

      // Widget neu zeichnen
      AppWidgetManager mgr = AppWidgetManager.getInstance(context);
      int[] ids = mgr.getAppWidgetIds(
          new android.content.ComponentName(context, PegelWidget.class)
      );
      for (int id : ids) {
        updateWidget(context, mgr, id);
      }

      // nächsten Update setzen
      scheduleNextUpdate(context);
    }
  }

  private void scheduleNextUpdate(Context context) {

    int minutes = context
        .getSharedPreferences("settings", Context.MODE_PRIVATE)
        .getInt("widget_interval", 30);  // 15/30/45/60

    long triggerAt = System.currentTimeMillis() + minutes * 60 * 1000;

    Intent i = new Intent(context, PegelWidget.class);
    i.setAction(UPDATE_ACTION);

    PendingIntent pi = PendingIntent.getBroadcast(
        context,
        0,
        i,
        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
    );

    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    am.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        triggerAt,
        pi
    );

    Log.i("WIDGET", "⏰ Widget-Update geplant in " + minutes + " Min");
  }
}

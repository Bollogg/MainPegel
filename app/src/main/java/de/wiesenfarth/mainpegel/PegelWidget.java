package de.wiesenfarth.mainpegel;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SizeF;
import android.widget.RemoteViews;

import java.util.Map;

public class PegelWidget extends AppWidgetProvider {

  public static final String UPDATE_ACTION = "de.wiesenfarth.mainpegel.UPDATE_WIDGET";

  @Override
  public void onUpdate(Context context, AppWidgetManager mgr, int[] appWidgetIds) {

    for (int id : appWidgetIds) {
      Log.i("WIDGET", "updateWidget() CALLER = onUpdate()");
      updateWidget(context, mgr, id);
    }

    // Update-Zyklus starten
    //ToDo: testen scheduleNextUpdate(context);
  }

// ... innerhalb Ihrer AppWidgetProvider-Klasse

  private void updateWidget(Context context, AppWidgetManager mgr, int widgetId) {

    Log.i("WIDGET", "⟳ updateWidget() für widgetId: " + widgetId);

    // WICHTIG → Pegel überprüfen, auch wenn App nicht läuft
    // ToDo: Testen, ob PegelLogic.run(context) hier richtig ist.
    // Evtl. in einen Hintergrunddienst oder WorkManager auslagern, falls es länger dauert.
    // PegelLogic.run(context);

    // RemoteViews-Instanz erstellen
    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_pegel);

    // 🔥 Widget-Größe mit moderner API ermitteln (ab API 31)
    Map<SizeF, RemoteViews> viewMapping = mgr.getAppWidgetOptions(widgetId).getParcelable(AppWidgetManager.OPTION_APPWIDGET_SIZES);
    SizeF maxSize = null;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && viewMapping != null && !viewMapping.isEmpty()) {
      // Die größte verfügbare Größe für die Bitmap-Generierung auswählen
      maxSize = viewMapping.keySet().stream()
          .max((size1, size2) -> Float.compare(size1.getWidth() * size1.getHeight(), size2.getWidth() * size2.getHeight()))
          .orElse(null);
    }

    int widthPx;
    int heightPx;

    if (maxSize != null) {
      widthPx = (int) maxSize.getWidth();
      heightPx = (int) maxSize.getHeight();
      Log.i("WIDGET", "Größe aus API 31+ (px) → " + widthPx + " x " + heightPx);
    } else {
      // Fallback für ältere APIs oder falls die neue API keine Daten liefert
      Log.w("WIDGET", "Fallback zur alten Größenberechnung wird verwendet.");
      widthPx = getWidgetWidthInPx(context, mgr, widgetId);
      heightPx = getWidgetHeightInPx(context, mgr, widgetId);
    }

    // Fallback-Werte, falls das System 0 oder unsinnig kleine Werte liefert
    if (widthPx <= 0) widthPx = 300;
    if (heightPx <= 0) heightPx = 130;

    Log.i("WIDGET", "Gerenderte Bitmapgröße (px): " + widthPx + " x " + heightPx);

    // Graph aus Cache erzeugen oder neu generieren
    Bitmap bmp = PegelBitmapGenerator.makePegelBitmap(context, widthPx, heightPx);
    if (bmp != null) {
      views.setImageViewBitmap(R.id.widgetChartImage, bmp);
    } else {
      Log.e("WIDGET", "Bitmap konnte nicht erstellt werden!");
      // Optional: Ein Fehler-Icon oder Platzhalter anzeigen
      views.setImageViewResource(R.id.widgetChartImage, R.layout.widget_pegel);
    }

    // Beispiel: Click-Handler für das Widget hinzufügen, um eine manuelle Aktualisierung auszulösen
    Intent updateIntent = new Intent(context, PegelWidget.class); // Ihre Widget-Provider-Klasse
    updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
    updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetId});

    // PendingIntent erstellen mit dem korrekten Flag für Immutability (wichtig für API 31+)
    int flags = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        : PendingIntent.FLAG_UPDATE_CURRENT;
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, widgetId, updateIntent, flags);
    views.setOnClickPendingIntent(R.id.widgetChartImage, pendingIntent); // Annahme: Ihr Root-Layout hat die ID widgetRootLayout

    // Das Widget-UI aktualisieren
    mgr.updateAppWidget(widgetId, views);
  }

  // Hilfsmethoden für die alte Größenberechnung (Fallback)
  private int getWidgetWidthInPx(Context context, AppWidgetManager mgr, int widgetId) {
    float density = context.getResources().getDisplayMetrics().density;
    // Verwenden von maxW für Landscape und minW für Portrait
    boolean isPortrait = context.getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT;
    int widthDp = isPortrait
        ? mgr.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        : mgr.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
    return (int) (widthDp * density);
  }

  private int getWidgetHeightInPx(Context context, AppWidgetManager mgr, int widgetId) {
    float density = context.getResources().getDisplayMetrics().density;
    boolean isPortrait = context.getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT;
    int heightDp = isPortrait
        ? mgr.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
        : mgr.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
    return (int) (heightDp * density);
  }

  private void ToDoupdateWidget(Context context, AppWidgetManager mgr, int widgetId) {

    Log.i("WIDGET", "⟳ onReceive() – action= updateWidget");

    // WICHTIG → Pegel überprüfen, auch wenn App nicht läuft
    //ToDo: testen PegelLogic.run(context);

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

    if (widthPx < 100) widthPx   = 300;   // falls System keine Daten liefert
    if (heightPx < 100) heightPx = 130;

    Log.i("WIDGET", "Gerenderte Bitmapgröße px: " + widthPx + " x " + heightPx);


    // Graph aus Cache erzeugen
    Bitmap bmp = PegelBitmapGenerator.makePegelBitmap(context, widthPx, heightPx);
    views.setImageViewBitmap(R.id.widgetChartImage, bmp);

    mgr.updateAppWidget(widgetId, views);
    // danach Widget-UI aktualisieren
    //ToDo: löschen WidgetUpdater.updateWidgetViews(context, mgr, appWidgetIds);
    //ComponentName thisWidget = new ComponentName(context, PegelWidget.class);
    //int[] ids = mgr.getAppWidgetIds(thisWidget);
    //WidgetUpdater.updateWidgetViews(context, mgr, ids);
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);

    if (UPDATE_ACTION.equals(intent.getAction())) {

      Log.i("WIDGET", "⟳ Widget Update ausgelöst – lade Pegel…");

      // Erst API ausführen → speichert Daten → sendet Broadcast
      //ToDo: testen PegelLogic.run(context);

      // Widget neu zeichnen
      AppWidgetManager mgr = AppWidgetManager.getInstance(context);
      int[] ids = mgr.getAppWidgetIds(
          new android.content.ComponentName(context, PegelWidget.class)
      );
      for (int id : ids) {
        Log.i("WIDGET", "updateWidget() CALLER = onReceive(" + intent.getAction() + ")");
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

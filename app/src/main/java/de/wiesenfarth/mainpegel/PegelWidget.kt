package de.wiesenfarth.mainpegel

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import android.util.SizeF
import android.widget.RemoteViews
import de.wiesenfarth.mainpegel.PegelBitmapGenerator.makePegelBitmap
import java.lang.Float
import kotlin.Comparator
import kotlin.Int
import kotlin.IntArray
import kotlin.String
import kotlin.intArrayOf

class PegelWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, mgr: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            Log.i("WIDGET", "updateWidget() CALLER = onUpdate()")
            updateWidget(context, mgr, id)
        }

        // Update-Zyklus starten
        //ToDo: testen scheduleNextUpdate(context);
    }

    // ... innerhalb Ihrer AppWidgetProvider-Klasse
    private fun updateWidget(context: Context, mgr: AppWidgetManager, widgetId: Int) {
        Log.i("WIDGET", "⟳ updateWidget() für widgetId: " + widgetId)

        // WICHTIG → Pegel überprüfen, auch wenn App nicht läuft
        // ToDo: Testen, ob PegelLogic.run(context) hier richtig ist.
        // Evtl. in einen Hintergrunddienst oder WorkManager auslagern, falls es länger dauert.
        // PegelLogic.run(context);

        // RemoteViews-Instanz erstellen
        val views = RemoteViews(context.getPackageName(), R.layout.widget_pegel)

        // 🔥 Widget-Größe mit moderner API ermitteln (ab API 31)
        val viewMapping: MutableMap<SizeF?, RemoteViews?>? =
            mgr.getAppWidgetOptions(widgetId).getParcelable(AppWidgetManager.OPTION_APPWIDGET_SIZES)
        var maxSize: SizeF? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && viewMapping != null && !viewMapping.isEmpty()) {
            // Die größte verfügbare Größe für die Bitmap-Generierung auswählen
            maxSize = viewMapping.keys.stream()
                .max(Comparator { size1: SizeF?, size2: SizeF? ->
                    Float.compare(
                        size1!!.getWidth() * size1.getHeight(),
                        size2!!.getWidth() * size2.getHeight()
                    )
                })
                .orElse(null)
        }

        var widthPx: Int
        var heightPx: Int

        if (maxSize != null) {
            widthPx = maxSize.getWidth().toInt()
            heightPx = maxSize.getHeight().toInt()
            Log.i("WIDGET", "Größe aus API 31+ (px) → " + widthPx + " x " + heightPx)
        } else {
            // Fallback für ältere APIs oder falls die neue API keine Daten liefert
            Log.w("WIDGET", "Fallback zur alten Größenberechnung wird verwendet.")
            widthPx = getWidgetWidthInPx(context, mgr, widgetId)
            heightPx = getWidgetHeightInPx(context, mgr, widgetId)
        }

        // Fallback-Werte, falls das System 0 oder unsinnig kleine Werte liefert
        if (widthPx <= 0) widthPx = 300
        if (heightPx <= 0) heightPx = 130

        Log.i("WIDGET", "Gerenderte Bitmapgröße (px): " + widthPx + " x " + heightPx)

        // Graph aus Cache erzeugen oder neu generieren
        val bmp = makePegelBitmap(context, widthPx, heightPx)
        if (bmp != null) {
            views.setImageViewBitmap(R.id.widgetChartImage, bmp)
        } else {
            Log.e("WIDGET", "Bitmap konnte nicht erstellt werden!")
            // Optional: Ein Fehler-Icon oder Platzhalter anzeigen
            views.setImageViewResource(R.id.widgetChartImage, R.layout.widget_pegel)
        }

        // Beispiel: Click-Handler für das Widget hinzufügen, um eine manuelle Aktualisierung auszulösen
        val updateIntent = Intent(context, PegelWidget::class.java) // Ihre Widget-Provider-Klasse
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))

        // PendingIntent erstellen mit dem korrekten Flag für Immutability (wichtig für API 31+)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else
            PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getBroadcast(context, widgetId, updateIntent, flags)
        views.setOnClickPendingIntent(
            R.id.widgetChartImage,
            pendingIntent
        ) // Annahme: Ihr Root-Layout hat die ID widgetRootLayout

        // Das Widget-UI aktualisieren
        mgr.updateAppWidget(widgetId, views)
    }

    // Hilfsmethoden für die alte Größenberechnung (Fallback)
    private fun getWidgetWidthInPx(context: Context, mgr: AppWidgetManager, widgetId: Int): Int {
        val density = context.getResources().getDisplayMetrics().density
        // Verwenden von maxW für Landscape und minW für Portrait
        val isPortrait = context.getResources()
            .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
        val widthDp = if (isPortrait)
            mgr.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        else
            mgr.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
        return (widthDp * density).toInt()
    }

    private fun getWidgetHeightInPx(context: Context, mgr: AppWidgetManager, widgetId: Int): Int {
        val density = context.getResources().getDisplayMetrics().density
        val isPortrait = context.getResources()
            .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
        val heightDp = if (isPortrait)
            mgr.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
        else
            mgr.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        return (heightDp * density).toInt()
    }

    private fun ToDoupdateWidget(context: Context, mgr: AppWidgetManager, widgetId: Int) {
        Log.i("WIDGET", "⟳ onReceive() – action= updateWidget")

        // WICHTIG → Pegel überprüfen, auch wenn App nicht läuft
        //ToDo: testen PegelLogic.run(context);
        val views = RemoteViews(context.getPackageName(), R.layout.widget_pegel)

        // 🔥 Widget-Größe ermitteln
        val options = mgr.getAppWidgetOptions(widgetId)

        val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val maxW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
        val minH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        val maxH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)

        Log.i(
            "WIDGET",
            "Größe dp → minW=" + minW + " maxW=" + maxW + " minH=" + minH + " maxH=" + maxH
        )

        // px berechnen (dp → px)
        val density = context.getResources().getDisplayMetrics().density
        var widthPx = (maxW * density).toInt()
        var heightPx = (maxH * density).toInt()

        if (widthPx < 100) widthPx = 300 // falls System keine Daten liefert

        if (heightPx < 100) heightPx = 130

        Log.i("WIDGET", "Gerenderte Bitmapgröße px: " + widthPx + " x " + heightPx)


        // Graph aus Cache erzeugen
        val bmp = makePegelBitmap(context, widthPx, heightPx)
        views.setImageViewBitmap(R.id.widgetChartImage, bmp)

        mgr.updateAppWidget(widgetId, views)
        // danach Widget-UI aktualisieren
        //ToDo: löschen WidgetUpdater.updateWidgetViews(context, mgr, appWidgetIds);
        //ComponentName thisWidget = new ComponentName(context, PegelWidget.class);
        //int[] ids = mgr.getAppWidgetIds(thisWidget);
        //WidgetUpdater.updateWidgetViews(context, mgr, ids);
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (UPDATE_ACTION == intent.getAction()) {
            Log.i("WIDGET", "⟳ Widget Update ausgelöst – lade Pegel…")

            // Erst API ausführen → speichert Daten → sendet Broadcast
            //ToDo: testen PegelLogic.run(context);

            // Widget neu zeichnen
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(
                ComponentName(context, PegelWidget::class.java)
            )
            for (id in ids) {
                Log.i("WIDGET", "updateWidget() CALLER = onReceive(" + intent.getAction() + ")")
                updateWidget(context, mgr, id)
            }

            // nächsten Update setzen
            scheduleNextUpdate(context)
        }
    }

    private fun scheduleNextUpdate(context: Context) {
        val minutes = context
            .getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getInt("widget_interval", 30) // 15/30/45/60

        val triggerAt = System.currentTimeMillis() + minutes * 60 * 1000

        val i = Intent(context, PegelWidget::class.java)
        i.setAction(UPDATE_ACTION)

        val pi = PendingIntent.getBroadcast(
            context,
            0,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pi
        )

        Log.i("WIDGET", "⏰ Widget-Update geplant in " + minutes + " Min")
    }

    companion object {
        const val UPDATE_ACTION: String = "de.wiesenfarth.mainpegel.UPDATE_WIDGET"
    }
}

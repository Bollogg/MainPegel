package de.net.wiesenfarth.mainpegel.Widget

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
import androidx.core.content.ContextCompat
import de.net.wiesenfarth.mainpegel.Graph.PegelBitmapGenerator
import de.net.wiesenfarth.mainpegel.MainActivity
import de.net.wiesenfarth.mainpegel.R
import java.lang.Float

/*******************************************************
 * Programm:  PegelWidget
 *
 * Beschreibung:
 * AppWidgetProvider für das Pegel-AppWidget.
 *
 * Diese Klasse steuert das Verhalten des Widgets
 * auf dem Android-Homescreen.
 *
 * Aufgaben:
 * - Aktualisieren des Widgets bei Systemereignissen
 * - Darstellen der Pegelgrafik
 * - Anzeigen der letzten Messwerte
 * - Reagieren auf Broadcast-Updates
 * - Starten der App bei Klick auf das Widget
 * - Ermitteln der Widgetgröße
 *
 * Das Widget funktioniert unabhängig von der App
 * und kann auch aktualisiert werden, wenn die
 * Haupt-App nicht geöffnet ist.
 *
 * Abhängigkeiten:
 * - PegelLogic (Datenabruf)
 * - PegelBitmapGenerator (Diagramm)
 * - SharedPreferences (Cache + Einstellungen)
 *
 * Autor:     Bollogg
 * Datum:     2026-03-15
 *******************************************************/
class PegelWidget : AppWidgetProvider() {

    /**
     * Wird vom System aufgerufen, wenn das Widget
     * aktualisiert werden soll (z.B. beim Platzieren
     * des Widgets oder bei einem Systemupdate).
     */
    override fun onUpdate(context: Context, mgr: AppWidgetManager, appWidgetIds: IntArray) {

        // Mehrere Widgets gleichzeitig möglich
        for (id in appWidgetIds) {
            Log.i("WIDGET", "updateWidget() CALLER = onUpdate()")

            updateWidget(context, mgr, id)
        }
    }

    /**
     * Aktualisiert ein einzelnes Widget.
     *
     * Aufgaben:
     * - Layout erstellen (RemoteViews)
     * - Widgetgröße bestimmen
     * - Grafik erzeugen
     * - Werte anzeigen
     * - Klickaktion setzen
     */
    private fun updateWidget(context: Context, mgr: AppWidgetManager, widgetId: Int) {

        Log.i("WIDGET", "⟳ updateWidget() für widgetId: $widgetId")

        // Widget-Layout laden
        val views = RemoteViews(context.packageName, R.layout.widget_pegel)

        /**
         * Ab Android 12 kann das System mehrere mögliche Widgetgrößen liefern.
         * Hier wird die größte Größe ausgewählt, damit das Diagramm optimal
         * dargestellt werden kann.
         */
        val viewMapping: MutableMap<SizeF?, RemoteViews?>? =
            mgr.getAppWidgetOptions(widgetId).getParcelable(AppWidgetManager.OPTION_APPWIDGET_SIZES)

        var maxSize: SizeF? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && viewMapping != null && !viewMapping.isEmpty()) {

            // Größte verfügbare Widgetgröße ermitteln
            maxSize = viewMapping.keys.stream()
                .max(Comparator { size1: SizeF?, size2: SizeF? ->
                    Float.compare(
                        size1!!.width * size1.height,
                        size2!!.width * size2.height
                    )
                })
                .orElse(null)
        }

        var widthPx: Int
        var heightPx: Int

        /**
         * Falls moderne Größeninformationen verfügbar sind → verwenden
         * sonst Fallback über alte Methode.
         */
        if (maxSize != null) {

            widthPx = maxSize.width.toInt()
            heightPx = maxSize.height.toInt()

        } else {

            // Fallback für ältere Android-Versionen
            widthPx = getWidgetWidthInPx(context, mgr, widgetId)
            heightPx = getWidgetHeightInPx(context, mgr, widgetId)

        }

        // Sicherheitsfallback bei ungültigen Größen
        if (widthPx <= 0) widthPx = 300
        if (heightPx <= 0) heightPx = 130

        /**
         * Pegelgrafik erzeugen
         * (Diagramm wird als Bitmap gerendert)
         */
        val bmp = PegelBitmapGenerator.makePegelBitmap(context, widthPx, heightPx)

        if (bmp != null) {

            views.setImageViewBitmap(R.id.widgetChartImage, bmp)

        } else {

            // Fallback-Bild wenn keine Grafik verfügbar ist
            views.setImageViewResource(R.id.widgetChartImage, R.mipmap.ic_launcher_aal_round)

        }

        /**
         * Klick auf Widget → App starten
         */
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Klickaktion setzen
        views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

        /**
         * Letzte Messwerte aus Cache laden
         */
        val cache = context.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)

        val correntValue = cache.getInt("last_value", -1)
        val correntTemp = cache.getFloat("last_temp", 0F)
        val time: String = cache.getString("last_time", "--:--")!!

        /**
         * Farben für die Anzeige setzen
         */
        val colorBlue = ContextCompat.getColor(context, R.color.lineColor)
        val colorRed = ContextCompat.getColor(context, R.color.tempLineColor)

        // Pegelwert anzeigen
        views.setTextColor(R.id.widget_pegelwert, colorBlue)
        views.setTextViewText(R.id.widget_pegelwert, "Pegel: $correntValue cm")

        // Wassertemperatur anzeigen
        views.setTextColor(R.id.widget_temp, colorRed)
        views.setTextViewText(R.id.widget_temp, "Wasser: $correntTemp °C")

        // Zeitstempel anzeigen
        views.setTextViewText(R.id.widget_timestamp, "Stand: $time Uhr")

        // Widget aktualisieren
        mgr.updateAppWidget(widgetId, views)
    }

    /**
     * Ermittelt die Widgetbreite in Pixeln
     * (Fallback für ältere Android-Versionen)
     */
    private fun getWidgetWidthInPx(context: Context, mgr: AppWidgetManager, widgetId: Int): Int {

        val density = context.resources.displayMetrics.density

        val isPortrait =
            context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        val widthDp = if (isPortrait)
            mgr.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        else
            mgr.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)

        return (widthDp * density).toInt()
    }

    /**
     * Ermittelt die Widgethöhe in Pixeln
     * (Fallback für ältere Android-Versionen)
     */
    private fun getWidgetHeightInPx(context: Context, mgr: AppWidgetManager, widgetId: Int): Int {

        val density = context.resources.displayMetrics.density

        val isPortrait =
            context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        val heightDp = if (isPortrait)
            mgr.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
        else
            mgr.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

        return (heightDp * density).toInt()
    }

    /**
     * Empfang von Broadcast-Nachrichten.
     * Wird verwendet, wenn neue Pegeldaten
     * verfügbar sind.
     */
    override fun onReceive(context: Context, intent: Intent) {

        super.onReceive(context, intent)

        if (intent.action == ACTION_DATA_UPDATED) {

            // Alle Widgets aktualisieren
            updateAllWidgets(context)

            Log.i("Widget/PegelWidget", "onReceive: " + intent.action)

        }
    }

    /**
     * Aktualisiert alle vorhandenen Widgets
     */
    private fun updateAllWidgets(context: Context) {

        val mgr = AppWidgetManager.getInstance(context)

        val ids = mgr.getAppWidgetIds(
            ComponentName(context, PegelWidget::class.java)
        )

        for (id in ids) {

            updateWidget(context, mgr, id)

        }
    }

    /**
     * Plant ein zukünftiges Widget-Update
     * über den Android AlarmManager.
     *
     * Das Intervall wird aus den Einstellungen geladen.
     */
    private fun scheduleNextUpdate(context: Context) {

        val minutes = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getInt("widget_interval", 30)

        val triggerAt = System.currentTimeMillis() + minutes * 60 * 1000

        val i = Intent(context, PegelWidget::class.java).apply {
            action = ACTION_UPDATE_REQUEST
        }

        val pi = PendingIntent.getBroadcast(
            context,
            0,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        /**
         * Ab Android 12 benötigt man eine spezielle Berechtigung
         * für exakte Alarme.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {

            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)

        } else {

            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)

        }
    }

    /**
     * Broadcast-Aktionen für das Widget
     */
    companion object {

        // Widget fordert ein neues Update an
        const val ACTION_UPDATE_REQUEST =
            "de.net.wiesenfarth.mainpegel.UPDATE_REQUEST"

        // Neue Pegeldaten sind verfügbar
        const val ACTION_DATA_UPDATED =
            "de.net.wiesenfarth.mainpegel.DATA_UPDATED"
    }
}
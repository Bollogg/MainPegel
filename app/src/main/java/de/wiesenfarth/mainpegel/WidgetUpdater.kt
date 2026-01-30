package de.wiesenfarth.mainpegel

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews

/*******************************************************
 * Programm:  WidgetUpdater
 *
 * Beschreibung:
 * Hilfsklasse für das Aktualisieren der Widget-Ansichten.
 *
 * Diese Klasse wird sowohl vom AppWidgetProvider (PegelWidget)
 * als auch vom WidgetUpdateService aufgerufen, um die Anzeige
 * des Widgets mit den zwischengespeicherten Pegeldaten zu aktualisieren.
 *
 * Sie liest Daten aus SharedPreferences ("pegel_cache") und schreibt
 * diese in die RemoteViews des Widgets.
 * @Website:   https://www.wiesenfarth-net.de
 * @Autor:     Bollog
 * @Datum:     2025-12-02
 * @Version:   2025.12
 */
object WidgetUpdater {
    /**
     * Aktualisiert die RemoteViews aller übergebenen Widget-Instanzen.
     *
     * @param context       Kontext der Anwendung (Service oder AppWidgetProvider)
     * @param mgr           AppWidgetManager zur Steuerung der Widget-Instanzen
     * @param appWidgetIds  Liste der zu aktualisierenden Widget-IDs
     */
    fun updateWidgetViews(context: Context, mgr: AppWidgetManager, appWidgetIds: IntArray) {
        // Daten aus SharedPreferences laden

        val cache = context.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)

        val value = cache.getInt("last_value", -1) // letzter Pegelwert
        val time: String = cache.getString("last_time", "—")!! // zugehöriger Zeitstempel

        // Für jede Widget-Instanz Ansicht aktualisieren
        for (id in appWidgetIds) {
            // RemoteViews für das Widget-Layout erzeugen

            val views = RemoteViews(context.getPackageName(), R.layout.widget_pegel)

            // Text für Pegel und Zeit zusammenstellen
            val txt = "Pegel: " + value + " cm\nZeit: " + time

            // Text im Widget setzen
            views.setTextViewText(R.id.widget_pegelwert, txt)

            // Widget aktualisieren
            mgr.updateAppWidget(id, views)
        }
    }
}

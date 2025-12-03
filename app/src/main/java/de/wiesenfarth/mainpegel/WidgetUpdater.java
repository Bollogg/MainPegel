package de.wiesenfarth.mainpegel;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
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
 *******************************************************/

public class WidgetUpdater {

  /**
   * Aktualisiert die RemoteViews aller übergebenen Widget-Instanzen.
   *
   * @param context       Kontext der Anwendung (Service oder AppWidgetProvider)
   * @param mgr           AppWidgetManager zur Steuerung der Widget-Instanzen
   * @param appWidgetIds  Liste der zu aktualisierenden Widget-IDs
   */
  public static void updateWidgetViews(Context context, AppWidgetManager mgr, int[] appWidgetIds) {

    // Daten aus SharedPreferences laden
    SharedPreferences cache = context.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE);

    int value = cache.getInt("last_value", -1);     // letzter Pegelwert
    String time = cache.getString("last_time", "—"); // zugehöriger Zeitstempel

    // Für jede Widget-Instanz Ansicht aktualisieren
    for (int id : appWidgetIds) {

      // RemoteViews für das Widget-Layout erzeugen
      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_pegel);

      // Text für Pegel und Zeit zusammenstellen
      String txt = "Pegel: " + value + " cm\nZeit: " + time;

      // Text im Widget setzen
      views.setTextViewText(R.id.widget_pegelwert, txt);

      // Widget aktualisieren
      mgr.updateAppWidget(id, views);
    }
  }
}

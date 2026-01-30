package de.wiesenfarth.mainpegel

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import de.wiesenfarth.mainpegel.PegelBitmapGenerator.makePegelBitmap

class WidgetUpdateService : Service() {
    //ToDo: prüfen ob noch gebraucht? sonst löschen
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("WIDGET", "WidgetUpdateService gestartet")
        updateWidget()

        stopSelf(startId)
        return START_NOT_STICKY
    }

    private fun updateWidget() {
        try {
            val manager = AppWidgetManager.getInstance(this)
            val widget = ComponentName(this, PegelWidget::class.java)

            // Pegelwerte aus cache lesen
            val prefs = getSharedPreferences("pegel_cache", MODE_PRIVATE)
            val value = prefs.getInt("last_value", -1)
            val time: String = prefs.getString("last_time", "--:--")!!

            // Widget-Layout laden
            val views = RemoteViews(getPackageName(), R.layout.widget_pegel)

            // ---- Bitmap erzeugen (z.B. Pegel-Grafik) ----
            val scale = getResources().getDisplayMetrics().density
            val width = (320 * scale).toInt()
            val height = (110 * scale).toInt()
            val bitmap = makePegelBitmap(this, width, height)
            if (bitmap != null) {
                views.setImageViewBitmap(R.id.widgetChartImage, bitmap)
            }

            // Pegel anzeigen
            if (value >= 0) {
                views.setTextViewText(R.id.widget_pegelwert, value.toString() + " cm")
            } else {
                views.setTextViewText(R.id.widget_pegelwert, "-- cm")
            }

            views.setTextViewText(R.id.widget_timestamp, time)

            // Widget aktualisieren
            manager.updateAppWidget(widget, views)

            Log.i("WIDGET", "Widget aktualisiert: " + value + " cm")
        } catch (e: Exception) {
            Log.e("WIDGET", "Fehler beim Widget-Update", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

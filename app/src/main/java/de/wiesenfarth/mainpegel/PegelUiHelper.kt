package de.wiesenfarth.mainpegel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

object PegelUiHelper {
    /*******************************************************
     * ladePegelstand
     *
     * Lädt die Pegeldaten über Retrofit vom Server
     */
    fun ladePegelstand(
        ctx: Context,
        textView: TextView,
        chart: LineChart,
        prefs: SharedPreferences
    ) {
        PegelLogic.run(ctx) // API starten und Daten abholen

        val cache = ctx.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)

        val value = cache.getInt("last_value", -1)
        val time: String = cache.getString("last_time", "--:--")!!

        val count = cache.getInt("count", 0)

        val list: MutableList<PegelResponse> = ArrayList<PegelResponse>()
        for (i in 0..<count) {
            val v = cache.getInt("value_" + i, -1)
            val ts: String = cache.getString("timestamp_" + i, "--")!!
            list.add(PegelResponse(ts, v))
        }
        val prev = list.get(list.size - 2)
        val prevValue = prev.value
        // Text anzeigen
        if (value >= 0) {
            textView.setText("Pegel alt:  " + prevValue + " cm\nPegel neu:  " + value + " cm\nAktuelle Messung um:  " + time + " Uhr")
        } else {
            textView.setText("Keine Daten")
        }

        // Graph zeichnen
        val hours = prefs.getInt("graph_hours", 4)
        aktualisiereGraph(ctx, chart, list, hours)
        forceWidgetUpdate(ctx)
    }

    /*******************************************************
     * aktualisiereGraph
     *
     * Zeichnet den Verlauf des Pegels als MPAndroidChart
     */
    private fun aktualisiereGraph(
        ctx: Context, lineChart: LineChart,
        daten: MutableList<PegelResponse>, hours: Int
    ) {
        // Y-Achse links

        val left = lineChart.getAxisLeft()
        left.setTextColor(ContextCompat.getColor(ctx, R.color.textColor))
        left.setAxisLineColor(ContextCompat.getColor(ctx, R.color.axisColor))
        left.setGridColor(ContextCompat.getColor(ctx, R.color.gridColor))
        lineChart.getAxisRight().setEnabled(false)

        // Hintergrundfarben
        lineChart.setBackgroundColor(ContextCompat.getColor(ctx, R.color.backgroundColor))
        lineChart.setDrawGridBackground(false)

        // Legende
        lineChart.getLegend().setTextColor(ContextCompat.getColor(ctx, R.color.legendTextColor))

        // Beschreibung ausblenden
        lineChart.getDescription().setEnabled(false)

        // --- Daten vorbereiten ---
        val entries: MutableList<Entry?> = ArrayList<Entry?>()
        val xLabels: MutableList<String?> = ArrayList<String?>()

        val apiFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMANY)
        val displayFormat = SimpleDateFormat("HH:mm", Locale.GERMANY)

        // Datenpunkte erzeugen
        for (i in daten.indices) {
            val p = daten.get(i)

            try {
                val d = apiFormat.parse(p.timestamp)
                entries.add(Entry(i.toFloat(), p.value.toFloat()))
                xLabels.add(displayFormat.format(d))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Dataset konfigurieren
        val dataSet = LineDataSet(
            entries,
            ctx.getString(R.string.level_curve) + " (" + hours + "h)"
        )

        dataSet.setLineWidth(2f)
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)

        // Farben aus Ressourcen (Tag/Nacht)
        dataSet.setColor(ContextCompat.getColor(ctx, R.color.lineColor))
        dataSet.setDrawFilled(true)
        dataSet.setFillColor(ContextCompat.getColor(ctx, R.color.fillColor))

        // Linien setzen
        lineChart.setData(LineData(dataSet))

        // X-Achse konfigurieren
        val xAxis = lineChart.getXAxis()
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
        xAxis.setGranularity(1f)
        xAxis.setLabelRotationAngle(-45f)

        xAxis.setTextColor(ContextCompat.getColor(ctx, R.color.textColor))
        xAxis.setAxisLineColor(ContextCompat.getColor(ctx, R.color.axisColor))
        xAxis.setGridColor(ContextCompat.getColor(ctx, R.color.gridColor))

        // Zeitlabels verwenden
        xAxis.setValueFormatter(IndexAxisValueFormatter(xLabels))

        // Aktualisieren
        lineChart.invalidate()
    }

    fun forceWidgetUpdate(ctx: Context) {
        try {
            val intent = Intent(ctx, PegelWidget::class.java)
            intent.setAction(PegelWidget.UPDATE_ACTION)
            ctx.sendBroadcast(intent)

            Log.i("WIDGET", "Widget-Update über Helper ausgelöst")
        } catch (e: Exception) {
            Log.e("WIDGET", "Fehler im Helper", e)
        }
    }
}

package de.net.wiesenfarth.mainpegel.Graph

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
import de.net.wiesenfarth.mainpegel.API.PegelLogic
import de.net.wiesenfarth.mainpegel.API.PegelResponse
import de.net.wiesenfarth.mainpegel.R
import de.net.wiesenfarth.mainpegel.Widget.PegelWidget
import java.text.SimpleDateFormat
import java.util.Locale

/*******************************************************
 * Objekt:  PegelUiHelper
 *
 * Beschreibung:
 * Zentrale Hilfsklasse für die UI-Darstellung der Pegeldaten.
 *
 * Aufgaben:
 * • Startet die Pegel-API-Abfrage
 * • Liest gecachte Pegeldaten aus SharedPreferences
 * • Aktualisiert TextView mit aktuellen Pegelwerten
 * • Zeichnet den Pegelverlauf als LineChart (MPAndroidChart)
 * • Triggert ein Widget-Update nach UI-Aktualisierung
 *
 * Verwendung:
 * Wird typischerweise aus Activities oder Fragments aufgerufen.
 *
 * Abhängigkeiten:
 * • PegelLogic (API / Cache)
 * • MPAndroidChart
 * • PegelWidget
 *
 * Autor:     Bollogg
 * Datum:     2026-01-30
 *******************************************************/
object PegelUiHelper {
	/**
	* Lädt den aktuellen Pegelstand und aktualisiert UI-Komponenten.
	*
	* Ablauf:
	* 1. Startet die Pegel-API (PegelLogic)
	* 2. Liest Pegeldaten aus dem Cache
	* 3. Aktualisiert TextView mit alten & neuen Werten
	* 4. Zeichnet den Pegelverlauf als Diagramm
	* 5. Erzwingt ein Widget-Update
	*
	* @param ctx      Context (Activity / Fragment)
	* @param textView TextView zur Anzeige der Pegelwerte
	* @param chart    LineChart zur grafischen Darstellung
	* @param prefs    SharedPreferences (UI-/User-Einstellungen)
	*/
	fun ladePegelstand(
		ctx: Context,
		textView: TextView,
		chart: LineChart,
		prefs: SharedPreferences
	)
  {
		// API starten und Daten in den Cache schreiben
    PegelLogic.run(ctx)

    val cache = ctx.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)

    val value = cache.getInt("last_value", -1)
    val time: String = cache.getString("last_time", "--:--")!!

    val count = cache.getInt("count", 0)

		// Zu wenig Daten → nichts anzeigen
    if (count < 2) {
      textView.text = "Zu wenig Pegeldaten"
      chart.clear()
      return
    }

		// Pegelverlauf aus Cache rekonstruieren
		val list: MutableList<PegelResponse> = ArrayList<PegelResponse>()
    for (i in 0..<count) {
      val v = cache.getInt("value_" + i, -1)
      val ts: String = cache.getString("timestamp_" + i, "--")!!
      list.add(PegelResponse(ts, v))
    }

		// Vorletzter Wert (Vergleich alt/neu)
    val prev = list.get(list.size - 2)
    val prevValue = prev.value

		// Textanzeige aktualisieren
    if (value >= 0) {
      textView.setText("Pegel alt:  " + prevValue + " cm\n " +
		      "Pegel neu:  " + value + " cm\n " +
		      "Aktuelle Messung um:  " + time + " Uhr")
    } else {
      textView.setText("Keine Daten")
    }

		// Diagramm aktualisieren
    val hours = prefs.getInt("graph_hours", 4)
    aktualisiereGraph(ctx, chart, list, hours)

		// Widget ebenfalls aktualisieren
    forceWidgetUpdate(ctx)
  }

	/**
	* Zeichnet den Pegelverlauf als Liniendiagramm (MPAndroidChart).
	*
	* @param ctx    Context für Ressourcen
	* @param chart LineChart-Instanz
	* @param daten Liste der Pegelwerte (Zeitstempel + Wert)
	* @param hours Zeitspanne des Diagramms (für Legende)
	*/
	private fun aktualisiereGraph(
		ctx: Context, lineChart: LineChart,
		daten: MutableList<PegelResponse>, hours: Int)
  {

		// Y-Achse links konfigurieren
    val left = lineChart.getAxisLeft()
    left.setTextColor(ContextCompat.getColor(ctx, R.color.textColor))
    left.setAxisLineColor(ContextCompat.getColor(ctx, R.color.axisColor))
    left.setGridColor(ContextCompat.getColor(ctx, R.color.gridColor))
    lineChart.axisRight.isEnabled = false

		// Diagramm-Erscheinungsbild
    lineChart.setBackgroundColor(ContextCompat.getColor(ctx, R.color.backgroundColor))
    lineChart.setDrawGridBackground(false)
    lineChart.getLegend().setTextColor(ContextCompat.getColor(ctx, R.color.legendTextColor))
    lineChart.description.isEnabled = false

		// Daten vorbereiten
    val entries = ArrayList<Entry>()
    val xLabels = ArrayList<String>()

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

		// Diagramm neu zeichnen
    lineChart.invalidate()
  }

	/**
	* Erzwingt ein sofortiges Update aller Pegel-Widgets.
	*
	* Wird nach UI-Aktualisierung aufgerufen, damit
	* App und Widget synchron bleiben.
	*/
  fun forceWidgetUpdate(ctx: Context) {
    try {
      val intent = Intent(ctx, PegelWidget::class.java)
      intent.setAction(PegelWidget.Companion.UPDATE_ACTION)
      ctx.sendBroadcast(intent)

      Log.i("WIDGET", "Widget-Update über Helper ausgelöst")
    } catch (e: Exception) {
      Log.e("WIDGET", "Fehler im Helper", e)
    }
  }
}
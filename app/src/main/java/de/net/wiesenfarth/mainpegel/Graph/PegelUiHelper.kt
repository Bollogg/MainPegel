package de.net.wiesenfarth.mainpegel.Graph

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.util.Log.i
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import de.net.wiesenfarth.mainpegel.API.PegelResponse
import de.net.wiesenfarth.mainpegel.API.TempResponse
import de.net.wiesenfarth.mainpegel.R
import de.net.wiesenfarth.mainpegel.Variable.getThemeColor
import de.net.wiesenfarth.mainpegel.Widget.PegelWidget
import de.net.wiesenfarth.mainpegel.Widget.PegelWidget.Companion.ACTION_DATA_UPDATED
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.min

/*******************************************************
 * Objekt:  PegelUiHelper
 *
 * Beschreibung:
 * Zentrale UI-Hilfsklasse zur Darstellung und
 * Synchronisierung von Pegeldaten innerhalb der App.
 *
 * Dieses Objekt kapselt sämtliche UI-relevanten Aufgaben:
 *
 * • Starten der API-Abfrage (über PegelLogic)
 * • Lesen der zuletzt gespeicherten Messwerte aus dem Cache
 * • Aktualisierung der Textanzeige (alt/neu-Vergleich)
 * • Rendering eines Pegelverlaufs mit MPAndroidChart
 * • Synchronisierung des AppWidgets mit der aktuellen UI
 *
 * Architekturprinzip:
 * - Die API speichert Daten ausschließlich im Cache
 * - Die UI liest nur aus dem Cache (keine Direktkopplung zur API)
 * - Dadurch bleiben App und Widget synchron
 *
 * Verwendet:
 * - SharedPreferences ("pegel_cache")
 * - MPAndroidChart (LineChart)
 * - PegelWidget (Broadcast-Update)
 *
 * Threading:
 * PegelLogic.run() arbeitet asynchron.
 * Die UI liest daher ggf. noch alte Cache-Werte.
 *
 * Autor:     Bollogg
 * Datum:     2026-01-30
 *******************************************************/
object PegelUiHelper {
	/**
	 * Lädt Pegeldaten und aktualisiert sämtliche UI-Komponenten.
	 *
	 * Ablauf:
	 * 1. API-Aufruf starten (asynchron)
	 * 2. Letzte Werte aus dem Cache lesen
	 * 3. Vergleich alt vs. neu berechnen
	 * 4. Textanzeige aktualisieren
	 * 5. Diagramm neu rendern
	 * 6. Widget-Update auslösen
	 *
	 * Hinweis:
	 * Die Methode ist UI-zentriert.
	 * Sie blockiert nicht auf die API-Antwort,
	 * sondern arbeitet mit den aktuell gespeicherten Cache-Daten.
	 *
	 * @param ctx      Context (Activity oder Fragment)
	 * @param textView TextView zur Anzeige der Messwerte
	 * @param chart    LineChart für den Verlauf
	 * @param prefs    SharedPreferences (Benutzereinstellungen)
	 */
	fun ladePegelstand(
		ctx: Context,
		textView: TextView,
		chart: LineChart,
		prefs: SharedPreferences
	)
  {
	  // Rekonstruiert PegelResponse-Objekte aus dem Cache.
		// Die API speichert Einzelwerte mit Index (value_i, timestamp_i).
		// Hier wird daraus wieder eine geordnete Liste erzeugt.
	  //PegelLogic.run(ctx)

    val cache = ctx.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)

    val value = cache.getInt("last_value", -1)
	  val temp = cache.getFloat("last_temp", -999f)
	  val time = cache.getString("last_time", "--:--") ?: "--:--"

    val count = cache.getInt("count", 0)

		// Pegelverlauf aus Cache rekonstruieren
	  val pegelList = mutableListOf<PegelResponse>()
	  // Temperaturverlauf aus Cache rekonstruieren
	  val tempList = mutableListOf<TempResponse>()

	  for (i in 0..<count) {
      val v = cache.getInt("value_" + i, -1)
	    val ts = cache.getString("timestamp_$i", "--") ?: "--"

	    if (v >= 0) {
		    pegelList.add(PegelResponse(ts, v))
	    }
    }

	  if (pegelList.size < 2) {
		  textView.text = "Zu wenig Pegeldaten"
		  chart.clear()
		  return
	  }

		// Vorletzter Wert (Vergleich alt/neu)
    val prev = pegelList.get(pegelList.size - 2)
    val prevValue = prev.value

		// Textanzeige aktualisieren
    if (value >= 0) {
	    textView.text = """
			Pegel alt: $prevValue cm
			Pegel neu: $value cm
			Wasser: $temp °C 
			Aktuelle Messung: $time Uhr
			""".trimIndent()

    } else {
      textView.setText("Keine Daten")
    }

		// Diagramm aktualisieren
    val hours = prefs.getInt("graph_hours", 4)
    aktualisiereGraph(ctx, chart, pegelList, hours)

		// Widget ebenfalls aktualisieren
    forceWidgetUpdate(ctx)
  }

	/**
	 * Zeichnet den Pegelverlauf in ein MPAndroidChart LineChart.
	 *
	 * Darstellung:
	 * - X-Achse: Zeit (HH:mm)
	 * - Y-Achse: Pegel in cm
	 * - Gefüllte Linie mit Farbverlauf
	 * - Keine Punkt-Markierungen
	 *
	 * @param ctx    Context (für Ressourcen & Farben)
	 * @param lineChart Ziel-Chart
	 * @param daten  Liste der Pegeldaten (chronologisch)
	 * @param hours  Zeitfenster des Diagramms (Anzeigezweck)
	 *
	 * Intern:
	 * - Zeitstempel werden vom API-Format
	 *   "yyyy-MM-dd'T'HH:mm:ss"
	 *   in
	 *   "HH:mm"
	 *   konvertiert.
	 */
	private fun aktualisiereGraph(
		ctx: Context, lineChart: LineChart,
		daten: MutableList<PegelResponse>, hours: Int)
  {

	  val cache = ctx.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)
	  val tempEntries = ArrayList<Entry>()

	  // Konfiguration der linken Y-Achse
		// Die rechte Achse ist deaktiviert (Single-Axis-Diagramm).
		// LINKe Achse (Pegel)
	  val left = lineChart.axisLeft
	  left.setTextColor(
		  ctx.getThemeColor(R.attr.graphText)
		)
	  left.setAxisLineColor(
		  ctx.getThemeColor(R.attr.colorBackground)
		)
	  left.setGridColor(
		  ctx.getThemeColor(R.attr.graphGrid)
		)
	  left.granularity = 1f
	  left.isEnabled = true

		// RECHTE Achse (Temperatur)
	  val right = lineChart.axisRight
	  right.isEnabled = true
	  right.setTextColor(
		  ctx.getThemeColor(R.attr.graphText)
		)
	  right.setAxisLineColor(
		  ctx.getThemeColor(R.attr.colorBackground)
		)
	  right.setGridColor(
		  ctx.getThemeColor(R.attr.graphGrid)
		)
	  right.granularity = 0.5f
	  right.setDrawGridLines(false)

		// Diagramm-Erscheinungsbild
	  lineChart.setBackgroundColor(
		  ctx.getThemeColor(R.attr.graphBackground)
	  )
    lineChart.setDrawGridBackground(false)
	  lineChart.getLegend().setTextColor(
		  ctx.getThemeColor(R.attr.graphText)
	  )
    lineChart.description.isEnabled = false

		// Daten vorbereiten
    val entries = ArrayList<Entry>()
    val xLabels = ArrayList<String>()

    val apiFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMANY)
    val displayFormat = SimpleDateFormat("HH:mm", Locale.GERMANY)

    // Datenpunkte erzeugen
	  for (i in daten.indices) {

		  val p = daten[i]

		  try {
			  val d = apiFormat.parse(p.timestamp)

			  // Pegel
			  entries.add(Entry(i.toFloat(), p.value.toFloat()))
			  xLabels.add(displayFormat.format(d))

			  // Temperatur aus Cache holen
			  val temp = cache.getFloat("temp_$i", Float.NaN)

			  if (!temp.isNaN()) {
				  tempEntries.add(Entry(i.toFloat(), temp))
			  }

		  } catch (e: Exception) {
			  e.printStackTrace()
		  }
	  }

	  Log.d("TEMP_DEBUG", "TempEntries size = ${tempEntries.size}")

	  // Dataset-Konfiguration:
		// - Linienbreite: 2dp
		// - Keine Kreispunkte
		// - Keine Zahlenwerte
		// - Gefüllte Fläche unter der Linie
		// - Farben abhängig vom Theme (Tag/Nacht)
    val dataSet = LineDataSet(
	    entries,
	    ctx.getString(R.string.level_curve) + " (" + hours + "h)"
    )
	  //Temperatur
	  dataSet.setLineWidth(2f)
    dataSet.setDrawCircles(false)
    dataSet.setDrawValues(false)

    // Farben aus Ressourcen (Tag/Nacht)
    dataSet.setColor(
	    ctx.getThemeColor(R.attr.graphLine)
		)
    dataSet.setDrawFilled(true)
    dataSet.setFillColor(
	    ctx.getThemeColor(R.attr.graphFill)
		)

		// Temperatur-Dataset
	  dataSet.axisDependency = YAxis.AxisDependency.LEFT

	  val tempDataSet = LineDataSet(tempEntries,
		                              ctx.getString(R.string.level_temperature) + " °C")
	  tempDataSet.axisDependency = YAxis.AxisDependency.RIGHT
	  tempDataSet.lineWidth = 2f
	  tempDataSet.setDrawCircles(false)
	  tempDataSet.setDrawValues(false)
	  tempDataSet.setColor(
		  ctx.getThemeColor(R.attr.graphTempLine)
		)
	  tempDataSet.setDrawFilled(true)
	  tempDataSet.setFillColor(
		  ctx.getThemeColor(R.attr.graphFillTemp)
	  )

	  // Linien setzen
	  val lineData = LineData(dataSet, tempDataSet)
	  lineChart.data = lineData

		// -------- Y-ACHSE LINKS (Pegel) --------
	  val leftAxis = lineChart.axisLeft
	  leftAxis.isEnabled = true
	  leftAxis.setDrawGridLines(true)
	  leftAxis.axisMinimum = dataSet.yMin - 5f
	  leftAxis.axisMaximum = dataSet.yMax + 5f
	  leftAxis.setLabelCount(6, true)

		// -------- Y-ACHSE RECHTS (Temperatur) --------
	  val rightAxis = lineChart.axisRight
	  rightAxis.isEnabled = true
	  rightAxis.setDrawGridLines(false)

	  if (tempEntries.isNotEmpty()) {
		  val tempMin = tempEntries.minOf { it.y }
		  val tempMax = tempEntries.maxOf { it.y }

		  rightAxis.axisMinimum = tempMin - 1f
		  rightAxis.axisMaximum = tempMax + 1f
	  } else {
		  rightAxis.axisMinimum = 0f
		  rightAxis.axisMaximum = 10f
	  }

	  rightAxis.setLabelCount(6, true)


	  // X-Achse konfigurieren
    val xAxis = lineChart.getXAxis()
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
    xAxis.setGranularity(1f)
	  xAxis.setLabelCount(min(xLabels.size, 6), true)

	  xAxis.setLabelRotationAngle(-45f)

    xAxis.setTextColor(
	    ctx.getThemeColor(R.attr.graphText)
		)
    xAxis.setAxisLineColor(
	    ctx.getThemeColor(R.attr.graphAxis)
		)
    xAxis.setGridColor(
	    ctx.getThemeColor(R.attr.graphGrid)
		)

    // Zeitlabels verwenden
    xAxis.setValueFormatter(IndexAxisValueFormatter(xLabels))

		// Diagramm neu zeichnen
    lineChart.invalidate()
  }

	/**
	 * Sendet einen Broadcast zur sofortigen Aktualisierung
	 * aller Pegel-Widgets.
	 *
	 * Zweck:
	 * - Synchronisiert Widget und App-Oberfläche
	 * - Verhindert veraltete Widget-Daten
	 *
	 * Technisch:
	 * - Sendet UPDATE_ACTION an PegelWidget
	 * - Das Widget zeichnet sich daraufhin neu
	 *
	 * Achtung:
	 * Diese Methode darf keine API-Aufrufe triggern,
	 * sonst entsteht eine Broadcast-Schleife.
	 */
  fun forceWidgetUpdate(ctx: Context) {
    try {
      val intent = Intent(ctx, PegelWidget::class.java)
	    intent.action = ACTION_DATA_UPDATED
      ctx.sendBroadcast(intent)

      Log.i("WIDGET", "Widget-Update über Helper ausgelöst")
    } catch (e: Exception) {
      Log.e("WIDGET", "Fehler im Helper", e)
    }
  }
}
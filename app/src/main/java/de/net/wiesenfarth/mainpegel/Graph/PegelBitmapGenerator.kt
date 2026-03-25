package de.net.wiesenfarth.mainpegel.Graph

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.core.content.ContextCompat
import de.net.wiesenfarth.mainpegel.R
import de.net.wiesenfarth.mainpegel.Variable.getThemeColor
import java.util.Locale
/*******************************************************
 * Objekt:  PegelBitmapGenerator
 *
 * Beschreibung:
 * Dieses Objekt erzeugt ein Bitmap mit einem Diagramm
 * zur Darstellung des zeitlichen Verlaufs von
 *
 * • Pegelwerten (cm)
 * • Temperaturwerten (°C)
 *
 * Das erzeugte Bitmap wird z.B. verwendet für:
 *
 * • AppWidgets
 * • statische Diagrammansichten
 * • Hintergrundgrafiken in der UI
 *
 * Datenquelle:
 * Die Messwerte werden aus dem lokalen Cache gelesen:
 *
 * SharedPreferences("pegel_cache")
 *
 * Gespeicherte Datenstruktur:
 *
 * count          -> Anzahl der gespeicherten Messwerte
 * value_i        -> Pegelwert (cm)
 * temp_i         -> Temperatur (°C)
 * Time_i         -> Zeitstempel (String)
 *
 * Grafikaufbau:
 *
 * Das Diagramm besteht aus:
 *
 * • X-Achse (Zeit)
 * • Y-Achse links  → Pegelwerte
 * • Y-Achse rechts → Temperaturwerte
 *
 * Linien:
 *
 * • Pegelverlauf       → Farbe aus graphLine
 * • Temperaturverlauf  → Rot
 *
 * Skalierung:
 *
 * Die Achsen werden automatisch an die vorhandenen
 * Minimal- und Maximalwerte angepasst.
 *
 * Konfiguration:
 *
 * Darstellungseigenschaften wie
 *
 * • Ränder
 * • Linienstärke
 * • Achsenstärke
 *
 * werden über PegelGraphConfig gesteuert.
 *
 * Architekturprinzip:
 *
 * - Daten werden ausschließlich aus dem Cache gelesen
 * - Keine direkte Kopplung zur API
 * - Dadurch identische Datenbasis für
 *   App, Widget und Diagramm
 *
 * Fehlerfälle:
 *
 * Wenn keine Pegeldaten vorhanden sind,
 * wird ein Bitmap mit dem Text
 *
 * "Keine Daten"
 *
 * erzeugt.
 *
 * Autor:     Bollogg
 * Datum:     2026-03-13
 *******************************************************/
object PegelBitmapGenerator {

	@JvmStatic
	fun makePegelBitmap(
		context: Context,
		width: Int,
		height: Int,
		config: PegelGraphConfig = PegelGraphConfig()
	): Bitmap {

		// Bitmap erzeugen
		val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		val canvas = Canvas(bitmap)

		// Hintergrundfarbe setzen
		canvas.drawColor(ContextCompat.getColor(context, R.color.graph_background))

		// Pegel-Cache laden
		val prefs = context.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)
		val count = prefs.getInt("count", 0)

		val levelValues = ArrayList<Float>()
		val tempValues = ArrayList<Float>()
		val timeValues = ArrayList<String>()

		// Messwerte aus dem Cache lesen
		for (i in 0 until count) {

			val level = prefs.getInt("value_$i", -1)

			if (level >= 0) {
				levelValues.add(level.toFloat())
				timeValues.add(prefs.getString("Time_$i", "") ?: "")

				val temp = prefs.getFloat("temp_$i", -999f)
				if (temp > -100) tempValues.add(temp)
			}
		}

		// Falls keine Daten vorhanden sind
		if (levelValues.isEmpty()) {
			val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
				color = Color.WHITE
				textSize = 40f
			}

			canvas.drawText("Keine Daten", 20f, height / 2f, paint)
			return bitmap
		}

		// Diagrammfläche berechnen
		val graphLeft = config.marginLeft
		val graphRight = width - config.marginRight
		val graphTop = config.marginTop
		val graphBottom = height - config.marginBottom

		val graphWidth = graphRight - graphLeft
		val graphHeight = graphBottom - graphTop

		// Achsen zeichnen
		val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
			color = Color.WHITE
			strokeWidth = config.axisStroke
		}

		canvas.drawLine(graphLeft, graphBottom, graphRight, graphBottom, axisPaint) // X-Achse
		canvas.drawLine(graphLeft, graphTop, graphLeft, graphBottom, axisPaint)     // Pegel-Achse (links)
		canvas.drawLine(graphRight, graphTop, graphRight, graphBottom, axisPaint)   // Temperatur-Achse (rechts)

		val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
			color = Color.WHITE
			textSize = 20f
		}

		// --- Pegel-Achse (links) ---
		textPaint.textAlign = Paint.Align.RIGHT

		val levelMin = levelValues.min().let {
			if (it == levelValues.max()) it - 5f else it
		}

		val levelMax = levelValues.max().let {
			if (it == levelValues.min()) it + 5f else it
		}
		
		val levelMid = (levelMax + levelMin) / 2f

		canvas.drawText("${levelMax.toInt()} cm", graphLeft - 10f, graphTop + 15f, textPaint)
		canvas.drawText("${levelMid.toInt()} cm", graphLeft - 10f, (graphTop + graphBottom) / 2f + 7f, textPaint)
		canvas.drawText("${levelMin.toInt()} cm", graphLeft - 10f, graphBottom, textPaint)

		// --- Temperatur-Achse (rechts) ---
		var tempMin = 0f
		var tempMax = 0f

		if (tempValues.isNotEmpty()) {

			textPaint.textAlign = Paint.Align.LEFT

			tempMin = tempValues.min().let {
				if (it == tempValues.max()) it - 2f else it
			}

			tempMax = tempValues.max().let {
				if (it == tempValues.min()) it + 2f else it
			}
			
			val tempMid = (tempMax + tempMin) / 2f

			val maxLabel = String.format(Locale.GERMANY, "%.1f °C", tempMax)
			val midLabel = String.format(Locale.GERMANY, "%.1f °C", tempMid)
			val minLabel = String.format(Locale.GERMANY, "%.1f °C", tempMin)

			canvas.drawText(maxLabel, graphRight + 10f, graphTop + 15f, textPaint)
			canvas.drawText(midLabel, graphRight + 10f, (graphTop + graphBottom) / 2f + 7f, textPaint)
			canvas.drawText(minLabel, graphRight + 10f, graphBottom, textPaint)
		}

		// Abstand der Punkte auf der X-Achse
		val stepX = if (levelValues.size > 1)
			graphWidth / (levelValues.size - 1)
		else
			graphWidth

		// --- Pegel-Linie ---
		val levelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
			//color = context.getThemeColor(R.attr.graphLine)
			color = ContextCompat.getColor(context, R.color.graph_line)
			strokeWidth = config.lineStroke
			style = Paint.Style.STROKE
		}

		for (i in 0 until levelValues.size - 1) {

			val x1 = graphLeft + stepX * i
			val y1 = graphBottom - ((levelValues[i] - levelMin) / (levelMax - levelMin) * graphHeight)

			val x2 = graphLeft + stepX * (i + 1)
			val y2 = graphBottom - ((levelValues[i + 1] - levelMin) / (levelMax - levelMin) * graphHeight)

			canvas.drawLine(x1, y1, x2, y2, levelPaint)
		}

		// --- Temperatur-Linie ---
		if (tempValues.isNotEmpty()) {

			val tempPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
				color = Color.RED
				strokeWidth = config.lineStroke
				style = Paint.Style.STROKE
			}

			for (i in 0 until tempValues.size - 1) {

				val x1 = graphLeft + stepX * i
				val y1 = graphBottom - ((tempValues[i] - tempMin) / (tempMax - tempMin) * graphHeight)

				val x2 = graphLeft + stepX * (i + 1)
				val y2 = graphBottom - ((tempValues[i + 1] - tempMin) / (tempMax - tempMin) * graphHeight)

				canvas.drawLine(x1, y1, x2, y2, tempPaint)
			}
		}

		// --- Zeitachse (X) ---
		textPaint.textAlign = Paint.Align.CENTER
		textPaint.textSize = 18f

		val labelStep =
			if (timeValues.size > 6) timeValues.size / 4
			else 1

		for (i in timeValues.indices step labelStep) {

			val x = graphLeft + stepX * i
			val label = timeValues[i].substringBefore(":")

			canvas.drawText(label, x, graphBottom + 45f, textPaint)
		}
		
		// Sicherstellen, dass der LETZTE Zeitstempel immer ganz rechts angezeigt wird
		if (timeValues.isNotEmpty() && (timeValues.size - 1) % labelStep != 0) {
			val x = graphLeft + graphWidth
			val label = timeValues.last().substringBefore(":")
			canvas.drawText(label, x, graphBottom + 45f, textPaint)
		}

		return bitmap
	}
}
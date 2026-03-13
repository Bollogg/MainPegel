package de.net.wiesenfarth.mainpegel.Graph

import android.graphics.Color
import de.net.wiesenfarth.mainpegel.R

/*******************************************************
 * Daten-Klasse:  PegelGraphConfig
 *
 * Beschreibung:
 * Konfigurationsklasse für die grafische Darstellung.
 *******************************************************/

data class PegelGraphConfig(
	val backgroundColor: Int = Color.parseColor("#202020"),
	val lineColor: Int = Color.parseColor("#4DB6FF"),
	val axisColor: Int = Color.GRAY,
	val pointColor: Int = Color.WHITE,

	val marginLeft: Float = 90f,   // Genug Platz für "166 cm"
	val marginTop: Float = 30f,
	val marginRight: Float = 80f,  // VIEL Platz für Temperatur-Beschriftung rechts
	val marginBottom: Float = 80f,  // VIEL Platz für die Zeit unten

	val axisStroke: Float = 3f,
	val lineStroke: Float = 4f,
	val pointRadius: Float = 5f,

	val emptyTextRes: Int = R.string.graph_no_data,
	val emptyTextSize: Float = 40f,
	val emptyTextX: Float = 20f,
	val emptyTextY: Float = 120f
)

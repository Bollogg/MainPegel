package de.net.wiesenfarth.mainpegel.Graph

import android.graphics.Color
import de.net.wiesenfarth.mainpegel.R
/*******************************************************
 * Daten-Klasse:  PegelGraphConfig
 *
 * Beschreibung:
 * Konfigurationsklasse für die grafische Darstellung
 * des Pegelverlaufs im Bitmap-Diagramm.
 *
 * Diese Datenklasse kapselt alle visuell relevanten
 * Parameter (Farben, Abstände, Linienstärken, Texte),
 * um Hardcoding im Zeichen-Code zu vermeiden.
 *
 * Sie ermöglicht:
 * - Zentrale Anpassung des Diagramm-Layouts
 * - Leichte Erweiterung (Themes, Dark Mode, Styles)
 * - Wiederverwendbarkeit (Widget, Activity, Preview)
 *
 * Verwendung:
 * Wird als optionaler Parameter an den
 * PegelBitmapGenerator übergeben.
 *
 * @JvmStatic
 * 	 fun makePegelBitmap(context: Context,
 * 	                     width  : Int,
 * 	                     height : Int,
 * 	                     config : PegelGraphConfig = PegelGraphConfig()
 * 	 ): Bitmap {
 *
 * Autor:     Bollogg
 * Datum:     2026-01-30
 *******************************************************/

data class PegelGraphConfig(
	val backgroundColor: Int = Color.parseColor("#202020"),
	val lineColor: Int = Color.parseColor("#4DB6FF"),
	val axisColor: Int = Color.GRAY,
	val pointColor: Int = Color.WHITE,

	val marginLeft: Float = 40f,
	val marginTop: Float = 20f,
	val marginRight: Float = 20f,
	val marginBottom: Float = 20f,

	val axisStroke: Float = 2f,
	val lineStroke: Float = 4f,
	val pointRadius: Float = 4f,

	val emptyTextRes: Int = R.string.graph_no_data,
	val emptyTextSize: Float = 40f,
	val emptyTextX: Float = 20f,
	val emptyTextY: Float = 120f
)
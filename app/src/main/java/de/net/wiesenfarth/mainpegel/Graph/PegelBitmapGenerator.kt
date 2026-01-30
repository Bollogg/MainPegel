package de.net.wiesenfarth.mainpegel.Graph

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.core.content.ContextCompat
import de.net.wiesenfarth.mainpegel.R

/*******************************************************
 * Programm:  PegelBitmapGenerator
 *
 * Beschreibung:
 * Diese Klasse erzeugt ein Bitmap mit einem einfachen
 * Liniendiagramm zur Darstellung eines Pegelverlaufs.
 *
 * Die Pegeldaten werden aus den SharedPreferences
 * ("pegel_cache") gelesen und grafisch aufbereitet.
 * Das erzeugte Bitmap ist u.a. für die Anzeige in
 * App-Widgets vorgesehen.
 *
 * Gespeicherte Preferences:
 * - count        : Anzahl der gespeicherten Messwerte
 * - value_X      : Pegelwert als Integer (cm)
 * - Time_X       : Zeitstempel der Messung (derzeit
 *                  noch nicht grafisch genutzt)
 *
 * Autor:     Bollogg
 * Datum:     2025-11-20
 *******************************************************/

object PegelBitmapGenerator {
	 /**
	 * Erstellt ein Bitmap mit dem Pegelverlauf.
	 *
	 * @param context Context zum Zugriff auf SharedPreferences
	 * @param width   Breite des Bitmaps
	 * @param height  Höhe des Bitmaps
	 * @return        Fertiges Bitmap mit Diagramm
	 */
	 @JvmStatic
	 fun makePegelBitmap(context: Context,
	                     width  : Int,
	                     height : Int,
	                     config : PegelGraphConfig = PegelGraphConfig()
	 ): Bitmap {

	    //public static Bitmap makePegelBitmap(Context context) {

	    //int width = (4 * 70) - 30;
	    //int height = (3 * 70) - 30;

			// Bitmap und Zeichenfläche erzeugen
	    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
	    val c = Canvas(bmp)

			// Hintergrundfarbe setzen
			c.drawColor(ContextCompat.getColor(context, R.color.graph_background))

			// Paint-Objekt für Linien, Punkte und Text
	    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

			// Zugriff auf gecachte Pegeldaten
			val prefs = context.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)

	    Log.i("WIDGET", "Bitmap: count=" + prefs.getInt("count", 0))
	    val count = prefs.getInt("count", 0)
	    val values = ArrayList<Float?>()

			// Pegelwerte einlesen
			// WICHTIG: Werte sind als int gespeichert → Konvertierung zu Float
	    for (i in 0..<count) {
	      val raw = prefs.getInt("value_" + i, -1)
	      if (raw >= 0) values.add(raw.toFloat()) // -1 ignorieren

	      val time: String = prefs.getString("Time_" + i, "No time")!!
	    }

			// Zeitstempel aktuell noch ungenutzt
	    /* ToDo:   for (int i = 0; i < count; i++) {
	    values.add((float) prefs.getInt("value_" + i, -1));
	    } */

			// Falls keine gültigen Daten vorhanden sind
	    if (values.isEmpty()) {
		    paint.color = config.pointColor
		    paint.textSize = config.emptyTextSize
		    c.drawText(
			    context.getString(config.emptyTextRes),
			    config.emptyTextX,
			    config.emptyTextY,
			    paint
		    )

	      //paint.setColor(Color.WHITE)
	      //paint.setTextSize(40f)
	      c.drawText("Keine Daten", 20f, 120f, paint)
	      return bmp
	    }

			// Minimal- und Maximalwert bestimmen
			var min = Float.Companion.MAX_VALUE
	    var max = Float.Companion.MIN_VALUE

	    for (v in values) {
	        if (v!! < min) min = v
	        if (v > max) max = v
	    }

			// Falls alle Werte identisch sind → Spreizung erzwingen
	    if (min == max) {
	        min -= 5f
	        max += 5f
	    }

			// Definition des Diagrammbereichs
	    val graphLeft = config.marginLeft
		  val graphRight  = width  - config.marginRight
	    val graphTop = config.marginTop
	    val graphBottom = height - config.marginBottom

	    val graphWidth = graphRight - graphLeft
	    val graphHeight = graphBottom - graphTop

			// Achsen zeichnen
	    paint.setColor(Color.GRAY)
	    paint.setStrokeWidth(config.axisStroke)
	    c.drawLine(graphLeft, graphBottom, graphRight, graphBottom, paint)
	    c.drawLine(graphLeft, graphTop, graphLeft, graphBottom, paint)

			// Farbe und Stärke für den Pegelverlauf
			paint.setColor(ContextCompat.getColor(context, R.color.graph_line))
	    paint.setStrokeWidth(config.lineStroke)

			// Horizontaler Abstand zwischen den Messpunkten
	    val stepX = graphWidth / (values.size - 1)

	    var prevX = -1f
	    var prevY = -1f

			// Linien zwischen den Messpunkten zeichnen
	    for (i in values.indices) {
	      val value: Float = values.get(i)!!

	      val x = graphLeft + stepX * i
	      val norm = (value - min) / (max - min)
	      val y = graphBottom - (norm * graphHeight)

	      if (i > 0) c.drawLine(prevX, prevY, x, y, paint)

	      prevX = x
	      prevY = y
	    }

			// Messpunkte als Linie darstellen
	    paint.setColor(Color.WHITE)
	    for (i in values.indices) {
	      val value: Float = values.get(i)!!

	      val x = graphLeft + stepX * i
	      val norm = (value - min) / (max - min)
	      val y = graphBottom - (norm * graphHeight)

	      c.drawCircle(x, y, config.pointRadius, paint)
	    }
	  /*
	  //ToDo: Text für hohen einblenden?
	  paint.setColor(Color.WHITE);
	  paint.setTextSize(30);
	  //c.drawText("Pegelverlauf", 20, 35, paint);
	  c.drawText("", 20, 35, paint);

	  paint.setTextSize(24);
	  c.drawText(min + " cm", 5, graphBottom, paint);
	  c.drawText(max + " cm", 5, graphTop + 10, paint);
		*/

		// Fertiges Bitmap zurückgeben
	  return bmp
	}

}
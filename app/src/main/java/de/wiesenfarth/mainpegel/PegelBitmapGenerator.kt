package de.wiesenfarth.mainpegel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log

object PegelBitmapGenerator {
    fun makePegelBitmap(context: Context, width: Int, height: Int): Bitmap {
        //public static Bitmap makePegelBitmap(Context context) {

        //int width = (4 * 70) - 30;
        //int height = (3 * 70) - 30;

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        c.drawColor(Color.parseColor("#202020"))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val prefs = context.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)

        Log.i("WIDGET", "Bitmap: count=" + prefs.getInt("count", 0))
        val count = prefs.getInt("count", 0)
        val values = ArrayList<Float?>()

        // WICHTIG: Werte als int gespeichert → als float konvertieren!
        for (i in 0..<count) {
            val raw = prefs.getInt("value_" + i, -1)
            if (raw >= 0) values.add(raw.toFloat()) // -1 ignorieren

            val time: String = prefs.getString("Time_" + i, "No time")!!
        }

        /* ToDo:   for (int i = 0; i < count; i++) {
      values.add((float) prefs.getInt("value_" + i, -1));
    }
*/
        if (values.isEmpty()) {
            paint.setColor(Color.WHITE)
            paint.setTextSize(40f)
            c.drawText("Keine Daten", 20f, 120f, paint)
            return bmp
        }

        var min = Float.Companion.MAX_VALUE
        var max = Float.Companion.MIN_VALUE

        for (v in values) {
            if (v!! < min) min = v
            if (v > max) max = v
        }

        if (min == max) {
            min -= 5f
            max += 5f
        }

        val graphLeft = 40f
        val graphRight = (width - 20).toFloat()
        val graphTop = 20f
        val graphBottom = (height - 20).toFloat() //40;

        val graphWidth = graphRight - graphLeft
        val graphHeight = graphBottom - graphTop

        paint.setColor(Color.GRAY)
        paint.setStrokeWidth(2f)
        c.drawLine(graphLeft, graphBottom, graphRight, graphBottom, paint)
        c.drawLine(graphLeft, graphTop, graphLeft, graphBottom, paint)

        paint.setColor(Color.parseColor("#4DB6FF"))
        paint.setStrokeWidth(4f)

        val stepX = graphWidth / (values.size - 1)

        var prevX = -1f
        var prevY = -1f

        for (i in values.indices) {
            val value: Float = values.get(i)!!

            val x = graphLeft + stepX * i
            val norm = (value - min) / (max - min)
            val y = graphBottom - (norm * graphHeight)

            if (i > 0) c.drawLine(prevX, prevY, x, y, paint)

            prevX = x
            prevY = y
        }

        paint.setColor(Color.WHITE)
        for (i in values.indices) {
            val value: Float = values.get(i)!!

            val x = graphLeft + stepX * i
            val norm = (value - min) / (max - min)
            val y = graphBottom - (norm * graphHeight)

            c.drawCircle(x, y, 4f, paint)
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
        return bmp
    }
}

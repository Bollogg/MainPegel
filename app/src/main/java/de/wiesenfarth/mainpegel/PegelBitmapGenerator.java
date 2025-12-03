package de.wiesenfarth.mainpegel;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;

public class PegelBitmapGenerator {

  public static Bitmap makePegelBitmap(Context context, int width, int height) {
  //public static Bitmap makePegelBitmap(Context context) {

    //int width = (4 * 70) - 30;
    //int height = (3 * 70) - 30;

    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas c = new Canvas(bmp);

    c.drawColor(Color.parseColor("#202020"));

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    SharedPreferences prefs = context.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE);

    Log.i("WIDGET", "Bitmap: count=" + prefs.getInt("count", 0));
    int count = prefs.getInt("count", 0);
    ArrayList<Float> values = new ArrayList<>();

    // WICHTIG: Werte als int gespeichert → als float konvertieren!
    for (int i = 0; i < count; i++) {
      int raw = prefs.getInt("value_" + i, -1);
      if (raw >= 0) values.add((float) raw);   // -1 ignorieren
      String time = prefs.getString("Time_" + i, "No time");
    }

/* ToDo:   for (int i = 0; i < count; i++) {
      values.add((float) prefs.getInt("value_" + i, -1));
    }
*/
    if (values.isEmpty()) {
      paint.setColor(Color.WHITE);
      paint.setTextSize(40);
      c.drawText("Keine Daten", 20, 120, paint);
      return bmp;
    }

    float min = Float.MAX_VALUE;
    float max = Float.MIN_VALUE;

    for (float v : values) {
      if (v < min) min = v;
      if (v > max) max = v;
    }

    if (min == max) {
      min -= 5;
      max += 5;
    }

    float graphLeft = 40;
    float graphRight = width - 20;
    float graphTop = 20;
    float graphBottom = height - 20; //40;

    float graphWidth = graphRight - graphLeft;
    float graphHeight = graphBottom - graphTop;

    paint.setColor(Color.GRAY);
    paint.setStrokeWidth(2);
    c.drawLine(graphLeft, graphBottom, graphRight, graphBottom, paint);
    c.drawLine(graphLeft, graphTop, graphLeft, graphBottom, paint);

    paint.setColor(Color.parseColor("#4DB6FF"));
    paint.setStrokeWidth(4);

    float stepX = graphWidth / (values.size() - 1);

    float prevX = -1;
    float prevY = -1;

    for (int i = 0; i < values.size(); i++) {
      float value = values.get(i);

      float x = graphLeft + stepX * i;
      float norm = (value - min) / (max - min);
      float y = graphBottom - (norm * graphHeight);

      if (i > 0) c.drawLine(prevX, prevY, x, y, paint);

      prevX = x;
      prevY = y;
    }

    paint.setColor(Color.WHITE);
    for (int i = 0; i < values.size(); i++) {
      float value = values.get(i);

      float x = graphLeft + stepX * i;
      float norm = (value - min) / (max - min);
      float y = graphBottom - (norm * graphHeight);

      c.drawCircle(x, y, 4, paint);
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
    return bmp;
  }
}

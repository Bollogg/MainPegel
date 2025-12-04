package de.wiesenfarth.mainpegel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PegelUiHelper {

  /*******************************************************
   * ladePegelstand
   *
   * Lädt die Pegeldaten über Retrofit vom Server
   *******************************************************/
  public static void ladePegelstand(Context ctx, TextView textView, LineChart chart, SharedPreferences prefs) {

    PegelLogic.run(ctx);   // API starten und Daten abholen

    SharedPreferences cache = ctx.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE);

    int value = cache.getInt("last_value", -1);
    String time = cache.getString("last_time", "--:--");

    int count = cache.getInt("count", 0);

    List<PegelResponse> list = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      int v = cache.getInt("value_" + i, -1);
      String ts = cache.getString("timestamp_" + i, "--");
      list.add(new PegelResponse(ts, v));
    }

    // Text anzeigen
    if (value >= 0) {
      textView.setText("Pegel: " + value + " cm\nZeit: " + time);
    } else {
      textView.setText("Keine Daten");
    }

    // Graph zeichnen
    int hours = prefs.getInt("graph_hours", 4);
    aktualisiereGraph(ctx, chart, list, hours);
    forceWidgetUpdate(ctx);
  }
  /*******************************************************
   * aktualisiereGraph
   *
   * Zeichnet den Verlauf des Pegels als MPAndroidChart
   *******************************************************/
  private static void aktualisiereGraph(Context ctx, LineChart lineChart,
                                       List<PegelResponse> daten, int hours) {

    // Y-Achse links
    YAxis left = lineChart.getAxisLeft();
    left.setTextColor(ContextCompat.getColor(ctx, R.color.textColor));
    left.setAxisLineColor(ContextCompat.getColor(ctx, R.color.axisColor));
    left.setGridColor(ContextCompat.getColor(ctx, R.color.gridColor));
    lineChart.getAxisRight().setEnabled(false);

    // Hintergrundfarben
    lineChart.setBackgroundColor(ContextCompat.getColor(ctx, R.color.backgroundColor));
    lineChart.setDrawGridBackground(false);

    // Legende
    lineChart.getLegend().setTextColor(ContextCompat.getColor(ctx, R.color.legendTextColor));

    // Beschreibung ausblenden
    lineChart.getDescription().setEnabled(false);

    // --- Daten vorbereiten ---
    List<Entry> entries = new ArrayList<>();
    List<String> xLabels = new ArrayList<>();

    SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMANY);
    SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm", Locale.GERMANY);

    // Datenpunkte erzeugen
    for (int i = 0; i < daten.size(); i++) {
      PegelResponse p = daten.get(i);

      try {
        Date d = apiFormat.parse(p.getTimestamp());
        entries.add(new Entry(i, p.getValue()));
        xLabels.add(displayFormat.format(d));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    // Dataset konfigurieren
    LineDataSet dataSet = new LineDataSet(entries,
        ctx.getString(R.string.level_curve) + " (" + hours + "h)");

    dataSet.setLineWidth(2f);
    dataSet.setDrawCircles(false);
    dataSet.setDrawValues(false);

    // Farben aus Ressourcen (Tag/Nacht)
    dataSet.setColor(ContextCompat.getColor(ctx, R.color.lineColor));
    dataSet.setDrawFilled(true);
    dataSet.setFillColor(ContextCompat.getColor(ctx, R.color.fillColor));

    // Linien setzen
    lineChart.setData(new LineData(dataSet));

    // X-Achse konfigurieren
    XAxis xAxis = lineChart.getXAxis();
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    xAxis.setGranularity(1f);
    xAxis.setLabelRotationAngle(-45f);

    xAxis.setTextColor(ContextCompat.getColor(ctx, R.color.textColor));
    xAxis.setAxisLineColor(ContextCompat.getColor(ctx, R.color.axisColor));
    xAxis.setGridColor(ContextCompat.getColor(ctx, R.color.gridColor));

    // Zeitlabels verwenden
    xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));

    // Aktualisieren
    lineChart.invalidate();

  }

  public static void forceWidgetUpdate(Context ctx) {
    try {
      Intent intent = new Intent(ctx, PegelWidget.class);
      intent.setAction(PegelWidget.UPDATE_ACTION);
      ctx.sendBroadcast(intent);

      Log.i("WIDGET", "Widget-Update über Helper ausgelöst");
    } catch (Exception e) {
      Log.e("WIDGET", "Fehler im Helper", e);
    }
  }
}

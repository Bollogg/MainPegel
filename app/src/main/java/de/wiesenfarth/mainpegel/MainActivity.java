package de.wiesenfarth.mainpegel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*******************************************************
 * Programm:  MainPegel
 *
 * Beschreibung:
 *   Aktuelle Main Pegel anzeigen
 *
 *
 * @Website:   https://www.wiesenfarth-net.de
 * @Autor:     Bollog
 * @Datum:     2025-11-17
 * @Version:   2025.11
 *******************************************************/
public class MainActivity extends AppCompatActivity {
  private String localityGuid;
  private SharedPreferences prefs;
  private  int intervalMinutes;

  private TextView textViewPegelstand;
  private Button buttonAktualisieren;

  private LineChart lineChart; //Graph

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //ToDo: Test
        Log.e("NET", "INTERNET PERMISSION: " +
        (checkSelfPermission(android.Manifest.permission.INTERNET) == 0));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        return insets;
      });


      prefs = getSharedPreferences("settings", MODE_PRIVATE);
      localityGuid = prefs.getString("locality_guid", CONST.WUERZBURG);

      prefs = getSharedPreferences("settings", MODE_PRIVATE);
      intervalMinutes = prefs.getInt("interval_minutes", 15);

      // Falls du den Wert weiterverarbeiten musst:
      updateWithLocality(localityGuid, intervalMinutes);


      textViewPegelstand = findViewById(R.id.textViewPegelstand);

      //Button Werte aktualliesieren
      buttonAktualisieren = findViewById(R.id.buttonAktualisieren);
      // Line Graph verbinden
      lineChart = findViewById(R.id.lineChart);

        //Lade Pegel
        ladePegelstand();

        buttonAktualisieren.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
                ladePegelstand();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        localityGuid = prefs.getString("locality_guid", CONST.WUERZBURG);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        intervalMinutes = prefs.getInt("interval_minutes", 15);

        // Falls du den Wert weiterverarbeiten musst:
        updateWithLocality(localityGuid, intervalMinutes);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true; // zeigt das Menü an
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Toast.makeText(this, getString(R.string.toast_settings), Toast.LENGTH_SHORT).show();
            // öffne Settings fenster
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_about) {
            Toast.makeText(this, getString(R.string.toast_about), Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*******************************************************
     * Funktion:  updateWithLocality
     *
     * Beschreibung:
     *   Uebergabe der Messbereiche von SettingsActivity
     *
     *
     * @Autor:     Bollog
     * @Datum:     2025-11-17
     *******************************************************/
    private void updateWithLocality(String guid, int min) {

        // Beispiel: Ausgabe
        System.out.println("Ausgewählte Messstelle: " + guid);
        System.out.println("Ausgewählte Minuten: " + min);

        //ToDo: API übergabe
        // Beispiel: UI aktualisieren
        //TextView txt = findViewById(R.id.txt_station);
        //txt.setText("Station GUID:\n" + guid);

        // Beispiel: API-Abfrage
        //loadPegelFromServer(guid);
    }

    /*******************************************************
     * Programm:  ladePegelstand
     *
     * Beschreibung:
     *   Laden des Pegels
     *
     *
     * @Autor:     Bollog
     * @Datum:     2025-11-17
     *******************************************************/
    private void ladePegelstand() {

      // Einstellungen laden
      prefs = getSharedPreferences("settings", MODE_PRIVATE);
      localityGuid = prefs.getString("locality_guid", CONST.WUERZBURG);

      int hours = prefs.getInt("graph_hours", 4);  // Standard: 4h
      String startParam = "PT" + hours + "H";


      // API Service
      PegelApiService apiService = RetrofitClient.getApiService();
      // Dynamischer Retrofit-Aufruf
      Call<List<PegelResponse>> call = apiService.getPegelstand(localityGuid, startParam);

      Log.d("PEGEL", "GUID: " + localityGuid);
      Log.d("PEGEL", "Start-Parameter: " + startParam);

      Log.e("PEGEL", "Retrofit URL: " + call.request().url());

      call.enqueue(new Callback<List<PegelResponse>>() {

                     {
                       Log.e("PEGEL", "===> Callback wurde initialisiert");
                     }


      @Override
      public void onResponse(Call<List<PegelResponse>> call, Response<List<PegelResponse>> response) {
          if (!response.isSuccessful()) {
              Log.e("PEGEL", "HTTP Fehler: " + response.code());
              textViewPegelstand.setText("Fehler: " + response.code());
              return;
          }

        // Liste empfangen
        List<PegelResponse> list = response.body();
        if (list == null || list.isEmpty()) {
          textViewPegelstand.setText("Keine Messwerte!");
          return;
        }

        // Letzter Wert (aktuellster)
        PegelResponse last = list.get(list.size() - 1);

        textViewPegelstand.setText(
            "Pegel: " + last.getValue() + " cm\n" +
            "Zeit: " + last.getTimestamp()
        );

        Log.d("PEGEL", "Neuster Wert: " + last.getTimestamp() + " -> " + last.getValue());

        // +++ LOGGEN aller Werte +++
        for (PegelResponse p : list) {
          Log.d("PEGEL", p.getTimestamp() + " -> " + p.getValue());
        }
        // Chart aktualisieren
        aktualisiereGraph(list);
    }

      @Override
      public void onFailure(Call<List<PegelResponse>> call, Throwable t) {
          t.printStackTrace();
          textViewPegelstand.setText("Netzwerkfehler!");
      }
  });


    System.out.println("Retrofit URL: " + call.request().url());
}

  /*******************************************************
   * Programm:  aktualisiereGraph
   *
   * Beschreibung:
   *  Aktuallieseire Groph
   *
   *
   * @Autor:     Bollog
   * @Datum:     2025-11-21
   *******************************************************/
  private void aktualisiereGraph(List<PegelResponse> daten) {

    List<Entry> entries = new ArrayList<>();

    // Zeitformat für die API-Zeitstempel
    SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMANY);
    SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm", Locale.GERMANY);

    List<String> xLabels = new ArrayList<>();

    for (int i = 0; i < daten.size(); i++) {
      PegelResponse m = daten.get(i);

      try {
        Date d = apiFormat.parse(m.getTimestamp());
        long x = i;   // wir benutzen Index als X-Wert
        entries.add(new Entry(x, m.getValue()));

        // Label hinzufügen
        xLabels.add(displayFormat.format(d));

      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    LineDataSet dataSet = new LineDataSet(entries, "Pegelverlauf (4h)");
    dataSet.setLineWidth(2f);
    dataSet.setDrawCircles(false);
    dataSet.setDrawValues(false);

    LineData lineData = new LineData(dataSet);
    lineChart.setData(lineData);

    // X-Achse anpassen → Zeitlabels
    XAxis xAxis = lineChart.getXAxis();
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    xAxis.setGranularity(1f);
    xAxis.setLabelRotationAngle(-45f);

    // ← HIER verwenden wir IndexAxisValueFormatter
    xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));

    lineChart.getAxisRight().setEnabled(false);   // Y-Achse links einschalten, rechts aus
    lineChart.getDescription().setEnabled(false);
    lineChart.invalidate();                       // Chart aktualisieren

  }

}
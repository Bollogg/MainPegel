package de.wiesenfarth.mainpegel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    private Button buttonAktualisieren;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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



        //Button Werte aktualliesieren
        buttonAktualisieren = findViewById(R.id.buttonAktualisieren);
        buttonAktualisieren.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ToDo: ladePegelstand();
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
/*ToDo    private void ladePegelstand() {
        PegelApiService apiService = RetrofitClient.getApiService();
        //Call<PegelResponse> call = apiService.getPegelstand("WUERZBURG");
        Call<PegelResponse> call = apiService.getPegelstand(CONST.WUERZBURG);

        call.enqueue(new Callback<PegelResponse>() {
            @Override
            public void onResponse(Call<PegelResponse> call, Response<PegelResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PegelResponse pegel = response.body();
                    textViewPegelstand.setText("Pegelstand: " + pegel.getValue() + " cm " + pegel.getUnit());
                } else {
                    textViewPegelstand.setText("Fehler beim Laden.");
                }
            }

            @Override
            public void onFailure(Call<PegelResponse> call, Throwable t) {
                textViewPegelstand.setText("Keine Verbindung.");
                Toast.makeText(MainActivity.this, "Fehler: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
*/
}
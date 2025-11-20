package de.wiesenfarth.mainpegel;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/*******************************************************
 * Programm:  SettingsActivity
 *
 * Beschreibung:
 *   Einstellungen
 *
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-17
 *******************************************************/
public class SettingsActivity extends AppCompatActivity {

    private Spinner spinnerLocality, spinnerInterval;
    private EditText waveThreshold;
    private Switch switchVibration, switchBeep;

    private SharedPreferences prefs;

    private String[] localityNames = {
            "Raunheim",
            "Frankfurt Osthafen",
            "Hanau Brücke DFH",
            "Auheim Brücke DFH",
            "Krotzenburg",
            "Mainflingen",
            "Kleinostheim WUK",
            "Obernau",
            "Kleinheubach",
            "Faulbach",
            "Wertheim",
            "Steinbach",
            "Würzburg",
            "Astheim",
            "Schweinfurt Neuer Hafen",
            "Trunstadt",
            "Bamberg",
            "Riedenburg Upstream"
    };

    private String[] localityValues = {
            CONST.RAUNHEIM,
            CONST.FRANKFURT_OSTHAFEN,
            CONST.HANAU_BRUECKE_DFH,
            CONST.AUHEIM_BRUECKE_DFH,
            CONST.KROTZENBURG,
            CONST.MAINFLINGEN,
            CONST.KLEINOSTHEIM_WUK,
            CONST.OBERNAU,
            CONST.KLEINHEUBACH,
            CONST.FAULBACH,
            CONST.WERTHEIM,
            CONST.STEINBACH,
            CONST.WUERZBURG,
            CONST.ASTHEIM,
            CONST.SCHWEINFURT_NEUER_HAFEN,
            CONST.TRUNSTADT,
            CONST.BAMBERG,
            CONST.RIEDENBURG_UP
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);


        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Titel (optional)
        getSupportActionBar().setTitle(R.string.menu_settings);
        // Links Pfeil
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // SharedPreferences
        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        // Views einbinden
        spinnerLocality = findViewById(R.id.spinner_locality);
        spinnerInterval = findViewById(R.id.spinner_interval);
        waveThreshold = findViewById(R.id.wave_threshold);
        waveThreshold.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(CONST.WAVE_THERESHOLD_MAX)   // max. 4 Stellen -> 9999
        });

        switchVibration = findViewById(R.id.switch_vibration);
        switchBeep = findViewById(R.id.switch_beep);

        // Auswahl der Pegelmessstelle
        ArrayAdapter<String> adapterLocality = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                localityNames
        );

        spinnerLocality.setAdapter(adapterLocality);

        ArrayAdapter<CharSequence> adapterInterval = ArrayAdapter.createFromResource(
                this,
                R.array.interval_display,
                android.R.layout.simple_spinner_item
        );
        adapterInterval.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInterval.setAdapter(adapterInterval);


        // Neuer Android-Back-Button Handler (wird IMMER ausgelöst)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                saveSettings();   // Einstellungen speichern
                finish();         // Activity schließen
            }
        });

        // Werte laden
        loadSettings();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Click auf Pfeil zurück → Activity schließen
        if (item.getItemId() == android.R.id.home) {
            saveSettings();
            finish(); // zurück zu MainActivity
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Einstellungen speichern
    private void saveSettings() {
        SharedPreferences.Editor editor = prefs.edit();

        //editor.putInt("locality", spinnerLocality.getSelectedItemPosition());
        int index = spinnerLocality.getSelectedItemPosition();
        editor.putString("locality_guid", localityValues[index]);
        editor.putInt("locality_index", index); // optional für UI

        //editor.putInt("interval", spinnerInterval.getSelectedItemPosition());
        // Display → Werte-Array laden von Spinner Interval
        String[] values = getResources().getStringArray(R.array.interval_values);

        int pos = spinnerInterval.getSelectedItemPosition();
        int selectedInterval = Integer.parseInt(values[pos]);  // z.B. 15, 30, 45...

        editor.putInt("interval_minutes", selectedInterval);

        editor.putString("wave_threshold", waveThreshold.getText().toString());

        editor.putBoolean("vibration", switchVibration.isChecked());
        editor.putBoolean("beep", switchBeep.isChecked());

        editor.apply();
    }

    // Einstellungen laden
    private void loadSettings() {
        int index;
        //spinnerLocality.setSelection(prefs.getInt("locality", 0));
        index = prefs.getInt("locality_index", 0);
        spinnerLocality.setSelection(index);

        //spinnerInterval.setSelection(prefs.getInt("interval", 0));
        int storedValue = prefs.getInt("interval_minutes", 15); // Default: 15
// Werte-Array laden
        String[] values = getResources().getStringArray(R.array.interval_values);

        // Default -> Index bestimmen
        index = 0;
        for (int i = 0; i < values.length; i++) {
            if (Integer.parseInt(values[i]) == storedValue) {
                index = i;
                break;
            }
        }
        waveThreshold.setText(prefs.getString("wave_threshold", "15"));

        switchVibration.setChecked(prefs.getBoolean("vibration", false));
        switchBeep.setChecked(prefs.getBoolean("beep", false));
    }
}
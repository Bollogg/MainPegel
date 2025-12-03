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
 *   Einstellungen der App wie Messstelle, Intervalle,
 *   Alarmoptionen, Schwellwert und Graph-Darstellung.
 *
 * Autor:     Bollog
 * Datum:     2025-11-17
 *******************************************************/
public class SettingsActivity extends AppCompatActivity {

    // UI-Elemente
    private Spinner spinnerLocality, spinnerInterval;
    private EditText waveThreshold;
    private Switch switchVibration, switchBeep;

    // Spinner für Stunden (Graph)
    private Spinner spinnerMeasure;

    // Einstellungen (Persistent)
    private SharedPreferences prefs;

    // Anzeigenamen der Pegel-Messstellen
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

    // GUIDs der Pegel-Messstellen
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

        // --------------------------------------------------------
        // Toolbar einrichten
        // --------------------------------------------------------
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.menu_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // SharedPreferences laden
        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        // --------------------------------------------------------
        // UI-Elemente verbinden
        // --------------------------------------------------------
        spinnerLocality = findViewById(R.id.spinner_locality);
        spinnerInterval = findViewById(R.id.spinner_interval);
        waveThreshold = findViewById(R.id.wave_threshold);
        switchVibration = findViewById(R.id.switch_vibration);
        switchBeep = findViewById(R.id.switch_beep);
        spinnerMeasure = findViewById(R.id.spinner_Measure);

        // Maximal 4 Ziffern (z. B. 9999)
        waveThreshold.setFilters(new InputFilter[]{
            new InputFilter.LengthFilter(CONST.WAVE_THERESHOLD_MAX)
        });

        // --------------------------------------------------------
        // Spinner: Anzahl Stunden für Graph (1–29)
        // --------------------------------------------------------
        String[] hourLabels = new String[29];
        for (int i = 0; i < 29; i++) {
            hourLabels[i] = (i + 1) + " Stunden";
        }

        ArrayAdapter<String> adapterHours = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            hourLabels
        );
        adapterHours.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMeasure.setAdapter(adapterHours);

        // --------------------------------------------------------
        // Spinner: Pegelmessstelle
        // --------------------------------------------------------
        ArrayAdapter<String> adapterLocality = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            localityNames
        );
        spinnerLocality.setAdapter(adapterLocality);

        // --------------------------------------------------------
        // Spinner: Abrufintervall (Displaywerte → XML)
        // --------------------------------------------------------
        ArrayAdapter<CharSequence> adapterInterval = ArrayAdapter.createFromResource(
            this,
            R.array.interval_display,
            android.R.layout.simple_spinner_item
        );
        adapterInterval.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInterval.setAdapter(adapterInterval);

        // --------------------------------------------------------
        // Zurück-Knopf (system) → Speichern + Schließen
        // --------------------------------------------------------
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                saveSettings();
                finish();
            }
        });

        // Einstellungen laden
        loadSettings();
    }

    // --------------------------------------------------------
    // Toolbar-Pfeil zurück → Einstellungen speichern
    // --------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            saveSettings();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --------------------------------------------------------
    // Einstellungen speichern
    // --------------------------------------------------------
    private void saveSettings() {
        SharedPreferences.Editor editor = prefs.edit();

        // Pegelmessstelle speichern
        int index = spinnerLocality.getSelectedItemPosition();
        editor.putString("locality_guid", localityValues[index]);
        editor.putInt("locality_index", index);

        // Abrufintervall (Display → Werte)
        String[] values = getResources().getStringArray(R.array.interval_values);
        int pos = spinnerInterval.getSelectedItemPosition();
        int selectedInterval = Integer.parseInt(values[pos]);
        editor.putInt("interval_minutes", selectedInterval);

        // Schwellwert
        //ToDo: löschen editor.putString("wave_threshold", waveThreshold.getText().toString());
        String input = waveThreshold.getText().toString().trim();
        if (input.isEmpty()) {
          input = "15";  // default
        }
        editor.putString("wave_threshold", input);

        // Alarmoptionen
        editor.putBoolean("vibration", switchVibration.isChecked());
        editor.putBoolean("beep", switchBeep.isChecked());

        // Stunden für Graph (Index 0 = 1 Stunde)
        editor.putInt("graph_hours", spinnerMeasure.getSelectedItemPosition() + 1);

        editor.apply();

        // WorkManager / AlarmManager Intervall nicht automatisch aktualisiert.
        PegelScheduler.schedule(getApplicationContext());
    }

    // --------------------------------------------------------
    // Einstellungen laden
    // --------------------------------------------------------
    private void loadSettings() {

        // Messstelle
        int index = prefs.getInt("locality_index", 0);
        spinnerLocality.setSelection(index);

        // Intervall
        int storedValue = prefs.getInt("interval_minutes", 15);
        String[] values = getResources().getStringArray(R.array.interval_values);

        int foundIndex = 0;
        for (int i = 0; i < values.length; i++) {
            if (Integer.parseInt(values[i]) == storedValue) {
                foundIndex = i;
                break;
            }
        }
        spinnerInterval.setSelection(foundIndex);

        // Schwellwert
        waveThreshold.setText(prefs.getString("wave_threshold", "15"));

        // Alarmoptionen
        switchVibration.setChecked(prefs.getBoolean("vibration", false));
        switchBeep.setChecked(prefs.getBoolean("beep", false));

        // Graph-Stunden
        int storedHours = prefs.getInt("graph_hours", 6);
        spinnerMeasure.setSelection(storedHours - 1);
    }
}

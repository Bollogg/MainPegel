package de.wiesenfarth.mainpegel

import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar


/*******************************************************
 * Programm:  SettingsActivity
 *
 * Beschreibung:
 * Einstellungen der App wie Messstelle, Intervalle,
 * Alarmoptionen, Schwellwert und Graph-Darstellung.
 *
 * Autor:     Bollog
 * Datum:     2025-11-17
 */
class SettingsActivity : AppCompatActivity() {
    // UI-Elemente
    private var spinnerLocality: Spinner? = null
    private var spinnerInterval: Spinner? = null
    private var waveThreshold: EditText? = null
    private var switchVibration: Switch? = null
    private var switchBeep: Switch? = null

    // Spinner für Stunden (Graph)
    private var spinnerMeasure: Spinner? = null

    // Einstellungen (Persistent)
    private var prefs: SharedPreferences? = null

    // Anzeigenamen der Pegel-Messstellen
    private val localityNames = arrayOf<String?>(
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
    )

    // GUIDs der Pegel-Messstellen
    private val localityValues = arrayOf<String?>(
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
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        // --------------------------------------------------------
        // Toolbar einrichten
        // --------------------------------------------------------
        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // KORRIGIERTE ZEILE:
        supportActionBar!!.title = getString(R.string.menu_settings) // Modernere Kotlin-Syntax
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // SharedPreferences laden
        prefs = getSharedPreferences("settings", MODE_PRIVATE)

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
        waveThreshold!!.setFilters(
            arrayOf<InputFilter>(
                LengthFilter(CONST.WAVE_THERESHOLD_MAX)
            )
        )

        // --------------------------------------------------------
        // Spinner: Anzahl Stunden für Graph (1–29)
        // --------------------------------------------------------
        val hourLabels = arrayOfNulls<String>(29)
        for (i in 0..28) {
            hourLabels[i] = (i + 1).toString() + " Stunden"
        }

        val adapterHours = ArrayAdapter<String?>(
            this,
            android.R.layout.simple_spinner_item,
            hourLabels
        )
        adapterHours.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMeasure!!.setAdapter(adapterHours)

        // --------------------------------------------------------
        // Spinner: Pegelmessstelle
        // --------------------------------------------------------
        val adapterLocality = ArrayAdapter<String?>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            localityNames
        )
        spinnerLocality!!.setAdapter(adapterLocality)

        // --------------------------------------------------------
        // Spinner: Abrufintervall (Displaywerte → XML)
        // --------------------------------------------------------
        val adapterInterval = ArrayAdapter.createFromResource(
            this,
            R.array.interval_display,
            android.R.layout.simple_spinner_item
        )
        adapterInterval.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerInterval!!.setAdapter(adapterInterval)

        // --------------------------------------------------------
        // Zurück-Knopf (system) → Speichern + Schließen
        // --------------------------------------------------------
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                saveSettings()
                finish()
            }
        })

        // Einstellungen laden
        loadSettings()
    }

    // --------------------------------------------------------
    // Toolbar-Pfeil zurück → Einstellungen speichern
    // --------------------------------------------------------
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() == android.R.id.home) {
            saveSettings()
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // --------------------------------------------------------
    // Einstellungen speichern
    // --------------------------------------------------------
    private fun saveSettings() {
        val editor = prefs!!.edit()

        // Pegelmessstelle speichern
        val index = spinnerLocality!!.getSelectedItemPosition()
        editor.putString("locality_guid", localityValues[index])
        editor.putInt("locality_index", index)

        // Abrufintervall (Display → Werte)
        val values = getResources().getStringArray(R.array.interval_values)
        val pos = spinnerInterval!!.getSelectedItemPosition()
        val selectedInterval = values[pos]!!.toInt()
        editor.putInt("interval_minutes", selectedInterval)

        // Schwellwert
        //ToDo: löschen editor.putString("wave_threshold", waveThreshold.getText().toString());
        var input = waveThreshold!!.getText().toString().trim { it <= ' ' }
        if (input.isEmpty()) {
            input = "15" // default
        }
        editor.putString("wave_threshold", input)

        // Alarmoptionen
        editor.putBoolean("vibration", switchVibration!!.isChecked())
        editor.putBoolean("beep", switchBeep!!.isChecked())

        // Stunden für Graph (Index 0 = 1 Stunde)
        editor.putInt("graph_hours", spinnerMeasure!!.getSelectedItemPosition() + 1)

        editor.apply()

        // WorkManager / AlarmManager Intervall nicht automatisch aktualisiert.
        PegelScheduler.schedule(getApplicationContext())
    }

    // --------------------------------------------------------
    // Einstellungen laden
    // --------------------------------------------------------
    private fun loadSettings() {
        // Messstelle

        val index = prefs!!.getInt("locality_index", 0)
        spinnerLocality!!.setSelection(index)

        // Intervall
        val storedValue = prefs!!.getInt("interval_minutes", 15)
        val values = getResources().getStringArray(R.array.interval_values)

        var foundIndex = 0
        for (i in values.indices) {
            if (values[i]!!.toInt() == storedValue) {
                foundIndex = i
                break
            }
        }
        spinnerInterval!!.setSelection(foundIndex)

        // Schwellwert
        waveThreshold!!.setText(prefs!!.getString("wave_threshold", "15"))

        // Alarmoptionen
        switchVibration!!.setChecked(prefs!!.getBoolean("vibration", false))
        switchBeep!!.setChecked(prefs!!.getBoolean("beep", false))

        // Graph-Stunden
        val storedHours = prefs!!.getInt("graph_hours", 6)
        spinnerMeasure!!.setSelection(storedHours - 1)
    }
}

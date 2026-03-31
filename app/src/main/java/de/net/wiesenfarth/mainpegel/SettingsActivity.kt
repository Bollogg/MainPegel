package de.net.wiesenfarth.mainpegel

import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputFilter
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import de.net.wiesenfarth.mainpegel.AlarmManager.PegelScheduler
import de.net.wiesenfarth.mainpegel.Variable.CONST

class SettingsActivity : AppCompatActivity() {

    private lateinit var spinnerLocality: Spinner
    private lateinit var spinnerInterval: Spinner
    private lateinit var spinnerMeasure: Spinner
    private lateinit var waveThreshold: EditText
    private lateinit var switchVibration: Switch
    private lateinit var switchBeep: Switch

    private lateinit var prefs: SharedPreferences

    private val localityNames = arrayOf(
        "Raunheim", "Frankfurt Osthafen", "Hanau Brücke DFH",
        "Auheim Brücke DFH", "Krotzenburg", "Mainflingen",
        "Kleinostheim WUK", "Obernau", "Kleinheubach",
        "Faulbach", "Wertheim", "Steinbach", "Würzburg",
        "Astheim", "Schweinfurt Neuer Hafen", "Trunstadt",
        "Bamberg", "Riedenburg Upstream"
    )

    private val localityValues = arrayOf(
        CONST.RAUNHEIM, CONST.FRANKFURT_OSTHAFEN, CONST.HANAU_BRUECKE_DFH,
        CONST.AUHEIM_BRUECKE_DFH, CONST.KROTZENBURG, CONST.MAINFLINGEN,
        CONST.KLEINOSTHEIM_WUK, CONST.OBERNAU, CONST.KLEINHEUBACH,
        CONST.FAULBACH, CONST.WERTHEIM, CONST.STEINBACH, CONST.WUERZBURG,
        CONST.ASTHEIM, CONST.SCHWEINFURT_NEUER_HAFEN, CONST.TRUNSTADT,
        CONST.BAMBERG, CONST.RIEDENBURG_UP
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        setupToolbar()
        initViews()

        prefs = getSharedPreferences("settings", MODE_PRIVATE)

        setupSpinners()
        setupBackHandler()

        loadSettings()

    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = getString(R.string.menu_settings)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun initViews() {
        spinnerLocality = findViewById(R.id.spinner_locality)
        spinnerInterval = findViewById(R.id.spinner_interval)
        spinnerMeasure = findViewById(R.id.spinner_Measure)

        waveThreshold = findViewById(R.id.wave_threshold)
        switchVibration = findViewById(R.id.switch_vibration)
        switchBeep = findViewById(R.id.switch_beep)

        waveThreshold.filters = arrayOf(InputFilter.LengthFilter(CONST.WAVE_THERESHOLD_MAX))
    }

    private fun setupSpinners() {

        // Stunden
        ArrayAdapter.createFromResource(
            this,
            R.array.hours_display,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerMeasure.adapter = it
        }

        // Messstelle
        spinnerLocality.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            localityNames
        )

        // Intervall
        ArrayAdapter.createFromResource(
            this,
            R.array.interval_display,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerInterval.adapter = it
        }
    }

    private fun setupBackHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                saveSettings()
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            saveSettings()
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    // ---------------- SAVE ----------------

    private fun saveSettings() {
        val editor = prefs.edit()

        // Messstelle
        val localityIndex = spinnerLocality.selectedItemPosition
        editor.putString("locality_guid", localityValues[localityIndex])
        editor.putInt("locality_index", localityIndex)

        // Intervall
        val intervalValues = resources.getStringArray(R.array.interval_values)
        val interval = intervalValues[spinnerInterval.selectedItemPosition].toInt()
        editor.putInt("interval_minutes", interval)

        // Schwellwert
        val threshold = waveThreshold.text.toString().ifEmpty { "15" }
        editor.putString("wave_threshold", threshold)

        // Switches
        editor.putBoolean("vibration", switchVibration.isChecked)
        editor.putBoolean("beep", switchBeep.isChecked)

        // Stunden (WICHTIG: echte Werte speichern!)
        val hourValues = resources.getStringArray(R.array.hours_values)
        val selectedHours = hourValues[spinnerMeasure.selectedItemPosition].toInt()
        editor.putInt("graph_hours", selectedHours)

        editor.apply()

        PegelScheduler.schedule(applicationContext)
    }

    // ---------------- LOAD ----------------

    private fun loadSettings() {

        // Messstelle
        val localityIndex = prefs.getInt("locality_index", 0)
            .coerceIn(0, localityNames.lastIndex)
        spinnerLocality.setSelection(localityIndex)

        // Intervall
        val storedInterval = prefs.getInt("interval_minutes", 15)
        val intervalValues = resources.getStringArray(R.array.interval_values)

        val intervalIndex = intervalValues.indexOfFirst {
            it.toInt() == storedInterval
        }.takeIf { it >= 0 } ?: 0

        spinnerInterval.setSelection(intervalIndex)

        // Schwellwert
        waveThreshold.setText(prefs.getString("wave_threshold", "15"))

        // Switches
        switchVibration.isChecked = prefs.getBoolean("vibration", false)
        switchBeep.isChecked = prefs.getBoolean("beep", false)

        // Stunden (robust!)
        val storedHours = prefs.getInt("graph_hours", 6)
        val hourValues = resources.getStringArray(R.array.hours_values)

        val hourIndex = hourValues.indexOfFirst {
            it.toInt() == storedHours
        }.takeIf { it >= 0 } ?: 0

        spinnerMeasure.setSelection(hourIndex)
    }
}
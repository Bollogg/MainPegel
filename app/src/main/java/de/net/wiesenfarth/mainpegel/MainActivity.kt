package de.net.wiesenfarth.mainpegel

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import android.Manifest
import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import de.net.wiesenfarth.mainpegel.Variable.CONST
import de.net.wiesenfarth.mainpegel.AlarmManager.NotificationHelper
import de.net.wiesenfarth.mainpegel.AlarmManager.PegelScheduler
import de.net.wiesenfarth.mainpegel.Graph.PegelUiHelper
import de.net.wiesenfarth.mainpegel.Widget.PegelWidget

/*******************************************************
 * Programm:  MainPegel
 *
 * Beschreibung:
 * Haupt-Activity der App.
 * Zeigt aktuelle Pegelstände des Mains für
 * verschiedene Messstationen und stellt
 * ein Diagramm des Pegelverlaufs dar.
 *
 * Funktionen:
 * - Anzeige aktueller Pegelstand
 * - Anzeige eines Pegeldiagramms
 * - Manuelles Aktualisieren der Daten
 * - Empfang von Updates vom Widget
 * - Start des Hintergrund-Schedulers
 *
 * @Website:   wiesenfarth-net.de
 * @Autor:     Bollogg
 * @Datum:     2026-03-25
 * @Version:   2026.03
 ********************************************************/
class MainActivity : AppCompatActivity() {

    // GUID der aktuell ausgewählten Messstelle
    private var localityGuid: String? = null

    // SharedPreferences zum Speichern der App-Einstellungen
    private lateinit var prefs: SharedPreferences

    // Aktualisierungsintervall der Pegeldaten in Minuten
    private var intervalMinutes = 0

    // UI-Komponenten
    private lateinit var textViewPegelstand: TextView
    private lateinit var buttonAktualisieren: Button

    // Diagramm (MPAndroidChart) zur Darstellung des Pegelverlaufs
    private lateinit var lineChart: com.github.mikephil.charting.charts.LineChart

    // Flag, ob Einstellungen geändert wurden
    private var settingsChanged = true

    /**
     * Wird beim Start der Activity aufgerufen.
     * Initialisiert UI, Einstellungen, Berechtigungen
     * sowie Hintergrunddienste.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Layout laden
        setContentView(R.layout.activity_main)

        // Aktiviert modernes Edge-to-Edge Layout (Android 13+ Design)
        this.enableEdgeToEdge()

        // Debug-Ausgabe: Prüfen, ob INTERNET Permission vorhanden ist
        Log.e(
            "NET", "INTERNET PERMISSION: " +
                (checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)
        )

        // Toolbar initialisieren
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        /**
         * Passt das Layout automatisch an Statusleiste und Navigationsleiste an.
         * Dadurch werden UI-Elemente nicht von Systemleisten überlagert.
         */
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById<View>(R.id.main),
            OnApplyWindowInsetsListener { v: View, insets: WindowInsetsCompat? ->
                val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            })

        // Notification Channel sicherstellen (für Android Notifications)
        NotificationHelper.ensureChannel(this)

        // Berechtigung für exakte Alarme prüfen (Android 12+)
        checkExactAlarmPermission()

        /**
         * Ab Android 13 muss die Notification-Permission
         * zur Laufzeit angefordert werden.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS
                    ), 1
                )
            }
        }

        // SharedPreferences laden
        prefs = getSharedPreferences("settings", MODE_PRIVATE)

        // gespeicherte Messstelle laden (Standard: Würzburg)
        localityGuid = prefs.getString("locality_guid", CONST.WUERZBURG)

        // Aktualisierungsintervall laden
        intervalMinutes = prefs.getInt("interval_minutes", 15)

        // Debug-Ausgabe der aktuellen Einstellungen
        updateWithLocality(localityGuid, intervalMinutes)

        /**
         * Scheduler starten
         * Dieser sorgt für regelmäßige Hintergrundupdates
         * der Pegeldaten.
         */
        PegelScheduler.schedule(this)

        // UI-Elemente initialisieren
        textViewPegelstand = findViewById(R.id.textViewPegelstand)
        buttonAktualisieren = findViewById(R.id.buttonAktualisieren)
        lineChart = findViewById(R.id.lineChart)

        // Pegeldaten aus Cache laden und anzeigen
        PegelUiHelper.ladePegelstand(this, textViewPegelstand, lineChart, prefs)

        /**
         * Button für manuelles Aktualisieren der Pegeldaten
         */
        buttonAktualisieren.setOnClickListener {
            Log.i("MAIN", "Manuelles Update gestartet")

            // Startet API-Update
            val started = de.net.wiesenfarth.mainpegel.API.PegelLogic.run(this)

            // Falls Update bereits läuft
            if (!started) {
                Toast.makeText(this, "Update läuft bereits", Toast.LENGTH_SHORT).show()
            }

            // UI erneut laden
            PegelUiHelper.ladePegelstand(this, textViewPegelstand, lineChart, prefs)
        }
    }

    /**
     * Wird aufgerufen, wenn die Activity wieder sichtbar wird.
     * Lädt Einstellungen neu und registriert den BroadcastReceiver.
     */
    override fun onResume() {
        super.onResume()

        // Einstellungen erneut laden
        prefs = getSharedPreferences("settings", MODE_PRIVATE)
        localityGuid = prefs.getString("locality_guid", CONST.WUERZBURG)
        intervalMinutes = prefs.getInt("interval_minutes", 15)

        /**
         * BroadcastReceiver registrieren.
         * Dieser empfängt Updates vom Widget oder Hintergrundprozess.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                dataReceiver,
                IntentFilter(PegelWidget.ACTION_DATA_UPDATED),
                RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(
                dataReceiver,
                IntentFilter(PegelWidget.ACTION_DATA_UPDATED)
            )
        }

        // Cached Daten sofort anzeigen
        PegelUiHelper.ladePegelstand(this, textViewPegelstand, lineChart, prefs)

        // Debug-Ausgabe
        updateWithLocality(localityGuid, intervalMinutes)

        settingsChanged = false
    }

    /**
     * Wird aufgerufen, wenn Activity in den Hintergrund geht.
     * Receiver wird entfernt um Speicherlecks zu vermeiden.
     */
    override fun onPause() {
        super.onPause()
        unregisterReceiver(dataReceiver)
    }

    /**
     * ActivityResultLauncher für SettingsActivity.
     * Erkennt, ob Einstellungen geändert wurden.
     */
    private val settingsLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data?.getBooleanExtra("settings_changed", false) == true) {
                    settingsChanged = true
                }
            }
        }

    /**
     * Erstellt das Options-Menü (Toolbar-Menü)
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Behandlung von Menüeinträgen
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            Toast.makeText(this, getString(R.string.toast_settings), Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        } else if (id == R.id.action_about) {
            Toast.makeText(this, getString(R.string.toast_info), Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, InfoActivity::class.java))
            return true
        } else if (id == R.id.action_privacy_policy) {
            Toast.makeText(this, getString(R.string.toast_privacy_policy), Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, DatenschutzerklaerungActivity::class.java))
            return true
        } else if (id == R.id.action_oss) {
            OssLicensesMenuActivity.setActivityTitle(getString(R.string.toast_oss_licenses))
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Debug-Methode zur Ausgabe der aktuellen Einstellungen
     */
    private fun updateWithLocality(guid: String?, min: Int) {
        Log.i("SETTINGS", "Messstelle: $guid")
        Log.i("SETTINGS", "Intervall: $min min")
    }

    /**
     * Prüft, ob die App exakte Alarme planen darf.
     * Falls nicht, wird der Benutzer zu den Systemeinstellungen geführt.
     */
    private fun checkExactAlarmPermission() {
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        if (!am.canScheduleExactAlarms()) {
            Log.w("PERMISSION", "Exact-Alarms NICHT erlaubt → User muss es erlauben.")
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } else {
            Log.i("PERMISSION", "Exact-Alarms sind erlaubt.")
        }
    }

    /**
     * BroadcastReceiver für Datenupdates.
     * Wird ausgelöst, wenn neue Pegeldaten vorliegen.
     */
    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (intent.action == PegelWidget.ACTION_DATA_UPDATED) {

                Log.i("MAIN", "Neue Daten empfangen → UI wird aktualisiert")

                // UI aktualisieren
                PegelUiHelper.ladePegelstand(
                    this@MainActivity,
                    textViewPegelstand,
                    lineChart,
                    prefs
                )
            }
        }
    }
}
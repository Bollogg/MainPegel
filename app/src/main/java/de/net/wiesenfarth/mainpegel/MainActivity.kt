package de.net.wiesenfarth.mainpegel

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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.github.mikephil.charting.charts.LineChart
import de.net.wiesenfarth.mainpegel.Variable.CONST
import de.net.wiesenfarth.mainpegel.AlarmManager.NotificationHelper
import de.net.wiesenfarth.mainpegel.AlarmManager.PegelScheduler
import de.net.wiesenfarth.mainpegel.Graph.PegelUiHelper
import de.net.wiesenfarth.mainpegel.API.PegelWorker
import de.net.wiesenfarth.mainpegel.R
import de.net.wiesenfarth.mainpegel.SettingsActivity
import java.util.Calendar
import java.util.concurrent.TimeUnit

/*******************************************************
 * Programm:  MainPegel
 *
 * Beschreibung:
 * Zeigt aktuelle Pegelstände am Main für
 * unterschiedliche Messstationen
 *
 * @Website:   wiesenfarth-net.de
 * @Autor:     Bollogg
 * @Datum:     2025-11-17
 * @Version:   2026.01
********************************************************/
class MainActivity : AppCompatActivity() {
    // GUID der ausgewählten Messstelle
    private var localityGuid: String? = null
    private var showSettings = true

    // Einstellungen aus SharedPreferences
    private lateinit var prefs: SharedPreferences
    // Aktualisierungsintervall
    private var intervalMinutes = 0

    // UI-Elemente
    private lateinit var textViewPegelstand: TextView
    private lateinit var buttonAktualisieren: Button
    private lateinit var lineChart: LineChart

    private lateinit var pegelReceiver: BroadcastReceiver
    private val isReceiverRegistered = false
    private var settingsChanged = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // NotificationChannel einmalig initialisieren
        NotificationHelper.ensureChannel(this)
        checkExactAlarmPermission() //Erlauben von Wecker und Errinerungen

        // Aktiviert moderne “Edge-to-Edge”-Darstellung
        this.enableEdgeToEdge()

        // Debug: Prüfen, ob INTERNET-Permission gesetzt ist
        Log.e(
            "NET", "INTERNET PERMISSION: " +
                    (checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)
        )

        // Toolbar aktivieren
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Korrekte Abstände für Status-/Navigationsleisten setzen
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById<View>(R.id.main),
            OnApplyWindowInsetsListener { v: View, insets: WindowInsetsCompat? ->
                val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
                v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            })

        // Einstellungen laden
        NotificationHelper.ensureChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf<String>(
                        Manifest.permission.POST_NOTIFICATIONS
                    ), 1
                )
            }
        }

        prefs = getSharedPreferences("settings", MODE_PRIVATE)
        localityGuid = prefs.getString("locality_guid", CONST.WUERZBURG)
        intervalMinutes = prefs.getInt("interval_minutes", 15)

        // Übergabe an eigene Methode
        updateWithLocality(localityGuid, intervalMinutes)


        val request =
            PeriodicWorkRequest.Builder(PegelWorker::class.java, 15, TimeUnit.MINUTES)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "pegel_update",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )

        // UI-Referenzen
        textViewPegelstand = findViewById<TextView>(R.id.textViewPegelstand)
        buttonAktualisieren = findViewById<Button>(R.id.buttonAktualisieren)
        lineChart = findViewById<LineChart>(R.id.lineChart)

        // Scheduler starten (Hintergrund-Aktualisierungen)
        PegelScheduler.schedule(this)

        // Direkt den ersten Pegel laden
        //PegelUiHelper.ladePegelstand(this, textViewPegelstand, lineChart, prefs);
        //PegelUiHelper.forceWidgetUpdate(this);

        // Button: manuelles Aktualisieren
        buttonAktualisieren!!.setOnClickListener(View.OnClickListener { v: View ->
            PegelUiHelper.ladePegelstand(this, textViewPegelstand, lineChart, prefs)
        })

        // Broadcast empfangen (vom Widget / Service)
        pegelReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.i("MAIN", "Broadcast empfangen → ladePegelstand()")
                PegelUiHelper.ladePegelstand(context, textViewPegelstand, lineChart, prefs)
                //PegelUiHelper.forceWidgetUpdate(context);
            }
        }

        val filter = IntentFilter()
        filter.addAction(ACTION_PEGEL_UPDATE)

        //Regestrieren von pegelReciver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //für Android 13 und größer
            registerReceiver(pegelReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            // kleiner Android 13
            registerReceiver(pegelReceiver, filter)
        }
    }

    override fun onStart() {
        super.onStart()

        //settingsLauncher.launch(
        //    Intent(this, SettingsActivity::class.java)
        //)

    }

    override fun onResume() {
        super.onResume()

        // Einstellungen neu laden, falls in Settings geändert
        prefs = getSharedPreferences("settings", MODE_PRIVATE)
        localityGuid = prefs.getString("locality_guid", CONST.WUERZBURG)
        intervalMinutes = prefs.getInt("interval_minutes", 15)

        // Scheduler & API neu starten
        // updateWithLocality(localityGuid, intervalMinutes);
        // Nach Rückkehr aus Activity (Settings, Info) erneut laden
        // Pegel lesen
        PegelUiHelper.ladePegelstand(this, textViewPegelstand, lineChart, prefs)

        // Scheduler starte Task für Hintergrund-Aktualisierungen
        PegelScheduler.schedule(this)

        // Graph aktualisieren
        updateWithLocality(localityGuid, intervalMinutes)
        settingsChanged = false
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Pegel lesen
        PegelUiHelper.ladePegelstand(this, textViewPegelstand, lineChart, prefs)
    }
    /*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SETTINGS && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("settings_changed", false)) {
                settingsChanged = true
            }
        }
    }
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()

        // Settings öffnen
        if (id == R.id.action_settings) {
            Toast.makeText(this, getString(R.string.toast_settings), Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SettingsActivity::class.java))
            return true

            // About
        } else if (id == R.id.action_about) {
            Toast.makeText(this, getString(R.string.toast_info), Toast.LENGTH_SHORT).show()
            startActivity((Intent(this, InfoActivity::class.java)))
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    /*******************************************************
     * updateWithLocality
     *
     * Wird nach Änderungen in SettingsActivity genutzt
     * (GUID + Intervall).
     */
    private fun updateWithLocality(guid: String?, min: Int) {
        Log.i("SETTINGS", "Messstelle: " + guid)
        Log.i("SETTINGS", "Intervall: " + min + " min")

        // Platzhalter für spätere API-Fortführung
    }


    /*******************************************************
     * berechneStartVerzoegerung
     *
     * Berechnet die Verzögerung für den ersten
     * WorkManager-Aufruf (1, 16, 31, 46 Minuten)
     */
    private fun berechneStartVerzoegerung(): Long {
        val cal = Calendar.getInstance()
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)

        // Triggerpunkte im 15-Minuten-Raster
        val trigger = intArrayOf(1, 16, 31, 46)
        var nextMin = 0

        // Nächsten passenden Trigger finden
        for (t in trigger) {
            if (minute < t || (minute == t && second == 0)) {
                nextMin = t
                break
            }
        }

        // Falls wir schon vorbei sind → nächste Stunde
        if (nextMin == 0) {
            nextMin = trigger[0]
            cal.add(Calendar.HOUR_OF_DAY, 1)
        }

        cal.set(Calendar.MINUTE, nextMin)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val now = System.currentTimeMillis()
        val target = cal.getTimeInMillis()

        return target - now
    }

    private fun checkExactAlarmPermission() {
        // Nur Android 12 oder neuer benötigt das!

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            return
        }

        val am = getSystemService(ALARM_SERVICE) as AlarmManager

        if (am.canScheduleExactAlarms()) {
            Log.i("PERMISSION", "Exact-Alarms sind erlaubt.")
            return
        }

        Log.w("PERMISSION", "Exact-Alarms NICHT erlaubt → User muss es erlauben.")

        // Benutzer auffordern, die Einstellung zu aktivieren
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        intent.setData(Uri.parse("package:" + getPackageName()))

        startActivity(intent)
    }

    private fun getIntSafe(prefs: SharedPreferences, key: String?, def: Int): Int {
        try {
            return prefs.getInt(key, def)
        } catch (e: ClassCastException) {
            val f = prefs.getFloat(key, def.toFloat())
            val v = Math.round(f)

            prefs.edit().putInt(key, v).apply()
            return v
        }
    }

    companion object {
        const val ACTION_PEGEL_UPDATE: String = "de.net.wiesenfarth.mainpegel.PEGEL_UPDATE"

        private const val REQUEST_SETTINGS = 1001
    }
}

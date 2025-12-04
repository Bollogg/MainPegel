package de.wiesenfarth.mainpegel;

import android.app.AlarmManager;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.Nullable;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/*******************************************************
 * Programm:  MainPegel
 *
 * Beschreibung:
 *   Zeigt aktuelle Pegelstände am Main für unterschiedliche Messstationen
 *
 * @Website:   https://www.wiesenfarth-net.de
 * @Autor:     Bollog
 * @Datum:     2025-11-17
 * @Version:   2025.11
 *******************************************************/

public class MainActivity extends AppCompatActivity {

  public static final String ACTION_PEGEL_UPDATE =
      "de.wiesenfarth.mainpegel.PEGEL_UPDATE";

  private static final int REQUEST_SETTINGS = 1001;

  // GUID der ausgewählten Messstelle
  private String localityGuid;

  // Einstellungen aus SharedPreferences
  private SharedPreferences prefs;

  // Aktualisierungsintervall
  private int intervalMinutes;

  // UI-Elemente
  private TextView textViewPegelstand;
  private Button buttonAktualisieren;
  private LineChart lineChart; // MPAndroidChart Diagramm

  private BroadcastReceiver pegelReceiver;
  private boolean isReceiverRegistered = false;
  private boolean settingsChanged = true;

  @Override
  protected void onResume() {
    super.onResume();

//  if (settingsChanged == true) {
    // Einstellungen neu laden, falls in Settings geändert
    prefs = getSharedPreferences("settings", MODE_PRIVATE);
    localityGuid = prefs.getString("locality_guid", CONST.WUERZBURG);
    intervalMinutes = prefs.getInt("interval_minutes", 15);

    // Scheduler & API neu starten
    updateWithLocality(localityGuid, intervalMinutes);
    // Nach Rückkehr aus Settings erneut laden
    //ToDo: ladePegelstand();
    //ToDo: forceWidgetUpdate();  // Widget sofort aktualisieren
    PegelUiHelper.ladePegelstand(this, textViewPegelstand, lineChart, prefs);
    //PegelUiHelper.forceWidgetUpdate(this);

    // Scheduler starten (Hintergrund-Aktualisierungen)
    PegelScheduler.schedule(this);

    // Graph aktualisieren
    updateWithLocality(localityGuid, intervalMinutes);
    settingsChanged = false;
//    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // NotificationChannel einmalig initialisieren
    NotificationHelper.ensureChannel(this);
    checkExactAlarmPermission(); //Erlauben von Wecker und Errinerungen

    // Aktiviert moderne “Edge-to-Edge”-Darstellung
    EdgeToEdge.enable(this);

    // Debug: Prüfen, ob INTERNET-Permission gesetzt ist
    Log.e("NET", "INTERNET PERMISSION: " +
        (checkSelfPermission(android.Manifest.permission.INTERNET) == 0));

    // Toolbar aktivieren
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    // Korrekte Abstände für Status-/Navigationsleisten setzen
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });

    // Einstellungen laden
    NotificationHelper.ensureChannel(this);
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
          != PackageManager.PERMISSION_GRANTED) {

        requestPermissions(new String[]{
            android.Manifest.permission.POST_NOTIFICATIONS
        }, 1);
      }
    }

    prefs = getSharedPreferences("settings", MODE_PRIVATE);
    localityGuid = prefs.getString("locality_guid", CONST.WUERZBURG);
    intervalMinutes = prefs.getInt("interval_minutes", 15);

    // Übergabe an eigene Methode
    updateWithLocality(localityGuid, intervalMinutes);


    PeriodicWorkRequest request =
        new PeriodicWorkRequest.Builder(PegelWorker.class, 15, TimeUnit.MINUTES)
            .build();

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "pegel_update",
        ExistingPeriodicWorkPolicy.UPDATE,
        request
    );

    // UI-Referenzen
    textViewPegelstand = findViewById(R.id.textViewPegelstand);
    buttonAktualisieren = findViewById(R.id.buttonAktualisieren);
    lineChart = findViewById(R.id.lineChart);

    // Scheduler starten (Hintergrund-Aktualisierungen)
    PegelScheduler.schedule(this);

    // Direkt den ersten Pegel laden
    PegelUiHelper.ladePegelstand(this, textViewPegelstand, lineChart, prefs);
    //PegelUiHelper.forceWidgetUpdate(this);

    // Button: manuelles Aktualisieren
    buttonAktualisieren.setOnClickListener(v -> {
      PegelLogic.run(this);
      PegelUiHelper.ladePegelstand(this, textViewPegelstand, lineChart, prefs);
      //PegelUiHelper.forceWidgetUpdate(this);

    });

    // Broadcast empfangen (vom Widget / Service)
    pegelReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.i("MAIN", "Broadcast empfangen → ladePegelstand()");
        //ToDo: ladePegelstand();
        PegelUiHelper.ladePegelstand(context, textViewPegelstand, lineChart, prefs);
        //PegelUiHelper.forceWidgetUpdate(context);
      }
    };

    IntentFilter filter = new IntentFilter();
    filter.addAction(ACTION_PEGEL_UPDATE);

    //Regestrieren von pegelReciver
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      //für Android 13 und größer
      registerReceiver(pegelReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    } else {
      // kleiner Android 13
      registerReceiver(pegelReceiver, filter);
    }

  }

  @Override
  protected void onPause() {
    super.onPause();
    //PegelUiHelper.ladePegelstand(this, textViewPegelstand, lineChart, prefs);
    //PegelUiHelper.forceWidgetUpdate(this);
  }
  @Override
  protected void onStop() {
    super.onStop();
    //PegelUiHelper.ladePegelstand(this, textViewPegelstand, lineChart, prefs);
    //PegelUiHelper.forceWidgetUpdate(this);
  }
  @Override
  protected void onDestroy() {
    super.onDestroy();
    //ToDo: forceWidgetUpdate();  // Widget sofort aktualisieren
    PegelUiHelper.ladePegelstand(this, textViewPegelstand, lineChart, prefs);
    //PegelUiHelper.forceWidgetUpdate(this);
    //if (pegelReceiver != null) {
    //  unregisterReceiver(pegelReceiver);
    //}
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_SETTINGS && resultCode == RESULT_OK) {
      if (data != null && data.getBooleanExtra("settings_changed", false)) {
        settingsChanged = true;
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Menü (Settings/About) einfügen
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    // Settings öffnen
    if (id == R.id.action_settings) {
      Toast.makeText(this, getString(R.string.toast_settings), Toast.LENGTH_SHORT).show();
      startActivity(new Intent(this, SettingsActivity.class));
      return true;

      // About
    } else if (id == R.id.action_about) {
      Toast.makeText(this, getString(R.string.toast_about), Toast.LENGTH_SHORT).show();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  /*******************************************************
   * updateWithLocality
   *
   * Wird nach Änderungen in SettingsActivity genutzt
   * (GUID + Intervall).
   *******************************************************/
  private void updateWithLocality(String guid, int min) {
    Log.i("SETTINGS", "Messstelle: " + guid);
    Log.i("SETTINGS", "Intervall: " + min + " min");

    // Platzhalter für spätere API-Fortführung
  }


  /*******************************************************
   * berechneStartVerzoegerung
   *
   * Berechnet die Verzögerung für den ersten
   * WorkManager-Aufruf (1, 16, 31, 46 Minuten)
   *******************************************************/
  private long berechneStartVerzoegerung() {

    Calendar cal = Calendar.getInstance();
    int minute = cal.get(Calendar.MINUTE);
    int second = cal.get(Calendar.SECOND);

    // Triggerpunkte im 15-Minuten-Raster
    int[] trigger = {1, 16, 31, 46};
    int nextMin = 0;

    // Nächsten passenden Trigger finden
    for (int t : trigger) {
      if (minute < t || (minute == t && second == 0)) {
        nextMin = t;
        break;
      }
    }

    // Falls wir schon vorbei sind → nächste Stunde
    if (nextMin == 0) {
      nextMin = trigger[0];
      cal.add(Calendar.HOUR_OF_DAY, 1);
    }

    cal.set(Calendar.MINUTE, nextMin);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);

    long now = System.currentTimeMillis();
    long target = cal.getTimeInMillis();

    return target - now;
  }

  private void checkExactAlarmPermission() {

    // Nur Android 12 oder neuer benötigt das!
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
      return;
    }

    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

    if (am.canScheduleExactAlarms()) {
      Log.i("PERMISSION", "Exact-Alarms sind erlaubt.");
      return;
    }

    Log.w("PERMISSION", "Exact-Alarms NICHT erlaubt → User muss es erlauben.");

    // Benutzer auffordern, die Einstellung zu aktivieren
    Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
    intent.setData(Uri.parse("package:" + getPackageName()));

    startActivity(intent);
  }

  private String formatTime(String apiTime) {
    try {
      // Beispiel apiTime: "2025-12-02T12:15:00+01:00" oder "2025-12-02T12:15:00Z"
      OffsetDateTime odt = OffsetDateTime.parse(apiTime);

      // In lokale Zeitzone umwandeln (Europe/Berlin oder System-Zeitzone)
      ZonedDateTime zdt = odt.atZoneSameInstant(ZoneId.systemDefault());

      // Ausgabeformat HH:mm
      DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm", Locale.GERMANY);
      return zdt.format(fmt);

    } catch (Exception e) {
      e.printStackTrace(); // für Debug
      return apiTime; // fallback: unverändert anzeigen
    }
  }

  private int getIntSafe(SharedPreferences prefs, String key, int def) {
    try {
      return prefs.getInt(key, def);
    } catch (ClassCastException e) {
      float f = prefs.getFloat(key, def);
      int v = Math.round(f);

      prefs.edit().putInt(key, v).apply();
      return v;
    }
  }
}

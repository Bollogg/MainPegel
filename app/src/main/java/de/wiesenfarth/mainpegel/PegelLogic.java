package de.wiesenfarth.mainpegel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PegelLogic {

  private static int lastValue = -1;
  public static final String CHANNEL_ID = "pegel_alarm";

  public static boolean run(Context context) {

    Log.i("LOGIC", "PegelLogic.run() gestartet");

    SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    String localityGuid = prefs.getString("locality_guid", CONST.WUERZBURG);
    int hours = prefs.getInt("graph_hours", 4);

    String startParam = "PT" + hours + "H";

    PegelApiService api = RetrofitClient.getApiService();
    Call<List<PegelResponse>> call = api.getPegelstand(localityGuid, startParam);

    Log.d("LOGIC", "API URL: " + call.request().url());

    call.enqueue(new Callback<List<PegelResponse>>() {
      @Override
      public void onResponse(Call<List<PegelResponse>> call, Response<List<PegelResponse>> res) {

        if (!res.isSuccessful()) {
          storeErrorState(context);
          sendUpdateBroadcast(context);
          return;
        }

        List<PegelResponse> list = res.body();

        if (list == null || list.isEmpty()) {
          storeErrorState(context);
          sendUpdateBroadcast(context);
          return;
        }

        // letzter Wert
        PegelResponse last = list.get(list.size() - 1);

        // Zeit formatieren
        String formattedTime = last.getTimestamp();

        // Pegelanstieg prüfen
        handleNewPegel(context, last.getValue(), formattedTime);

        // CACHE SPEICHERN
        SharedPreferences cache = context.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = cache.edit();

        e.putInt("last_value", last.getValue());
        e.putString("last_time", formattedTime);
        e.putInt("count", list.size());

        // Verlauf
        for (int idx = 0; idx < list.size(); idx++) {
          PegelResponse r = list.get(idx);
          e.putInt("value_" + idx, r.getValue());
          e.putString("timestamp_" + idx, r.getTimestamp());
        }

        e.apply();

        Log.i("LOGIC", "Pegel gespeichert → Broadcast wird gesendet");
        sendUpdateBroadcast(context);
      }

      @Override
      public void onFailure(Call<List<PegelResponse>> call, Throwable t) {
        storeErrorState(context);
        sendUpdateBroadcast(context);
        Log.e("LOGIC", "API Fehler: " + t.getMessage());
      }
    });

    return true;
  }

  // 🔥 KORREKT: handleNewPegel AUSSERHALB der Callback Klasse
  private static void handleNewPegel(Context ctx, int newValue, String time) {

    if (lastValue < 0) {
      lastValue = newValue;
      return;
    }

    int delta = newValue - lastValue;

    // Schwellwert aus Settings laden, default = 15 cm
    SharedPreferences prefs = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE);
    String s = prefs.getString("wave_threshold", "15");
    // Flags aus Settings laden
    boolean vibrateEnabled = prefs.getBoolean("vibrate_alarm", true);
    boolean soundEnabled   = prefs.getBoolean("sound_alarm", true);

    int threshold;
    try {
      threshold = Integer.parseInt(s);
    } catch (Exception ex) {
      threshold = 15;  // fallback
    }

    if (delta >= threshold) {
      // System-Benachrichtigung senden (mit Standardton!)
      NotificationHelper.sendWaveAlert(ctx, newValue, delta, time);
      // Vibration
      if (vibrateEnabled == true) {
        vibrateDevice(ctx);
      }
      if(soundEnabled == true){
        playSystemNotificationSound(ctx);
      }
   }

    lastValue = newValue;
  }
  private static void storeErrorState(Context c) {
    SharedPreferences cache = c.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE);
    cache.edit()
        .putString("last_time", "Netzwerkfehler")
        .apply();
  }
  private static void sendUpdateBroadcast(Context c) {
    Intent bc = new Intent(PegelWidget.UPDATE_ACTION);
    c.sendBroadcast(bc);
  }
  private static void ToDosendUpdateBroadcast(Context c) {
    Intent bc = new Intent(MainActivity.ACTION_PEGEL_UPDATE);
    c.sendBroadcast(bc);
  }
  private static void vibrateDevice(Context ctx) {

    Vibrator vibrator = null;

    // Android 12+ (API 31+)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
      VibratorManager vm = (VibratorManager) ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
      if (vm != null) vibrator = vm.getDefaultVibrator();
    } else {
      // Android < 12
      vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
    }

    if (vibrator == null || !vibrator.hasVibrator()) {
      Log.e("VIBRATION", "Kein Vibrator vorhanden!");
      return;
    }

    // Vibrationsmuster (funktioniert auf allen Geräten)
    long[] pattern = {0, 500, 200, 700};

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      vibrator.vibrate(
          VibrationEffect.createWaveform(pattern, -1)
      );
    } else {
      vibrator.vibrate(pattern, -1);
    }
  }

  private static void ToDo_vibrateDevice(Context ctx) {

    Vibrator vibrator;

    // Android 12+ → VibratorManager
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
      VibratorManager vm =
          (VibratorManager) ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
      vibrator = vm != null ? vm.getDefaultVibrator() : null;

    } else {
      // Vor Android 12
      vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
    }

    if (vibrator != null && vibrator.hasVibrator()) {
      vibrator.vibrate(
          VibrationEffect.createOneShot(
              800,  // Dauer
              VibrationEffect.DEFAULT_AMPLITUDE
          )
      );
    }
  }
  private static void playSystemNotificationSound(Context ctx) {
    try {
      Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
      RingtoneManager.getRingtone(ctx, uri).play();
    } catch (Exception e) {
      Log.e("LOGIC", "Konnte Systemton nicht abspielen", e);
    }
  }
}

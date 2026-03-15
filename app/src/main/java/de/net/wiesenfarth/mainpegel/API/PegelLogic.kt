package de.net.wiesenfarth.mainpegel.API

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import de.net.wiesenfarth.mainpegel.Variable.CONST
import de.net.wiesenfarth.mainpegel.AlarmManager.NotificationHelper
import de.net.wiesenfarth.mainpegel.Widget.PegelWidget
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/*******************************************************
 * Objekt:      PegelLogic
 *
 * Beschreibung:
 * -----------------------------------------------------
 * Zentrale Geschäftslogik für Pegel- und Temperaturdaten.
 *
 * Dieses Singleton verarbeitet alle Daten zwischen:
 *  • REST-API
 *  • lokalem Cache
 *  • Alarmfunktionen
 *  • Widget / UI Updates
 *
 * Architekturprinzip:
 * -----------------------------------------------------
 * API → SharedPreferences (Cache)
 * UI  → liest ausschließlich aus Cache
 *
 * Dadurch sind App, Widget und Hintergrunddienste
 * entkoppelt und synchron.
 *
 * Hauptaufgaben:
 * -----------------------------------------------------
 * • Abrufen von Pegel- und Temperaturdaten über Retrofit
 * • Zusammenführen der API-Ergebnisse
 * • Alarmprüfung bei Pegelanstieg
 * • Speicherung der Messwerte im Cache
 * • Aktualisieren von UI und Widget über Broadcast
 *
 * Thread-Sicherheit:
 * -----------------------------------------------------
 * Es darf immer nur EIN API-Lauf aktiv sein.
 * isRunning verhindert parallele Netzwerkanfragen.
 *
 * Autor:  Bollogg
 * Stand:  2026-03-15
 *******************************************************/
object PegelLogic {

  /** Fehlerstatus beim Abruf der Pegeldaten */
  private var waterError = false

  /** Fehlerstatus beim Abruf der Temperaturdaten */
  private var tempError = false

  /**
   * Letzter bekannter Pegelwert.
   *
   * Wird benötigt, um Pegelanstieg zu berechnen.
   * -1 bedeutet: noch kein Wert vorhanden.
   */
  private var lastValue = -1

  /**
   * Schutz gegen parallele API-Aufrufe.
   * true = ein Abruf läuft bereits
   */
  private var isRunning = false

  /**
   * Startet einen vollständigen API-Abruf.
   *
   * Ablauf:
   * ------------------------------------------------
   * 1. Prüfen ob bereits ein Lauf aktiv ist
   * 2. Einstellungen laden
   * 3. Zwei parallele API-Requests starten
   *      • Pegelstand
   *      • Wassertemperatur
   * 4. Ergebnisse zusammenführen
   *
   * @param context ApplicationContext
   * @return true wenn Abruf gestartet wurde
   */
  @Synchronized
  fun run(context: Context, onFinished: (() -> Unit)? = null): Boolean {

    // Letzten bekannten Pegelwert aus Cache laden
    if (lastValue < 0) {
      val cache = context.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)
      lastValue = cache.getInt("last_value", -1)
    }

    // Abbrechen wenn bereits ein Abruf läuft
    if (isRunning) {
      Log.w("API/PegelLogic", "⛔ Abgebrochen – bereits ein Lauf aktiv")
      return false
    }

    isRunning = true
    waterError = false
    tempError = false

    Log.i("API/PegelLogic", "PegelLogic.run() gestartet")

    // Einstellungen laden
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val localityGuid: String = prefs.getString("locality_guid", CONST.WUERZBURG)!!
    val hours = prefs.getInt("graph_hours", 4)

    // Zeitraum für API (ISO-8601 Dauerformat)
    val startParam = "PT${hours}H"

    // Retrofit API-Service
    val api = RetrofitClient.apiService

    // API Calls vorbereiten
    val callW = api.getPegelstand(localityGuid, startParam)
    val callWT = api.getWassertemperatur(localityGuid, startParam)

    // Ergebniscontainer
    var waterList: List<PegelResponse>? = null
    var tempList: List<TempResponse>? = null

    /**
     * Synchronisationsmethode.
     *
     * Wird nach jedem API-Callback aufgerufen.
     * Verarbeitung erfolgt erst wenn beide Antworten vorliegen.
     */
    fun finishIfReady() {

      if (waterList == null || tempList == null) return

      try {

        if (waterError && tempError) {
          storeErrorState(context)
        } else {
          processResults(context, waterList, tempList)
        }

        // UI und Widget informieren
        sendDataUpdatedBroadcast(context)

      } catch (e: Exception) {

        Log.e("API/PegelLogic", "Fehler bei der Datenverarbeitung", e)

      } finally {

        isRunning = false
        onFinished?.invoke()
        Log.i("API/PegelLogic", "PegelLogic.run() abgeschlossen")

      }
    }

    /** API Callback Pegelstand */
    callW.enqueue(object : Callback<List<PegelResponse>> {

      override fun onResponse(
        call: Call<List<PegelResponse>>,
        res: Response<List<PegelResponse>>
      ) {
        waterList = res.body() ?: emptyList()
        finishIfReady()
      }

      override fun onFailure(call: Call<List<PegelResponse>>, t: Throwable) {
        waterError = true
        waterList = emptyList()
        finishIfReady()
      }
    })

    /** API Callback Wassertemperatur */
    callWT.enqueue(object : Callback<List<TempResponse>> {

      override fun onResponse(
        call: Call<List<TempResponse>>,
        res: Response<List<TempResponse>>
      ) {
        tempList = res.body() ?: emptyList()
        finishIfReady()
      }

      override fun onFailure(call: Call<List<TempResponse>>, t: Throwable) {
        tempError = true
        tempList = emptyList()
        finishIfReady()
      }
    })

    return true
  }

  /**
   * Verarbeitet die API-Ergebnisse und speichert sie im Cache.
   */
  private fun processResults(
    context: Context,
    list: List<PegelResponse>?,
    temp: List<TempResponse>?
  ) {

    if (list == null || list.isEmpty()) {
      storeErrorState(context)
      return
    }

    val lastWater = list.last()

    val formattedTime = formatTime(lastWater.timestamp)

    // Pegelanstieg prüfen
    handleNewPegel(context, lastWater.value, formattedTime)

    val cache = context.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)
    val e = cache.edit()

    // Letzten Pegelwert speichern
    e.putInt("last_value", lastWater.value)
    e.putString("last_time", formattedTime)

    // Anzahl Messwerte
    e.putInt("count", list.size)

    /**
     * Verlauf speichern
     */
    for ((idx, r) in list.withIndex()) {

      e.putInt("value_$idx", r.value)
      e.putString("timestamp_$idx", r.timestamp)
      e.putString("Time_$idx", formatTime(r.timestamp) ?: "--:--")

      val waterTime = OffsetDateTime.parse(r.timestamp)

      // Temperaturwert zum gleichen Zeitpunkt suchen
      val matchingTemp = temp?.mapNotNull {

        try {

          val tTime = OffsetDateTime.parse(it.timestamp)

          if (!tTime.isAfter(waterTime))
            Pair(tTime, it.value.toFloat())
          else null

        } catch (e: Exception) {
          null
        }

      }?.maxByOrNull { it.first }

      if (matchingTemp != null) {
        e.putFloat("temp_$idx", matchingTemp.second)
      } else {
        e.remove("temp_$idx")
      }
    }

    /**
     * Letzte Temperatur speichern
     */
    if (temp != null && temp.isNotEmpty()) {

      val lastTemp = temp.last()

      e.putFloat("last_temp", lastTemp.value.toFloat())
      e.putString("last_temp_time", formatTime(lastTemp.timestamp))

    } else {

      e.putFloat("last_temp", -999f)
      e.remove("last_temp_time")

    }

    e.apply()
  }

  /** Sendet Broadcast zur Aktualisierung von Widget und UI */
  private fun sendDataUpdatedBroadcast(context: Context) {

    val intent = Intent(PegelWidget.ACTION_DATA_UPDATED)
    intent.setPackage(context.packageName)

    context.sendBroadcast(intent)
  }

  /**
   * Prüft Pegelanstieg und löst ggf. Alarm aus.
   */
  private fun handleNewPegel(ctx: Context, newValue: Int, time: String?) {

    if (lastValue < 0) {
      lastValue = newValue
      return
    }

    val delta = newValue - lastValue

    val prefs = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)

    val threshold = prefs.getString("wave_threshold", "15")?.toIntOrNull() ?: 15

    if (delta >= threshold) {

      NotificationHelper.sendWaveAlert(
        ctx,
        newValue.toFloat(),
        delta.toFloat(),
        time ?: "--"
      )

      if (prefs.getBoolean("vibrate_alarm", true))
        vibrateDevice(ctx)

      if (prefs.getBoolean("sound_alarm", true))
        playSystemNotificationSound(ctx)
    }

    lastValue = newValue
  }

  /** Speichert Fehlerstatus im Cache */
  private fun storeErrorState(c: Context) {

    c.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)
      .edit()
      .putString("last_time", "Netzwerkfehler")
      .apply()
  }

  /** Löst Vibrationsalarm aus */
  private fun vibrateDevice(ctx: Context) {

    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      (ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
      ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    val pattern = longArrayOf(0, 500, 200, 700)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
    } else {
      vibrator.vibrate(pattern, -1)
    }
  }

  /** Spielt den System-Benachrichtigungston ab */
  private fun playSystemNotificationSound(ctx: Context) {

    try {

      val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
      RingtoneManager.getRingtone(ctx, uri).play()

    } catch (e: Exception) {

      Log.e("API/PegelLogic", "Konnte Systemton nicht abspielen", e)

    }
  }

  /**
   * Wandelt API-Zeitstempel in lokale Uhrzeit um.
   *
   * Format: HH:mm
   */
  private fun formatTime(apiTime: String?): String? {

    return try {

      val odt = OffsetDateTime.parse(apiTime)
      val zdt = odt.atZoneSameInstant(ZoneId.systemDefault())

      DateTimeFormatter
        .ofPattern("HH:mm", Locale.GERMANY)
        .format(zdt)

    } catch (e: Exception) {

      apiTime

    }
  }
}
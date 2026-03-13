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
 * Dieses Singleton kapselt die komplette Datenverarbeitung
 * zwischen API, Cache, Alarmfunktion und UI-Aktualisierung.
 *
 * Die Klasse arbeitet vollständig asynchron über Retrofit
 * und speichert alle relevanten Daten im lokalen Cache.
 *
 * Architektur-Prinzip:
 * -----------------------------------------------------
 * - API speichert ausschließlich in SharedPreferences
 * - UI liest ausschließlich aus dem Cache
 * - Widget wird per Broadcast aktualisiert
 *
 * Dadurch sind App, Widget und Hintergrunddienste
 * immer synchron und voneinander entkoppelt.
 *
 *
 * Hauptaufgaben:
 * -----------------------------------------------------
 * • Asynchroner Abruf von:
 *     - Pegelstand (Wasserhöhe)
 *     - Wassertemperatur
 *
 * • Gemeinsame Verarbeitung beider API-Ergebnisse
 *
 * • Schwellwertüberwachung (Pegelanstieg)
 *
 * • Auslösen eines Alarms bei kritischem Anstieg:
 *     - Notification
 *     - Vibration (optional)
 *     - Systemton (optional)
 *
 * • Persistierung aller Daten im Cache:
 *     - letzter Pegelwert
 *     - Verlaufsliste
 *     - Zeitstempel
 *     - Temperatur
 *
 * • Broadcast an Widget & UI nach Datenänderung
 *
 *
 * Thread-Sicherheit:
 * -----------------------------------------------------
 * - Es darf immer nur EIN API-Lauf aktiv sein.
 * - Zugriff wird über @Synchronized abgesichert.
 * - isRunning verhindert parallele Netzwerkanfragen.
 *
 *
 * Datenfluss:
 * -----------------------------------------------------
 * run()
 *   → Retrofit Call Wasser
 *   → Retrofit Call Temperatur
 *   → finishIfReady()
 *       → Cache speichern
 *       → Alarm prüfen
 *       → Broadcast senden
 *
 *
 * Verwendete Komponenten:
 * -----------------------------------------------------
 * - RetrofitClient
 * - PegelResponse
 * - NotificationHelper
 * - PegelWidget (ACTION_DATA_UPDATED)
 *
 *
 * Autor:  Bollogg
 * Stand:  2026-02-13
 *******************************************************/
 object PegelLogic {
  /** Gibt an, ob beim Pegelabruf ein Fehler aufgetreten ist */
  private var waterError = false
  /** Gibt an, ob beim Temperaturabruf ein Fehler aufgetreten ist */
  private var tempError  = false

  /**
   * Letzter bekannter Pegelwert in Zentimetern.
   *
   * -1 bedeutet: Es wurde noch kein Wert verarbeitet.
   * Wird zur Berechnung der Pegeldifferenz verwendet.
   */
  private var lastValue = -1
  //private var lastValue = cache.getInt("last_value", -1)


  /**
   * Verhindert parallele API-Aufrufe.
   * true = ein Abruf läuft bereits
   */
  private var isRunning = false

  /** Notification-Channel-ID für Pegelalarme */
  const val CHANNEL_ID: String = "pegel_alarm"

  /**
   * Startet einen vollständigen API-Abruf.
   *
   * Verhalten:
   * -----------------------------------------------------
   * - Prüft, ob bereits ein Lauf aktiv ist.
   * - Lädt Benutzereinstellungen (Messstelle, Zeitraum).
   * - Startet zwei parallele Retrofit-Requests:
   *      • Pegelstand
   *      • Wassertemperatur
   *
   * Die eigentliche Verarbeitung erfolgt erst,
   * wenn BEIDE Requests abgeschlossen sind.
   *
   * @param context Gültiger Application-Context
   * @return true  → Lauf gestartet
   *         false → Lauf bereits aktiv
   */
  @Synchronized
  fun run(context: Context): Boolean {


    if (lastValue < 0) {
      val cache = context.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)
      lastValue = cache.getInt("last_value", -1)
    }

    // Schutz gegen parallele Läufe
    if (isRunning) {
      Log.w("LOGIC", "⛔ Abgebrochen – bereits ein Lauf aktiv")
      return false
    }

    isRunning = true
    waterError = false
    tempError = false

    Log.i("LOGIC", "PegelLogic.run() gestartet")

    // Einstellungen laden
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val localityGuid: String = prefs.getString("locality_guid", CONST.WUERZBURG)!!
    val hours = prefs.getInt("graph_hours", 4)

    // Startzeitraum für API (ISO-8601 Dauerformat)
    val startParam = "PT${hours}H"

    // Retrofit API-Aufruf vorbereiten
    val api = RetrofitClient.apiService
    val callW = api.getPegelstand(localityGuid, startParam)
    val callWT = api.getWassertemperatur(localityGuid, startParam)

    // Gemeinsame Ergebnis-Container
    var waterList: List<PegelResponse>? = null
    var tempList: List<TempResponse>? = null

    /**
     * Interne Synchronisationsmethode.
     *
     * Wird nach jeder API-Antwort aufgerufen.
     * Die Verarbeitung erfolgt erst, wenn:
     *
     *   waterList != null
     *   UND
     *   tempList  != null
     *
     * Aufgaben:
     * -----------------------------------------------------
     * - Fehlerbehandlung
     * - Letzten Pegelwert ermitteln
     * - Alarmprüfung durchführen
     * - Verlauf in Cache speichern
     * - Broadcast senden
     *
     * Diese Methode ist der zentrale Abschluss
     * eines API-Zyklus.
     */
    fun finishIfReady() {

      if (waterList == null ) return //ToDo: || tempList == null) return

      try {
        if (waterError && tempError) {
          storeErrorState(context)
          sendDataUpdatedBroadcast(context)
          return
        }

        val list = waterList ?: return
        val temp = tempList ?: return

        if (list.isEmpty()) {
          storeErrorState(context)
          sendDataUpdatedBroadcast(context)
          return
        }


        val lastWater = list.last()
        val formattedTime = formatTime(lastWater.timestamp)

        handleNewPegel(context, lastWater.value, formattedTime)

        val cache = context.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)
        val e = cache.edit()

        // ---- Wasserstand ----
        e.putInt("last_value", lastWater.value)
        e.putString("last_time", formattedTime)
        e.putInt("count", list.size)

      /*ToDo: löschen
        for ((idx, r) in list.withIndex()) {
          e.putInt("value_$idx", r.value)
          e.putFloat("temp_$idx", tempValue)

          e.putString("timestamp_$idx", r.timestamp)
        }
*/

        // ---- Temperatur Map erstellen (timestamp → value) ----
        for ((idx, r) in list.withIndex()) {

          e.putInt("value_$idx", r.value)
          e.putString("timestamp_$idx", r.timestamp)

          // NEU: Formatiert die Zeit für den Graph (z.B. "14:30")
          val fTime = formatTime(r.timestamp) ?: "--:--"
          e.putString("Time_$idx", fTime)

          // passende Temperatur suchen (letzte bekannte <= Zeit)
          val waterTime = OffsetDateTime.parse(r.timestamp)

          val matchingTemp = temp
          .mapNotNull {
            try {
              val tTime = OffsetDateTime.parse(it.timestamp)
              if (!tTime.isAfter(waterTime)) Pair(tTime, it.value.toFloat())
              else null
            } catch (e: Exception) { null }
          }
          .maxByOrNull { it.first }

          if (matchingTemp != null) {
            e.putFloat("temp_$idx", matchingTemp.second)
          } else {
            e.remove("temp_$idx")
          }
        }

        // ---- Temperatur (letzter Wert speichern) ----
        if (temp.isNotEmpty()) {
          val lastTemp = temp.last()
          e.putFloat("last_temp", lastTemp.value.toFloat())
          e.putString("last_temp_time", formatTime(lastTemp.timestamp))
        } else {
          e.putFloat("last_temp", -999f)
          e.remove("last_temp_time")   // optional, aber sauber
        }
        e.apply()
        sendDataUpdatedBroadcast(context)
      } finally {
      isRunning = false
    }
  }
    // Asynchroner API-Aufruf
    callW.enqueue(object : Callback<List<PegelResponse>> {
      override fun onResponse(
        call: Call<List<PegelResponse>>,
        res: Response<List<PegelResponse>>
      ) {
        if (res.isSuccessful && res.body() != null) {
          waterList = res.body()
        } else {
          waterList = emptyList()
        }
        finishIfReady()
      }

      override fun onFailure(call: Call<List<PegelResponse>>, t: Throwable) {
        waterError = true
        waterList  = emptyList()
        finishIfReady()
      }
    })

    callWT.enqueue(object : Callback<List<TempResponse>> {
      override fun onResponse(
        call: Call<List<TempResponse>>,
        res: Response<List<TempResponse>>
      ) {
        if (res.isSuccessful && res.body() != null) {
          tempList = res.body()
        } else {
          tempList = emptyList()
        }
        finishIfReady()
      }

      override fun onFailure(call: Call<List<TempResponse>>, t: Throwable) {
        tempError = true
        tempList  = emptyList()
        finishIfReady()
      }
    })
    return true
  }

  /**
   * Sendet einen System-Broadcast an das Widget
   * und ggf. an aktive Activities.
   *
   * ACTION:
   * PegelWidget.ACTION_DATA_UPDATED
   *
   * Zweck:
   * UI und Widget neu rendern,
   * nachdem neue Daten im Cache gespeichert wurden.
   */
  private fun sendDataUpdatedBroadcast(context: Context) {
    val intent = Intent(PegelWidget.ACTION_DATA_UPDATED)
    context.sendBroadcast(intent)
  }


  /**
   * Prüft, ob der Pegelanstieg den
   * konfigurierten Schwellwert überschreitet.
   *
   * Vergleich:
   * delta = newValue - lastValue
   *
   * Wenn delta >= threshold:
   *     → Notification
   *     → Vibration (optional)
   *     → Systemton (optional)
   *
   * @param ctx Android Context
   * @param newValue Neuer Pegelwert (cm)
   * @param time Formatierte Uhrzeit (HH:mm)
   */
  private fun handleNewPegel(ctx: Context, newValue: Int, time: String?) {

    // Erster Wert → nur speichern
    if (lastValue < 0) {
      lastValue = newValue
      return
    }

    val delta = newValue - lastValue

    // Einstellungen laden
    val prefs = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val threshold = prefs
      .getString("wave_threshold", "15")
      ?.toIntOrNull() ?: 15
    val vibrateEnabled = prefs.getBoolean("vibrate_alarm", true)
    val soundEnabled = prefs.getBoolean("sound_alarm", true)

    // Schwellwert überschritten → Alarm
    if (delta >= threshold) {
      NotificationHelper.sendWaveAlert(
        ctx,
        newValue.toFloat(),
        delta.toFloat(),
        time ?: "--"
      )

      if (vibrateEnabled) vibrateDevice(ctx)
      if (soundEnabled) playSystemNotificationSound(ctx)
    }

    lastValue = newValue
  }

  /**
   * Speichert einen Fehlerzustand im lokalen Cache.
   *
   * Wird verwendet bei:
   * - Netzwerkfehler
   * - HTTP-Fehler
   * - Leeren Daten
   *
   * UI kann anhand des gespeicherten Wertes
   * einen Fehler anzeigen.
   */
  private fun storeErrorState(c: Context) {
    val cache = c.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)
    cache.edit()
        .putString("last_time", "Netzwerkfehler")
        .apply()
	}

  /**
   * Führt eine Geräte-Vibration aus.
   *
   * Unterstützt:
   * - Android < O (legacy API)
   * - Android O+
   * - Android S+ (VibratorManager)
   *
   * Verwendet ein fest definiertes Vibrationsmuster.
   */
  private fun vibrateDevice(ctx: Context) {
    val vibrator: Vibrator? =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager?
        vm?.defaultVibrator
      } else {
        ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
      }

    if (vibrator == null || !vibrator.hasVibrator()) {
      Log.e("VIBRATION", "Kein Vibrator vorhanden!")
      return
    }

    val pattern = longArrayOf(0, 500, 200, 700)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
    } else {
      vibrator.vibrate(pattern, -1)
    }
  }

  /**
   * Spielt den systemweiten Benachrichtigungston ab.
   *
   * Verwendet den aktuell vom Benutzer eingestellten
   * Notification-Sound.
   *
   * Fehler beim Abspielen werden protokolliert.
   */
  private fun playSystemNotificationSound(ctx: Context) {
    try {
      val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
      RingtoneManager.getRingtone(ctx, uri).play()
    } catch (e: Exception) {
      Log.e("LOGIC", "Konnte Systemton nicht abspielen", e)
    }
  }

  /**
   * Konvertiert einen ISO-8601-Zeitstempel der API
   * in die lokale Uhrzeit (Format HH:mm).
   *
   * Beispiel:
   * 2026-02-12T13:45:00+00:00 → 14:45 (je nach Zeitzone)
   *
   * @param apiTime Zeitstempel der API
   * @return Formatierte Uhrzeit oder Originalwert bei Fehler
   */
  private fun formatTime(apiTime: String?): String? {
    return try {
      val odt = OffsetDateTime.parse(apiTime)
      val zdt = odt.atZoneSameInstant(ZoneId.systemDefault())
      val fmt = DateTimeFormatter.ofPattern("HH:mm", Locale.GERMANY)
      zdt.format(fmt)
    } catch (e: Exception) {
      apiTime // Fallback: Originalwert
    }
  }
}
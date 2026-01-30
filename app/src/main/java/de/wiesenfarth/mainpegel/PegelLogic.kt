package de.wiesenfarth.mainpegel

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import de.wiesenfarth.mainpegel.NotificationHelper.sendWaveAlert
import de.wiesenfarth.mainpegel.RetrofitClient.apiService
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
 * Zentrale Geschäftslogik der App.
 *
 * - Ruft Pegeldaten über die REST-API ab (Retrofit)
 * - Prüft Pegeländerungen (Schwellwert)
 * - Löst bei starkem Anstieg Alarm (Notification, Ton, Vibration) aus
 * - Speichert aktuelle Pegeldaten im lokalen Cache
 * - Informiert Widgets/UI per Broadcast über neue Daten
 *
 * Dieses Objekt ist als Singleton implementiert, da
 * immer nur ein Pegelabruf gleichzeitig laufen darf.
 *
 * @Website:   wiesenfarth-net.de
 * @Autor:     Bollogg
 ********************************************************/
object PegelLogic {
    /** letzter bekannter Pegelwert (cm), -1 = noch kein Wert vorhanden */
    private var lastValue = -1

    /** verhindert parallele API-Aufrufe */
    private var isRunning = false

    /** Notification-Channel-ID für Pegelalarme */
    const val CHANNEL_ID: String = "pegel_alarm"

    /**
     * Startet den Pegelabruf.
     *
     * @param context gültiger Android-Context (z. B. aus Receiver oder Service)
     * @return true, wenn der Abruf gestartet wurde, false bei Abbruch
     */
    fun run(context: Context): Boolean {

        // Schutz gegen parallele Läufe
        if (isRunning) {
            Log.w("LOGIC", "⛔ Abgebrochen – bereits ein Lauf aktiv")
            return false
        }

        isRunning = true
        Log.i("LOGIC", "PegelLogic.run() gestartet")

        // Einstellungen laden
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val localityGuid: String = prefs.getString("locality_guid", CONST.WUERZBURG)!!
        val hours = prefs.getInt("graph_hours", 4)

        // Startzeitraum für API (ISO-8601 Dauerformat)
        //ToDo val startParam = "PT" + hours + "H"
        val startParam = "PT${hours}H"

        // Retrofit API-Aufruf vorbereiten
        val api = apiService
        val call = api.getPegelstand(localityGuid, startParam)

        //Log.d("LOGIC", "API URL: " + call.request().url)
        Log.d("LOGIC", "API URL: ${call.request().url}")

        // Asynchroner API-Aufruf
        call.enqueue(object : Callback<MutableList<PegelResponse>> {
            /**
             * Erfolgreiche HTTP-Antwort (200–299)
             */
            override fun onResponse(
                call: Call<MutableList<PegelResponse>>,
                res: Response<MutableList<PegelResponse>>
            ) {
                isRunning = false

                // HTTP-Fehler (z. B. 404, 500)
                if (!res.isSuccessful()) {
                    storeErrorState(context)
                    sendUpdateBroadcast(context)
                    return
                }

                val list = res.body()

                // Keine oder leere Daten
                if (list == null || list.isEmpty()) {
                    storeErrorState(context)
                    sendUpdateBroadcast(context)
                    return
                }

                // Letzter (aktueller) Pegelwert
                val last = list.get(list.size - 1)

                // Zeitstempel formatiert (HH:mm)
                val formattedTime = formatTime(last.timestamp)

                // Prüfen, ob der Pegel stark gestiegen ist
                handleNewPegel(context, last.value, formattedTime)

                // --- CACHE SPEICHERN ---
                val cache = context.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)
                val e = cache.edit()

                e.putInt("last_value", last.value)
                e.putString("last_time", formattedTime)
                e.putInt("count", list.size)

                // Verlauf speichern (für Diagramm)
                for (idx in list.indices) {
                    val r = list.get(idx)
                    e.putInt("value_" + idx, r.value)
                    e.putString("timestamp_" + idx, r.timestamp)
                }

                e.apply()

                Log.i("LOGIC", "Pegel gespeichert → Broadcast wird gesendet")
                sendUpdateBroadcast(context)
            }

            /**
             * Netzwerkfehler, Timeout oder Exception
             */
            override fun onFailure(call: Call<MutableList<PegelResponse>>, t: Throwable) {
                isRunning = false

                storeErrorState(context)
                sendUpdateBroadcast(context)
                Log.e("LOGIC", "API Fehler: " + t.message)
            }
        })
        return true
    }

    /**
     * Prüft, ob der Pegelanstieg den Schwellwert überschreitet
     * und löst ggf. Alarmaktionen aus.
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
        val s = prefs.getString("wave_threshold", "15")!!
        val vibrateEnabled = prefs.getBoolean("vibrate_alarm", true)
        val soundEnabled = prefs.getBoolean("sound_alarm", true)

        // Schwellwert parsen
        val threshold = try {
            s.toInt()
        } catch (ex: Exception) {
            15
        }

        // Schwellwert überschritten → Alarm
        if (delta >= threshold) {
            sendWaveAlert(ctx, newValue.toFloat(), delta.toFloat(), time)

            if (vibrateEnabled) vibrateDevice(ctx)
            if (soundEnabled) playSystemNotificationSound(ctx)
        }

        lastValue = newValue
    }

    /** Speichert einen Fehlerzustand im Cache */
    private fun storeErrorState(c: Context) {
        val cache = c.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)
        cache.edit()
            .putString("last_time", "Netzwerkfehler")
            .apply()
    }

    /** Informiert Widgets und UI über neue Pegeldaten */
    private fun sendUpdateBroadcast(c: Context) {
        val bc = Intent(c, PegelWidget::class.java)
        bc.action = PegelWidget.UPDATE_ACTION
        c.sendBroadcast(bc)
    }

    /** Führt eine Geräte-Vibration aus (API-abhängig) */
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


    /** Spielt den System-Benachrichtigungston ab */
    private fun playSystemNotificationSound(ctx: Context) {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(ctx, uri).play()
        } catch (e: Exception) {
            Log.e("LOGIC", "Konnte Systemton nicht abspielen", e)
        }
    }

    /**
     * Wandelt API-Zeitstempel in lokale Uhrzeit (HH:mm) um
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

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

object PegelLogic {
    private var lastValue = -1
    private var isRunning = false
    const val CHANNEL_ID: String = "pegel_alarm"

    fun run(context: Context): Boolean {
        if (isRunning) {
            Log.w("LOGIC", "⛔ Abgebrochen – bereits ein Lauf aktiv")
            return false
        }
        isRunning = true
        Log.i("LOGIC", "PegelLogic.run() gestartet")

        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val localityGuid: String = prefs.getString("locality_guid", CONST.WUERZBURG)!!
        val hours = prefs.getInt("graph_hours", 4)

        val startParam = "PT" + hours + "H"

        val api = apiService
        val call = api.getPegelstand(localityGuid, startParam)

        Log.d("LOGIC", "API URL: " + call!!.request().url)

        call.enqueue(object : Callback<MutableList<PegelResponse?>?> {
            override fun onResponse(
                call: Call<MutableList<PegelResponse?>?>,
                res: Response<MutableList<PegelResponse>?>
            ) {
                isRunning = false

                if (!res.isSuccessful()) {
                    storeErrorState(context)
                    sendUpdateBroadcast(context)
                    return
                }

                val list = res.body()

                if (list == null || list.isEmpty()) {
                    storeErrorState(context)
                    sendUpdateBroadcast(context)
                    return
                }

                // letzter Wert
                val last = list.get(list.size - 1)

                // Zeit formatieren
                val formattedTime = formatTime(last.timestamp)
                // Pegelanstieg prüfen
                handleNewPegel(context, last.value, formattedTime)

                // CACHE SPEICHERN
                val cache = context.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)
                val e = cache.edit()

                e.putInt("last_value", last.value)
                e.putString("last_time", formattedTime)
                e.putInt("count", list.size)

                // Verlauf
                for (idx in list.indices) {
                    val r = list.get(idx)
                    e.putInt("value_" + idx, r.value)
                    e.putString("timestamp_" + idx, r.timestamp)
                }

                e.apply()

                Log.i("LOGIC", "Pegel gespeichert → Broadcast wird gesendet")
                sendUpdateBroadcast(context)
            }

            override fun onFailure(call: Call<MutableList<PegelResponse?>?>, t: Throwable) {
                isRunning = false

                storeErrorState(context)
                sendUpdateBroadcast(context)
                Log.e("LOGIC", "API Fehler: " + t.message)
            }
        })
        return true
    }

    // 🔥 KORREKT: handleNewPegel AUSSERHALB der Callback Klasse
    private fun handleNewPegel(ctx: Context, newValue: Int, time: String?) {
        if (lastValue < 0) {
            lastValue = newValue
            return
        }

        val delta = newValue - lastValue

        // Schwellwert aus Settings laden, default = 15 cm
        val prefs = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val s: String = prefs.getString("wave_threshold", "15")!!
        // Flags aus Settings laden
        val vibrateEnabled = prefs.getBoolean("vibrate_alarm", true)
        val soundEnabled = prefs.getBoolean("sound_alarm", true)

        var threshold: Int
        try {
            threshold = s.toInt()
        } catch (ex: Exception) {
            threshold = 15 // fallback
        }

        if (delta >= threshold) {
            // System-Benachrichtigung senden (mit Standardton!)
            sendWaveAlert(ctx, newValue.toFloat(), delta.toFloat(), time)
            // Vibration
            if (vibrateEnabled == true) {
                vibrateDevice(ctx)
            }
            if (soundEnabled == true) {
                playSystemNotificationSound(ctx)
            }
        }

        lastValue = newValue
    }

    private fun storeErrorState(c: Context) {
        val cache = c.getSharedPreferences("pegel_cache", Context.MODE_PRIVATE)
        cache.edit()
            .putString("last_time", "Netzwerkfehler")
            .apply()
    }

    private fun sendUpdateBroadcast(c: Context) {
        val bc = Intent(c, PegelWidget::class.java)
        bc.setAction(PegelWidget.UPDATE_ACTION)
        c.sendBroadcast(bc)
    }

    private fun vibrateDevice(ctx: Context) {
        var vibrator: Vibrator? = null

        // Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager?
            if (vm != null) vibrator = vm.getDefaultVibrator()
        } else {
            // Android < 12
            vibrator = ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        }

        if (vibrator == null || !vibrator.hasVibrator()) {
            Log.e("VIBRATION", "Kein Vibrator vorhanden!")
            return
        }

        // Vibrationsmuster (funktioniert auf allen Geräten)
        val pattern = longArrayOf(0, 500, 200, 700)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(pattern, -1)
            )
        } else {
            vibrator.vibrate(pattern, -1)
        }
    }

    private fun playSystemNotificationSound(ctx: Context?) {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(ctx, uri).play()
        } catch (e: Exception) {
            Log.e("LOGIC", "Konnte Systemton nicht abspielen", e)
        }
    }

    private fun formatTime(apiTime: String?): String? {
        try {
            // Beispiel apiTime: "2025-12-02T12:15:00+01:00" oder "2025-12-02T12:15:00Z"
            val odt = OffsetDateTime.parse(apiTime)

            // In lokale Zeitzone umwandeln (Europe/Berlin oder System-Zeitzone)
            val zdt = odt.atZoneSameInstant(ZoneId.systemDefault())

            // Ausgabeformat HH:mm
            val fmt = DateTimeFormatter.ofPattern("HH:mm", Locale.GERMANY)
            return zdt.format(fmt)
        } catch (e: Exception) {
            e.printStackTrace() // für Debug
            return apiTime // fallback: unverändert anzeigen
        }
    }
}

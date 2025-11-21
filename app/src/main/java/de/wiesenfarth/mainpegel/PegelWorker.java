package de.wiesenfarth.mainpegel;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
/*******************************************************
 * Programm:  PegelWorker
 *
 * Beschreibung:
 *  Pegelstand wird alle 15 min aufgerufen
 *  ToDo: Konstante für Würzburg ändern
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-20
 *******************************************************/

public class PegelWorker extends Worker {

    public PegelWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        PegelApiService apiService = RetrofitClient.getApiService();
        Call<List<PegelResponse>> call = apiService.getPegelstand(CONST.WUERZBURG);

        try {
            Response<List<PegelResponse>> response = call.execute();
            if (response.body() != null && !response.body().isEmpty()) {

                // letzter Wert = aktueller Messwert
                List<PegelResponse> list = response.body();
                PegelResponse pegel = list.get(list.size() - 1);

                Log.d("PegelWorker", "Neuer Pegelstand: " + pegel.getValue() + " " + pegel.getTimestamp());
                return Result.success();
            } else {
                return Result.retry();
            }
        } catch (Exception e) {
            Log.e("PegelWorker", "Fehler beim Abrufen der Daten", e);
            return Result.retry();
        }
    }
}
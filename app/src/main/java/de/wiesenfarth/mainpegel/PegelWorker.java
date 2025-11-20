package de.wiesenfarth.mainpegel;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Call;
import retrofit2.Response;
/*******************************************************
 * Programm:  PegelWorker
 *
 * Beschreibung:
 *  Begelstand wird alle 15 min aufgerufen
 *
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
        Call<PegelResponse> call = apiService.getPegelstand(CONST.WUERZBURG);

        try {
            Response<PegelResponse> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {

                PegelResponse pegel = response.body();
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
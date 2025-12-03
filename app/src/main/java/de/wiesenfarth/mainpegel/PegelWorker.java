package de.wiesenfarth.mainpegel;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/*******************************************************
 * Klasse:      PegelWorker
 *
 * Beschreibung:
 *   Worker für die Android WorkManager-API.
 *
 *   Dieser Worker wird (falls aktiviert) regelmäßig
 *   ausgeführt und ruft die PegelLogic auf, die den
 *   aktuellen Wasserstand über die API abfragt.
 *
 *   WorkManager wird in diesem Projekt als Alternative
 *   zum AlarmManager genutzt — aktuell ruft der Worker
 *   nur PegelLogic.run() auf.
 *
 * Ablauf:
 *   WorkManager → PegelWorker.doWork()
 *              → PegelLogic.run(Context)
 *              → true  → Result.success()
 *              → false → Result.retry()
 *
 * Vorteil:
 *   WorkManager verarbeitet:
 *     - Doze Mode
 *     - Akkuoptimierungen
 *     - App-Neustarts
 *     - Netzverfügbarkeit (falls definiert)
 *
 * Autor:        Bollog
 * Datum:        2025-11-20
 *******************************************************/
public class PegelWorker extends Worker {

    /**
     * Standard-Konstruktor für WorkManager.
     *
     * @param context  App- oder Worker-Kontext
     * @param params   Worker-Konfiguration (vom System)
     */
    public PegelWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    /**
     * Hauptmethode des Workers.
     * Wird auf einem Hintergrund-Thread vom WorkManager ausgeführt.
     *
     * Rückgabe:
     *   SUCCESS → Job abgeschlossen
     *   RETRY   → WorkManager versucht es später erneut
     */
    @NonNull
    @Override
    public Result doWork() {

      // Startet die Logik, welche den Pegelstand per API holt
      boolean ok = PegelLogic.run(getApplicationContext());
      PegelLogic.run(getApplicationContext());

      // Neuer Arlarm für den nächsten Durchlauf
      PegelScheduler.schedule(getApplicationContext());
      // Ergebnis für WorkManager zurückgeben
      return ok ? Result.success() : Result.retry();
    }
}

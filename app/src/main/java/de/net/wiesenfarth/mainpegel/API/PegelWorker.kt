package de.net.wiesenfarth.mainpegel.API

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.net.wiesenfarth.mainpegel.AlarmManager.PegelScheduler

/*******************************************************
 * Klasse:      PegelWorker
 *
 * Beschreibung:
 * Worker für die Android WorkManager-API.
 *
 * Dieser Worker wird (falls aktiviert) regelmäßig
 * ausgeführt und ruft die PegelLogic auf, die den
 * aktuellen Wasserstand über die API abfragt.
 *
 * WorkManager wird in diesem Projekt als Alternative
 * zum AlarmManager genutzt — aktuell ruft der Worker
 * nur PegelLogic.run() auf.
 *
 * Ablauf:
 * WorkManager → PegelWorker.doWork()
 * → PegelLogic.run(Context)
 * → true  → Result.success()
 * → false → Result.retry()
 *
 * Vorteil:
 * WorkManager verarbeitet:
 * - Doze Mode
 * - Akkuoptimierungen
 * - App-Neustarts
 * - Netzverfügbarkeit (falls definiert)
 *
 * Autor:        Bollog
 * Datum:        2025-11-20
 */
class PegelWorker
/**
 * Standard-Konstruktor für WorkManager.
 *
 * @param context  App- oder Worker-Kontext
 * @param params   Worker-Konfiguration (vom System)
 */
	(context: Context, params: WorkerParameters) : Worker(context, params) {
	/**
	 * Hauptmethode des Workers.
	 * Wird auf einem Hintergrund-Thread vom WorkManager ausgeführt.
	 *
	 * Rückgabe:
	 * SUCCESS → Job abgeschlossen
	 * RETRY   → WorkManager versucht es später erneut
	 */
	override fun doWork(): Result {
    // Startet die Logik, welche den Pegelstand per API holt

    val ok = PegelLogic.run(getApplicationContext())
    PegelLogic.run(getApplicationContext())

    // Neuer Arlarm für den nächsten Durchlauf
    PegelScheduler.schedule(getApplicationContext())
    // Ergebnis für WorkManager zurückgeben
    return if (ok) Result.success() else Result.retry()
}
}
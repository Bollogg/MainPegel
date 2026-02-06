package de.net.wiesenfarth.mainpegel.API

import de.net.wiesenfarth.mainpegel.API.PegelResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/*******************************************************
 * Programm:  PegelApiService
 *
 * Beschreibung:
 * Retrofit-Interface zum Abrufen von Pegeldaten.
 * Lädt die Messwerte einer bestimmten Station für einen
 * angegebenen Zeitraum (z. B. letzte 6 Stunden).
 *
 * Wird vom PegelForegroundService genutzt, um die
 * aktuellen Daten aus der Pegel-API zu laden.
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-20
 *******************************************************/
interface PegelApiService {
  /**
   * Ruft Messwerte einer Pegelstation ab.
   *
   * Beispiel-URL (schematisch):
   * stations/123456/W/measurements.json?start=PT6H
   * ToDo: für Wassertemperatur
   * ToDo: /stations/915d76e1-3bf9-4e37-9a9a-4d144cd771cc/WT/measurements.json
   *
   * @param station  Stations-ID, wird dynamisch in die URL eingesetzt
   * @param start    Zeitraum, z. B. "PT6H" für letzte 6 Stunden
   * @return         Liste von PegelResponse-Objekten als Retrofit-Call
   */
  @GET("stations/{station}/W/measurements.json")
  fun getPegelstand(
    @Path("station") station: String,  // Platzhalter in der URL
    @Query("start") start: String // Query-Parameter für Zeitintervall
  ): Call<MutableList<PegelResponse>>
}
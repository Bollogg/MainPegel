package de.net.wiesenfarth.mainpegel.API

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit-Serviceinterface für die Pegel-Online REST-API (WSV).
 *
 * Dieses Interface definiert die HTTP-Endpunkte zum Abruf von:
 * - Wasserstand (`W`)
 * - Wassertemperatur (`WT`)
 *
 * Die konkrete Implementierung wird zur Laufzeit automatisch
 * von Retrofit erzeugt.
 *
 * Verwendung:
 * Wird von [PegelLogic] genutzt, um aktuelle Messdaten
 * einer Pegelstation abzurufen.
 *
 * Basis-URL:
 * https://www.pegelonline.wsv.de/webservices/rest-api/v2/
 *
 * Allgemeines Endpunkt-Schema:
 * ```
 * stations/{station}/{parameter}/measurements.json?start=PT6H
 * ```
 *
 * Der Parameter `start` erwartet eine ISO-8601-Dauerangabe,
 * z. B.:
 * - `PT6H`  → letzte 6 Stunden
 * - `PT24H` → letzte 24 Stunden
 *
 * @author Bollogg
 * @since 2026-02-12
 */
interface PegelApiService {

  /**
   * Ruft Wasserstandsmesswerte (`W`) einer Pegelstation ab.
   *
   * Beispiel:
   * ```
   * stations/123456/W/measurements.json?start=PT6H
   * ```
   *
   * @param station
   * Stations-ID (GUID oder numerische ID),
   * wird dynamisch in die URL eingesetzt.
   *
   * @param start
   * Zeitraum im ISO-8601-Dauerformat
   * (z. B. `PT6H` für die letzten 6 Stunden).
   *
   * @return
   * Ein [Call] mit einer unveränderlichen Liste von [PegelResponse].
   * Jeder Eintrag enthält Zeitstempel und Messwert.
   */
  @GET("stations/{station}/W/measurements.json")
  fun getPegelstand(
    @Path("station") station: String,
    @Query("start") start: String
  ): Call<List<PegelResponse>>

  /**
   * Ruft Wassertemperatur-Messwerte (`WT`) einer Pegelstation ab.
   *
   * Beispiel:
   * ```
   * stations/123456/WT/measurements.json?start=PT6H
   * ```
   *
   * @param station
   * Stations-ID (GUID oder numerische ID).
   *
   * @param start
   * Zeitraum im ISO-8601-Dauerformat.
   *
   * @return
   * Ein [Call] mit einer unveränderlichen Liste von [PegelResponse].
   */
  @GET("stations/{station}/WT/measurements.json")
  fun getWassertemperatur(
    @Path("station") station: String,
    @Query("start") start: String
  ): Call<List<TempResponse>>
}

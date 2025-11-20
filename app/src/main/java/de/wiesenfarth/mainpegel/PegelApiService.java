package de.wiesenfarth.mainpegel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
/*******************************************************
 * Programm:  PegelApiService
 *
 * Beschreibung:
 *  Daten von API laden
 *
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-20
 *******************************************************/

public interface PegelApiService {

    @GET("stations/{station}/W/currentmeasurement.json")
    Call<PegelResponse> getPegelstand(@Path("station") String station);
}

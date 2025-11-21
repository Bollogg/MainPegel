package de.wiesenfarth.mainpegel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
/*******************************************************
 * Programm:  PegelApiService
 *
 * Beschreibung:
 *  Daten von API laden für 6 Stunden
 *  für Station (Konstante)
 *
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-20
 *******************************************************/
public interface PegelApiService {

    @GET("stations/{station}/W/measurements.json?start=PT4H")
    Call<List<PegelResponse>> getPegelstand(@Path("station") String station);
}

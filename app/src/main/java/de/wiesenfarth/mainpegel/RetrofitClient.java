package de.wiesenfarth.mainpegel;

import android.util.Log;

import okhttp3.OkHttpClient;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
/*******************************************************
 * Programm:  RetrofitClient
 *
 * Beschreibung:
 *  Hole die API daten vom Server mit "getApiService"
 *
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-20
 *******************************************************/

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static PegelApiService getApiService() {

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://www.pegelonline.wsv.de/webservices/rest-api/v2/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(new OkHttpClient.Builder()
                            .addInterceptor(new HttpLoggingInterceptor(
                                    msg -> Log.e("HTTP", msg))
                                    .setLevel(HttpLoggingInterceptor.Level.BODY)
                            )
                            .build()
                    )
                    .build();
        }

        return retrofit.create(PegelApiService.class);
    }
}
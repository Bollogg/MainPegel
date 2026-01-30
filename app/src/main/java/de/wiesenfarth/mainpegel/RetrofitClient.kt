package de.wiesenfarth.mainpegel

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*******************************************************
 * Klasse:      RetrofitClient
 *
 * Beschreibung:
 * Stellt eine einzige zentrale Retrofit-Instanz bereit,
 * über die API-Aufrufe an Pegelonline ausgeführt werden.
 *
 * Die Methode getApiService() liefert ein Interface
 * des Typs PegelApiService zurück → darüber werden
 * API-Endpunkte wie getPegelstand(...) aufgerufen.
 *
 * Besonderheiten:
 * - Singleton: Retrofit wird nur einmal erzeugt.
 * - HTTP-Logging aktiviert (Level.BODY)
 * → zeigt komplette Requests/Responses im Log,
 * sehr hilfreich beim Debuggen.
 * - GsonConverterFactory
 * → wandelt JSON <→ Java-Objekte (PegelResponse).
 *
 * Autor:        Bollog
 * Datum:        2025-11-20
 */
object RetrofitClient {
    // Hält die zentrale Retrofit-Instanz (Singleton)
    private var retrofit: Retrofit? = null

    @JvmStatic
    val apiService: PegelApiService
        /**
         * Liefert den API-Service für alle REST-Abfragen.
         *
         * Erzeugt bei Bedarf (Lazy Loading) ein Retrofit-Objekt.
         *
         * @return PegelApiService – Interface mit den Endpunkten
         */
        get() {
            // Retrofit nur einmal erzeugen → spart Ressourcen

            if (retrofit == null) {
                // Logging Interceptor: gibt HTTP-Daten im Log aus

                val logging = HttpLoggingInterceptor(
                    HttpLoggingInterceptor.Logger { msg: String? ->
                        Log.e(
                            "HTTP",
                            msg!!
                        )
                    }) // eigener Logger
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)

                // OkHttpClient – HTTP-Bibliothek hinter Retrofit
                val client = OkHttpClient.Builder()
                    .addInterceptor(logging) // Logging via Interceptor
                    .build()

                // Retrofit-Objekt erstellen
                retrofit = Retrofit.Builder()
                    .baseUrl("https://www.pegelonline.wsv.de/webservices/rest-api/v2/") // Basis-URL für alle Endpunkte
                    .addConverterFactory(GsonConverterFactory.create()) // JSON ↔ Java Mapping (Gson)
                    .client(client) // eigener OkHttpClient (mit Logging)
                    .build()
            }

            // API-Service zu dieser Retrofit-Instanz erzeugen
            return retrofit!!.create<PegelApiService>(PegelApiService::class.java)
        }
}

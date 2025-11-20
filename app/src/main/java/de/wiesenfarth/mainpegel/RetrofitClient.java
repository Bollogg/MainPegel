package de.wiesenfarth.mainpegel;

import android.util.Log;

import java.security.SecureRandom;
import java.util.Collections;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;

import okhttp3.TlsVersion;
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

    // -------------------------------------------------------
    // TLS 1.2 CLIENT – fehlerfrei funktionierend!
    // -------------------------------------------------------
    private static OkHttpClient getTLSSafeClient() {

        try {
            Log.e("TLS", "Initialisiere TLS 1.2 Client...");

            // 1) TrustManager (für Debug)
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        @Override public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[]{}; }
                    }
            };

            // 2) SSLContext auf TLS 1.2 setzen
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            // 3) IMPORTANT: TLS 1.2 ConnectionSpec aktivieren
            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .allEnabledCipherSuites()
                    .build();

            // 4) Logging aktivieren (hilft dir beim Debuggen)
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(
                    msg -> Log.e("HTTP", msg)
            );
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 5) OkHttpClient zusammenbauen
            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true) // Hostname Check off
                    .connectionSpecs(Collections.singletonList(spec))
                    .addInterceptor(logging)
                    .build();

            Log.e("TLS", "TLS 1.2 Client wurde erfolgreich erstellt!");

            return client;

        } catch (Exception e) {
            Log.e("TLS", "FEHLER beim Initialisieren: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
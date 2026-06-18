// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
package com.hp.jetadvantage.link.pkgmgt.connect;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hp.jetadvantage.link.pkgmgt.Constants;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.TlsVersion;

public class OXPdConnect {
    private static final String TAG = Constants.TAG + "OXPdConnect";
    private static volatile OXPdConnect instance;
    private String ip;
    private String token;

    private OXPdConnect() {
    }

    private static OkHttpClient getUnsafeOkHttpClient(OkHttpClient.Builder builder) {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                    .supportsTlsExtensions(true)
                    .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
                    .cipherSuites(
                            //CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
                            //CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
                            //CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, // 20+
                            //CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, //20+
                            //CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256, //20+
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, // 11+
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA, // 11+
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,   // 11+
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,   // 11+
                            CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,     // 1+
                            CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA,     // 1+
                            CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,   // 1-22
                            CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA,   // 11-22
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,   // 11-23
                            CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,     // 11-23
                            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,       // 9+
                            CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA       // 9+
                    )
                    .build();

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(0, TimeUnit.SECONDS)
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .connectionSpecs(Collections.singletonList(spec))
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    }).retryOnConnectionFailure(false).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static OXPdConnect getInstance() {
        if (instance == null) {
            synchronized (OXPdConnect.class) {
                if (instance == null) {
                    instance = new OXPdConnect();
                }
            }
        }
        return instance;
    }

    public final String getWebServicesHttpsRequestURL(@NonNull String host, int port) {
        if (TextUtils.isEmpty(host)) host = getIp();
        if (port <= 0) return "https://" + host;
        return "https://" + host + ":" + port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setToken(String workpathToken) {
        token = workpathToken;
    }

    public String getToken() {
        return token;
    }

    public CompletableFuture<String> sendGetRequest(String url) throws Exception {
        if (TextUtils.isEmpty(getToken())) {
            throw new IOException("Token is empty");
        }

        String duneRequestUrl = OXPdConnect.getInstance().getWebServicesHttpsRequestURL(null, 0) + url;
        return CompletableFuture.supplyAsync(() -> {
            try {
                Log.i(TAG, "duneRequestUrl: " + duneRequestUrl);
                Request request1 = (new Request.Builder())
                        .url(duneRequestUrl)
                        .addHeader("Authorization", "Bearer " + getToken())
                        .get()
                        .build();

                Response response1 = getUnsafeOkHttpClient(null).newCall(request1).execute();
                ResponseBody body1 = response1.body();
                String bodyString1 = body1 != null ? body1.string() : null;
                if (!response1.isSuccessful()) {
                    throw new IOException("REST call failed with HTTP " + response1.code() + ": " + bodyString1);
                } else {
                    return bodyString1;
                }
            } catch (Exception e) {
                Log.e(TAG, "sendGetRequest: " + e.getMessage(), e);
                throw new RuntimeException(e.getMessage());
            }
        });
    }
}

package com.example.justfly.map;

import android.content.Context;
import android.content.SharedPreferences;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.maplibre.android.module.http.HttpRequestImpl;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/** Applies the installed osmdroid HTTP settings to the optional OpenTopo tile requests. */
final class MapHttpConfiguration {
    private static final String ADDITIONAL_HEADER = "osmdroid.additionalHttpRequestProperty.";
    private static final long DEFAULT_EXPIRATION_MS = 7L * 24 * 60 * 60 * 1000;
    private static Map<String, Object> configured;

    static synchronized void install(Context context, SharedPreferences preferences) {
        Map<String, Object> settings = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : preferences.getAll().entrySet()) {
            if (entry.getKey().startsWith(ADDITIONAL_HEADER)
                    || entry.getKey().equals("osmdroid.userAgentValue")
                    || entry.getKey().equals("osmdroid.TileDownloaderFollowRedirects")
                    || entry.getKey().equals("osmdroid.tileDownloadThreads")
                    || entry.getKey().equals("osmdroid.ExpirationExtendedDuration")
                    || entry.getKey().equals("osmdroid.ExpirationOverride")) {
                settings.put(entry.getKey(), entry.getValue());
            }
        }
        if (settings.equals(configured)) {
            return;
        }
        String userAgent = preferences.getString("osmdroid.userAgentValue", context.getPackageName());
        boolean redirects = preferences.getBoolean("osmdroid.TileDownloaderFollowRedirects", true);
        int threads = Math.max(1, preferences.getInt("osmdroid.tileDownloadThreads", 2));
        long extension = preferences.getLong("osmdroid.ExpirationExtendedDuration", 0);
        long override = preferences.getLong("osmdroid.ExpirationOverride", -1);
        Map<String, String> headers = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            if (entry.getKey().startsWith(ADDITIONAL_HEADER) && entry.getValue() instanceof String) {
                headers.put(entry.getKey().substring(ADDITIONAL_HEADER.length()), (String) entry.getValue());
            }
        }
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(threads);
        dispatcher.setMaxRequestsPerHost(threads);
        OkHttpClient client = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .followRedirects(redirects)
                .followSslRedirects(redirects)
                .addInterceptor(chain -> {
                    Request.Builder request = chain.request().newBuilder().header("User-Agent", userAgent);
                    for (Map.Entry<String, String> header : headers.entrySet()) {
                        request.header(header.getKey(), header.getValue());
                    }
                    Response response = chain.proceed(request.build());
                    if (!response.isSuccessful()) {
                        return response;
                    }
                    Date expires = response.headers().getDate("Expires");
                    long lifetime = expirationLifetime(response.header("Cache-Control"),
                            expires == null ? null : expires.getTime(), System.currentTimeMillis(),
                            override, extension);
                    return response.newBuilder().header("Cache-Control", "max-age=" + Math.max(0, lifetime / 1000))
                            .removeHeader("Expires").build();
                }).build();
        HttpRequestImpl.setOkHttpClient(client);
        configured = settings;
    }

    /** Matches the old tile policy: override, max-age, Expires, then seven days. */
    static long expirationLifetime(String cacheControl, Long expires, long now,
                                   long override, long extension) {
        if (override != -1) {
            return override;
        }
        if (cacheControl != null) {
            for (String directive : cacheControl.split(", ")) {
                if (directive.startsWith("max-age=")) {
                    try {
                        return Long.parseLong(directive.substring("max-age=".length())) * 1000 + extension;
                    } catch (NumberFormatException ignored) {
                        // Invalid durations fall through to Expires, as before.
                    }
                }
            }
        }
        return (expires == null ? DEFAULT_EXPIRATION_MS : expires - now) + extension;
    }
}

package com.netbyte.vtunnel.utils;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtil {

    private static OkHttpClient client = new OkHttpClient();

    public static String get(String url,String headerName,String headerValue) throws IOException {
        Request request = new Request.Builder()
                .addHeader(headerName,headerValue)
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

}

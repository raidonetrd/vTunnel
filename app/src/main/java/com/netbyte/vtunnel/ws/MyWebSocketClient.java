package com.netbyte.vtunnel.ws;

import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;

import static org.asynchttpclient.Dsl.*;

import com.netbyte.vtunnel.model.Config;

import java.io.FileOutputStream;
import java.util.concurrent.ExecutionException;

public class MyWebSocketClient {

    public static WebSocket buildWebSocket(String url, String key)  {
        WebSocket websocket = null;
        try {
            websocket = asyncHttpClient().prepareGet(url).addHeader("key", key).setRequestTimeout(5000)
                    .execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(
                            new MyWebSocketListener()).build()).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return websocket;
    }

    public static WebSocket buildWebSocket(String url, String key, Config config, FileOutputStream out)  {
        WebSocket websocket = null;
        try {
            websocket = asyncHttpClient().prepareGet(url).addHeader("key", key).setRequestTimeout(10000)
                    .execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(
                            new MyWebSocketListener(config,out)).build()).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return websocket;
    }

}

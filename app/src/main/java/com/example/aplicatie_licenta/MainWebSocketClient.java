package com.example.aplicatie_licenta;

import java.net.URI;
import tech.gusavila92.websocketclient.WebSocketClient;

public abstract class MainWebSocketClient extends WebSocketClient {

    private boolean isRunning;

    /**
     * Initialize all the variables
     *
     * @param uri URI of the WebSocket server
     */
    public MainWebSocketClient(URI uri) {
        super(uri);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }
}

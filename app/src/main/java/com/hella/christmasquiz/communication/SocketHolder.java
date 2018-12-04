package com.hella.christmasquiz.communication;

import okhttp3.WebSocket;

public class SocketHolder {
    private static WebSocket socket;

    public static synchronized WebSocket getSocket(){
        return socket;
    }

    public static synchronized void setSocket(WebSocket socket){
        SocketHolder.socket = socket;
    }
}

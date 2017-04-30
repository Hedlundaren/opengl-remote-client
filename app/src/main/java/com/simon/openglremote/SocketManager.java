package com.simon.openglremote;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class SocketManager implements WebSocketClient.Listener {

    private ArrayList<SocketListener> socketListeners;
    private WebSocketClient client;
    private boolean isConnected;
    private MainActivity app;
    private String ipAddress;


    public SocketManager(MainActivity mainActivity) {
        app = mainActivity;
        socketListeners = new ArrayList<>();
        isConnected = false;

    }


    public void connectToServer() {
        if(!isConnected){
            setIpAddress(app.getIP());
            app.setStatus(ipAddress);
            List<BasicNameValuePair> extraHeaders = Arrays.asList(
                    new BasicNameValuePair("Cookie", "session=abcd")
            );
            client = new WebSocketClient(URI.create(ipAddress), this, extraHeaders);
            client.connect();


        }
        else{
            sendMessage("4;1");
            app.setStatus("Goodbye");
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    @Override
    public void onConnect() {
        isConnected = true;
        app.setStatus("Connected");
        app.setConnected();


    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void onMessage(byte[] data) {
    }

    @Override
    public void onDisconnect(int code, String reason) {
        isConnected = false;
        app.setStatus("Disconnected");

    }

    @Override
    public void onError(Exception error) {

    }

    public void sendMessage(String message) {
        if (this.isConnected) {
            client.send(message);
        }
    }

    public void subscribeSocketListener(SocketListener socketListener) {
        socketListeners.add(socketListener);
    }

    public void unsubscribeSocketListener(SocketListener socketListener) {
        socketListeners.remove(socketListener);
    }

    public void notifySocketListeners(int event) {
        for (SocketListener socketListener : socketListeners) {
            if (socketListener != null)
                socketListener.OnSocketEvent(event);
        }
    }

    public interface SocketListener {
        void OnSocketEvent(int event);
    }


    public void setIpAddress(String ip){
        Log.d("test", ip);
        if(ip.equals("EDUROAM")){
            ipAddress = "ws://172.22.4.45:8080/echo";
        }else if(ip.equals("NTUSECURE")){
            Log.d("test", "SI");
            ipAddress = "ws://10.27.58.171:8080/echo";
        }else{
            ipAddress = "ws://" + ip + ":8080/echo";
        }
    }
}

package com.example.aplicatie_licenta;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    // defining the elements of the function
    private MainWebSocketClient mainWebSocketClient;
    Button connectBtn, frontBtn, backBtn, rightBtn, leftBtn, autoDriveBtn, ledsBtn, buzzerBtn;
    TextView distanceText, temperatureText, humidityText;
    int ledsContor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        ledsContor = 0;

        //Create WebSocket and connect to it
        createWebSocketClient();

        //defining the variables for the views
        distanceText = findViewById(R.id.idDistanceText);
        temperatureText = findViewById(R.id.idTemperatureText);
        humidityText = findViewById(R.id.idHumidityText);

        //defining the variables for the buttons
        connectBtn = findViewById(R.id.idConnectBtn);
        frontBtn = findViewById(R.id.frontBtn);
        backBtn = findViewById(R.id.backBtn);
        rightBtn = findViewById(R.id.rightBtn);
        leftBtn = findViewById(R.id.leftBtn);
        rightBtn = findViewById(R.id.rightBtn);
        autoDriveBtn = findViewById(R.id.autoDriveBtn);
        ledsBtn = findViewById(R.id.ledsBtn);
        buzzerBtn = findViewById(R.id.buzzerBtn);



        //send distance request to the server every 2 second
        ScheduledExecutorService executorService1 = Executors.newSingleThreadScheduledExecutor();
        executorService1.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (mainWebSocketClient.isRunning()){
                    mainWebSocketClient.send("distance");
                }
            }
        }, 0, 2 , TimeUnit.SECONDS);

        //send temperature and humidity request to the server every 7 second
        ScheduledExecutorService executorService2 = Executors.newSingleThreadScheduledExecutor();
        executorService2.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (mainWebSocketClient.isRunning()){
                    mainWebSocketClient.send("temp_hum");
                }
            }
        }, 0, 7 , TimeUnit.SECONDS);


        // setting the message request for each button using lambda functions
        frontBtn.setOnClickListener(v -> mainWebSocketClient.send("front"));
        backBtn.setOnClickListener(v -> mainWebSocketClient.send("back"));
        rightBtn.setOnClickListener(v -> mainWebSocketClient.send("right"));
        leftBtn.setOnClickListener(v -> mainWebSocketClient.send("left"));
        autoDriveBtn.setOnClickListener(v -> {
            mainWebSocketClient.send("auto");
            autoDriveBtn.setBackgroundColor(Color.RED);
            }
        );
        ledsBtn.setOnClickListener(v -> {
            ledsContor ++;
            if(ledsContor % 2 == 1){
                mainWebSocketClient.send("leds_on");
                //changing the color of the button
                ledsBtn.setBackgroundColor(Color.GREEN);

            } else {
                mainWebSocketClient.send("leds_off");
                //changing the color of the button
                ledsBtn.setBackgroundColor(Color.BLUE);
            }
        });
        buzzerBtn.setOnClickListener(v -> mainWebSocketClient.send("buzzer"));

    }

    //creating the URI  to connect to the Raspberry using Raspberry IP and port 8888
    private void createWebSocketClient() {
        URI uri;
        try {
            // Connect to local host
            uri = new URI("ws://192.168.100.12:8888/ws");
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        //creating a WebSocket
        mainWebSocketClient = new MainWebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.i("WebSocket", "Session is starting");
                setRunning(true);
                //changing the text and the color of the button
                connectBtn.setText("OPENED");
                connectBtn.setBackgroundColor(Color.GREEN);
            }

            //the function used to receive datas from Raspberry and to print them on the application
            @Override
            public void onTextReceived(String message) {
                Log.i("WebSocket", "Message received");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            //the message received for distance
                            if (!message.contains("|")){
                                distanceText.setText(message);
                            }else {
                                //the message received for temperature and humidity
                                String[] infos = message.split("\\|");
                                humidityText.setText(infos[0]);
                                temperatureText.setText(infos[1]);
                            }

                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onBinaryReceived(byte[] data) {

            }

            @Override
            public void onPingReceived(byte[] data) {

            }

            @Override
            public void onPongReceived(byte[] data) {

            }

            @Override
            public void onException(Exception e) {

            }

            //the function used when the WebSocket is closed
            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Closed ");
                setRunning(false);
                //changing the text and the color of the button
                connectBtn.setText("CLOSED");
                connectBtn.setBackgroundColor(Color.RED);
            }
        };
        //if the app could not connect to Raspberry for 5ms, it closes the WebSocket
        mainWebSocketClient.setConnectTimeout(5000);
        //if the app could not receive infos from Raspberry for 10ms, it closes the WebSocket
        mainWebSocketClient.setReadTimeout(10000);
        //if the WebSocket is closed, the app tries to open another one automatically after 5ms
        mainWebSocketClient.enableAutomaticReconnection(5000);
        mainWebSocketClient.connect();
    }
}
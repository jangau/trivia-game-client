package com.hella.christmasquiz;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hella.christmasquiz.communication.GameStates;
import com.hella.christmasquiz.communication.SocketHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends Activity {
    private ProgressBar spinner;
    private Button connectButton;
    private String ipAddress;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = findViewById(R.id.spinnerIdle);
        spinner.setVisibility(View.VISIBLE);
        spinner.setIndeterminate(true);
        connectButton = findViewById(R.id.buttonConnect);
        client = new OkHttpClient();

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Input IP");

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT );
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ipAddress = input.getText().toString();

                        if (SocketHolder.getSocket() == null) {
                            // Attempt to connect to server
                            Request connectionRequest = new Request.Builder().url("ws://" + ipAddress + "/ws/game/").build();
                            GameWebSocketListener listener = new GameWebSocketListener();
                            WebSocket ws = client.newWebSocket(connectionRequest, listener);
                            SocketHolder.setSocket(ws);

                            // Hide button
                            connectButton.setVisibility(View.GONE);
                        }

//                        client.dispatcher().executorService().shutdown();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    private final class GameWebSocketListener extends WebSocketListener{
        private static final int NORMAL_CLOSE_STATUS = 1000;

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            final String message = new String(text);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject response = new JSONObject(message);
                        String type = response.get("type").toString();

                        if (type.compareTo("register_device") == 0){
                            if (GameStates.getTeam() == null){
                                JSONArray teams = response.getJSONArray("teams");
                                final int gameID = response.getInt("game");
                                final String[] teamNames = new String[teams.length()];
                                for (int i=0; i < teams.length(); i++){
                                    teamNames[i] = (String) teams.get(i);
                                }

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Select your team");
                                builder.setItems(teamNames, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        JSONObject registerSend = new JSONObject();
                                        try {
                                            registerSend.put("team", teamNames[which]);
                                            registerSend.put("game", gameID);
                                            registerSend.put("type", "register");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        SocketHolder.getSocket().send(registerSend.toString());
                                        GameStates.setTeam(teamNames[which]);
                                        Toast.makeText(getApplicationContext(), teamNames[which], Toast.LENGTH_LONG).show();
                                    }
                                });
                                builder.show();
                            }
                        }
                        if (type.compareTo("category.send") == 0){

                            String teamName = response.get("to").toString();

                            if (teamName.compareTo(GameStates.getTeam()) != 0){
                                return;
                            }

                            JSONObject categories = response.getJSONObject("categories");
                            ArrayList<String> categoriesList = new ArrayList<>();
                            ArrayList<Boolean> categoriesEnabled = new ArrayList<>();

                            for (Iterator<String> keyIterator = categories.keys(); keyIterator.hasNext(); ){
                                String categoryName = keyIterator.next();
                                categoriesList.add(categoryName);
                                categoriesEnabled.add(categories.getBoolean(categoryName));
                            }


                            Intent intent = new Intent(MainActivity.this, SelectCategory.class);
                            intent.putStringArrayListExtra("Categories", categoriesList);
                            intent.putExtra("CategoriesEnabled", categoriesEnabled);
                            startActivity(intent);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason){
            final String message = new String(reason);
            SocketHolder.setSocket(null);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectButton.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response){
            final String message = t.getMessage();
            SocketHolder.setSocket(null);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectButton.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

}

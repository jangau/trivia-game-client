package com.hella.christmasquiz;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
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

import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MainActivity extends Activity {
    private ProgressBar spinner;
    private Button connectButton;
    private String ipAddress;
    private OkHttpClient client;
    private String deviceID;

    @Override
    protected void onResume(){
        super.onResume();
        GameStates.setViewName("Main");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = findViewById(R.id.spinnerIdle);
        spinner.setVisibility(View.VISIBLE);
        spinner.setIndeterminate(true);
        connectButton = findViewById(R.id.buttonConnect);
        client = new OkHttpClient();
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

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
        public void onOpen(WebSocket webSocket, Response response){
            JSONObject connectSend = new JSONObject();
            try {
                connectSend.put("type", "connected");
                connectSend.put("device", deviceID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            webSocket.send(connectSend.toString());
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            final String message = new String(text);
            if (GameStates.getViewName() != "Main"){
                // Don't override what is being displayed
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject response = new JSONObject(message);
                        String type = response.get("type").toString();

                        if (type.compareTo("register_device") == 0){
                            final int gameID = response.getInt("game");
                            if (GameStates.getTeam() == null || (gameID != GameStates.getGameID())){
                                JSONArray teams = response.getJSONArray("teams");
                                GameStates.setGameID(gameID);
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
                                            registerSend.put("device", deviceID);
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
                            return;
                        }
                        if (type.compareTo("category.send") == 0){
                            if (GameStates.getTeam() == null){
                                // We are not registered, ignore
                                return;
                            }

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
                            if (GameStates.getViewName().compareTo("Main") == 0) {
                                GameStates.setViewName("Category");
                                startActivity(intent);
                            }

                            return;
                        }
                        if (type.compareTo("question_send") == 0){
                            if (GameStates.getTeam() == null){
                                // We are not registered, return
                                return;
                            }
                            String teamName = response.get("team").toString();

                            if (teamName.compareTo(GameStates.getTeam()) != 0 && (teamName.compareTo("all") != 0)) {
                                return;
                            }

                            String questionText = response.getString("question");
                            String questionID = response.getString("questionID");
                            JSONObject answers = response.getJSONObject("answers");
                            ArrayList<String> answerIDs = new ArrayList<String>();
                            for (Iterator<String> iter = answers.keys(); iter.hasNext(); ){
                                answerIDs.add(iter.next());
                            }
                            ArrayList<String> answersText = new ArrayList<>();

                            for (int i = 0; i < answers.length(); i++){
                                answersText.add(answers.getString(answerIDs.get(i)));
                            }

                            Intent intent = new Intent(MainActivity.this, AnswerQuestion.class);
                            intent.putExtra("Question", questionText);
                            intent.putExtra("QuestionID", questionID);
                            intent.putExtra("Answers", answersText);
                            intent.putExtra("AnswerIDS", answerIDs);
                            if (GameStates.getViewName().compareTo("Main") == 0) {
                                GameStates.setViewName("Answer");
                                startActivity(intent);
                            }
                            return;
                        }
                        if (type.compareTo("device_reconnect") == 0){
                            String devID = response.getString("device");
                            if (devID.compareTo(deviceID) == 0){
                                GameStates.setTeam(response.getString("team"));
                                GameStates.setGameID(response.getInt("game"));
                            }
                        }

                        if (type.compareTo("unregistered") == 0){
                            String devID = response.getString("device_id");
                            if (devID.compareTo(deviceID) == 0){
                                GameStates.setGameID(null);
                                GameStates.setTeam(null);
                            }
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

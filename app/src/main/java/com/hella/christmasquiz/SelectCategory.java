package com.hella.christmasquiz;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hella.christmasquiz.communication.GameStates;
import com.hella.christmasquiz.communication.SocketHolder;
import com.hella.christmasquiz.communication.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.WebSocket;

public class SelectCategory extends Activity {
    String selectedCategory = null;
    Integer selectedButtonId = null;

    @Override
    protected void onResume(){
        super.onResume();
        GameStates.setViewName("Category");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        Intent intent = getIntent();
        final List<String> categories = intent.getStringArrayListExtra("Categories");
        List<Boolean> categoriesEnabled = (List<Boolean>) intent.getSerializableExtra("CategoriesEnabled");

        // Populate categories
        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add((Button) findViewById(R.id.buttonCateg1));
        buttons.add((Button) findViewById(R.id.buttonCateg2));
        buttons.add((Button) findViewById(R.id.buttonCateg3));
        buttons.add((Button) findViewById(R.id.buttonCateg4));
        buttons.add((Button) findViewById(R.id.buttonCateg5));
        buttons.add((Button) findViewById(R.id.buttonCateg6));
        buttons.add((Button) findViewById(R.id.buttonCateg7));

        Integer btnIdx = 1;
        for (String category: categories) {
            final Button btn = buttons.get(btnIdx-1);
            btn.setText(category);
            btn.setEnabled(categoriesEnabled.get(btnIdx-1));
            btnIdx ++;

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedButtonId != null) {
                        Button oldSelectedButton = findViewById(selectedButtonId);
                        oldSelectedButton.setBackground(getResources().getDrawable(R.drawable.categorybuttons));
                        if (oldSelectedButton.getId() == btn.getId()){
                            // Deactivate the confirm
                            Button confirmButton = findViewById(R.id.buttonConfirm);
                            confirmButton.setEnabled(false);
                            confirmButton.setTextColor(getResources().getColor(R.color.hellaYellow));
                            selectedButtonId = null;
                            selectedCategory = null;
                            return;
                        }
                    } else {
                        // Activate the confirm button
                        Button confirmButton = findViewById(R.id.buttonConfirm);
                        confirmButton.setEnabled(true);
                    }
                    selectedCategory = btn.getText().toString();
                    selectedButtonId = btn.getId();
                    btn.setBackground(getResources().getDrawable(R.drawable.categorybuttonselected));
                }
            });
        }

        // Disable confirm button
        final Button confirmButton = findViewById(R.id.buttonConfirm);
        confirmButton.setEnabled(false);
        confirmButton.setTextColor(getResources().getColor(R.color.hellaYellow));

        // Set confirm button callback
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!confirmButton.isEnabled()) {
                    return;
                }

                if (selectedCategory == null){
                    Toast.makeText(getApplicationContext(), "No category selected!", Toast.LENGTH_LONG).show();
                    return;
                }


                JSONObject categorySend = new JSONObject();
                try {
                    categorySend.put("type", "category");
                    categorySend.put("team", GameStates.getTeam());
                    categorySend.put("category", selectedCategory);
                    categorySend.put("game", GameStates.getGameID());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SocketHolder.getSocket().send(categorySend.toString());

                // We get here if no error, finish this activity
                finish();

            }
        });

    }


    @Override
    public void onBackPressed() {
        // Do nothing
    }
}

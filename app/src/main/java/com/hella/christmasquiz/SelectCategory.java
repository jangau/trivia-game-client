package com.hella.christmasquiz;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;

import com.hella.christmasquiz.communication.Utils;

import java.util.ArrayList;
import java.util.List;

public class SelectCategory extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        Intent intent = getIntent();
        List<String> categories = intent.getStringArrayListExtra("Categories");
        List<Boolean> categoriesEnabled = (List<Boolean>) intent.getSerializableExtra("CategoriesEnabled");

        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add((Button) findViewById(R.id.buttonCateg1));
        buttons.add((Button) findViewById(R.id.buttonCateg2));
        buttons.add((Button) findViewById(R.id.buttonCateg3));

        Integer btnIdx = 1;
        for (String category: categories) {
            Button btn = buttons.get(btnIdx-1);
            btn.setText(category);
            btn.setEnabled(categoriesEnabled.get(btnIdx-1));
            btnIdx ++;
        }

    }
}

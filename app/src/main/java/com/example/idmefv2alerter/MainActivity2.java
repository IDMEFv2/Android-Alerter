package com.example.idmefv2alerter;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.text.TextWatcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity2 extends AppCompatActivity {

    private static MainActivity2 instance;
    public static MainActivity2 getInstance() {
        return instance;
    }

    private MyApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        instance = this;
        app = (MyApp) getApplicationContext();

        InitializeSpinnerCategory();

        Button button1 = findViewById(R.id.act2_alert);
        button1.setOnClickListener(v -> {
            finish();
        });

        Button button2 = findViewById(R.id.act2_help);
        button2.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, MainActivity3.class);
            startActivity(intent);
            finish();
        });


        InitializeTexts();
    }

    public void InitializeSpinnerCategory() {
        Spinner spinner = findViewById(R.id.category_val);
        String[] items = {"Phone", "Car", "Motorcycle", "Bike", "Truck" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        app.setUserCategory(items[0]);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = findViewById(R.id.category_val);
                String[] items = {"Phone", "Car", "Motorcycle", "Bike", "Truck" };
                app.setUserCategory(items[spinner.getSelectedItemPosition()]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
    }

    public void InitializeTexts() {
        ((EditText) findViewById(R.id.username_val)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
               app.setUser(s.toString());
            }
        });
        ((EditText) findViewById(R.id.url_val)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                app.setUrl(s.toString());
            }
        });
        ((EditText) findViewById(R.id.login_val)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                app.setLogin(s.toString());
            }
        });
        ((EditText) findViewById(R.id.password_val)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                app.setPassword(s.toString());
            }
        });
    }
}
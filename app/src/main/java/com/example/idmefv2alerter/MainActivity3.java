package com.example.idmefv2alerter;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);

        TextView textView = findViewById(R.id.help);
        textView.setText(Html.fromHtml(getString(R.string.app_help)));
        Linkify.addLinks(textView, Linkify.WEB_URLS);

        Button button1 = findViewById(R.id.act3_alert);
        button1.setOnClickListener(v -> {
            finish();
        });

        Button button2 = findViewById(R.id.act3_settings);
        button2.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity3.this, MainActivity2.class);
            startActivity(intent);
            finish();
        });

        MyApp app = (MyApp) getApplicationContext();
        if ( app.getPeriod() != 0 )
            ((Button) findViewById(R.id.act3_settings)).setEnabled(false);
    }
}
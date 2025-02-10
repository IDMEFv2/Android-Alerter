package com.example.idmefv2alerter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

import android.util.Base64;
import android.widget.Toast;

import android.Manifest;

public class MainActivity extends AppCompatActivity {

   private static MainActivity instance;
   public static MainActivity getInstance() {
       return instance;
   }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        instance = this;

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);

        UpdateGPS.scheduleAlarm(MainActivity.this, 0);

        Button mybut = findViewById(R.id.EnvoyerIDMEFv2);

        Spinner spinner = findViewById(R.id.idmefv2_spin);
        String[] items = {"manual", "10m", "30m", "1h", "6h", "12h", "24h"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            String prevpos = (String) spinner.getItemAtPosition(0);
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (prevpos == parent.getItemAtPosition(position)) {
                    return;
                }
                prevpos = (String) parent.getItemAtPosition(position);
                Spinner spinner = findViewById(R.id.idmefv2_spin);
                Integer[] items_int = {null, 10*60*1000, 30*60*1000, 1*60*60*1000, 6*60*60*1000, 12*60*60*1000, 24*60*60*1000};
                Integer sel = items_int[spinner.getSelectedItemPosition()];
                AlarmReceiver.stopAlarm(MainActivity.this);
                if (sel != null) {
                    AlarmReceiver.scheduleAlarm(MainActivity.this,items_int[(int) spinner.getSelectedItemPosition()],0);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

        mybut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendIDMEF(MainActivity.this, v);
                Toast.makeText(MainActivity.this, "IDMEFv2 sent", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateTextView(String text, Integer id) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(id);
                textView.setText(text);
            }
        });
    }

    public void sendIDMEF(Context context, View v) {
        try {
            Instant instant = Instant.now() ;
            EditText descr = findViewById(R.id.idmefv2_descr);
            EditText cat = findViewById(R.id.idmefv2_cat);
            EditText siem_srv = findViewById(R.id.idmefv2_url);
            EditText siem_login = findViewById(R.id.idmefv2_url_login);
            EditText siem_password = findViewById(R.id.idmefv2_url_pass);
            TextView myloc = findViewById(R.id.idmefv2_location);

            String myloc_text = ",         \"Location\": \"" + myloc.getText() + "\"\n";

            String source = "{\n" +
                    "     \"Description\": \"" + descr.getText() + "\",\n" +
                    "     \"Priority\": \"Medium\",\n" +
                    "     \"CreateTime\": \"" + instant.toString() + "\",\n" +
                    "     \"StartTime\": \"" + instant.toString() + "\",\n" +
                    "     \"Category\": [\n" +
                    "       \"" + cat.getText() + "\"\n" +
                    "     ],\n" +
                    "     \"Analyzer\": {\n" +
                    "       \"Name\": \"SIEM\",\n" +
                    "       \"Hostname\": \"siem.acme.com\",\n" +
                    "       \"Type\": \"Cyber\",\n" +
                    "       \"Model\": \"Concerto SIEM 5.2\",\n" +
                    "       \"Category\": [\n" +
                    "         \"SIEM\",\n" +
                    "         \"LOG\"\n" +
                    "       ],\n" +
                    "       \"Data\": [\n" +
                    "         \"Log\"\n" +
                    "       ],\n" +
                    "       \"Method\": [\n" +
                    "         \"Monitor\",\n" +
                    "         \"Signature\"\n" +
                    "       ],\n" +
                    "       \"IP\": \"192.0.2.1\"\n" +
                    "     },\n" +
                    "     \"Sensor\": [\n" +
                    "       {\n" +
                    "         \"IP\": \"192.0.2.5\",\n" +
                    "         \"Name\": \"syslog\",\n" +
                    "         \"Hostname\": \"www.acme.com\",\n" +
                    "         \"Model\": \"rsyslog 8.2110\"\n" +
                    myloc_text +
                    "       }\n" +
                    "     ],\n" +
                    "     \"Target\": [\n" +
                    "       {\n" +
                    "         \"IP\": \"192.0.2.2\",\n" +
                    "         \"Hostname\": \"www.acme.com\",\n" +
                    "         \"User\": \"root\"\n" +
                    myloc_text +
                    "       }\n" +
                    "     ]\n" +
                    " }";
            JSONObject json=new JSONObject(source);
            String jsonString = json.toString();
            new PostData().execute(siem_srv.getText().toString(),siem_login.getText().toString(),siem_password.getText().toString(),jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

  class PostData extends AsyncTask<String, Void, String> {

      @Override
      protected String doInBackground(String... strings) {
          try {
              URL url = new URL(strings[0]);
              HttpURLConnection client = (HttpURLConnection) url.openConnection();
              client.setRequestMethod("POST");
              client.setRequestProperty("Content-Type", "application/json");
              client.setRequestProperty("Accept", "application/json");

              String auth = strings[1] + ":" + strings[2];
              byte[] encodedAuth = Base64.encode(auth.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
              String authHeaderValue = "Basic " + new String(encodedAuth);
              client.setRequestProperty("Authorization", authHeaderValue);

              client.setDoOutput(true);

              try (OutputStream os = client.getOutputStream()) {
                  byte[] input = strings[3].getBytes("utf-8");
                  os.write(input, 0, input.length);
              }

              try (BufferedReader br = new BufferedReader(
                      new InputStreamReader(client.getInputStream(), "utf-8"))) {

                  StringBuilder response = new StringBuilder();

                  String responseLine = null;

                  while ((responseLine = br.readLine()) != null) {
                      response.append(responseLine.trim());
                  }

                  return response.toString();
              }

          } catch (Exception e) {
              e.printStackTrace();
              return "Fail to post the data : " + e.getMessage();
          }
      }

      @Override
      protected void onPostExecute(String result) {
      }

  }

}
package com.example.idmefv2alerter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.util.Base64;
import android.widget.Toast;

import android.Manifest;

public class MainActivity extends AppCompatActivity {

   private static MainActivity instance;
   public static MainActivity getInstance() {
       return instance;
   }
   private MyApp app;

   private Integer[] rad_col = {0,0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        instance = this;
        app = (MyApp) getApplicationContext();

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);

        UpdateGPS.scheduleAlarm(MainActivity.this, 0);

        InitializeSpinnerPriority();

        InitializeSpinnerPeriod();

        InitializeHeadButtons();

        InitializeSendIDMEFButton();

        InitializeTexts();
    }

    public void InitializeSendIDMEFButton() {
        Button mybut = findViewById(R.id.EnvoyerIDMEFv2);

        mybut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( app.getPeriod() == 0) {
                    sendIDMEF(MainActivity.this, v);
                    Toast.makeText(MainActivity.this, "IDMEFv2 sent", Toast.LENGTH_SHORT).show();
                } else {
                    if ( mybut.getText().toString() == "STOP") {
                        mybut.setBackgroundColor(0);
                        mybut.setText("START");
                        AlarmReceiver.stopAlarm(MainActivity.this);
                        EnableALL();
                    } else {
                        mybut.setBackgroundColor(Color.RED);
                        mybut.setText("STOP");
                        Integer[] items_int = {null, 1*60*1000, 10*60*1000, 30*60*1000, 1*60*60*1000, 6*60*60*1000, 12*60*60*1000, 24*60*60*1000};
                        Integer sel = items_int[app.getPeriod()];
                        AlarmReceiver.stopAlarm(MainActivity.this);
                        AlarmReceiver.scheduleAlarm(MainActivity.this,sel,0);
                        DisableALL();
                    }
                }
            }
        });
    }

    public void EnableALL() {
        ((Spinner) findViewById(R.id.priority_val)).setEnabled(true);
        ((Spinner) findViewById(R.id.period_val)).setEnabled(true);
        ((EditText) findViewById(R.id.message_val)).setEnabled(true);
        ((RadioButton) findViewById(R.id.radioButton1)).setEnabled(true);
        ((RadioButton) findViewById(R.id.radioButton1)).setTextColor(rad_col[0]);
        ((RadioButton) findViewById(R.id.radioButton2)).setEnabled(true);
        ((RadioButton) findViewById(R.id.radioButton2)).setTextColor(rad_col[1]);
        ((Button) findViewById(R.id.act1_settings)).setEnabled(true);

        ((TextView) findViewById(R.id.priority)).setEnabled(true);
        ((TextView) findViewById(R.id.period)).setEnabled(true);
        ((TextView) findViewById(R.id.latitude)).setEnabled(true);
        ((TextView) findViewById(R.id.latitude_val)).setEnabled(true);
        ((TextView) findViewById(R.id.longitude)).setEnabled(true);
        ((TextView) findViewById(R.id.longitude_val)).setEnabled(true);
    }

    public void DisableALL() {
        rad_col[0] = ((RadioButton) findViewById(R.id.radioButton1)).getCurrentTextColor();
        rad_col[1] = ((RadioButton) findViewById(R.id.radioButton2)).getCurrentTextColor();
        ((Spinner) findViewById(R.id.priority_val)).setEnabled(false);
        ((Spinner) findViewById(R.id.period_val)).setEnabled(false);
        ((EditText) findViewById(R.id.message_val)).setEnabled(false);
        ((RadioButton) findViewById(R.id.radioButton1)).setEnabled(false);
        ((RadioButton) findViewById(R.id.radioButton1)).setTextColor(ContextCompat.getColor(this, R.color.gray));
        ((RadioButton) findViewById(R.id.radioButton2)).setEnabled(false);
        ((RadioButton) findViewById(R.id.radioButton2)).setTextColor(ContextCompat.getColor(this, R.color.gray));
        ((Button) findViewById(R.id.act1_settings)).setEnabled(false);

        ((TextView) findViewById(R.id.priority)).setEnabled(false);
        ((TextView) findViewById(R.id.period)).setEnabled(false);
        ((TextView) findViewById(R.id.latitude)).setEnabled(false);
        ((TextView) findViewById(R.id.latitude_val)).setEnabled(false);
        ((TextView) findViewById(R.id.longitude)).setEnabled(false);
        ((TextView) findViewById(R.id.longitude_val)).setEnabled(false);
    }

    public void InitializeHeadButtons() {
        Button button1 = findViewById(R.id.act1_settings);
        button1.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(intent);
        });

        Button button2 = findViewById(R.id.act1_help);
        button2.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity3.class);
            startActivity(intent);
        });
    }

    public void InitializeSpinnerPriority() {
        Spinner spinner = findViewById(R.id.priority_val);
        String[] items = {"Info", "Unknown", "Low", "Medium", "High"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        app.setPriority(items[0]);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = findViewById(R.id.priority_val);
                String[] items = {"Info", "Unknown", "Low", "Medium", "High"};
                app.setPriority(items[spinner.getSelectedItemPosition()]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
    }

    public void InitializeSpinnerPeriod() {
        Spinner spinner = findViewById(R.id.period_val);
        String[] items = {"manual", "1m", "10m", "30m", "1h", "6h", "12h", "24h"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        app.setPeriod(0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = findViewById(R.id.period_val);
                app.setPeriod(spinner.getSelectedItemPosition());
                Button mybut = findViewById(R.id.EnvoyerIDMEFv2);
                if ( spinner.getSelectedItemPosition() == 0 ) {
                    mybut.setText("Send IDMEFv2");
                } else {
                    mybut.setText("START");
                    mybut.setBackgroundColor(0);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });


    }

    public void InitializeTexts() {
        ((EditText) findViewById(R.id.message_val)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                app.setMessage(s.toString());
            }
        });

        ((RadioGroup) findViewById(R.id.prob_or_no_val)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton selectedButton = findViewById(checkedId);
                String valeur = selectedButton.getText().toString();
                app.setDescr(valeur);
            }
        });
    }

    public void updateTextViewLa(String text, Integer id) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(id);
                textView.setText(text);
                if ( text != "Unknown")
                    app.setLatitude(text);
                else
                    app.setLatitude("Unknown");
            }
        });
    }

    public void updateTextViewLo(String text, Integer id) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(id);
                textView.setText(text);
                if ( text != "Unknown")
                    app.setLongitude(text);
                else
                    app.setLongitude("Unknown");
            }
        });
    }

    public static String getPublicIP() {
        try {
            URL url = new URL("https://api.ipify.org?format=text");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String ip = in.readLine();
            in.close();
            return ip;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void sendIDMEF(Context context, View v) {
        try {
            Instant instant = Instant.now() ;

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(() -> {
                return getPublicIP();
            });

            String myip = future.get();

            if (myip != "") {
                myip = ",         \"IP\": \"" + myip + "\"\n";
            }

            String source = "{\n" +
                    "     \"Description\": \"" + app.getDescr() + "\",\n" +
                    "     \"Priority\": \""+ app.getPriority() +"\",\n" +
                    "     \"CreateTime\": \"" + instant.toString() + "\",\n" +
                    "     \"StartTime\": \"" + instant.toString() + "\",\n" +
                    "     \"Category\": [\n" +
                    "       \"" + app.getCategory() + "\"\n" +
                    "     ],\n" +
                    app.getJSONNote() +
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
                    "         \"Name\": \"Smartphone\",\n" +
                    "         \"Model\": \"" + Build.MANUFACTURER + " " + Build.MODEL + "\"\n" +
                    myip +
                    app.getJSONLoc() +
                    "       }\n" +
                    "     ],\n" +
                    "     \"Target\": [\n" +
                    "       {\n" +
                    "         \"Hostname\": \"" + Build.MANUFACTURER + " " + Build.MODEL + "\"\n" +
                    app.getJSONUser() +
                    app.getJSONCategory() +
                    myip +
                    app.getJSONLoc() +
                    "       }\n" +
                    "     ]\n" +
                    " }";

            JSONObject json=new JSONObject(source);
            String jsonString = json.toString();
            new PostData().execute(app.getUrl(),app.getLogin(),app.getPassword(),jsonString);
            Log.e("IDMEFv2",jsonString);
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
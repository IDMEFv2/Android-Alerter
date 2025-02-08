package com.example.idmefv2alerter;

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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

import android.location.Location;
import android.location.LocationManager;

import android.Manifest;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    private Handler handler;
    private Handler send_idmefv2_period;
    private Runnable runnable;
    private Runnable runnable_idmefv2_period;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);

        Button mybut = findViewById(R.id.EnvoyerIDMEFv2);


        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                // Fonction à exécuter toutes les 2 secondes
                TextView myloc = findViewById(R.id.idmefv2_location);
                String myloc_text = null;
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                try {
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        myloc_text = "Lat : " + location.getLatitude() + "\nLong : " + location.getLongitude();
                    } else {
                        myloc_text = "Unknown location";
                    }
                } catch (SecurityException e) {
                    myloc_text = "You need to give location permission to fully use this app";
                }
                myloc.setText(myloc_text);

                // Rappeler cette fonction après 2000 ms (2 secondes)
                handler.postDelayed(this, 2000);
            }
        };

        // Lancer immédiatement la fonction
        handler.post(runnable);

        // Récupérer le Spinner
        Spinner spinner = findViewById(R.id.idmefv2_spin);

        // Définir les options de la liste déroulante
        String[] items = {"manual", "10m", "30m", "1h", "6h", "12h", "24h"};

        // Créer un adaptateur
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Appliquer l'adaptateur au Spinner
        spinner.setAdapter(adapter);

        send_idmefv2_period = new Handler();
        runnable_idmefv2_period = new Runnable() {
            @Override
            public void run() {
                Spinner spinner = findViewById(R.id.idmefv2_spin);
                Integer[] items_int = {null, 600000, 1800000, 3600000, 21600000, 43200000, 86400000};
                Integer sel = items_int[spinner.getSelectedItemPosition()];
                if (sel != null) {
                    send_idmefv2_period.removeCallbacksAndMessages(null);
                    send_idmefv2_period.postDelayed(this, items_int[(int) spinner.getSelectedItemPosition()] );
                    findViewById(R.id.EnvoyerIDMEFv2).performClick();
                }
            }
        };

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            String prevpos = (String) spinner.getItemAtPosition(0);
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (prevpos == parent.getItemAtPosition(position)) {
                    return;
                }
                prevpos = (String) parent.getItemAtPosition(position);
                Spinner spinner = findViewById(R.id.idmefv2_spin);
                Integer[] items_int = {null, 600000, 1800000, 3600000, 21600000, 43200000, 86400000};
                Integer sel = items_int[spinner.getSelectedItemPosition()];
                if (sel != null) {
                    send_idmefv2_period.removeCallbacksAndMessages(null);
                    send_idmefv2_period.postDelayed(runnable_idmefv2_period, items_int[(int) spinner.getSelectedItemPosition()] );
                    findViewById(R.id.EnvoyerIDMEFv2).performClick();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Rien à faire ici
            }

        });

        mybut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "click", Toast.LENGTH_SHORT).show();
                try {
                    Instant instant = Instant.now() ;
                    EditText descr = findViewById(R.id.idmefv2_descr);
                    EditText cat = findViewById(R.id.idmefv2_cat);
                    EditText siem_srv = findViewById(R.id.idmefv2_url);
                    EditText siem_login = findViewById(R.id.idmefv2_url_login);
                    EditText siem_password = findViewById(R.id.idmefv2_url_pass);

                    String myloc_text = "";
                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    try {
                        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            myloc_text = "" + location.getLatitude() + ", " + location.getLongitude();
                        }
                    } catch (SecurityException e) {
                    }

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
                            "         \"Model\": \"rsyslog 8.2110\",\n" +
                            "         \"Location\": \"" + myloc_text + "\"\n" +
                            "       }\n" +
                            "     ],\n" +
                            "     \"Target\": [\n" +
                            "       {\n" +
                            "         \"IP\": \"192.0.2.2\",\n" +
                            "         \"Hostname\": \"www.acme.com\",\n" +
                            "         \"Location\": \"" + myloc_text + "\",\n" +
                            "         \"User\": \"root\"\n" +
                            "       }\n" +
                            "     ]\n" +
                            " }";
                    JSONObject json=new JSONObject(source);
                    String jsonString = json.toString();
                    new PostData().execute(siem_srv.getText().toString(),siem_login.getText().toString(),siem_password.getText().toString(),jsonString);
                    Spinner spinner = findViewById(R.id.idmefv2_spin);
                    Integer[] items_int = {null, 600000, 1800000, 3600000, 21600000, 43200000, 86400000};
                    Integer sel = items_int[spinner.getSelectedItemPosition()];
                    if (sel != null) {
                        send_idmefv2_period.removeCallbacksAndMessages(null);
                        send_idmefv2_period.postDelayed(runnable_idmefv2_period, items_int[(int) spinner.getSelectedItemPosition()]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

  // on below line creating a class to post the data.
  class PostData extends AsyncTask<String, Void, String> {

      @Override
      protected String doInBackground(String... strings) {
          try {

              // on below line creating a url to post the data.
              URL url = new URL(strings[0]);

              // on below line opening the connection.
              HttpURLConnection client = (HttpURLConnection) url.openConnection();

              // on below line setting method as post.
              client.setRequestMethod("POST");

              // on below line setting content type and accept type.
              client.setRequestProperty("Content-Type", "application/json");
              client.setRequestProperty("Accept", "application/json");

              String auth = strings[1] + ":" + strings[2];
              byte[] encodedAuth = Base64.encode(auth.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
              String authHeaderValue = "Basic " + new String(encodedAuth);
              client.setRequestProperty("Authorization", authHeaderValue);

              // on below line setting client.
              client.setDoOutput(true);

              // on below line we are creating an output stream and posting the data.
              try (OutputStream os = client.getOutputStream()) {
                  byte[] input = strings[3].getBytes("utf-8");
                  os.write(input, 0, input.length);
              }
              // on below line creating and initializing buffer reader.
              try (BufferedReader br = new BufferedReader(
                      new InputStreamReader(client.getInputStream(), "utf-8"))) {

                  // on below line creating a string builder.
                  StringBuilder response = new StringBuilder();

                  // on below line creating a variable for response line.
                  String responseLine = null;

                  // on below line writing the response
                  while ((responseLine = br.readLine()) != null) {
                      response.append(responseLine.trim());
                  }

                  // on below line displaying a toast message.
                  return response.toString();
              }


          } catch (Exception e) {

              // on below line handling the exception.
              e.printStackTrace();
              return "Fail to post the data : " + e.getMessage();

          }
      }
      @Override
      protected void onPostExecute(String result) {
          String resp = null;
          if (Objects.equals(result, "ok")) {
              resp = "IDMEFv2 sent";
          } else {
              resp = result;
          }
          Toast.makeText(MainActivity.this, resp, Toast.LENGTH_SHORT).show();
      }
  }
}
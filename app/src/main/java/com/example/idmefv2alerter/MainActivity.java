package com.example.idmefv2alerter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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

public class MainActivity extends AppCompatActivity {

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
        Button mybut = findViewById(R.id.EnvoyerIDMEFv2);
        mybut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Instant instant = Instant.now() ;
                    EditText descr = findViewById(R.id.idmefv2_descr);
                    EditText cat = findViewById(R.id.idmefv2_cat);

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
                            "         \"Location\": \"Server room A1, rack 10\"\n" +
                            "       }\n" +
                            "     ],\n" +
                            "     \"Target\": [\n" +
                            "       {\n" +
                            "         \"IP\": \"192.0.2.2\",\n" +
                            "         \"Hostname\": \"www.acme.com\",\n" +
                            "         \"Location\": \"Server room A1, rack 10\",\n" +
                            "         \"User\": \"root\"\n" +
                            "       }\n" +
                            "     ]\n" +
                            " }";
                    JSONObject json=new JSONObject(source);
                    String jsonString = json.toString();
                    new PostData().execute("http://141.95.158.49/api_idmefv2/","admin","S4Sadmin",jsonString);

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
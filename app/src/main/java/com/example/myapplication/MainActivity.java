package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends Activity {
    Button post;
    String bearerToken;
    String apiKey = "MWE4MTlhNGYtYjA3Zi00NWEwLTg3ZDMtOTRiNzZhODU5NjFhOmI0ZjViNDdlZTllNzRjOTZhNjRhOGZhNzc3NjY2NGY1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        post = (Button) findViewById(R.id.post);

        post.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    postHttpResponse();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void postHttpResponse() throws IOException {

        String url = "https://developers.lingvolive.com/api/v1.1/authenticate";
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Authorization",  "Basic " + apiKey)
                .build();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                bearerToken = response.body().string();
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                intent.putExtra("bearerToken",bearerToken);
                startActivity(intent);
                finish();

                //show.setText(bearerToken);
            }
        });
    }

}


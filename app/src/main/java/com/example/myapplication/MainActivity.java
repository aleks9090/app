package com.example.myapplication;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

    //в MainActivity будет создано подключение к AbbyyLingvo для получения token'а. В приложении это стартовое Activity
public class MainActivity extends Activity {
    //определение кнопки авторизоваться, токена и ApiKey(ключа приложения)
    Button post;
    String bearerToken;
    String apiKey = "MWE4MTlhNGYtYjA3Zi00NWEwLTg3ZDMtOTRiNzZhODU5NjFhOmI0ZjViNDdlZTllNzRjOTZhNjRhOGZhNzc3NjY2NGY1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        post = (Button) findViewById(R.id.post);
    // создание метода обработки нажатия кнопки
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
    // делаем запрос POST на сервер
                try {
                    postHttpResponse();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    // запрос POST создается с помощью okhttp
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
            // если ответа нет
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                Toast.makeText(getBaseContext(),mMessage,Toast.LENGTH_SHORT).show();
            }
            // если на запрос приходит успешный ответ
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //берем токен
                bearerToken = response.body().string();
                //Происходит передача токена и переход на Main2Activity
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                intent.putExtra("bearerToken",bearerToken);
                startActivity(intent);
                finish();
            }
        });
    }

}


package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Main2Activity extends Activity {
    private static final String TAG = "Response";
    Button get;
    TextView answer;
    EditText word;
    ListView userList;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor userCursor;
    SimpleCursorAdapter userAdapter;
    long userId=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        get = (Button) findViewById(R.id.get);
        word = (EditText) findViewById(R.id.word);
        answer = (TextView) findViewById(R.id.answer);
        userList = (ListView) findViewById(R.id.list);
        get.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            getHttpResponse();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });


                databaseHelper = new DatabaseHelper(getApplicationContext());
            userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(Main2Activity.this, Main3Activity.class);
                    intent.putExtra("id", id);
                    startActivity(intent);
                }
            });
           }

    public void getHttpResponse() throws IOException {
            //final String a = word.getText().toString();
            if (word.getText().toString().length() > 0) {
                String bearerToken = getIntent().getStringExtra("bearerToken");
                // String url = "https://developers.lingvolive.com/api/v1/Minicard?text="+word.getText().toString()+"&srcLang=1033&dstLang=1049";
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority("developers.lingvolive.com")
                        .appendPath("api")
                        .appendPath("v1")
                        .appendPath("Minicard")
                        .appendQueryParameter("text", word.getText().toString())
                        .appendQueryParameter("srcLang", "1033")
                        .appendQueryParameter("dstLang", "1049");
                String myUrl = builder.build().toString();
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                Request request = new Request.Builder()
                        .url(myUrl)
                        .method("GET", null)
                        .addHeader("Authorization", "Bearer " + bearerToken)
                        .build();
                client.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(Call call, IOException e) {
                       final String mMessage = e.getMessage().toString();
                        Log.w("failure Response", mMessage);
                        //call.cancel();

                        answer.post(new Runnable() {
                            @Override
                            public void run() {
                                answer.setText(mMessage);;
                            }
                        });


                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String mMessage = response.body().string();
                       boolean isPresent = mMessage.indexOf("translations") != -1 ? true : false;
                        if (isPresent) {
                            answer.post(new Runnable() {
                                @Override
                                public void run() {
                            answer.setText("Перевод слова " + word.getText().toString() + " не найден. Убедитесь в правильности написания");
                        }
                    });
                        }


                        try {
                            JSONObject json = new JSONObject(mMessage);
                            JSONObject json2 = json.getJSONObject("Translation");
                            final String TranslationE = json2.getString("Translation");
                            // String someString = json.getString("someString");
                            answer.post(new Runnable() {
                                @Override
                                public void run() {
                                    answer.setText(TranslationE);;
                                }
                            });

                        } catch (JSONException e) {
                            //
                        }
                    }
                });
            }else{
                Toast.makeText(getBaseContext(),"поле ввода не может быть пустым",Toast.LENGTH_SHORT).show();
            }
        }
    private static long back_pressed;
    @Override
    public void onBackPressed () {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(getBaseContext(), "Для выхода нажмите еще раз", Toast.LENGTH_SHORT).show();
        }
        back_pressed = System.currentTimeMillis();
    }


    @Override
    public void onResume() {
        super.onResume();
                db = databaseHelper.getReadableDatabase();
                //получаем данные из бд в виде курсора
                userCursor =  db.rawQuery("select * from "+ DatabaseHelper.TABLE, null);
                // определяем, какие столбцы из курсора будут выводиться в ListView
                String[] headers = new String[] {DatabaseHelper.COLUMN_ENWORD, DatabaseHelper.COLUMN_RUWORD};
                // создаем адаптер, передаем в него курсор
                userAdapter = new SimpleCursorAdapter(Main2Activity.this, R.layout.change_listview, userCursor, headers, new int[]{android.R.id.text1, android.R.id.text2}, 0);
                userList.setAdapter(userAdapter);

        // открываем подключение

    }


    // по нажатию на кнопку запускаем UserActivity для добавления данных
    public void add(View view){
        Intent intent = new Intent(Main2Activity.this, Main3Activity.class);
        intent.putExtra("enWord",word.getText().toString());
        intent.putExtra("ruWord",answer.getText().toString());
        startActivity(intent);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

                // Закрываем подключение и курсор
                db.close();
                userCursor.close();

    }
}


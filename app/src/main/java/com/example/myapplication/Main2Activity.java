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

// в этом Activity происходит работа с ABBYY LINGVO API и БД Mysql

public class Main2Activity extends Activity {
    // определение элементов
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
    // создание элементов
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        get = (Button) findViewById(R.id.get);
        word = (EditText) findViewById(R.id.word);
        answer = (TextView) findViewById(R.id.answer);
        userList = (ListView) findViewById(R.id.list);

    // создание обработчика нажатия на кнопку "Перевести"
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

    // определяем databaseHelper
                databaseHelper = new DatabaseHelper(getApplicationContext());
    // создание обработчика нажатия на кнопку "Сохранить"
            userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(Main2Activity.this, Main3Activity.class);
                    intent.putExtra("id", id);
                    startActivity(intent);
                }
            });
           }
    // запрос GET создается с помощью okhttp
    public void getHttpResponse() throws IOException {
     // создание условия что editText не пустой
               if (word.getText().toString().length() > 0) {
     // если условие выполняется, то получаем из MainActivity токен
                String bearerToken = getIntent().getStringExtra("bearerToken");
    // с помощью Uri.Builder создается URL
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority("developers.lingvolive.com")
                        .appendPath("api")
                        .appendPath("v1")
                        .appendPath("Minicard")
     //В URL вставляем слово, введенное пользователем в editText
                        .appendQueryParameter("text", word.getText().toString())
                        .appendQueryParameter("srcLang", "1033")
                        .appendQueryParameter("dstLang", "1049");
                String myUrl = builder.build().toString();
     //отправка GET с токеном на сервер
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                Request request = new Request.Builder()
                        .url(myUrl)
                        .method("GET", null)
                        .addHeader("Authorization", "Bearer " + bearerToken)
                        .build();
                client.newCall(request).enqueue(new Callback() {
     // если запрос неудачный
                    @Override
                    public void onFailure(Call call, IOException e) {
      // в переменную mMessage получаем ответ
                       final String mMessage = e.getMessage().toString();
                        Log.w("failure Response", mMessage);
                        //call.cancel();
      // Оборачиваем с помощью Runnable textView, т.к. okhttp вызывается в фоновом режиме, а изменение UI возможно только в UI-потоке
                        answer.post(new Runnable() {
                            @Override
                            public void run() {
       // Выводим текст в textView
                                answer.setText(mMessage);;
                            }
                        });


                    }
       // В случае успешного запроса
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
       // в mMessage выводится полученный текст перевода слова от сервера
                        String mMessage = response.body().string();
       // проверяем на корректность запрос пользователя и имеется ли перевод данного слова на сервере
       // в случае если ответ содержит в себе слово "translations"
       // то в textView  выводится "Перевод слова " + слово пользователя + " не найден. Убедитесь в правильности написания"
                       boolean isPresent = mMessage.indexOf("translations") != -1 ? true : false;
                        if (isPresent) {
                            answer.post(new Runnable() {
                                @Override
                                public void run() {
                            answer.setText("Перевод слова " + word.getText().toString() + " не найден. Убедитесь в правильности написания");
                        }
                    });
                        }

        // Данные от сервера приходят в JSON формате
        // Создаем json объект, чтобы вытащить необходимое значение
                        try {
                            JSONObject json = new JSONObject(mMessage);
                            JSONObject json2 = json.getJSONObject("Translation");
                            final String TranslationE = json2.getString("Translation");

         // Выводим переведенное слово
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
     // если editTExt пустой, то выводится след. всплывающее сообщение
            }else{
                Toast.makeText(getBaseContext(),"поле ввода не может быть пустым",Toast.LENGTH_SHORT).show();
            }
        }

     // создание выхода из приложения по двойному нажатию на кнопку back
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

       }


    // по нажатию на кнопку запускаем Main3Activity для добавления данных
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


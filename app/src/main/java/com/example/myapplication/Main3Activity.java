package com.example.myapplication;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Main3Activity extends Activity {

    EditText enWord;
    EditText ruWord;
    Button delButton;
    Button saveButton;
    DatabaseHelper sqlHelper;
    SQLiteDatabase db;
    Cursor userCursor;
    long userId=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        enWord = (EditText) findViewById(R.id.enWord);
        ruWord = (EditText) findViewById(R.id.ruWord);
        delButton = (Button) findViewById(R.id.deleteButton);
        saveButton = (Button) findViewById(R.id.saveButton);

        String Word_en = getIntent().getStringExtra("enWord");
        String Word_ru = getIntent().getStringExtra("ruWord");
      enWord.setText(Word_en);
      ruWord.setText(Word_ru);
             sqlHelper = new DatabaseHelper(this);
        db = sqlHelper.getWritableDatabase();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getLong("id");
        }
        // если 0, то добавление
        if (userId > 0) {
            // получаем элемент по id из бд
            userCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE + " where " +
                    DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
            userCursor.moveToFirst();
            enWord.setText(userCursor.getString(1));
            ruWord.setText(userCursor.getString(2));
            userCursor.close();
        } else {
            // скрываем кнопку удаления
            delButton.setVisibility(View.GONE);
        }
    }
    public void save(View view) {
        if (enWord.getText().toString().length() > 0){
            ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_ENWORD, enWord.getText().toString());
        cv.put(DatabaseHelper.COLUMN_RUWORD, ruWord.getText().toString());

        if (userId > 0) {
            db.update(DatabaseHelper.TABLE, cv, DatabaseHelper.COLUMN_ID + "=" + String.valueOf(userId), null);
        } else {
            db.insert(DatabaseHelper.TABLE, null, cv);
        }
        goHome();
    }else{
            Toast.makeText(getBaseContext(),"Пожалуйста, напишите английское слово, которое хотите запомнить",Toast.LENGTH_LONG).show();
        }
    }
    public void delete(View view){
        db.delete(DatabaseHelper.TABLE, "_id = ?", new String[]{String.valueOf(userId)});
        goHome();
    }
    private void goHome(){
        // закрываем подключение
        db.close();
        // переход к главной activity
        Intent intent = new Intent(Main3Activity.this, Main2Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}

package com.natalie.naturbb;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    ListView list_view;
    DatabaseHelper dbHelper = new DatabaseHelper(this);
    SQLiteDatabase database = null;
    Cursor dbCursor;

    // first create all the variables we are using: ListView, DatabaseHelper, SQLiteDatabase, Cursor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list_view = findViewById(R.id.parkList);
        //parkList is an adapter for list_view
        try {
            dbHelper.createDataBase();
            //creating the database and prevent crash with try&catch
        } catch (IOException ioe) {
        }
        database = dbHelper.getDataBase();

        dbCursor = database.rawQuery("SELECT * FROM natur_table;", null);

        ArrayAdapter<CharSequence> adapter = createAdapterHtml(dbCursor);
        //create HTML list items with adapter
        list_view.setAdapter(adapter);
        //adapter needs the layout view file and data (the dbCursor points to the data records)
    }

    private ArrayAdapter<CharSequence> createAdapterHtml(Cursor cursor) {
        int length = cursor.getCount();
        cursor.moveToFirst();
        Spanned[] html_array = new Spanned[length];
        int index_name = cursor.getColumnIndex("region");
        int index_region = cursor.getColumnIndex("title");
        for (int i = 0; i < length; i++) {
            html_array[i] = Html.fromHtml(cursor.getString(index_name) + "<br><i>" + cursor.getString(index_region) + "</i>");
            cursor.moveToNext();
        }

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, R.layout.list_item, html_array);
        //list_item is layout for each park in db table

        return adapter;
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
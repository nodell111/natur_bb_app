package com.natalie.naturbb.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.natalie.naturbb.DatabaseHelper;
import com.natalie.naturbb.R;

import java.io.IOException;

public class listfragment extends Fragment {

    DatabaseHelper dbHelper = new DatabaseHelper(getContext());
    SQLiteDatabase database = null;
    Cursor dbCursor;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_listfragment, container, false);
        ListView list_view = (ListView) view.findViewById(R.id.parkList);
        //parkList is an adapter for list_view
        try {
            dbHelper.createDataBase();
            //creating the database and prevent crash with try&catch
        } catch (IOException ioe) {
        }
        database = dbHelper.getDataBase();

        dbCursor = database.rawQuery("SELECT DISTINCT region FROM natur_table ORDER BY region;", null);

        ArrayAdapter<CharSequence> adapter = createAdapterHtml(dbCursor);
        //create HTML list items with adapter
        list_view.setAdapter(adapter);
        //adapter needs the layout view file and data (the dbCursor points to the data records)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_listfragment, container, false);
    }

    private ArrayAdapter<CharSequence> createAdapterHtml(Cursor cursor) {
        int length = cursor.getCount();
        cursor.moveToFirst();
        Spanned[] html_array = new Spanned[length];
        int index_name = cursor.getColumnIndex("region");
//        int index_region = cursor.getColumnIndex("title");
//        for (int i = 0; i < length; i++) {
//            html_array[i] = Html.fromHtml(cursor.getString(index_name) + "<br><i>" + cursor.getString(index_region) + "</i>");
//            cursor.moveToNext();
//        }

        for (int i = 0; i < length; i++) {
            html_array[i] = Html.fromHtml(cursor.getString(index_name));
            cursor.moveToNext();
        }

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getContext(), R.layout.list_item, html_array);
        //list_item is layout for each park in db table

        return adapter;
    }

//    @Override
//    protected void onDestroy() {
//        dbHelper.close();
//        super.onDestroy();
//    }
}
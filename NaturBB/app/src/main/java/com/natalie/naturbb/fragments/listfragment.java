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
import android.widget.SearchView;

import com.natalie.naturbb.DatabaseHelper;
import com.natalie.naturbb.MainActivity;
import com.natalie.naturbb.R;

import java.io.IOException;
import java.io.LineNumberInputStream;

public class listfragment extends Fragment {

    // Declare variables
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private Cursor dbCursor;
    private ListView list_view;
    private SearchView searchView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_listfragment, container, false);
        list_view = view.findViewById(R.id.parkList);
        //parkList is an adapter for list_view

        dbHelper = new DatabaseHelper(view.getContext());

        // Initialize DatabaseHelper and SQLiteDatabase in onCreateView
        try {
            dbHelper.createDataBase();
            //creating the database and prevent crash with try&catch
        } catch (IOException ioe) {
        }
        database = dbHelper.getDataBase();

        dbCursor = database.rawQuery("SELECT * FROM natur_table_park ORDER BY region asc;", null);


        ArrayAdapter<CharSequence> adapter = createAdapterHtml(dbCursor);
        //create HTML list items with adapter
        list_view.setAdapter(adapter);
        //adapter needs the layout view file and data (the dbCursor points to the data records)

        // Obtain the SearchView from the main activity
        searchView = getActivity().findViewById(R.id.searchbar);

        setupSearchView();

        return view;
    }

    private ArrayAdapter<CharSequence> createAdapterHtml(Cursor cursor) {
        int length = cursor.getCount();
        cursor.moveToFirst();
        Spanned[] html_array = new Spanned[length];
        int index_name = cursor.getColumnIndex("region");
//        int index_name_en = cursor.getColumnIndex("region_en");
//        for (int i = 0; i < length; i++) {
//            html_array[i] = Html.fromHtml(cursor.getString(index_name) + "<br><i>" + cursor.getString(index_name_en) + "</i>");
//            cursor.moveToNext();
//        }

        for (int i = 0; i < length; i++) {
            html_array[i] = Html.fromHtml(cursor.getString(index_name));
            cursor.moveToNext();
        }

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_item, html_array);
        //list_item is layout for each park in db table

        return adapter;
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle the query submission if needed
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String keyword = searchView.getQuery().toString();
                if (dbCursor != null) {
                    dbCursor.close();
                }
                dbCursor = database.rawQuery(
                        "SELECT * FROM natur_table_park WHERE region LIKE ? ORDER BY region asc",
                        new String[]{"%" + keyword + "%"}
                );
                ArrayAdapter<CharSequence> adapter = createAdapterHtml(dbCursor);
                if (list_view != null) {
                    list_view.setAdapter(adapter);
                }
                return true;
            }
        });
    }


    @Override
    public void onDestroy() {
        // Close database in onDestroy method
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }


}
package com.natalie.naturbb.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.CompoundButton;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Switch;

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
    private Switch switchSortSize;
    private Switch switchSortName;



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


        // Use getActivity() to find views in the activity's layout
        searchView = getActivity().findViewById(R.id.searchbar);
        switchSortSize = getActivity().findViewById(R.id.sortSize);
        switchSortName = getActivity().findViewById(R.id.sortName);

        setupSearchView();
        setupSwitchGroup();
        return view;
    }

    private ArrayAdapter<CharSequence> createAdapterHtml(Cursor cursor) {
        int length = cursor.getCount();
        cursor.moveToFirst();
        Spanned[] html_array = new Spanned[length];
        String[] image_names = new String[length]; // New array for image names
        int index_name = cursor.getColumnIndex("region");
        int image_name = cursor.getColumnIndex("image_name");

//        int index_name_en = cursor.getColumnIndex("region_en");
//        for (int i = 0; i < length; i++) {
//            html_array[i] = Html.fromHtml(cursor.getString(index_name) + "<br><i>" + cursor.getString(index_name_en) + "</i>");
//            cursor.moveToNext();
//        }

        for (int i = 0; i < length; i++) {
            html_array[i] = Html.fromHtml(cursor.getString(index_name));
            image_names[i] = cursor.getString(image_name);
            cursor.moveToNext();
        }

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                getActivity(),
                R.layout.list_item,
                //list_item is layout for each park in db table
                R.id.textViewItem,
                html_array
        ){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                // Set background drawable dynamically based on image_name
                String imageName = image_names[position];
                int resId = getResources().getIdentifier(imageName, "drawable", getActivity().getPackageName());
                view.setBackgroundResource(resId);

                return view;
            }
        };


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

    //when switch for size is checked then sort list by size column
    private void setupSwitchGroup() {
        switchSortSize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchSortName.setChecked(false);
                    sortListBySize();
                }
            }
        });

        switchSortName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchSortSize.setChecked(false);
                    sortListByName();
                }
            }
        });
    }

    private void sortListBySize() {
        if (dbCursor != null) {
            dbCursor.close();
        }
        dbCursor = database.rawQuery("SELECT * FROM natur_table_park ORDER BY area_km2 ASC;", null);
        // Assuming your adapter is named 'adapter'
//        adapter.clear();
//        adapter.addAll(createAdapterHtml(dbCursor));
//        adapter.notifyDataSetChanged();
        ArrayAdapter<CharSequence> adapter = createAdapterHtml(dbCursor);
        if (list_view != null) {
            list_view.setAdapter(adapter);
        }
    }

    private void sortListByName() {
        if (dbCursor != null) {
            dbCursor.close();
        }
        dbCursor = database.rawQuery("SELECT * FROM natur_table_park ORDER BY region ASC;", null);
        // Assuming your adapter is named 'adapter'
//        adapter.clear();
//        adapter.addAll(createAdapterHtml(dbCursor));
//        adapter.notifyDataSetChanged();
        ArrayAdapter<CharSequence> adapter = createAdapterHtml(dbCursor);
        if (list_view != null) {
            list_view.setAdapter(adapter);
        }
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
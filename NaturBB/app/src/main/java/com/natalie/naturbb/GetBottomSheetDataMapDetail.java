package com.natalie.naturbb;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GetBottomSheetDataMapDetail {
    public String description;
    public String category;
    public String city;

    public GetBottomSheetDataMapDetail(String poiName) {
        showMapBottomSheetFragment(poiName);
    }
    private void showMapBottomSheetFragment(String poiName) {
        description = getDescriptionFromDatabase(poiName);
        category = getCategoryFromDatabase(poiName);
        city = getCityFromDatabase(poiName);
    }

    private String getCategoryFromDatabase(String poiName) {
        String category = "";
        SQLiteDatabase database = ListFragment.dbHelper.getDataBase();
        Cursor cursor = database.rawQuery(
                "SELECT category FROM natur_table WHERE title = ?",
                new String[]{poiName}
        );
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("category");
                if (columnIndex != -1) {
                    category = cursor.getString(columnIndex);
                } else {
                    Log.e("getCategoryFromDatabase", "Column 'category' not found in the cursor.");
                }
            } else {
                Log.e("getCategoryFromDatabase", "Cursor is null or empty.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return category;
    }


    private String getDescriptionFromDatabase(String poiName) {
        String description = "";
        SQLiteDatabase database = ListFragment.dbHelper.getDataBase();
        Cursor cursor = database.rawQuery(
                "SELECT description FROM natur_table WHERE title = ?",
                new String[]{poiName}
        );
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("description");
                if (columnIndex != -1) {
                    description = cursor.getString(columnIndex);
                } else {
                    Log.e("getDescriptionFromDatabase", "Column 'description' not found in the cursor.");
                }
            } else {
                Log.e("getDescriptionFromDatabase", "Cursor is null or empty.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            return description;
        }
    }

    private String getCityFromDatabase(String poiName) {
        String city = "";
        SQLiteDatabase database = ListFragment.dbHelper.getDataBase();
        Cursor cursor = database.rawQuery(
                "SELECT city FROM natur_table WHERE title = ?",
                new String[]{poiName}
        );
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("city");
                if (columnIndex != -1) {
                    city = cursor.getString(columnIndex);
                } else {
                    Log.e("getCityFromDatabase", "Column 'city' not found in the cursor.");
                }
            } else {
                Log.e("getCityFromDatabase", "Cursor is null or empty.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            return city;
        }
    }
}
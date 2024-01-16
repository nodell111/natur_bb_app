package com.natalie.naturbb;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GetBottomSheetData {
    public String description;
    public String info;
    public String park_image;

    public GetBottomSheetData(String parkName) {
        showListBottomSheetFragment(parkName);
    }
    private void showListBottomSheetFragment(String parkName) {
        description = getDescriptionFromDatabase(parkName);
        info = getInfoFromDatabase(parkName);
        park_image = getImageFromDatabase(parkName);
    }

    private String getImageFromDatabase(String parkName) {
        String image = "";
        SQLiteDatabase database = ListFragment.dbHelper.getDataBase();
        Cursor cursor = database.rawQuery(
                "SELECT image_name FROM natur_table_park WHERE region = ?",
                new String[]{parkName}
        );
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("image_name");
                if (columnIndex != -1) {
                    image = cursor.getString(columnIndex);
                } else {
                    Log.e("getInfoFromDatabase", "Column 'image_name' not found in the cursor.");
                }
            } else {
                Log.e("getInfoFromDatabase", "Cursor is null or empty.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return image;
    }

    private String getInfoFromDatabase(String parkName) {
        String info = "";
        SQLiteDatabase database = ListFragment.dbHelper.getDataBase();
        Cursor cursor = database.rawQuery(
                "SELECT info FROM natur_table_park WHERE region = ?",
                new String[]{parkName}
        );
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("info");
                if (columnIndex != -1) {
                    info = cursor.getString(columnIndex);
                } else {
                    Log.e("getInfoFromDatabase", "Column 'info' not found in the cursor.");
                }
            } else {
                Log.e("getInfoFromDatabase", "Cursor is null or empty.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return info;
    }


    private String getDescriptionFromDatabase(String parkName) {
        String description = "";
        SQLiteDatabase database = ListFragment.dbHelper.getDataBase();
        Cursor cursor = database.rawQuery(
                "SELECT descrip FROM natur_table_park WHERE region = ?",
                new String[]{parkName}
        );
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("descrip");
                if (columnIndex != -1) {
                    description = cursor.getString(columnIndex);
                } else {
                    Log.e("getDescriptionFromDatabase", "Column 'descrip' not found in the cursor.");
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
}

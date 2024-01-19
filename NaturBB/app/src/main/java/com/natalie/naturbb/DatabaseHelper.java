package com.natalie.naturbb;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static String DB_PATH; // Path to the database file
    private static String DB_PATH_PREFIX = "/data/user/0/"; // Prefix for the database path
    private static String DB_PATH_SUFFIX = "/databases/"; // Suffix for the database path
    private static String DB_NAME = "natur_bb.db"; // Name of the database file
    private SQLiteDatabase myDataBase; // Reference to the SQLiteDatabase instance
    private final Context myContext; // Application context

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    // Method to create or open the database
    public void createDataBase() throws IOException {
        // Construct the full path to the database file
        DB_PATH = DB_PATH_PREFIX + myContext.getPackageName() + DB_PATH_SUFFIX + DB_NAME;

        // Check if the database already exists
        boolean dbExist = checkDataBase();
        SQLiteDatabase db_Read = null;

        // If the database does not exist, create it by copying from assets
        if (!dbExist) {
            db_Read = this.getReadableDatabase();
            db_Read.close();
            try {
                copyDataBase(); // Copy the database from assets to the app's data directory
            } catch (IOException e) {
                // Handle the IOException
            }
        }
    }

    // Method to check if the database already exists
    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            // Attempt to open the database file
            checkDB = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            checkDB.close();
            return true; // Database exists
        } catch (SQLiteException e) {
            return false; // Database does not exist
        }
    }

    // Method to copy the database from assets to the app's data directory
    private void copyDataBase() throws IOException {
        InputStream assetsDB = myContext.getAssets().open(DB_NAME); // Open the database file in assets
        File directory = new File(DB_PATH);

        // If the destination directory does not exist, create it
        if (!directory.exists()) {
            directory.mkdir();
        }

        // Copy the contents of the assets database to the app's data directory
        OutputStream dbOut = new FileOutputStream(DB_PATH);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = assetsDB.read(buffer)) > 0) {
            dbOut.write(buffer, 0, length);
        }
        dbOut.flush();
        dbOut.close();
    }

    // Method to get a readable instance of the database
    public SQLiteDatabase getDataBase() throws SQLException {
        myDataBase = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        return myDataBase;
    }

    // SQLiteOpenHelper callback method called when the database needs to be created
    @Override
    public void onCreate(SQLiteDatabase db) {
        // No implementation here since the database is already created
    }

    // SQLiteOpenHelper callback method called when the database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No implementation here since the database schema is static
    }
}

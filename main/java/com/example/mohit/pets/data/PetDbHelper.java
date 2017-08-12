package com.example.mohit.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.Preference;

import java.text.SimpleDateFormat;

/**
 * Created by mohit on 22/5/17.
 */

public class PetDbHelper extends SQLiteOpenHelper {

    private static final String SQL_DELETE_ENTRIES="DROP TABLE IF EXISTS "+ StorePetsContract.StorePets.TABLE_NAME;

    public static final int DATABASE_VERSION=1;
    public static final String DATABASE_NAME="shelters.db";

    public PetDbHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }


    //called when we first create the database for the very first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        //creates a string that contains the sql statement to create the pets table
                String SQL_CREATE_ENTRIES="CREATE TABLE "+
                StorePetsContract.StorePets.TABLE_NAME+
                " ("+ StorePetsContract.StorePets._ID+" INTEGER PRIMARY KEY,"+
                StorePetsContract.StorePets.COLUMN_PET_NAME+" TEXT NOT NULL,"+
                StorePetsContract.StorePets.COLUMN_PET_BREED+" TEXT NOT NULL,"+
                StorePetsContract.StorePets.COLUMN_PET_GENDER+" INTEGER ,"+
                StorePetsContract.StorePets.COLUMN_PET_WEIGHT+" INTEGER NOT NULL)";
                //execute the SQL statement
                db.execSQL(SQL_CREATE_ENTRIES);
    }
    //called when the database is opened
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }
    //called when we need to upgrade the database i.e., create a new version of it
    //like addings some columns to it
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //this database is only a cache for online database thus we will remove all the data whenever
        //we need upgrade
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}

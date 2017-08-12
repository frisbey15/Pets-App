package com.example.mohit.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Switch;

import com.example.mohit.pets.data.StorePetsContract.StorePets;

/**
 * Created by mohit on 24/5/17.
 */

public class PetProvider extends ContentProvider {


    //tag for the log messages
    private static final String LOG_TAG=PetProvider.class.getSimpleName();
    /*
    * ID's assigned to the acceptable Uri's by the petprovider
    * URI Matcher code for the content uri for the pets table
    * */
    private static final int PETS=100;
    //uri matcher code for the content uri for a single pet in pets table
    private static final int PET_ID=101;

    //database helper object
    private PetDbHelper mDbHelper;
    /**
     * Initialize the provider and the database helper object.
     */

    /*
    * uri matcher object to match the content uri to a corresponding code
    * */
    private static final UriMatcher sUriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
    //static initalizer .this is run the first time anything is called from this class
    static {
        //the calls to addUri() go here, for all the acceptable uri's that the provider should recognize
        //all the paths added to uri matcher have a corresponding code to return when a match is found
        sUriMatcher.addURI(StorePetsContract.CONTENT_AUTHORITY,StorePets.TABLE_NAME,PETS);
        sUriMatcher.addURI(StorePetsContract.CONTENT_AUTHORITY,StorePets.TABLE_NAME+"/#",PET_ID);

    }
    @Override
    public boolean onCreate() {

        //to access the database we need to instantiate the subclass petDbHelper class,and
        //pass in the context of the current activity
        mDbHelper=new PetDbHelper(getContext());

        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        //connecting to the database with the help of a mDbHelper class
        SQLiteDatabase db=mDbHelper.getReadableDatabase();

        Cursor cursor;
        int result=sUriMatcher.match(uri);
        switch (result){
            //we will set notification for the uri in this case only

            case PETS:
                cursor=db.query(StorePets.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                //set notification uri for the cursor
                //so we know what content uri was the cursor created for
                //and if the data at this uri changes , then we need to update the cursor
                cursor.setNotificationUri(getContext().getContentResolver(),uri);
                break;
            //here we cant set the notification uri , as the only row in cursor gets deleted
            //and it will move cursor position to {-1 position} which will cause an error
            case PET_ID:
                //the case when we have only one row in the cursor table
                //thus we need to find the id of the row which we want to return through the cursor
                //thus we have to modify our selection string , and selectionArgs stirng array
                 selection=StorePets._ID+"=?";
                selectionArgs=new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor=db.query(StorePets.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("can not query unknown URi"+uri);
        }

        //set notification uri for the cursor
        //so we know what content uri was the cursor created for
        //and if the data at this uri changes , then we need to update the cursor
        //cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        int result=sUriMatcher.match(uri);

        switch (result){
            case PETS:

                return insertPet(uri,contentValues);

            default:
                throw new IllegalArgumentException("can not query unknown Uri for insert:"+uri);
        }
    }
    //method deifned by user to insert the pet data into the database
    //and returns the updated uri
    private Uri insertPet(Uri uri,ContentValues contentValues){
        SQLiteDatabase db=mDbHelper.getWritableDatabase();

        String name=new String(contentValues.getAsString((StorePets.COLUMN_PET_NAME)));
        if(name==null){
            throw new IllegalArgumentException("pet requires a name :");
        }
        String breed=new String(contentValues.getAsString(StorePets.COLUMN_PET_BREED));
        if(breed==null){
            throw new IllegalArgumentException("pet requires a breed name");
        }
        Integer weight=contentValues.getAsInteger(StorePets.COLUMN_PET_WEIGHT);
        if(weight ==null || weight<=0){
            Log.v(LOG_TAG,"the value of weight is :-------------"+weight);
            throw new IllegalArgumentException("pet cannot have negative weight-----------------");
        }

        if(name!=null&&breed!=null&&weight>0) {
            long _id = db.insert(StorePets.TABLE_NAME, null, contentValues);
            if(_id==-1){
                Log.e(LOG_TAG,"pet data was not stored in the database"+uri);
                return  null;
            }

            //notify all listeners that the uri has been changed now
            //thus the cursor needs to get updated
            //which will be done automatically
            getContext().getContentResolver().notifyChange(uri,null);

            return Uri.withAppendedPath(uri, String.valueOf(_id));
        }
        else{
            return null;
        }
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     * returns the number of rows affected due to updation of the database
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        //no of rows affected by the update method
        int count;

        int result=sUriMatcher.match(uri);
        switch (result){
            case PETS:
                count= updatePets(uri,contentValues,selection,selectionArgs);
                break;

            case PET_ID:
                selection=StorePets._ID+"=?";
                selectionArgs=new String[]{String.valueOf(ContentUris.parseId(uri))};
                count=updatePets(uri,contentValues,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("can not query given uri"+uri);
        }
        //if the number of rows updated are greater than zero
        if(count>0){
            //we need to update to all the listeners that the uri has been updated
            //thus they need to reload the cursors
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return count;
    }
    /*
    * function to update the data of a pet
    * */
    private int updatePets(Uri uri,ContentValues contentValues,String selection,String[] selectionArgs){
        //get access to the database
        SQLiteDatabase db=mDbHelper.getWritableDatabase();
        //sanity check of the database
        /*
        * if the key is present in the content values that was passed to update the database,
        * check that the name is not null
        * */
        if(contentValues.containsKey(StorePets.COLUMN_PET_NAME)) {
            String name = new String(contentValues.getAsString((StorePets.COLUMN_PET_NAME)));
            if (name == null) {
                throw new IllegalArgumentException("pet requires a name :");
            }
        }
        /*
        * if the breed key is present in the content values that was passed to update the database,
        * check that the breed is not null
        * */
        if(contentValues.containsKey(StorePets.COLUMN_PET_BREED)) {
            String breed = new String(contentValues.getAsString(StorePets.COLUMN_PET_BREED));
            if (breed == null) {
                throw new IllegalArgumentException("pet requires a breed name");
            }
        }
        /*
        * if the weight key is present in the content values that was passed to update the database,
        * check that the weight is not null and is greater than zero
        *
        * */
        if(contentValues.containsKey(StorePets.COLUMN_PET_WEIGHT)) {
            Integer weight = contentValues.getAsInteger(StorePets.COLUMN_PET_WEIGHT);
            if (weight != null && weight <= 0) {
                Log.v(LOG_TAG,"the weight is wrong here ------"+weight );
                throw new IllegalArgumentException("pet cannot have negative weight--------");

            }
        }

        //if there are no values to update the database , then dont update the database
        if(contentValues.size()==0){
            return 0;
        }


        int count=db.update(StorePets.TABLE_NAME,contentValues,selection,selectionArgs);
        //returns the database rows affected due to the update event
        return  count;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db=mDbHelper.getWritableDatabase();
        int result=sUriMatcher.match(uri);
        //number of rows deleted by the delete operation
        int count;
        switch (result){
            case PETS:
                count= db.delete(StorePets.TABLE_NAME,selection,selectionArgs);
                break;
            case PET_ID:
                selection=StorePets._ID+"=?";
                selectionArgs=new String[]{String.valueOf(ContentUris.parseId(uri))};
                count= db.delete(StorePets.TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("invalid uri to delete from pets table data----"+uri);
        }

        //if the number of rows deleted are greater than zero, then only we need to inform
        //the listeners that the uri has been changed now
        if(count>0){
            //update all the listeners that the uri has been changed now
            //thus we need to reload the cursor objects
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return count;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        int result=sUriMatcher.match(uri);
        switch (result){
            case PETS:
                return StorePetsContract.CONTENT_LIST_TYPE;
            case PET_ID:
                return StorePetsContract.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("illegal uri for the getType method:---"+uri);
        }
    }
}


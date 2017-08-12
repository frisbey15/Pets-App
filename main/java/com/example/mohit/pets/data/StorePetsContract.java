package com.example.mohit.pets.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by mohit on 20/5/17.
 */


/*
* API Contract for the pets app
* */
public final class StorePetsContract {

    //defining the conent autority of the {content Uri}
    public static final String CONTENT_AUTHORITY="com.example.mohit.pets";
    //defining the base content uri
    public static final Uri BASE_CONTENT_URI=Uri.parse("content://"+CONTENT_AUTHORITY);
    //Path to the pets table
    public static final String PATH_PETS="pets";

    //THE MIME type for a list of pets
    public static final String CONTENT_LIST_TYPE= ContentResolver.CURSOR_DIR_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_PETS;
    //the mime type for a single pet
    public static final String CONTENT_ITEM_TYPE=ContentResolver.CURSOR_ITEM_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_PETS;

    //made a private constructor so that no one can instanstiate this class
    private StorePetsContract(){

    }



    /*
     *  public Inner class that defines the constants associated with the pet database table
     *  each entry in the table represents a single pet
     */
    public static class StorePets implements BaseColumns{

        /*
            Content uri to access the pet data in the provider
         */
        public static final Uri CONTENT_URI=Uri.withAppendedPath(BASE_CONTENT_URI,PATH_PETS);

        //name of the database table
        public static final String TABLE_NAME="pets";

        /*
        * unique id for the pets
        * of Integer type
        * */
        public static final String _ID=BaseColumns._ID;

        /*
        * Name of the pet
        * TYPE:TEXT
        * */
        public static final String COLUMN_PET_NAME="name";

        /*
         *Breed of the pet
          * TYPE:TEXT
         */

        public static final String COLUMN_PET_BREED="breed";

        /*
        * Gender of the pet
        * TYPE:INTEGER
        * */
        public static final String COLUMN_PET_GENDER="gender";

        /*
        * Gender of the pet
        * TYPE:INTEGER
        * */
        public static final String COLUMN_PET_WEIGHT="weight";

        /*
        possible values for the gender of the pets
        */
        public static final int GENDER_UNKNOWN=0;
        public static final int GENDER_MALE=1;
        public static final int GENDER_FEMALE=2;

    }
}

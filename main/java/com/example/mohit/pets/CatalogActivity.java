package com.example.mohit.pets;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.preference.ListPreference;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.database.sqlite.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mohit.pets.data.PetDbHelper;
import com.example.mohit.pets.data.StorePetsContract;
import com.example.mohit.pets.data.StorePetsContract.StorePets;

import java.util.ArrayList;


public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG=CatalogActivity.class.getName();
    private static final int PET_LOADER=0;
    private static int REQUEST_CODE=1;
    public PetDbHelper mDbHelper;

    private PetCursorAdapter cursorAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


        //find the list view which will be populated by the pets data
        /*
        * here we will set an emptyview for the listView , when there is no data in the list view
        * to get displayed.
        * */
        ListView listView=(ListView)findViewById(R.id.list_item);
        View emptyView=findViewById(R.id.emptyView);
        listView.setEmptyView(emptyView);


        //to access the database we instantiate our subclass of SQLiteOpenHelper
        //and pass in the context , which is the current activity
        mDbHelper=new PetDbHelper(this);
        //initializing the cursor adapter to contain null cursor value, i.e., it don't contain any
        //data
        cursorAdapter=new PetCursorAdapter(this,null);
        listView.setAdapter(cursorAdapter);


        /*
        * prepare the loader
        * try reconnecting with an existing onw or start a new one
        * */
        getLoaderManager().initLoader(PET_LOADER,null,this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //we will start a new activity on click event
                Intent i=new Intent(CatalogActivity.this,EditorActivity.class);
                Uri uri=Uri.withAppendedPath(StorePets.CONTENT_URI,String.valueOf(id));
                i.putExtra("uri",uri.toString());
              //  Log.v(LOG_TAG,"the passed uri value is:------------"+uri.toString());
                startActivity(i);
            }
        });
    }


    //this function inserts the dummy data into the list
    public void insertData(){

        ContentValues contentValues=new ContentValues();
        contentValues.put(StorePets.COLUMN_PET_NAME,"frisbey");
        contentValues.put(StorePets.COLUMN_PET_BREED,"labrador");
        contentValues.put(StorePets.COLUMN_PET_GENDER,1);
        contentValues.put(StorePets.COLUMN_PET_WEIGHT,12);

        getContentResolver().insert(StorePets.CONTENT_URI,contentValues);

    }

    //displays the pets details on the list by quering through the database
    //without the help of the loaders
    //which is a bad programming practice , so we wont use it anymore in out task
    public void displayDatabaseInfo(){

        //create and/or open a database to read from it
        SQLiteDatabase db=mDbHelper.getReadableDatabase();
        //we need to display all the rows of the data base thus we need to make a query to the data
        //base in order to fetch the data

        //define a projection which specifies the columns we need to read from the database
        String [] projection={StorePets._ID,StorePets.COLUMN_PET_NAME,StorePets.COLUMN_PET_BREED
                ,StorePets.COLUMN_PET_GENDER,StorePets.COLUMN_PET_WEIGHT};
        //filter the results using selection
        //String selection=StorePets._ID+" =?";
        //String [] selectionArgs={"1"};
        /*
        *Cursor cursor=db.query(StorePets.TABLE_NAME,projection,null,null,null,null,null);
        */

        Cursor cursor=getContentResolver().query(StorePets.CONTENT_URI,projection,null,null,null);

        TextView displayView = (TextView) findViewById(R.id.text_view_pet);

        PetCursorAdapter cursorAdapter=new PetCursorAdapter(this,cursor);
        ListView listView=(ListView)findViewById(R.id.list_item);
        listView.setAdapter(cursorAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertData();
                //displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            //displays a dialog box to delete all the pets information
            case R.id.action_delete_all_entries:
                showDeleteDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*
  * function which creates an alert dialog box whenever we want to delete a pet info
  * and it depends on the user whether he want to delete or want to keep a pet in the database
  * */
    private void showDeleteDialog(){
        //creating an alertdialog.builder and set the message
        //and click listeners for the positive and negative buttons of the dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_pets_dailog_msg);
        builder.setPositiveButton(R.string.delete_pet_dialog_delete,new DialogInterface.OnClickListener(){
            //delete all the pets
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllPets();
            }
        });
        builder.setNegativeButton(R.string.delete_pet_dialog_cancel,new DialogInterface.OnClickListener(){
            //pet don't get deleted and we remove the dialog from the screen
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog!=null){
                    dialog.dismiss();
                }
            }
        });
        //create and show the dialog
        AlertDialog alertDialog=builder.create();
        alertDialog.show();

    }
    private void deleteAllPets(){
        getContentResolver().delete(StorePets.CONTENT_URI,null,null);
    }


    //these are the methods that needs to be over ridden in order to load data from the database
    //on a separate thread using the loaders

    //called when a new loader needs to be created
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //creates or open a database in order to query data from it
        SQLiteDatabase db=mDbHelper.getReadableDatabase();

        //define a projection which specifies the columns we need to read from the database
        String [] projection={StorePets._ID,StorePets.COLUMN_PET_NAME,StorePets.COLUMN_PET_BREED
                ,StorePets.COLUMN_PET_GENDER,StorePets.COLUMN_PET_WEIGHT};

        //cursor loader is a loader that queries the content resolver and returns a cursor
        return new CursorLoader(this,StorePets.CONTENT_URI,projection,null,null,null);
    }

    //called when the load has been finished in the loader
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //swap the new cursor in
        //(the framework will take care of the closing the old cursor one we return.)
        cursorAdapter.swapCursor(data);
    }
    //called when a previously created loader needs to be reset, making the data unavailable
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //this is called when the last cursor is about to be get closed
        //we need to make sure we are no longer using it
        cursorAdapter.swapCursor(null);
    }
}
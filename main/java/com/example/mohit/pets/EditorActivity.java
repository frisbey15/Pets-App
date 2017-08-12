package com.example.mohit.pets;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageVolume;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.app.LoaderManager;
import android.widget.Toast;

import com.example.mohit.pets.data.PetDbHelper;
import com.example.mohit.pets.data.StorePetsContract;
import com.example.mohit.pets.data.StorePetsContract.StorePets;
/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor>{

    //checks whether the user has change anything in the pet details
   private boolean mPetHasChanged=false;

    private String activity_name="Add New Pet";

    private PetDbHelper mDbHelper;

    private static final String LOG_TAG=EditorActivity.class.getName();

    private Uri passed_uri=null;

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    //saves the value of the id at which the following pet is saved
    long _id;

    private View.OnTouchListener mTouchListener=new View.OnTouchListener(){

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mPetHasChanged=true;
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            String uri_string=bundle.getString("uri");
            passed_uri=Uri.parse(uri_string);

        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        //setup click listener for each of the view so that we can know if the user has made some changes
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();

        //if the uri is passed
        //that is we are editing the data of an existing pet
        //so we need to fetch the data with the help of loaders from the content providers
        if(passed_uri!=null){
            activity_name="Edit Pet";
            getLoaderManager().initLoader(0,null,this);
        }
        this.setTitle(activity_name);

        //invalidate the options menu
        //i.e., whenever we want to change menu in runtime , we need to invalidateOptionsMenu
        //so that we can call {onPrepareOptionsMenu} and then we can do the required change in menu
        //items, which is hiding the delete button here
        if(passed_uri==null) {
            invalidateOptionsMenu();
        }
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = StorePetsContract.StorePets.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                         mGender = StorePetsContract.StorePets.GENDER_FEMALE; // Female
                    } else {
                        mGender = StorePetsContract.StorePets.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = StorePetsContract.StorePets.GENDER_UNKNOWN; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);

            return true;
    }
    //override this method in order to set the options menu according to the needs
    //whether we want to add pet or edit pet
    //i.e, whether we want delete option in menu or not
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //if this is a add pet activity we want to hide the delete options menu
        if(passed_uri==null){
            MenuItem item=menu.findItem(R.id.action_delete);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //save pet to the database
                savePet();
                //jump back to the catalog activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                //we need to show an alert box in order to delete a pet from the database
                // we need to delete the pet from the database
                showDeleteDialog();
                return true;

            //we need to take care of the case when the user clicked this button without saving
            //the changes made by him.
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                //no changes were made by the user
                if(!mPetHasChanged) {
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(this);
                }
                else{
                    //display a dialog box showing the user that the changes haven't been changed
                    //and set up a click listener to handle when the user clicked the discared changes button
                    DialogInterface.OnClickListener discardClickListener=new DialogInterface.OnClickListener(){
                        //we need to discard the changes and close the current activity
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NavUtils.navigateUpFromSameTask(EditorActivity.this);
                        }
                    };
                    //display the alert dialog box
                    showUnsavedChangesDialog(discardClickListener);
                }
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
        builder.setMessage(R.string.delete_pet_dialog_msg);
        builder.setPositiveButton(R.string.delete_pet_dialog_delete,new DialogInterface.OnClickListener(){
            //delete the pet and return to the parent activity
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletePet();
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
    //delete the pet details from the pets database table
    private void deletePet(){
        int no_of_rows=getContentResolver().delete(passed_uri,null,null);
        finish();
    }


    //insert the pet details into the pets database table
    //or updates the pet details into the pets database table
    private void savePet(){
        //this was the method to insert data when we used to connect to the database directly
        //without the use of the content providers
        /*
        PetDbHelper petDbHelper= new PetDbHelper(this);
        SQLiteDatabase db=petDbHelper.getWritableDatabase();
        */
        //checks the cases when the input is null
        //i.e., the user didn't enter the pet name or breed or weight
        //then the data won't get saved in the database
        if(TextUtils.isEmpty(mNameEditText.getText().toString()))
        {
            Toast.makeText(this,"Can't save:Pet requires a name",Toast.LENGTH_SHORT).show();
            finish();
        }
        else if(TextUtils.isEmpty(mBreedEditText.getText().toString())){
            Toast.makeText(this,"Can't save:Enter breed",Toast.LENGTH_SHORT).show();
            finish();
        }
        else if(TextUtils.isEmpty(mWeightEditText.getText().toString())){
            Toast.makeText(this,"Can't save:Enter Weight",Toast.LENGTH_SHORT).show();
            finish();
        }

        else {
            //now we will insert the data with the help of the content providers
            ContentValues contentValues = new ContentValues();
            contentValues.put(StorePets.COLUMN_PET_NAME, mNameEditText.getText().toString().trim());
            contentValues.put(StorePets.COLUMN_PET_BREED, mBreedEditText.getText().toString().trim());
            contentValues.put(StorePets.COLUMN_PET_GENDER, mGender);
            contentValues.put(StorePets.COLUMN_PET_WEIGHT, Integer.parseInt(mWeightEditText.getText().toString()));


            //the case when we open this activity to create a new pet
            if (passed_uri == null) {
                Uri uri = getContentResolver().insert(StorePets.CONTENT_URI, contentValues);
                _id = Long.valueOf(uri.getLastPathSegment());
            } else {
                int id = getContentResolver().update(passed_uri, contentValues, null, null);
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    /*(Edit Pet) cases: down here
    * these are the methods when we are updating the pet
    * down here first we are quering from the database  so that we can display the info
    * of the pet on which we clicked on
    * */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection={StorePets._ID,StorePets.COLUMN_PET_NAME,StorePets.COLUMN_PET_BREED,
        StorePets.COLUMN_PET_GENDER,StorePets.COLUMN_PET_WEIGHT};
        //cursorLoader is created to query the content resolver and returns a cursor
        return new CursorLoader(this,passed_uri,projection,null,null,null);
    }

    //the data is now accessed from the database and is inside the Cursor object
    //thus we need to display the info on the screen manually
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //initially the value of the the cursor is -1,
        //thus we need to move it to the next row in order to read data from it
        data.moveToNext();
        //collecting the data from the cursor object which contains info about a single pet
        String name=data.getString(data.getColumnIndex(StorePets.COLUMN_PET_NAME));
        String breed=data.getString(data.getColumnIndex(StorePets.COLUMN_PET_BREED));
        int gender=data.getInt(data.getColumnIndex(StorePets.COLUMN_PET_GENDER));
        int weight=data.getInt(data.getColumnIndex(StorePets.COLUMN_PET_WEIGHT));


        //displaying the pet info on screen
        //by setting the textView value on screen from the pets data
        mNameEditText.setText(name);
        mBreedEditText.setText(breed);
        mWeightEditText.setText(String.valueOf(weight));
        //setting the spinner value depending on the three different cases
        //when the gender is unknown (0)
        if(gender==StorePets.GENDER_UNKNOWN){
            mGenderSpinner.setSelection(0);
        }
        //when the gender is male (1)
        else if(gender==StorePets.GENDER_MALE){
            mGenderSpinner.setSelection(1);
        //when the gender is female(2)
        }else{
            mGenderSpinner.setSelection(2);
        }
    }

    //we need to clear the data from the textviews , as the loader no longer exists
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mGenderSpinner.setSelection(0);
        mWeightEditText.setText("");
    }


    /*
    * function which creates an alert dialogue when we leave the activity without saving
    * the changes in the pet's info
    * */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardClickListener){
        //create an alertdialog.builder and set the message,
        //and click listeners for the positive and negative buttons on the dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.unsaved_changes_dialog_discard,discardClickListener);
        builder.setNegativeButton(R.string.unsaved_changes_dialog_edit,new DialogInterface.OnClickListener(){
            //handles the case when the user clicked the "Keep Editing " button
            //so dismiss the dialog and keep editing
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog !=null){
                    dialog.dismiss();
                }
            }
        });
        //create and show the alert Dialog
        AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }



    //overriding this method for the case when the user click this button without saving the data
    //thus we need to display an alertDialog on the screen
    @Override
    public void onBackPressed() {
        //when there is no change made by the user
        if(!mPetHasChanged) {
            super.onBackPressed();
            return;
        }
        //if there is some change made by the user in the pet details
        //create a dialog box in order to warn the user
        //and create a click listener in order to handle the event when the user has click the discared button
        else{
            DialogInterface.OnClickListener discardClickListener=new  DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //user clicked the discard button, so close the current activity
                    finish();
                }
            };
            //display the alert dialog box that there are unsaved changes
            showUnsavedChangesDialog(discardClickListener);
        }
    }
}
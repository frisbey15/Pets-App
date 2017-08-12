package com.example.mohit.pets;

import android.content.Context;
import android.database.Cursor;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.example.mohit.pets.data.StorePetsContract.StorePets;
import com.example.mohit.pets.data.StorePetsContract;

/**
 * Created by mohit on 25/5/17.
 */

public class PetCursorAdapter extends CursorAdapter {

    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    //creates a new view to be displayed when we dont have any view to be recycled
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_pet,parent,false);
    }

    //binds the data with the view in order to get displayed on the screen
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //find the views from the layout in order to change their data contents
        TextView name=(TextView)view.findViewById(R.id.name);
        TextView summary=(TextView)view.findViewById(R.id.summary);

        String pet_name=cursor.getString(cursor.getColumnIndexOrThrow(StorePets.COLUMN_PET_NAME));
        String pet_breed=cursor.getString(cursor.getColumnIndexOrThrow(StorePets.COLUMN_PET_BREED));
        //setting the data contents for the corresponding view of the listitem Views
        //populate view with extracted properties
        name.setText(pet_name);
        summary.setText(pet_breed);
    }
}

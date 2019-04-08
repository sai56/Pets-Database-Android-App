/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.database.Cursor;
import android.app.AlertDialog;


import com.example.android.pets.data.PetContract.PetEntry;


/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int PET_LOADER = 0;
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
    private Uri currentPetUri;

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentPetUri = intent.getData();

        if(currentPetUri==null) {
            setTitle(getString(R.string.editor_activity_title_new_pet));
            invalidateOptionsMenu();
        }
        else {
            setTitle(getString(R.string.editor_activity_title_edit_pet));
            getLoaderManager().initLoader(PET_LOADER, null, this);
        }


            // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();
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
                        mGender =PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = PetEntry.GENDER_UNKNOWN; // Unknown
            }
        });
    }
    private void savePet(){


        long newID;
        ContentValues values = new ContentValues();

        String nameString = mNameEditText.getText().toString().trim();
        values.put(PetEntry.COLUMN_PET_NAME,nameString);

        String breedString = mBreedEditText.getText().toString().trim();
        values.put(PetEntry.COLUMN_PET_BREED,breedString);

        int weight;
        String weightString = mWeightEditText.getText().toString().trim();
        if(TextUtils.isEmpty(weightString)){
            weight = 0;
        }
        else {
            weight = Integer.parseInt(weightString);
        }
        values.put(PetEntry.COLUMN_PET_WEIGHT,weight);

        values.put(PetEntry.COLUMN_PET_GENDER,mGender);
        if(TextUtils.isEmpty(nameString) && TextUtils.isEmpty(breedString) && TextUtils.isEmpty(weightString)){
            return;
        }

        if(currentPetUri==null) {

            Uri newPet = getContentResolver().insert(PetEntry.CONTENT_URI, values);
            newID = ContentUris.parseId(newPet);
        }
        else {
            ContentValues values1 = new ContentValues();

            String nameString1 = mNameEditText.getText().toString().trim();
            values.put(PetEntry.COLUMN_PET_NAME,nameString1);

            String breedString1 = mBreedEditText.getText().toString().trim();
            values.put(PetEntry.COLUMN_PET_BREED,breedString1);

            String weightString1 = mWeightEditText.getText().toString().trim();
            int weight1=Integer.parseInt(weightString1);
            values.put(PetEntry.COLUMN_PET_WEIGHT,weight1);

            values.put(PetEntry.COLUMN_PET_GENDER,mGender);
            newID = getContentResolver().update(currentPetUri, values1, null, null);
        }
        if(newID==-1){
            Toast.makeText(this,R.string.Error ,Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this,R.string.Success,Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                savePet();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (currentPetUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        getContentResolver().delete(currentPetUri,null,null);
        Toast.makeText(this,"Pet deleted",Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[]projection={
                    PetEntry._ID,
                    PetEntry.COLUMN_PET_NAME,
                    PetEntry.COLUMN_PET_BREED,
                    PetEntry.COLUMN_PET_GENDER,
                    PetEntry.COLUMN_PET_WEIGHT
        };
        return new CursorLoader(this,currentPetUri,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_NAME));
            mNameEditText.setText(name);

            String breed = cursor.getString(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_BREED));
            mBreedEditText.setText(breed);

            Integer weight = (cursor.getInt(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_WEIGHT)));
            mWeightEditText.setText(Integer.toString(weight));

            int gender = cursor.getInt(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_GENDER));
            switch (gender){
                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
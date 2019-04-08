package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.android.pets.R;
import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

public class PetProvider extends ContentProvider {

     /** URI matcher code for the content URI for the pets table */
    private static final int PETS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PET_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        //Associates a Uri with a particular code
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS,PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS+"/#",PET_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    private PetDbHelper mHelper ;
    @Override
    public boolean onCreate() {

        mHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        //getReadableDatabase creates and/or opens a database

        SQLiteDatabase database = mHelper.getReadableDatabase();

        /**This cursor will hold the result of the query
         * Cursor is an interface which provides random read and write access to the result
         * returned by a database query
         */
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(PetEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;

    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }
    private Uri insertPet(Uri uri, ContentValues values) {

         // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it

        String validName = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if(validName==null)
            throw new  IllegalArgumentException("Name field required");

        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);

        if(gender==null || (gender!=PetEntry.GENDER_FEMALE && gender!=PetEntry.GENDER_MALE && gender!=PetEntry.GENDER_UNKNOWN))
            throw new IllegalArgumentException("Gender field required");

        Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);

        if(weight != null && weight<0)
            throw new IllegalArgumentException("Invalid weight entered");

        SQLiteDatabase db = mHelper.getWritableDatabase();
        long id = db.insert(PetEntry.TABLE_NAME,null,values);

        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if(values.containsKey(PetEntry.COLUMN_PET_NAME)){
            String validName = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if(validName==null)
                throw new IllegalArgumentException("Name field is required");
        }
        if(values.containsKey(PetEntry.COLUMN_PET_GENDER)){
            Integer validGender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if(validGender!=null || (validGender!=PetEntry.GENDER_FEMALE && validGender!=PetEntry.GENDER_MALE && validGender!=PetEntry.GENDER_UNKNOWN))
                throw new IllegalArgumentException("Gender field is required");
        }
        if(values.containsKey(PetEntry.COLUMN_PET_WEIGHT)){
            Integer validWeight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if(validWeight != null && validWeight<0)
                throw new IllegalArgumentException("Invalid Weight entered");
        }
        if(values.size()==0)
            return 0;
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int rowsUpdated = db.update(PetEntry.TABLE_NAME,values,selection,selectionArgs);
        if(rowsUpdated!=0)
            getContext().getContentResolver().notifyChange(uri,null);
        return rowsUpdated;
    }
    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
                rowsDeleted= database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                if(rowsDeleted!=0)
                    getContext().getContentResolver().notifyChange(uri,null);
                return rowsDeleted;
            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted= database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                if(rowsDeleted!=0)
                    getContext().getContentResolver().notifyChange(uri,null);
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}

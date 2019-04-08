package com.example.android.pets.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class PetContract {
    private PetContract(){}

    public static final String CONTENT_AUTHORITY = "com.example.android.pets";

    /**Uri => Uniform resource identifier

      ->Uri basically is a path to a file or a resource on your storage or on your web
      ->Uri.parse in the foll use case concatenates the two strings and converts them into a Uri object
      ->BaseColumns is simply an interface that has two final constants namely ID and COUNT

     */

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PETS = "pets";

    public final static class PetEntry implements BaseColumns {

        public final static String TABLE_NAME = "pets";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PET_NAME = "name";
        public final static String COLUMN_PET_BREED = "breed";
        public final static String COLUMN_PET_GENDER = "gender";
        public final static String COLUMN_PET_WEIGHT = "weight";

        public final static int GENDER_UNKNOWN = 0;
        public final static int GENDER_MALE = 1;
        public final static int GENDER_FEMALE = 2;

        /**

         MIME type = Multipurpose Internet Mail Extensions

         MIME type associated with the internet is basically a way of telling the client of how to handle data received from the server side
         In the same way MIME type linked with content providers give info as how and what kind of data is to be expected

         */

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS);
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;
        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;
    }

}

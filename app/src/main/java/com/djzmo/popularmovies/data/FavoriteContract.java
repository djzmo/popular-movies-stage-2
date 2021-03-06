package com.djzmo.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class FavoriteContract {

    public static final String AUTHORITY = "com.djzmo.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_FAVORITES = "favorites";

    public static final class FavoriteEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_REMOTE_ID = "remoteId";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER_URL = "posterUrl";
        public static final String COLUMN_TIMESTAMP = "created_at";
    }

}

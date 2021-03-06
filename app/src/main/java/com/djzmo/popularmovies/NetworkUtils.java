package com.djzmo.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;

public class NetworkUtils {

    public final static String API_BASE_URL = "https://api.themoviedb.org/3";
    public final static String POPULAR_ENDPOINT = "/movie/popular";
    public final static String TOP_RATED_ENDPOINT = "/movie/top_rated";
    public final static String MOVIE_DETAIL_ENDPOINT = "/movie/{id}";
    public final static String TRAILERS_ENDPOINT = "/movie/{id}/videos";
    public final static String REVIEWS_ENDPOINT = "/movie/{id}/reviews";
    public final static String THUMBNAIL_BASE_URL = "http://image.tmdb.org/t/p";

    private final static String API_KEY_QUERY = "api_key";

    public static boolean isOnline(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    protected static URL buildCommonApiUrl(Context c, String urlString) {
        Uri uri = Uri.parse(urlString).buildUpon()
                .appendQueryParameter(API_KEY_QUERY, c.getString(R.string.API_KEY))
                .build();
        URL url = null;

        try {
            url = new URL(uri.toString());
        }
        catch(MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL getPopularUrl(Context c) {
        return buildCommonApiUrl(c, API_BASE_URL + POPULAR_ENDPOINT);
    }

    public static URL getTopRatedUrl(Context c) {
        return buildCommonApiUrl(c, API_BASE_URL + TOP_RATED_ENDPOINT);
    }

    public static URL getMovieDetailUrl(Context c, String id) {
        String urlString = API_BASE_URL + MOVIE_DETAIL_ENDPOINT;
        urlString = urlString.replace("{id}", id);
        return buildCommonApiUrl(c, urlString);
    }

    public static URL getTrailersUrl(Context c, String id) {
        String urlString = API_BASE_URL + TRAILERS_ENDPOINT;
        urlString = urlString.replace("{id}", id);
        return buildCommonApiUrl(c, urlString);
    }

    public static URL getReviewsUrl(Context c, String id) {
        String urlString = API_BASE_URL + REVIEWS_ENDPOINT;
        urlString = urlString.replace("{id}", id);
        return buildCommonApiUrl(c, urlString);
    }

}

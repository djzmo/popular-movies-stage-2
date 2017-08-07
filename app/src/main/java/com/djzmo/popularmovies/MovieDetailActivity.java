package com.djzmo.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.djzmo.popularmovies.data.FavoriteContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.djzmo.popularmovies.data.FavoriteContract.FavoriteEntry.COLUMN_POSTER_URL;
import static com.djzmo.popularmovies.data.FavoriteContract.FavoriteEntry.COLUMN_REMOTE_ID;
import static com.djzmo.popularmovies.data.FavoriteContract.FavoriteEntry.COLUMN_TITLE;

public class MovieDetailActivity extends AppCompatActivity {

    public static final String MOVIE_KEY = "movie";
    public static final String TRAILERS_KEY = "trailers";
    public static final String REVIEWS_KEY = "reviews";

    private ScrollView mDetailContent;
    private TextView mErrorTextView, mTrailerHeadingTextView, mReviewHeadingTextView;
    private Button mErrorTryAgainButton;
    private ProgressBar mLoadingIndicator;
    private MovieInformation mMovieInformation;
    private VideoInformation[] mTrailers;
    private ReviewInformation[] mReviews;
    private LinearLayout mTrailersContainer, mReviewsContainer;
    private boolean mDetailTaskFlag, mTrailerTaskFlag, mReviewTaskFlag;
    private String mRemoteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        setTitle(R.string.movie_detail);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        mDetailContent = (ScrollView) findViewById(R.id.sv_detail_content);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_detail);
        mErrorTextView = (TextView) findViewById(R.id.tv_detail_error);
        mErrorTryAgainButton = (Button) findViewById(R.id.btn_try_again);
        mTrailerHeadingTextView = (TextView) findViewById(R.id.tv_trailer_heading);
        mTrailersContainer = (LinearLayout) findViewById(R.id.ll_trailers_container);
        mReviewHeadingTextView = (TextView) findViewById(R.id.tv_review_heading);
        mReviewsContainer = (LinearLayout) findViewById(R.id.ll_reviews_container);

        mErrorTryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadDetail(mRemoteId);
            }
        });

        mDetailTaskFlag = false;
        mTrailerTaskFlag = false;
        mReviewTaskFlag = false;

        showLoadingIndicator();

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if(intent.hasExtra(Intent.EXTRA_TEXT)) {
                mRemoteId = intent.getStringExtra(Intent.EXTRA_TEXT);
                loadDetail(mRemoteId);
            }
            else {
                Toast.makeText(this, R.string.please_supply_id, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(MOVIE_KEY, mMovieInformation);
        outState.putParcelableArray(TRAILERS_KEY, mTrailers);
        outState.putParcelableArray(REVIEWS_KEY, mReviews);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        MovieInformation data = (MovieInformation) savedInstanceState.getParcelable(MOVIE_KEY);
        VideoInformation[] trailers = (VideoInformation[]) savedInstanceState.getParcelableArray(TRAILERS_KEY);
        ReviewInformation[] reviews = (ReviewInformation[]) savedInstanceState.getParcelableArray(REVIEWS_KEY);

        mDetailTaskFlag = true;
        mTrailerTaskFlag = true;
        mReviewTaskFlag = true;
        mRemoteId = data.remoteId;

        setMovieInformation(data);
        setTrailersData(trailers);
        setReviewsData(reviews);

        showDetailContentView(true);

        super.onRestoreInstanceState(savedInstanceState);
    }

    private boolean isFavorited() {
        Cursor result = getContentResolver().query(FavoriteContract.FavoriteEntry.CONTENT_URI, null, COLUMN_REMOTE_ID + " = ?", new String[] {mMovieInformation.remoteId}, null);

        return result.getCount() > 0;
    }

    private void fillViews() {
        TextView title = (TextView) findViewById(R.id.tv_detail_title);
        TextView synopsis = (TextView) findViewById(R.id.tv_detail_synopsis);
        TextView userRating = (TextView) findViewById(R.id.tv_detail_rating);
        TextView year = (TextView) findViewById(R.id.tv_detail_year);
        TextView runtime = (TextView) findViewById(R.id.tv_detail_duration);
        ImageView poster = (ImageView) findViewById(R.id.iv_detail_thumbnail);

        Picasso.with(this).load(mMovieInformation.posterUrl).error(R.drawable.user_placeholder_error).into(poster);
        title.setText(mMovieInformation.title);
        synopsis.setText(mMovieInformation.synopsis);
        userRating.setText(String.format(getString(R.string.rating_formatted), String.valueOf(mMovieInformation.userRating)));
        year.setText(mMovieInformation.releaseDate.substring(0, mMovieInformation.releaseDate.indexOf('-')));
        runtime.setText(String.format(getString(R.string.runtime_formatted), String.valueOf(mMovieInformation.runtime)));
    }

    public void markAsFavorite(View v) {
        ImageButton favoriteButton = (ImageButton) findViewById(R.id.btn_mark_favorite);

        if(isFavorited()) {
            String remoteId = mMovieInformation.remoteId;
            Uri uri = FavoriteContract.FavoriteEntry.CONTENT_URI;
            uri = uri.buildUpon().appendPath(remoteId).build();

            getContentResolver().delete(uri, COLUMN_REMOTE_ID + " = ?", new String[] {mMovieInformation.remoteId});
            favoriteButton.setImageResource(R.drawable.ic_star_border_black_48dp);

            Toast.makeText(this, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show();
        }
        else {
            ContentValues values = new ContentValues();
            values.put(COLUMN_REMOTE_ID, mMovieInformation.remoteId);
            values.put(COLUMN_TITLE, mMovieInformation.title);
            values.put(COLUMN_POSTER_URL, mMovieInformation.posterUrl);

            Uri uri = getContentResolver().insert(FavoriteContract.FavoriteEntry.CONTENT_URI, values);

            if(uri != null)
            {
                favoriteButton.setImageResource(R.drawable.ic_star_black_48dp);
                Toast.makeText(this, R.string.marked_as_favorite, Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDetail(String id) {
        mDetailTaskFlag = false;
        mTrailerTaskFlag = false;
        mReviewTaskFlag = false;

        showLoadingIndicator();
        new FetchDetailsTask().execute(id);
        new FetchTrailersTask().execute(id);
        new FetchReviewsTask().execute(id);
    }

    private void setMovieInformation(MovieInformation data) {
        mMovieInformation = data;
    }

    private void setTrailersData(VideoInformation[] data) {
        mTrailers = data;

        mTrailersContainer.removeAllViews();

        if(data == null || data.length == 0) {
            mTrailerHeadingTextView.setVisibility(View.GONE);
            mTrailersContainer.setVisibility(View.GONE);
        }
        else {
            mTrailerHeadingTextView.setVisibility(View.VISIBLE);
            mTrailersContainer.setVisibility(View.VISIBLE);

            for(int i = 0; i < data.length; ++i) {
                LayoutInflater inflater = LayoutInflater.from(this);
                View row = inflater.inflate(R.layout.trailer_list_item, mTrailersContainer, false);

                final VideoInformation information = data[i];
                TextView trailerName = (TextView) row.findViewById(R.id.tv_trailer_name);

                trailerName.setText(information.name);
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(information.site != null && information.site.toLowerCase().equals("youtube")) {
                            String youtubeUrl = "https://youtube.com/watch?v=" + information.key;
                            Uri uri = Uri.parse(youtubeUrl);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                            if(intent.resolveActivity(getPackageManager()) != null)
                                startActivity(intent);
                        }
                        else Toast.makeText(MovieDetailActivity.this, R.string.type_not_supported, Toast.LENGTH_SHORT).show();
                    }
                });

                mTrailersContainer.addView(row);
            }
        }
    }

    private void setReviewsData(ReviewInformation[] data) {
        mReviews = data;

        mReviewsContainer.removeAllViews();

        if(data == null || data.length == 0) {
            mReviewHeadingTextView.setVisibility(View.GONE);
            mReviewsContainer.setVisibility(View.GONE);
        }
        else {
            mReviewHeadingTextView.setVisibility(View.VISIBLE);
            mReviewsContainer.setVisibility(View.VISIBLE);

            for(int i = 0; i < data.length; ++i) {
                LayoutInflater inflater = LayoutInflater.from(this);
                View row = inflater.inflate(R.layout.review_list_item, mReviewsContainer, false);

                final ReviewInformation information = data[i];
                TextView tvAuthor = (TextView) row.findViewById(R.id.tv_review_author);
                TextView tvContent = (TextView) row.findViewById(R.id.tv_review_content);

                tvAuthor.setText(information.author);
                tvContent.setText(information.content);

                mReviewsContainer.addView(row);
            }
        }
    }

    private void showDetailContentView() {
        ImageButton favoriteButton = (ImageButton) findViewById(R.id.btn_mark_favorite);

        if(isFavorited())
            favoriteButton.setImageResource(R.drawable.ic_star_black_48dp);

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);
        mErrorTryAgainButton.setVisibility(View.INVISIBLE);
        mDetailContent.setVisibility(View.VISIBLE);
    }

    private void showDetailContentView(boolean onlyWhenReady) {
        if(onlyWhenReady && mTrailerTaskFlag && mDetailTaskFlag && mReviewTaskFlag)
        {
            fillViews();
            showDetailContentView();
        }
        else if(!onlyWhenReady)
            showDetailContentView();
    }

    private void showErrorMessage() {
        mErrorTextView.setText(R.string.error_message);

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mDetailContent.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTryAgainButton.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(String message) {
        mErrorTextView.setText(message);

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mDetailContent.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTryAgainButton.setVisibility(View.INVISIBLE);
    }

    private void showErrorMessage(String message, boolean withButton) {
        mErrorTextView.setText(message);

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mDetailContent.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTryAgainButton.setVisibility(withButton ? View.VISIBLE : View.INVISIBLE);
    }

    private void showLoadingIndicator() {
        mDetailContent.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class FetchDetailsTask extends AsyncTask<String, Void, MovieInformation> {

        private boolean mNoConnection;

        @Override
        protected MovieInformation doInBackground(String... strings) {
            if(!NetworkUtils.isOnline(MovieDetailActivity.this) || strings.length == 0)
            {
                mNoConnection = true;
                return null;
            }

            String id = strings[0];
            URL url = NetworkUtils.getMovieDetailUrl(MovieDetailActivity.this, id);
            MovieInformation movie = new MovieInformation();

            try {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                String jsonString = CommonUtils.readInputStream(urlConnection.getInputStream());

                JSONObject data = new JSONObject(jsonString);
                movie.title = data.getString("original_title");
                movie.synopsis = data.getString("overview");
                movie.remoteId = String.valueOf(data.getInt("id"));
                movie.userRating = data.getDouble("vote_average");
                movie.releaseDate = data.getString("release_date");
                movie.posterUrl = NetworkUtils.THUMBNAIL_BASE_URL + "/w185" + data.getString("poster_path");
                movie.runtime = data.getInt("runtime");
            }
            catch(IOException | JSONException e) {
                e.printStackTrace();
            }

            return movie;
        }

        @Override
        protected void onPostExecute(MovieInformation data) {
            if(data != null)
            {
                mDetailTaskFlag = true;
                showDetailContentView(true);
                setMovieInformation(data);
            }
            else if(mNoConnection)
                showErrorMessage(getString(R.string.no_internet_connection), true);
            else showErrorMessage();
        }
    }

    public class FetchTrailersTask extends AsyncTask<String, Void, VideoInformation[]> {

        private boolean mNoConnection;

        @Override
        protected VideoInformation[] doInBackground(String... strings) {
            if(!NetworkUtils.isOnline(MovieDetailActivity.this) || strings.length == 0)
            {
                mNoConnection = true;
                return null;
            }

            String id = strings[0];
            URL url = NetworkUtils.getTrailersUrl(MovieDetailActivity.this, id);
            VideoInformation[] trailers = null;

            try {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                String jsonString = CommonUtils.readInputStream(urlConnection.getInputStream());

                JSONObject root = new JSONObject(jsonString);
                JSONArray results = root.getJSONArray("results");

                trailers = new VideoInformation[results.length()];

                for(int i = 0; i < results.length(); ++i) {
                    JSONObject data = results.getJSONObject(i);
                    trailers[i] = new VideoInformation();
                    trailers[i].site = data.getString("site");
                    trailers[i].key = data.getString("key");
                    trailers[i].name = data.getString("name");
                }
            }
            catch(IOException | JSONException e) {
                e.printStackTrace();
            }

            return trailers;
        }

        @Override
        protected void onPostExecute(VideoInformation[] data) {
            if(mNoConnection)
                showErrorMessage(getString(R.string.no_internet_connection), true);
            else {
                mTrailerTaskFlag = true;
                showDetailContentView(true);
                setTrailersData(data);
            }
        }
    }

    public class FetchReviewsTask extends AsyncTask<String, Void, ReviewInformation[]> {

        private boolean mNoConnection;

        @Override
        protected ReviewInformation[] doInBackground(String... strings) {
            if(!NetworkUtils.isOnline(MovieDetailActivity.this) || strings.length == 0)
            {
                mNoConnection = true;
                return null;
            }

            String id = strings[0];
            URL url = NetworkUtils.getReviewsUrl(MovieDetailActivity.this, id);
            ReviewInformation[] reviews = null;

            try {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                String jsonString = CommonUtils.readInputStream(urlConnection.getInputStream());

                JSONObject root = new JSONObject(jsonString);
                JSONArray results = root.getJSONArray("results");
                int count = root.getInt("total_results");

                if(count > 0) {
                    reviews = new ReviewInformation[results.length()];

                    for(int i = 0; i < results.length(); ++i) {
                        JSONObject data = results.getJSONObject(i);
                        reviews[i] = new ReviewInformation();
                        reviews[i].remoteId = data.getString("id");
                        reviews[i].author = data.getString("author");
                        reviews[i].content = data.getString("content");
                    }
                }
            }
            catch(IOException | JSONException e) {
                e.printStackTrace();
            }

            return reviews;
        }

        @Override
        protected void onPostExecute(ReviewInformation[] data) {
            if(mNoConnection)
                showErrorMessage(getString(R.string.no_internet_connection), true);
            else {
                mReviewTaskFlag = true;
                showDetailContentView(true);
                setReviewsData(data);
            }
        }
    }
}

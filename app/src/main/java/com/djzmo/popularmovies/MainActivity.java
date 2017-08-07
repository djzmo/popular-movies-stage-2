package com.djzmo.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.djzmo.popularmovies.data.FavoriteContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements MovieAdapter.MovieAdapterOnClickHandler {

    public static final int NUM_OF_ITEMS = 20;
    public static final String MOVIES_KEY = "movies";
    public static final String MENU_STATE_KEY = "menu_state";
    public static final String LIST_STATE_KEY = "list_state";
    public static final String LIST_POS_KEY = "list_pos";

    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorTextView;
    private Button mErrorTryAgainButton;
    private Menu mMenu;
    private MovieAdapter mAdapter;
    private String mCurrentDisplay;
    private int mCurrentPosition;
    private Parcelable mLayoutState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.pop_movies);

        mCurrentDisplay = "popular";
        mCurrentPosition = 0;

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);

        mAdapter = new MovieAdapter(this, this);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_movies);
        mErrorTextView = (TextView) findViewById(R.id.tv_movie_error);
        mErrorTryAgainButton = (Button) findViewById(R.id.btn_try_again);

        mErrorTryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMovieData(mCurrentDisplay);
            }
        });

        if(getString(R.string.API_KEY).equals("YOUR_API_KEY"))
            showErrorMessage(getString(R.string.api_key_not_set), false);
        else if(savedInstanceState == null)
            loadMovieData(mCurrentDisplay);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArray(MOVIES_KEY, mAdapter.getData());
        outState.putString(MENU_STATE_KEY, mCurrentDisplay);
        mLayoutState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(LIST_STATE_KEY, mLayoutState);
        outState.putInt(LIST_POS_KEY, ((GridLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        MovieInformation[] data = (MovieInformation[]) savedInstanceState.getParcelableArray(MOVIES_KEY);
        mAdapter.setData(data);
        mCurrentDisplay = savedInstanceState.getString(MENU_STATE_KEY);
        mLayoutState = savedInstanceState.getParcelable(LIST_STATE_KEY);
        mCurrentPosition = savedInstanceState.getInt(LIST_POS_KEY);
        showMovieDataView();
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mCurrentDisplay.equals("favorites"))
            loadMovieData(mCurrentDisplay);

        if(mLayoutState != null)
            mRecyclerView.getLayoutManager().onRestoreInstanceState(mLayoutState);

        if(mCurrentPosition > 0)
            mRecyclerView.scrollToPosition(mCurrentPosition);
    }

    private void loadMovieData(String type) {
        showLoadingIndicator();
        new FetchMovieTask().execute(type);
    }

    private void showMovieDataView() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);
        mErrorTryAgainButton.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.smoothScrollToPosition(0);
    }

    private void showErrorMessage() {
        mErrorTextView.setText(R.string.error_message);

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTryAgainButton.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(String message) {
        mErrorTextView.setText(message);

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTryAgainButton.setVisibility(View.INVISIBLE);
    }

    private void showErrorMessage(String message, boolean withButton) {
        mErrorTextView.setText(message);

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTryAgainButton.setVisibility(withButton ? View.VISIBLE : View.INVISIBLE);
    }

    private void showLoadingIndicator() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);
        mErrorTryAgainButton.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(MovieInformation information) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, information.remoteId);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mMenu = menu;

        MenuItem mostPopular = mMenu.findItem(R.id.menuitem_sort_most_popular);
        MenuItem topRated = mMenu.findItem(R.id.menuitem_sort_top_rated);
        MenuItem favorites = mMenu.findItem(R.id.menuitem_sort_favorites);

        if(mCurrentDisplay.equals("popular")) {
            mostPopular.setChecked(true);
            topRated.setChecked(false);
            favorites.setChecked(false);
        }
        else if(mCurrentDisplay.equals("top_rated")) {
            mostPopular.setChecked(false);
            topRated.setChecked(true);
            favorites.setChecked(false);
        }
        else if(mCurrentDisplay.equals("favorites")) {
            mostPopular.setChecked(false);
            topRated.setChecked(false);
            favorites.setChecked(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        MenuItem mostPopular = mMenu.findItem(R.id.menuitem_sort_most_popular);
        MenuItem topRated = mMenu.findItem(R.id.menuitem_sort_top_rated);
        MenuItem favorites = mMenu.findItem(R.id.menuitem_sort_favorites);

        boolean dirty = false;

        if(id == R.id.menuitem_sort_most_popular && !mCurrentDisplay.equals("popular"))
        {
            mostPopular.setChecked(true);
            topRated.setChecked(false);
            favorites.setChecked(false);
            mCurrentDisplay = "popular";
            dirty = true;
        }
        else if(id == R.id.menuitem_sort_top_rated && !mCurrentDisplay.equals("top_rated"))
        {
            topRated.setChecked(true);
            mostPopular.setChecked(false);
            favorites.setChecked(false);
            mCurrentDisplay = "top_rated";
            dirty = true;
        }
        else if(id == R.id.menuitem_sort_favorites && !mCurrentDisplay.equals("favorites"))
        {
            favorites.setChecked(true);
            mostPopular.setChecked(false);
            topRated.setChecked(false);
            mCurrentDisplay = "favorites";
            dirty = true;
        }

        if(dirty)
            loadMovieData(mCurrentDisplay);

        return super.onOptionsItemSelected(item);
    }

    public class FetchMovieTask extends AsyncTask<String, Void, MovieInformation[]> {

        boolean mNoConnection;

        @Override
        protected MovieInformation[] doInBackground(String... strings) {

            String type = "popular";

            if(strings.length > 0)
                type = strings[0];

            if(!type.equals("popular") && !type.equals("top_rated") && !type.equals("favorites"))
                type = "popular";

            if(!type.equals("favorites") && !NetworkUtils.isOnline(MainActivity.this))
            {
                mNoConnection = true;
                return null;
            }

            MovieInformation[] movieInformation;

            if(type.equals("favorites")) {
                Cursor cursor = getContentResolver().query(FavoriteContract.FavoriteEntry.CONTENT_URI, null, null, null, FavoriteContract.FavoriteEntry.COLUMN_TIMESTAMP);
                int count = cursor.getCount();
                movieInformation = new MovieInformation[count];

                if(count == 0)
                    movieInformation = null;
                else
                {
                    for(int i = 0; i < count; ++i) {
                        cursor.moveToPosition(i);
                        MovieInformation movie = new MovieInformation();
                        movie.remoteId = cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_REMOTE_ID));
                        movie.posterUrl = cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_POSTER_URL));
                        movieInformation[i] = movie;
                    }
                }
            }
            else {
                movieInformation = new MovieInformation[NUM_OF_ITEMS];
                URL url = type.equals("popular") ? NetworkUtils.getPopularUrl(MainActivity.this) : NetworkUtils.getTopRatedUrl(MainActivity.this);

                try {
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    String jsonString = CommonUtils.readInputStream(urlConnection.getInputStream());

                    JSONObject root = new JSONObject(jsonString);
                    JSONArray results = root.getJSONArray("results");

                    for(int i = 0; i < NUM_OF_ITEMS; ++i) {
                        JSONObject data = results.getJSONObject(i);
                        MovieInformation movie = new MovieInformation();
                        movie.title = data.getString("original_title");
                        movie.synopsis = data.getString("overview");
                        movie.remoteId = String.valueOf(data.getInt("id"));
                        movie.userRating = data.getDouble("vote_average");
                        movie.releaseDate = data.getString("release_date");
                        movie.posterUrl = NetworkUtils.THUMBNAIL_BASE_URL + "/w185" + data.getString("poster_path");
                        movieInformation[i] = movie;
                    }
                }
                catch(IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            return movieInformation;
        }

        @Override
        protected void onPostExecute(MovieInformation[] data) {
            if(data != null) {
                showMovieDataView();
                mAdapter.setData(data);
            }
            else if(mNoConnection)
                showErrorMessage(getString(R.string.no_internet_connection), true);
            else showErrorMessage(mCurrentDisplay.equals("favorites") ? getString(R.string.have_not_favorited) : getString(R.string.error_message));
        }
    }
}

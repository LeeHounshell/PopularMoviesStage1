package com.smartvariables.lee.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

import info.movito.themoviedbapi.model.MovieDb;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment
        extends Fragment
        implements AdapterView.OnItemClickListener,
        MovieContext {
    private static String TAG = "LEE: <" + MainActivityFragment.class.getSimpleName() + ">";
    private static List<MovieDb> movieList;
    private static MovieAdapter movieAdapter;
    private GridView gridView;

    public MainActivityFragment() {
        Log.v(TAG, "MainActivityFragment");
    }

    public static List<MovieDb> getMovieList() {
        return movieList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        // init gridView with empty data
        gridView = (GridView) view.findViewById(R.id.poster_gridview);
        if (gridView != null) {
            gridView.setOnItemClickListener(this);
            movieList = new ArrayList<MovieDb>();
            movieAdapter = new MovieAdapter(
                    getActivity(), R.layout.grid_item_movie_view, movieList);
        } else {
            Log.e(TAG, "problem accessing GridView");
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        if (movieAdapter != null && MainActivity.getMainActivity() != null) {
            gridView.setAdapter(movieAdapter);
            loadMovies();
        } else {
            Log.e(TAG, "possible problem with MovieAdapter");
        }
    }

    public void loadMovies() {
        Thread loadMoviesThread = new Thread() {
            @Override
            public void run() {
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
                        getActivity());
                String defaultSortDesc = getResources().getString(R.string.pref_popular);
                String defaultSortCriteria = getResources().getString(R.string.sort_popular);
                final String sortDesc = sharedPrefs.getString("sortDesc", defaultSortDesc);
                final String sortOrder = sharedPrefs.getString("sortBy", defaultSortCriteria);
                MainActivity.getMainActivity()
                        .getHandler()
                        .post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        loadMovies(sortDesc, sortOrder);
                                    }
                                });
            }
        };
        loadMoviesThread.start();
    }

    public void loadMovies(
            String sortDesc,
            String sortKey) {
        Log.v(TAG, "LoadMovies");
        // use AsyncTask to check Internet and populate the grid
        String newTitle = getResources().getString(R.string.app_name) + ": " + sortDesc;
        getActivity().setTitle(newTitle);
        PerformAsyncMovieSearchTask loadMovieDbTask = new PerformAsyncMovieSearchTask(
                (MovieContext) this);
        loadMovieDbTask.execute(TmdbApiKeyHolder.getKey(), sortKey);
        gridView.smoothScrollToPosition(0);
    }

    @Override
    public void onItemClick(
            AdapterView<?> parent,
            View view,
            int position,
            long id) {
        Log.v(TAG, "onItemClick - position=" + position);
        Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
        intent.putExtra("cache_position", new Integer(position));
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu,
            MenuInflater inflater) {
        Log.v(TAG, "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main_poster_grid, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(TAG, "onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
                getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        int id = item.getItemId();

        if (id == R.id.pref_popular) {
            movieAdapter.setMovieData(new ArrayList<MovieDb>());
            String popularityDesc = getResources().getString(R.string.pref_popular);
            String byPopularity = getResources().getString(R.string.sort_popular);
            editor.putString("sortDesc", popularityDesc);
            editor.putString("sortBy", byPopularity);
            editor.commit();
            loadMovies(popularityDesc, byPopularity);
            return true;
        } else if (id == R.id.pref_rated) {
            movieAdapter.setMovieData(new ArrayList<MovieDb>());
            String ratedDesc = getResources().getString(R.string.pref_rated);
            String byRated = getResources().getString(R.string.sort_rated);
            editor.putString("sortDesc", ratedDesc);
            editor.putString("sortBy", byRated);
            editor.commit();
            loadMovies(ratedDesc, byRated);
            return true;
        } else if (id == R.id.pref_release) {
            movieAdapter.setMovieData(new ArrayList<MovieDb>());
            String releaseDesc = getResources().getString(R.string.pref_release);
            String byRelease = getResources().getString(R.string.sort_release);
            editor.putString("sortDesc", releaseDesc);
            editor.putString("sortBy", byRelease);
            editor.commit();
            loadMovies(releaseDesc, byRelease);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public MovieAdapter getMovieGridViewAdapter() {
        return movieAdapter;
    }

}

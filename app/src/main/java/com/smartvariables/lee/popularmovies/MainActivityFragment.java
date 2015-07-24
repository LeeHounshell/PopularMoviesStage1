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


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment
        extends Fragment
        implements AdapterView.OnItemClickListener,
        MovieContext {
    private static String TAG = "LEE: <" + MainActivityFragment.class.getSimpleName() + ">";
    private static MainActivityFragment mainActivityFragment;
    private static MovieAdapter movieAdapter;
    private ArrayList<MovieDbInfo> recoveryList;
    private GridView gridView;

    public MainActivityFragment() {
        Log.v(TAG, "MainActivityFragment");
    }

    public static MainActivityFragment getMainActivityFragment() {
        return mainActivityFragment;
    }

    public static ArrayList<MovieDbInfo> getMovieList() {
        if (movieAdapter == null) {
            return null;
        }
        return movieAdapter.getMovieList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        mainActivityFragment = this;
        setHasOptionsMenu(true);
        recoveryList = new ArrayList<MovieDbInfo>();
        if (savedInstanceState != null && savedInstanceState.containsKey("movieList")) {
            recoveryList = savedInstanceState.getParcelableArrayList("movieList");
            Log.v(TAG, "recover using instance state - recoveryList=" + recoveryList);
        } else {
            Log.v(TAG, "no 'movieList' to recover!");
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        gridView = (GridView) view.findViewById(R.id.poster_gridview);
        if (gridView != null) {
            Log.v(TAG, "create MovieAdapter - recoveryList.size()=" + recoveryList.size());
            movieAdapter = new MovieAdapter(
                    getActivity(), R.layout.grid_item_movie_view, recoveryList);
            gridView.setAdapter(movieAdapter);
            gridView.setOnItemClickListener(this);
            loadMovies();
        } else {
            Log.e(TAG, "problem accessing GridView");
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        if (movieAdapter != null) {
            if (movieAdapter.getCount() == 0 && !MainActivity.isConnected()) {
                Log.v(TAG, "no data cached, and no Internet.. need to complain");
                MainActivity.getMainActivity().checkIfNeedToDisplayNoInternetDialog();
            } else {
                //Log.v(TAG, "notifyDataSetChanged");
                movieAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        ArrayList<MovieDbInfo> theMovieList = getMovieList();
        outState.putParcelableArrayList("movieList", theMovieList);
        super.onSaveInstanceState(outState);
        Log.v(TAG, "--> onSaveInstanceState");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
        mainActivityFragment = null;
    }

    public void loadMovies() {
        Log.v(TAG, "loadMovies (Thread)");
        Thread loadMoviesThread = new Thread() {
            @Override
            public void run() {
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
                        MainActivity.getMainActivity());
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
        Log.v(TAG, "loadMovies");
        // use AsyncTask to check Internet and populate the grid
        String newTitle = getResources().getString(R.string.app_name) + ": " + sortDesc;
        getActivity().setTitle(newTitle);
        if (MainActivity.getMainActivity().isConnected()) {
            Log.v(TAG, "WE HAVE INTERNET!");
            PerformAsyncMovieSearchTask loadMovieDbTask = new PerformAsyncMovieSearchTask(
                    (MovieContext) this);
            loadMovieDbTask.execute(TmdbApiKeyHolder.getKey(), sortKey);
        } else {
            Log.w(TAG, "loadMovies: no Internet..");
            MainActivity.getMainActivity().checkIfNeedToDisplayNoInternetDialog();
        }
        gridView.smoothScrollToPosition(0);
    }

    @Override
    public void onItemClick(
            AdapterView<?> parent,
            View view,
            int position,
            long id) {
        Log.v(TAG, "onItemClick - position=" + position);
        if (MainActivity.isConnected() /*|| (movieAdapter.getCount() > position)*/ ) {
            if (position >= getMovieList().size()) {
                Log.e(TAG, "no movie for index position=" + position + ", getMovieList.size()=" + getMovieList().size());
                return;
            }
            Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
            intent.putExtra("movie", getMovieList().get(position));
            startActivity(intent);
        } else {
            Log.v(TAG, "item click, but no Internet!");
            MainActivity.getMainActivity().checkIfNeedToDisplayNoInternetDialog();
        }
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
        if (!MainActivity.isConnected()) {
            Log.v(TAG, "option click, no Internet..");
            MainActivity.getMainActivity().checkIfNeedToDisplayNoInternetDialog();
            return false;
        }
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
                getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        int id = item.getItemId();

        if (id == R.id.pref_popular) {
            String popularityDesc = getResources().getString(R.string.pref_popular);
            String byPopularity = getResources().getString(R.string.sort_popular);
            editor.putString("sortDesc", popularityDesc);
            editor.putString("sortBy", byPopularity);
            editor.commit();
            loadMovies(popularityDesc, byPopularity);
            return true;
        } else if (id == R.id.pref_rated) {
            String ratedDesc = getResources().getString(R.string.pref_rated);
            String byRated = getResources().getString(R.string.sort_rated);
            editor.putString("sortDesc", ratedDesc);
            editor.putString("sortBy", byRated);
            editor.commit();
            loadMovies(ratedDesc, byRated);
            return true;
        } else if (id == R.id.pref_release) {
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

    public MovieAdapter getMovieAdapter() {
        return movieAdapter;
    }

    @Override
    public ArrayList<MovieDbInfo> getDefaultMovieList() {
        return recoveryList;
    }

}

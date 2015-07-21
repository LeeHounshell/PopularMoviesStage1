package com.smartvariables.lee.popularmovies;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.model.Discover;
import info.movito.themoviedbapi.model.MovieDb;

public class PerformAsyncMovieSearchTask
        extends AsyncTask<String, Void, List<MovieDb>> {
    private static String TAG = "LEE: <" + PerformAsyncMovieSearchTask.class.getSimpleName() + ">";
    private final MovieContext theContext;
    private List<MovieDb> movieList;

    public PerformAsyncMovieSearchTask(MovieContext theContext) {
        this.theContext = theContext;
    }

    @Override
    protected void onPreExecute() {
        Log.v(TAG, "onPreExecute");
        movieList = new ArrayList<MovieDb>();
    }

    @Override
    protected List<MovieDb> doInBackground(String... params) {
        Log.v(TAG, "doInBackground");
        assert params[0] != null : "invalid API key!";
        assert params[1] != null : "invalid sort order!";
        if (MainActivity.getMainActivity() != null && MainActivity.getMainActivity()
                .isConnected()) {
            TmdbApi tmdb = new TmdbApi(params[0]);
            if (tmdb != null) {
                Log.v(TAG, "tmdb=" + tmdb + ", sortBy=" + params[1]);
                Discover discover = new Discover();
                discover.sortBy(params[1]);
                try {
                    movieList = tmdb.getDiscover()
                            .getDiscover(discover)
                            .getResults();
                } catch (NullPointerException e) {
                    Log.e(TAG, "problem accessing tmdb. sorBy=" + params[1] + " - error=" + e);
                }
            } else {
                Log.e(TAG, "unable to access TmdbApi - is the API key correct?");
            }
        } else {
            Log.e(TAG, "is Internet on? - possibly in Airplane mode?");
        }
        return movieList;
    }

    @Override
    protected void onPostExecute(List<MovieDb> movieList) {
        // update grid UI with movies..
        Log.v(TAG, "onPostExecute: theContext=" + theContext + ", movieList=" + movieList);
        theContext.getMovieGridViewAdapter()
                .setMovieData(movieList);
    }

}

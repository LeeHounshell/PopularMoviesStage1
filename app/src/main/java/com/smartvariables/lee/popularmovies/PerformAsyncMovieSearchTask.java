package com.smartvariables.lee.popularmovies;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.model.Discover;
import info.movito.themoviedbapi.model.MovieDb;

public class PerformAsyncMovieSearchTask
        extends AsyncTask<String, Void, ArrayList<MovieDbInfo>> {
    private static String TAG = "LEE: <" + PerformAsyncMovieSearchTask.class.getSimpleName() + ">";
    private final MovieContext theContext;
    private ArrayList<MovieDbInfo> movieList;

    public PerformAsyncMovieSearchTask(MovieContext theContext) {
        this.theContext = theContext;
        this.movieList = theContext.getDefaultMovieList();
    }

    @Override
    protected void onPreExecute() {
        Log.v(TAG, "onPreExecute");
    }

    @Override
    protected ArrayList<MovieDbInfo> doInBackground(String... params) {
        Log.v(TAG, "doInBackground");
        assert params[0] != null : "invalid API key!";
        assert params[1] != null : "invalid sort order!";
        if (MainActivity.isConnected()) {
            TmdbApi tmdb = new TmdbApi(params[0]);
            if (tmdb != null) {
                movieList = new ArrayList<MovieDbInfo>();
                Log.v(TAG, "tmdb=" + tmdb + ", sortBy=" + params[1]);
                Discover discover = new Discover();
                discover.sortBy(params[1]);
                try {
                    List<MovieDb> theMovieList = tmdb.getDiscover()
                            .getDiscover(discover)
                            .getResults();
                    for (MovieDb movie : theMovieList) {
                        MovieDbInfo movieDbInfo = new MovieDbInfo(movie);
                        movieList.add(movieDbInfo);
                        Log.v(TAG, "ADD to movieList: movieDbInfo=" + movieDbInfo.getTitle());
                    }
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
    protected void onPostExecute(ArrayList<MovieDbInfo> movieList) {
        // update grid UI with movies..
        Log.v(TAG, "onPostExecute: theContext=" + theContext + ", movieList.size()=" + movieList.size());
        theContext.getMovieAdapter()
                .setMovieData(movieList);
    }

}

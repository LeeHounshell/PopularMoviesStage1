package com.smartvariables.lee.popularmovies;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.util.List;

import info.movito.themoviedbapi.model.MovieDb;


public class MovieDetailsActivity
        extends ActionBarActivity {
    private static String TAG = "LEE: <" + MovieDetailsActivity.class.getSimpleName() + ">";
    private static MovieDetailsActivity detailActivity;
    private static MovieDb movie;

    public static MovieDetailsActivity getDetailActivity() {
        return detailActivity;
    }

    public static MovieDb getMovie() {
        Log.v(TAG, "getMovie");
        return movie;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        detailActivity = this;
        setContentView(R.layout.activity_movie_details);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        List<MovieDb> movieList = MovieAdapter.getMovieList();
        Integer position = getIntent().getIntExtra("cache_position", 0);
        movie = movieList.get(position);
        Log.d(TAG, "detail: movie=" + movie);
    }

}

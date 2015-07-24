package com.smartvariables.lee.popularmovies;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;


public class MovieDetailsActivity
        extends ActionBarActivity {
    private static String TAG = "LEE: <" + MovieDetailsActivity.class.getSimpleName() + ">";
    private static MovieDetailsActivity detailActivity;
    private static MovieDbInfo movie;

    public static MovieDetailsActivity getDetailActivity() {
        //Log.v(TAG, "getDetailActivity");
        return detailActivity;
    }

    public static MovieDbInfo getMovie() {
        //Log.v(TAG, "getMovie");
        return movie;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        detailActivity = this;
        setContentView(R.layout.activity_movie_details);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        movie = (MovieDbInfo) getIntent().getParcelableExtra("movie");
        Log.d(TAG, "detail: movie=" + movie);
    }

}

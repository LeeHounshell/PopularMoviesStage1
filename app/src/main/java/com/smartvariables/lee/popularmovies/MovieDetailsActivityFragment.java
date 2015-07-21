package com.smartvariables.lee.popularmovies;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;


/**
 * Movie details layout contains title, release date, movie poster, vote average, and plot synopsis.
 */
public class MovieDetailsActivityFragment
        extends Fragment {
    private static String TAG = "LEE: <" + MovieDetailsActivityFragment.class.getSimpleName() + ">";
    private View detailsView;
    private MovieViewHolder detailHolder;

    public MovieDetailsActivityFragment() {
        Log.v(TAG, "MovieDetailsActivityFragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        setHasOptionsMenu(false);
        detailHolder = null;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        detailsView = inflater.inflate(R.layout.fragment_movie_details, container, false);
        return detailsView;
    }

    @Override
    public void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();
        if (MovieDetailsActivity.getMovie() != null) {
            if (detailHolder == null) {
                detailHolder = new MovieViewHolder(
                        this.getActivity(),
                        MovieDetailsActivity.getMovie(),
                        (RatingBar) detailsView.findViewById(R.id.rating_bar),
                        (TextView) detailsView.findViewById(R.id.release_date),
                        (ImageView) detailsView.findViewById(R.id.poster_image),
                        (TextView) detailsView.findViewById(R.id.movie_title),
                        (TextView) detailsView.findViewById(R.id.plot_synopsis),
                        (TextView) detailsView.findViewById(R.id.missing_art),
                        MovieViewHolder.TextForMissingImage.SHOW_NONE);
            }
            new Handler().postDelayed(
                    new Runnable() {

                        @Override
                        public void run() {
                            MovieViewHolder.fixGuiWhenInvalidImageLoaded(detailHolder);
                        }
                    }, detailHolder.getDelayUntilExpectedUpdate());
        }
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu,
            MenuInflater inflater) {
        Log.v(TAG, "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_poster_details, menu);
    }

}

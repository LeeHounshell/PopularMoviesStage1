/*
 * code uses 'themoviedbapi' https://github.com/holgerbrandl/themoviedbapi/
 */
package com.smartvariables.lee.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

public class MovieAdapter
        extends ArrayAdapter<MovieDbInfo> {
    private final static int STATIC_TITLE_LEN_MAX = 20;
    private static String TAG = "LEE: <" + MovieAdapter.class.getSimpleName() + ">";
    private static ArrayList<MovieDbInfo> movieList;
    private Context context;
    private int resource;

    public MovieAdapter(
            Context context,
            int resource,
            ArrayList<MovieDbInfo> movieList) {
        super(context, resource, movieList);
        //Log.v(TAG, "MovieAdapter");
        this.context = context;
        this.resource = resource;
        MovieAdapter.movieList = movieList;
    }

    public static ArrayList<MovieDbInfo> getMovieList() {
        return movieList;
    }

    public void setMovieData(ArrayList<MovieDbInfo> movieList) {
        Log.v(TAG, "===> setMovieData - movieList.size()=" + movieList.size() + " <===");
        MovieAdapter.movieList = movieList;
        //Log.v(TAG, "notifyDataSetChanged");
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (movieList == null) {
            return 0;
        }
        return movieList.size();
    }

    @Override
    public View getView(
            int position,
            View convertView,
            ViewGroup parent) {
        //Log.v(TAG, "getView");
        View row = convertView;
        if (movieList.size() <= position) {
            Log.e(TAG, "movieList[] is not initialized yet! - position=" + position);
            return row;
        }
        final MovieViewHolder holder;

        boolean needToCheckValidImage = false;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(resource, parent, false);
            holder = new MovieViewHolder(
                    context,
                    movieList.get(position),
                    (RatingBar) row.findViewById(R.id.rating_bar),
                    (TextView) row.findViewById(R.id.release_date),
                    (ImageView) row.findViewById(R.id.poster_image),
                    (TextView) row.findViewById(R.id.movie_title),
                    (TextView) row.findViewById(R.id.plot_synopsis),
                    (TextView) row.findViewById(R.id.missing_art),
                    MovieViewHolder.TextForMissingImage.SHOW_TITLE);
            needToCheckValidImage = true;
            row.setTag(holder);
        } else {
            holder = (MovieViewHolder) row.getTag();
        }
        if (!holder.getMovie()
                .equals(movieList.get(position))) {
            holder.initializeMovieViewHolderWithMovie(movieList.get(position));
            needToCheckValidImage = true;
        }

        if (needToCheckValidImage) {
            //Log.v(TAG, "start Thread to check if image loaded ok.. movie=" + holder.getMovie());
            new Handler().postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            MovieViewHolder.fixGuiWhenInvalidImageLoaded(holder);
                            //Log.v(TAG, "notifyDataSetChanged");
                            notifyDataSetChanged();
                        }
                    }, holder.getDelayUntilExpectedUpdate());
        } else {
            //Log.v(TAG, "already cached movie=" + holder.getMovie());
        }
        return row;
    }

    public enum MovieImageViewState {
        LOADING, IMAGE_OK, IMAGE_FAIL
    }

}

package com.smartvariables.lee.popularmovies;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// UI data holder for one movie item
public class MovieViewHolder {
    private static String TAG = "LEE: <" + MovieViewHolder.class.getSimpleName() + ">";
    private static String TMDB_BASE_URL = "http://image.tmdb.org/t/p/";
    private Context context;
    private MovieDbInfo movie;
    private Uri movieInfoUri;
    private RatingBar ratingBar;
    private MovieAdapter.MovieImageViewState movieImageViewState;
    private TextView releaseDateTextView;
    private ImageView posterImageView;
    private TextView movieTitleTextView;
    private TextView plotSynopsis;
    private TextView missingArtTextView;
    private int textColor;
    private int backgroundColor;
    private int delayUntilExpectedUpdate;
    private TextForMissingImage textForMissingImage;

    public MovieViewHolder(
            Context context,
            MovieDbInfo movie,
            RatingBar ratingBar,
            TextView releaseDateTextView,
            ImageView posterImageView,
            TextView movieTitleTextView,
            TextView plotSynopsis,
            TextView missingArtTextView,
            TextForMissingImage textForMissingImage) {
        //Log.v(TAG, "MovieViewHolder - movie=" + movie);
        this.setContext(context);
        this.setRatingBar(ratingBar);
        this.setReleaseDateTextView(releaseDateTextView);
        this.setPosterImageView(posterImageView);
        this.setMovieTitleTextView(movieTitleTextView);
        this.setPlotSynopsis(plotSynopsis);
        this.setMissingArtTextView(missingArtTextView);
        this.setTextForMissingImage(textForMissingImage);
        initializeMovieViewHolderWithMovie(movie);
    }

    public static boolean fixGuiWhenInvalidImageLoaded(MovieViewHolder holder) {
        //Log.v(TAG, "fixGuiWhenInvalidImageLoaded: movie=" + holder.getMovie());
        boolean corrupt = false;
        if (holder.getPosterImageView() == null) {
            corrupt = true;
        } else {
            try {
                Drawable drawable = holder.getPosterImageView()
                        .getDrawable();
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                if (bitmap.getHeight() <= 0 || bitmap.getWidth() <= 0) {
                    Log.w(TAG, "BITMAP SIZE PROBLEM");
                    corrupt = true;
                }
            } catch (NullPointerException e) {
                //Log.w(TAG, "NO BITMAP!");
                corrupt = true;
            }
        }
        if (corrupt) {
            // we have an posterImageView but nothing usable is there..
            //Log.w(TAG, "NO IMAGE: cover for missing poster art");
            if (holder.getTextForMissingImage() == TextForMissingImage.SHOW_TITLE) {
                holder.getMissingArtTextView()
                        .setBackgroundColor(holder.getBackgroundColor());
                holder.getMissingArtTextView()
                        .setText(
                                trim(
                                        holder.getMovie()
                                                .getTitle()));
                holder.getMissingArtTextView()
                        .setTextColor(holder.getTextColor());
                holder.getMissingArtTextView()
                        .setVisibility(View.VISIBLE);
            }
            // show a placeholder for the image..
            Drawable noPosterImage = holder.getContext()
                    .getResources()
                    .getDrawable(R.drawable.noimage);
            holder.getPosterImageView()
                    .setImageDrawable(noPosterImage);
            holder.setMovieImageViewState(MovieAdapter.MovieImageViewState.IMAGE_FAIL);
        } else {
            holder.setMovieImageViewState(MovieAdapter.MovieImageViewState.IMAGE_OK);
            holder.getMissingArtTextView()
                    .setVisibility(View.GONE);
        }
        return corrupt;
    }

    // note: also converts null to empty String
    private static String trim(String toTrim) {
        if (toTrim != null) {
            return toTrim.trim();
        }
        return "";
    }

    public void initializeMovieViewHolderWithMovie(MovieDbInfo movie) {
        //Log.v(TAG, "initializeMovieViewHolderWithMovie - movie=" + movie);
        if (movie == null) {
            Log.e(TAG, "attempt to initialize MovieViewHolder with a null movie!");
            return;
        }
        this.setMovie(movie);
        if (this.getMovie()
                .getPosterPath() != null) {
            this.setMovieInfoUri(
                    Uri.parse(getBaseUrl())
                            .buildUpon()
                            .appendEncodedPath(
                                    getSizeParameter())
                            .appendEncodedPath(
                                    this.getMovie()
                                            .getPosterPath())
                            .build());
        } else {
            //Log.w(TAG, "unable to get movie poster path!");
            this.setMovieInfoUri(null);
            this.setMovieImageViewState(MovieAdapter.MovieImageViewState.IMAGE_FAIL);
        }
        //Log.v(TAG, "title=" + this.getMovie().getTitle());
        if (this.getRatingBar() != null) {
            float rating = this.getMovie()
                    .getVoteAverage() / (float) 2.0;
            this.getRatingBar()
                    .setRating(rating);
        }
        if (this.getPosterImageView() != null) {
            this.getPosterImageView()
                    .setVisibility(View.VISIBLE);
        }
        if (this.getMovieTitleTextView() != null) {
            this.getMovieTitleTextView()
                    .setText(trim(movie.getTitle()));
        }
        if (this.getReleaseDateTextView() != null) {
            this.getReleaseDateTextView()
                    .setText(getReadableDate(trim(movie.getReleaseDate())));
        }
        if (this.getPlotSynopsis() != null) {
            this.getPlotSynopsis()
                    .setText(
                            trim(
                                    this.getMovie()
                                            .getOverview()));
        }
        //this.backgroundColor = context.getResources().getColor(R.color.blue);
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setTextColor(Color.WHITE);
        if (this.getMissingArtTextView() != null) {
            this.getMissingArtTextView()
                    .setBackgroundColor(this.getBackgroundColor());
            this.getMissingArtTextView()
                    .setVisibility(View.GONE);
        }
        this.setDelayUntilExpectedUpdate(300);
        if (this.getMovieInfoUri() != null && MainActivity.isConnected()) {
            //Log.v(TAG, "Internet: request=" + this.getMovieInfoUri());
            this.setMovieImageViewState(MovieAdapter.MovieImageViewState.LOADING);
            Picasso.with(getContext())
                    .load(this.getMovieInfoUri())
                    .into(this.getPosterImageView());
            this.setDelayUntilExpectedUpdate(9000);
        } else {
            this.setMovieImageViewState(MovieAdapter.MovieImageViewState.IMAGE_FAIL);
            //Log.v(TAG, "no Internet or bad URL");
        }
        if (this.getMovieImageViewState() == MovieAdapter.MovieImageViewState.IMAGE_FAIL) {
            // show a placeholder for the image..
            Drawable noPosterImage = this.getContext()
                    .getResources()
                    .getDrawable(R.drawable.noimage);
            this.getPosterImageView()
                    .setImageDrawable(noPosterImage);
        }
    }

    private String getReadableDate(String releaseDate) {
        //Log.v(TAG, "getReadableDate - releaseDate=" + releaseDate);
        if (releaseDate != null) {
            try {
                SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = fromFormat.parse(releaseDate);
                SimpleDateFormat toFormat = new SimpleDateFormat("MMMM, dd yyyy");
                releaseDate = toFormat.format(date);
            } catch (ParseException e) {
                Log.e(TAG, "the release date seems invalid. e=" + e);
            }
        }
        return releaseDate;
    }

    private String getBaseUrl() {
        return TMDB_BASE_URL;
    }

    private String getSizeParameter() {
        // determine based on device screen size
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        //int height = size.y;
        if (width <= 400) {
            return "w92";
        } else if (width <= 800) {
            return "w154";
        } else if (width <= 1280) {
            return "w185";
        } else {
            return "w342";
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public MovieDbInfo getMovie() {
        return movie;
    }

    public void setMovie(MovieDbInfo movie) {
        this.movie = movie;
    }

    public Uri getMovieInfoUri() {
        return movieInfoUri;
    }

    public void setMovieInfoUri(Uri movieInfoUri) {
        this.movieInfoUri = movieInfoUri;
    }

    public RatingBar getRatingBar() {
        return ratingBar;
    }

    public void setRatingBar(RatingBar ratingBar) {
        this.ratingBar = ratingBar;
    }

    public MovieAdapter.MovieImageViewState getMovieImageViewState() {
        return movieImageViewState;
    }

    public void setMovieImageViewState(MovieAdapter.MovieImageViewState movieImageViewState) {
        this.movieImageViewState = movieImageViewState;
    }

    public TextView getReleaseDateTextView() {
        return releaseDateTextView;
    }

    public void setReleaseDateTextView(TextView releaseDateTextView) {
        this.releaseDateTextView = releaseDateTextView;
    }

    public ImageView getPosterImageView() {
        return posterImageView;
    }

    public void setPosterImageView(ImageView posterImageView) {
        this.posterImageView = posterImageView;
    }

    public TextView getMovieTitleTextView() {
        return movieTitleTextView;
    }

    public void setMovieTitleTextView(TextView movieTitleTextView) {
        this.movieTitleTextView = movieTitleTextView;
    }

    public TextView getPlotSynopsis() {
        return plotSynopsis;
    }

    public void setPlotSynopsis(TextView plotSynopsis) {
        this.plotSynopsis = plotSynopsis;
    }

    public TextView getMissingArtTextView() {
        return missingArtTextView;
    }

    public void setMissingArtTextView(TextView missingArtTextView) {
        this.missingArtTextView = missingArtTextView;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getDelayUntilExpectedUpdate() {
        return delayUntilExpectedUpdate;
    }

    public void setDelayUntilExpectedUpdate(int delayUntilExpectedUpdate) {
        this.delayUntilExpectedUpdate = delayUntilExpectedUpdate;
    }

    public TextForMissingImage getTextForMissingImage() {
        return textForMissingImage;
    }

    public void setTextForMissingImage(TextForMissingImage textForMissingImage) {
        this.textForMissingImage = textForMissingImage;
    }

    public enum TextForMissingImage {
        SHOW_NONE, SHOW_TITLE
    }

}

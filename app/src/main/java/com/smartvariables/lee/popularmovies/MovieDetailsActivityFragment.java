package com.smartvariables.lee.popularmovies;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Movie details layout contains title, release date, movie poster, popularity average, and plot synopsis.
 */
public class MovieDetailsActivityFragment
        extends Fragment {
    private static String TAG = "LEE: <" + MovieDetailsActivityFragment.class.getSimpleName() + ">";
    private View detailsView;
    private MovieViewHolder detailHolder;
    private Intent shareIntent;
    private ShareActionProvider shareActionProvider;

    public MovieDetailsActivityFragment() {
        Log.v(TAG, "MovieDetailsActivityFragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        setHasOptionsMenu(true);
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

    /*
     * useful: http://stackoverflow.com/questions/19118051/unable-to-cast-action-provider-to-share-action-provider
     */
    @Override
    public void onCreateOptionsMenu(
            Menu menu,
            MenuInflater inflater) {
        Log.v(TAG, "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_poster_details, menu);
        // Get its ShareActionProvider for this MenuItem
        final MenuItem item = menu.findItem(R.id.menu_item_share);
        // Get the provider and hold onto it to set/change the share intent.
        prepareActionSend();
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(shareIntent);
        } else {
            Log.w(TAG, "no ShareActionProvider!");
        }
    }

    /*
     * useful: http://stackoverflow.com/questions/7661875/how-to-use-share-image-using-sharing-intent-to-share-images-in-android
     */
    private void prepareActionSend() {
        Log.v(TAG, "prepareActionSend");
        Log.v(TAG, "possibly share this movie=" + detailHolder.getMovie());
        shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        // useful: http://stackoverflow.com/questions/8306623/get-bitmap-attached-to-imageview
        ImageView imageView = detailHolder.getPosterImageView();
        if (imageView != null) {
            imageView.setDrawingCacheEnabled(true);
            imageView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            imageView.layout(0, 0,
                    imageView.getMeasuredWidth(), imageView.getMeasuredHeight());
            imageView.buildDrawingCache(true);
            Bitmap poster = Bitmap.createBitmap(imageView.getDrawingCache());
            imageView.setDrawingCacheEnabled(false);
            if (poster != null) {
                String filename = "the_movie_poster.jpg";
                Log.v(TAG, "save image to: " + filename);
                shareIntent.setType("application/image");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                poster.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                File f = new File(Environment.getExternalStorageDirectory() + File.separator + filename);
                try {
                    f.createNewFile();
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                    bytes.close();
                } catch (IOException e) {
                    Log.e(TAG, "problem creating share: " + filename);
                }
                //String sdcard = Environment.getExternalStorageDirectory().getPath();
                String sdcard = "/sdcard/"; // the file must be in a 'sharable' location
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + sdcard + filename));
            }
        }
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, detailHolder.getMovie().getTitle());
        String movieInfo = detailHolder.getMovie().toString();
        String check_out = getResources().getString(R.string.check_out);
        String app_name = getResources().getString(R.string.app_name);
        shareIntent.putExtra(Intent.EXTRA_TEXT, check_out + " '" + app_name + "':\n\n" + movieInfo);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(TAG, "onOptionsItemSelected");
        int id = item.getItemId();
        switch (id) {
            case R.id.home: {
                Log.v(TAG, "case R.id.home - UP PRESSED");
                getActivity().onBackPressed();
                return true;
            }
            case R.id.menu_item_share: {
                Log.v(TAG, "case R.id.menu_item_share");
                Log.v(TAG, "share it..");
                startActivity(Intent.createChooser(shareIntent, "Share movie.."));
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}

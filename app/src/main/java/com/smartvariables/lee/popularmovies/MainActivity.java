/*
 * code patterned from: http://javatechig.com/android/download-and-display-image-in-android-gridview
 */
package com.smartvariables.lee.popularmovies;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;


public class MainActivity
        extends ActionBarActivity {
    private static String TAG = "LEE: <" + MainActivity.class.getSimpleName() + ">";
    private static MainActivity mainActivity;
    AlertDialog.Builder noInternetDialog;
    private boolean connected;
    private Handler handler;

    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainActivity = this;
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        handler = new Handler();
        noInternetDialog = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        getMainActivity().connected = isNetworkAvailable();
                    }
                }).start();
    }

    public Handler getHandler() {
        return handler;
    }

    public boolean isConnected() {
        return connected;
    }

    protected boolean isNetworkAvailable() {
        // from StackOverflow: http://stackoverflow.com/questions/9570237/android-check-internet-connection
        final Context context = (Context) getMainActivity();
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE)
        );
        getMainActivity().connected =
                connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null &&
                        connectivityManager.getActiveNetworkInfo()
                                .isConnected();

        if (!isConnected() && noInternetDialog == null) {
            getMainActivity().getHandler()
                    .post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Log.v(TAG, "oops, no Internet connection!");
                                    noInternetDialog = new AlertDialog.Builder(
                                            getMainActivity());
                                    noInternetDialog.setTitle(
                                            getResources().getString(R.string.offline));
                                    String app_name = getResources().getString(
                                            R.string.app_name);
                                    String message = getResources().getString(
                                            R.string.no_internet);
                                    if (isAirplaneModeOn()) {
                                        message = getResources().getString(
                                                R.string.airplane_mode);
                                    }
                                    message = String.format(message, app_name);
                                    noInternetDialog.setMessage(message);
                                    noInternetDialog.setIcon(R.mipmap.ic_launcher);

                                    noInternetDialog.setPositiveButton(
                                            isAirplaneModeOn() ? getResources().getString(
                                                    R.string.airplane) : getResources().getString(
                                                    R.string.connect),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int id) {
                                                    dialog.dismiss();
                                                    if (isAirplaneModeOn()) {
                                                        Log.v(
                                                                TAG,
                                                                "open airplane mode settings");
                                                        Intent i = new Intent(
                                                                Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                                                        i.putExtra(
                                                                ":android:show_fragment",
                                                                "com.android.settings.AirplaneModeSettings");
                                                        i.putExtra(
                                                                ":android:no_headers",
                                                                true);
                                                        startActivity(i);
                                                    } else {
                                                        Log.v(TAG, "open wifi settings");
                                                        Intent i = new Intent(
                                                                Settings.ACTION_WIFI_SETTINGS);
                                                        startActivity(i);
                                                    }
                                                }
                                            });

                                    noInternetDialog.setNegativeButton(
                                            getResources().getString(R.string.exit),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int id) {
                                                    Log.v(
                                                            TAG,
                                                            "close down the main activity");
                                                    dialog.dismiss();
                                                    getMainActivity().finish();
                                                }
                                            });

                                    if (!isConnected()) {
                                        noInternetDialog.show();
                                    }
                                }
                            });
        }
        return getMainActivity().connected;
    }

    /**
     * Gets the state of Airplane Mode.
     * from: http://stackoverflow.com/questions/4319212/how-can-one-detect-airplane-mode-on-android
     *
     * @param context
     * @return true if enabled.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected boolean isAirplaneModeOn() {
        final Context context = (Context) getMainActivity();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(
                    context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(
                    context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

}

/*
 * code patterned from: http://javatechig.com/android/download-and-display-image-in-android-gridview
 */
package com.smartvariables.lee.popularmovies1;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
    private static volatile boolean connected;
    private static volatile boolean internetDialogActive;
    AlertDialog.Builder noInternetDialog;
    private BroadcastReceiver internetReceiver;
    private Handler handler;

    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    public static boolean isConnected() {
        return MainActivity.connected;
    }

    protected static boolean isNetworkAvailable() {
        // from StackOverflow: http://stackoverflow.com/questions/9570237/android-check-internet-connection
        final Context context = (Context) getMainActivity();
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE)
        );
        MainActivity.connected =
                connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null &&
                        connectivityManager.getActiveNetworkInfo()
                                .isConnected();
        return MainActivity.connected;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
        unregisterReceiver(internetReceiver);
        if (noInternetDialog != null) {
            noInternetDialog = null;
            internetDialogActive = false;
        }
        mainActivity = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        mainActivity = this;
        setContentView(R.layout.activity_main);
        handler = new Handler();
        noInternetDialog = null;
        internetDialogActive = false;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        // if we lose Internet then reconnect, this BroadcastReceiver will detect the change and reload the movieList..
        internetReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG, "onReceive - intent="+intent);
                boolean networkAvailable = MainActivity.isNetworkAvailable();
                if (networkAvailable) {
                    Log.v(TAG, "NETWORK STATE CHANGED! - networkAvailable=" + networkAvailable);
                    MainActivityFragment activityFragment = (MainActivityFragment) MainActivityFragment.getMainActivityFragment();
                    if (activityFragment != null) {
                        Log.v(TAG, "found the MainActivityFragment - loadMovies");
                        activityFragment.loadMovies();
                    }
                }
            }
        };
        registerReceiver(internetReceiver, filter);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.v(TAG, "==> onRestoreInstanceState");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v(TAG, "==> onSaveInstanceState");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.connected = isNetworkAvailable();
                    }
                }).start();
    }

    public Handler getHandler() {
        return handler;
    }

    public void checkIfNeedToDisplayNoInternetDialog() {
        Log.v(TAG, "checkIfNeedToDisplayNoInternetDialog");
        if (!isNetworkAvailable() && internetDialogActive == false) {
            getMainActivity().getHandler()
                    .post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Log.v(TAG, "oops, no Internet connection!");
                                    showNoInternetDialog();
                                }
                            });
        }
    }

    public void showNoInternetDialog() {
        internetDialogActive = true;
        Log.v(TAG, "showNoInternetDialog");
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
                        internetDialogActive = false;
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
                getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(
                            DialogInterface dialog,
                            int id) {
                        dialog.dismiss();
                        internetDialogActive = false;
                    }
                });

        noInternetDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.v(TAG, "onBackPressed within Dialog..");
                dialog.dismiss();
                internetDialogActive = false;
            }
        });

        if (!isConnected()) {
            noInternetDialog.show();
        }
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

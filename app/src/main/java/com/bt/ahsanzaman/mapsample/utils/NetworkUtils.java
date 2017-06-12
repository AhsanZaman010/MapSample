package com.bt.ahsanzaman.mapsample.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Ahsan Zaman on 10-06-2017.
 */

public class NetworkUtils {

    public static boolean isInternetOn(Context context) {
        if (context == null)
            return false;

        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isConnected();
        }

        return false;
    }

}

package com.bt.ahsanzaman.mapsample.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Ahsan Zaman on 09-06-2017.
 */

public class PermissionUtils {

    public static boolean isGranted(Activity context, int requestCode, String[] permissionList) {

            for (String permission : permissionList) {
                if (ContextCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(context,
                            permissionList, requestCode);
                    return false;
                } else {
                    return true;
                }
        }

        return true;
    }

}

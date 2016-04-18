package id.ipaddr.mobile.android.location.currentlocation.helper;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by iippermana on 3/27/16.
 */
public class GPSHelper {

    public static boolean isAbleToGetLocation(Context context){
        if (!isOnline(context) && !isGPSEnable(context)){
            return false;
        }
        return true;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    private static boolean isGPSEnable(Context context){
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if ( manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            return  true;
        }
        return  false;
    }

}

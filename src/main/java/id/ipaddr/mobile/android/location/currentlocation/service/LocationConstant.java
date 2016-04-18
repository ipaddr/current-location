package id.ipaddr.mobile.android.location.currentlocation.service;

/**
 * Created by iippermana on 4/15/16.
 */
public class LocationConstant {
    public static final String ACTION_CURRENT_LOCATION = "id.ipaddr.mobile.android.location.currentlocation.service.ACTION_CURRENT_LOCATION";
    public static final String EXTRA_CURRENT_LOCATION = "id.ipaddr.mobile.android.location.currentlocation.service.EXTRA_CURRENT_LOCATION";
    //    public static final int LOCATION_TIMEOUT_MILLISECONDS=600000; //10 minutes
    public static final int LOCATION_TIMEOUT_MILLISECONDS=2 * 60000; //1 minutes
    public static final int LOCATION_INTERVAL_MILISECONDS = 10 * 1000; // 10 seconds
    public static final int LOCATION_FASTEST_INTERVAL_MILISECONDS = 1 * 1000; // 1 seconds
}

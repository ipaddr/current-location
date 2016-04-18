package id.ipaddr.mobile.android.location.currentlocation.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import id.ipaddr.mobile.android.location.currentlocation.helper.GoogleHelper;

/**
 * Created by iippermana on 3/27/16.
 */
public class LocationService extends Service
        implements
        GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener
        , LocationListener {

    private Handler mSessionTimeoutHandler = new Handler();
    private Runnable mSessionTimeoutTask = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "User session expired");
            stopSessionTimer(true);
        }
    };

    public void startSessionTimer() {
        Log.d(TAG, "Start session timeout.");
        mSessionTimeoutHandler.postDelayed(mSessionTimeoutTask, LocationConstant.LOCATION_TIMEOUT_MILLISECONDS);
    }

    //TODO
    public void stopSessionTimer(boolean isTimeOut) {
        Log.d(TAG, "Cancel session timeout.");
        try {
            if (isTimeOut) {
                //TODO
            }
            mSessionTimeoutHandler.removeCallbacks(mSessionTimeoutTask);

            stopLocationUpdates();
            mRequestingLocationUpdates = false;
            mLocationRequest = null;
            mGoogleApiClient = null;
            mLocationServiceStatus.onLocationServiceStatusDisconnected();
            stopSelf();
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
    }

    private IBinder mBinder = new LocationServiceBinder();

    public final class LocationServiceBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    private LocationServiceStatus mLocationServiceStatus;

    public void setLocationServiceStatus(LocationServiceStatus locationServiceStatus) {
        this.mLocationServiceStatus = locationServiceStatus;
    }

    private static final String TAG = LocationService.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient = null;

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    private LocationRequest mLocationRequest = null;

    public LocationRequest getmLocationRequest() {
        return mLocationRequest;
    }

    private boolean mRequestingLocationUpdates = false;

    @Override
    public IBinder onBind(Intent intent) {
        init();
        return mBinder;
    }

    public void stopLocationService() {
        stopSessionTimer(false);
    }

    private void init() {
        /**
         * Check google play services and register gcm id;
         */
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (GoogleHelper.isGooglePlayInstalled(this)) {
            buildGoogleApiCLient();
            createLocationRequest();
            mGoogleApiClient.connect();
            mRequestingLocationUpdates = true;
        } else {
            Log.d(TAG, "No valid Google Play Services APK found.");
        }
        /**
         *
         */
    }

    private synchronized void buildGoogleApiCLient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LocationConstant.LOCATION_INTERVAL_MILISECONDS);
        mLocationRequest.setFastestInterval(LocationConstant.LOCATION_FASTEST_INTERVAL_MILISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void stopLocationUpdates() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    public void startLocationUpdates() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected() && null != mLocationRequest) {
            startSessionTimer();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    //region google play services
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged(Location location)");
        Log.d(TAG, "onLocationChanged(Location location) : latitude : " + String.valueOf(location.getLatitude()));
        Log.d(TAG, "onLocationChanged(Location location) : longitude : " + String.valueOf(location.getLongitude()));

        Intent broadcastLocationListener = new Intent();
        broadcastLocationListener.setAction(LocationConstant.ACTION_CURRENT_LOCATION);
        broadcastLocationListener.putExtra(LocationConstant.EXTRA_CURRENT_LOCATION, location);
        sendBroadcast(broadcastLocationListener);

        stopSessionTimer(false);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected(Bundle bundle)");
        if (mRequestingLocationUpdates) {
            Log.d(TAG, "onConnected(Bundle bundle) -> IF");
            // notify component about
            if (null != mLocationServiceStatus)
                mLocationServiceStatus.onLocationServiceStatusConnected();
            else
                onConnected(bundle);
        }
        else{
            Log.d(TAG, "onConnected(Bundle bundle) -> ELSE");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(int i)");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed(ConnectionResult connectionResult)");
    }
    //endregion
}

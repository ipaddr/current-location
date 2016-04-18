package id.ipaddr.mobile.android.location.currentlocation.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import id.ipaddr.mobile.android.location.currentlocation.helper.GPSHelper;
import id.ipaddr.mobile.android.location.currentlocation.service.LocationService;
import id.ipaddr.mobile.android.location.currentlocation.service.LocationServiceStatus;

/**
 * Created by iippermana on 4/15/16.
 */
public class LocationFragment extends Fragment implements
        ResultCallback<LocationSettingsResult>
        , LocationServiceStatus
{

    // static TAG
    public static final String TAG = LocationFragment.class.getSimpleName();

    private IntentFilter mIntentFilter;

    /**
     * Constant used in the picker place location.
     */
    public static final int REQUEST_PLACE_PICKER = 1111;
    /**
     * Constant used in the location settings dialog.
     */
    public static final int REQUEST_CHECK_SETTINGS = 1212;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    protected LocationSettingsRequest mLocationSettingsRequest;

    //region bind service
    private LocationService mLocationService;
    private boolean mBound = false;

    public void setupMyCurrentLocation(){
        if (isAdded()){
            if (isAdded() && mBound){
                mLocationService.startLocationUpdates();
            }
            else if (isAdded()){
                // create new
                // update current location & radius
                // Bind to local service
                Intent intent = new Intent(getActivity(), LocationService.class);
                getActivity().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            }
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocationServiceBinder mBinder = (LocationService.LocationServiceBinder)service;
            mLocationService = mBinder.getService();
            mLocationService.setLocationServiceStatus(LocationFragment.this);
            mBound = true;
            Log.d(TAG, "mBound = true;");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            Log.d(TAG, "mBound = false;");
        }
    };
    //endregion bind service

    //region LocationServiceStatus
    @Override
    public void onLocationServiceStatusConnected() {
        if (isAdded() && mBound) {
            buildLocationSettingsRequest();
            checkLocationSettings();
        }
    }

    @Override
    public void onLocationServiceStatusDisconnected() {
        if (isAdded() && mBound){
            getActivity().unbindService(mServiceConnection);
            mBound = false;
        }
    }
    //endregion LocationServiceStatus

    //region ResultCallback<LocationSettingsResult> & gps service
    //TODO
    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    protected void buildLocationSettingsRequest() {
        if (mBound){
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationService.getmLocationRequest());
            builder.setAlwaysShow(true);
            mLocationSettingsRequest = builder.build();
        }
    }

    /**
     * Check if the device's location settings are adequate for the app's needs using the
     * {@link com.google.android.gms.location.SettingsApi #checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} method, with the results provided through a {@code PendingResult}.
     */
    protected void checkLocationSettings() {
        if (mBound){
            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(
                            mLocationService.getmGoogleApiClient(),
                            mLocationSettingsRequest
                    );
            result.setResultCallback(this);
        }
    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        Log.d(TAG, "onResult(LocationSettingsResult locationSettingsResult)");
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                if (mBound) {
                    mLocationService.startLocationUpdates();
                }
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                Toast.makeText(getActivity(), "Location settings are inadequate.", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult(int requestCode, int resultCode, Intent data)");
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        if (mBound) {
                            mLocationService.startLocationUpdates();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        if (mBound) {
                            mLocationService.stopLocationService();
                        }
                        if (GPSHelper.isAbleToGetLocation(getActivity()) && mBound)
                            mLocationService.startLocationUpdates();
                        else
//                            FragmentUtil.dismissProgressDialog(getActivity(), TAG);
                            break;
                    default:
                        break;
                }
                break;
        }
    }
    //endregion


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIntentFilter = new IntentFilter();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }
}

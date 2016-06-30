package org.cysoft.carovignobot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.cysoft.carovignobot.common.CyBssConstants;
import org.cysoft.carovignobot.service.AddressIntentService;

public class SearchLocationActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final String LOG_TAG = this.getClass().getName();

    private GoogleApiClient mGoogleApiClient = null;
    private LocationRequest mLocationRequest = null;
    private Location mCurrentLocation=null;

    private int mRefreshInterval=CyBssConstants.DEFAULT_REFRESH_INTERVAL;

    private final int REQUEST_PERMISSION = 10001;
    private final int REQUEST_CHECK_SETTINGS = 10002;

    private SearchLocationFragment searchLocationFragment=null;


   private BroadcastReceiver adrressReceiver=new BroadcastReceiver() {
       @Override
       public void onReceive(Context context, Intent intent) {
           Log.i(LOG_TAG, "onReceive");
           String address = intent.getStringExtra(getResources().getString(R.string.address_broadcast_label));
           if (searchLocationFragment!=null)
               searchLocationFragment.setTextLocation(address);
       }
   };


   @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       Log.i(LOG_TAG,"onCreate");
       setContentView(R.layout.activity_search_location);

        String locType=(String)getIntent().getExtras().get("locType");
        setTitle(locType);

        SharedPreferences sharedPref = getSharedPreferences(
               getString(R.string.app_key_store), Context.MODE_PRIVATE);
        mRefreshInterval = sharedPref.getInt(getString(R.string.refresh_interval), CyBssConstants.DEFAULT_REFRESH_INTERVAL);

        searchLocationFragment=(SearchLocationFragment)getSupportFragmentManager().findFragmentById(R.id.searchFragment);
        if (searchLocationFragment!=null) {
           searchLocationFragment.setLocationType(locType);
        }

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (mLocationRequest == null)
            createLocationRequest();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG,"onStart");

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG,"onResume");
        registerReceiver(adrressReceiver, new IntentFilter(getResources().getString(R.string.address_broadcast)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG,"onPause");
        unregisterReceiver(adrressReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG,"onStop");
        mGoogleApiClient.disconnect();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG,"onDestroy");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG_TAG, "onConnected");
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, REQUEST_PERMISSION);
                }
            else
                {
                // Message Box
                Log.i(LOG_TAG, "onConnected: No Permission found !");
                new AlertDialog.Builder(this)
                           .setTitle(getResources().getString(R.string.warn_title))
                           .setMessage(getResources().getString(R.string.warn_loc_permission))
                           .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }
                            )
                            .show();
                }
            return;
        }
        else
            addLocationRequest();
    }


    protected void startIntentService(Location location) {
        Intent intent = new Intent(this, AddressIntentService.class);
        //intent.putExtra(CyBssConstants.RECEIVER, addressResultReceiver);
        intent.putExtra(CyBssConstants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }


    protected void addLocationRequest(){

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        final PendingResult<LocationSettingsResult> results = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        results.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates locationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(LOG_TAG, "Success");
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(LOG_TAG, "addLocationRequest:Resolution_Required");
                        try {
                              status.startResolutionForResult(SearchLocationActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(LOG_TAG, "Setting_Change_Unavailable");
                        Toast.makeText(SearchLocationActivity.this,getResources().getString(R.string.toast_setting_change_unavailable),Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(LOG_TAG, "onActivityResult() called with: " + "requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                Log.i(LOG_TAG,"onActivityResult()->REQUEST_CHECK_SETTINGS");
                if (resultCode== Activity.RESULT_OK)
                    addLocationRequest();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }



    protected void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        catch (SecurityException se){
            Log.e(LOG_TAG, "onConnected:" + se.getMessage());
        }
    }

    protected void createLocationRequest(){
        mLocationRequest=new LocationRequest();
        mLocationRequest.setInterval(mRefreshInterval*1000);
        Log.i(LOG_TAG,"Interval="+mLocationRequest.getInterval());
        mLocationRequest.setFastestInterval(mRefreshInterval*1000/2);
        Log.i(LOG_TAG,"Faster Interval="+mLocationRequest.getFastestInterval());
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder=new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(LOG_TAG, "onRequestPermissionsResult");
        switch(requestCode){
            case REQUEST_PERMISSION:
                Log.i(LOG_TAG, "onRequestPermissionsResult:Request Permission:"+grantResults.length);
                boolean granted=false;
                for(int i=0;i<grantResults.length;i++)
                    if (grantResults[i]==PackageManager.PERMISSION_GRANTED)
                        granted=true;
                if (granted) {
                    Toast.makeText(this,getResources().getString(R.string.toast_loc_permission),Toast.LENGTH_LONG).show();
                    addLocationRequest();
                }
                break;

        }
    }




    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG,"onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG,"onConnectionFailed:"+connectionResult.toString());

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(LOG_TAG,"onLocationChanged");
        Log.i(LOG_TAG,"onLocationChanged-Location="+location.getLatitude()+","+location.getLongitude());
        if (Geocoder.isPresent())
            startIntentService(location);
        else
            Log.i(LOG_TAG,"onLocationChanged-Geocoder not present!");

        if (mCurrentLocation==null || mCurrentLocation.getLatitude()!=location.getLatitude()
                || mCurrentLocation.getLongitude()!=location.getLongitude()) {

            if (searchLocationFragment!=null)
                searchLocationFragment.setCurrentLocation(location);
            mCurrentLocation=location;
        }

    }

    /*
    @Override
    public void onBackPressed(){

        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            Log.i(LOG_TAG, "popping backstack");
            fm.popBackStack();
        } else {
            Log.i(LOG_TAG, "nothing on backstack, calling super");
            super.onBackPressed();

        }
        //finish();
    }
    */
}

package org.cysoft.carovignobot;

import android.*;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.internal.StringListResponse;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.cysoft.carovignobot.common.CyBssConstants;
import org.cysoft.carovignobot.model.CyLocation;
import org.cysoft.carovignobot.task.FindLocationResultListener;
import org.cysoft.carovignobot.task.FindLocationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.facebook.FacebookSdk;

public class MainActivity extends
        AppCompatActivity
        implements
        FindLocationResultListener,GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener

{
    private final String LOG_TAG=this.getClass().getName();

    private final int REQUEST_PERMISSION = 10001;

    private GoogleMap googleMap=null;
    private String mCurrentMarkLocId=null;
    private double mCurrentLatitude=0.0d;
    private double mCurrentLongitude=0.0d;
    private String mCurrentLocName="";


    private GoogleApiClient mGoogleApiClient = null;
    private boolean mGoLastLocation=false;

    private Marker currentMarker=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        //AppEventsLogger.activateApp(this);
        // end facebook

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ((ImageButton)findViewById(R.id.buttonBot)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getResources().getString(R.string.bot_url)));
                startActivity(intent);
            }
        });

        ((ImageButton)findViewById(R.id.buttonSearch)).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final CharSequence locTypes[]=new CharSequence[]{ getResources().getString(R.string.loc_tourist_site_name),
                    getResources().getString(R.string.loc_event_name),
                    getResources().getString(R.string.loc_story_name)
            };
            AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getResources().getString(R.string.loc_selection_title));
            builder.setItems(locTypes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(LOG_TAG, locTypes[which].toString());

                    Intent intent = new Intent(MainActivity.this,SearchLocationActivity.class);
                    intent.putExtra("locType",locTypes[which].toString());
                    startActivity(intent);

                }
            });
            builder.show();
            }
        });


        ((ImageButton)findViewById(R.id.buttonMaps)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final CharSequence mapTypes[]=new CharSequence[]{ getResources().getString(R.string.google_map_type_name),
                        getResources().getString(R.string.osm_map_type_name)};
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getResources().getString(R.string.map_type_selection_title));
                builder.setItems(mapTypes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(LOG_TAG,mapTypes[which].toString());
                        final int mapTypeSelected=which;

                        final CharSequence maps[] = new CharSequence[]{getResources().getString(R.string.tourist_site_map_name),
                                getResources().getString(R.string.event_map_name),
                                getResources().getString(R.string.story_map_name),
                        };

                        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(getResources().getString(R.string.map_selection_title));
                        builder.setItems(maps, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(LOG_TAG,maps[which].toString());
                                Log.i(LOG_TAG,mapTypes[mapTypeSelected].toString());

                                if (mapTypes[mapTypeSelected].toString().equals(
                                        getResources().getString(R.string.google_map_type_name))){

                                    Intent intent = new Intent(MainActivity.this,MapActivity.class);
                                    intent.putExtra("mapType",maps[which].toString());
                                    startActivity(intent);

                                }
                                else {
                                    String url;

                                    // OSM Map
                                    if (maps[which].toString().equals(getResources().getString(R.string.event_map_name)))
                                        url=getResources().getString(R.string.event_map_url_osm);
                                    else
                                    if (maps[which].toString().equals(getResources().getString(R.string.story_map_name)))
                                        url=getResources().getString(R.string.story_map_url_osm);
                                    else
                                        url=getResources().getString(R.string.tourist_site_map_url_osm);

                                    Log.i(LOG_TAG,"mapUrl="+url);

                                    Intent intent = new Intent(MainActivity.this,WebMapActivity.class);
                                    intent.putExtra("mapUrl",url);
                                    intent.putExtra("mapType",maps[which].toString());
                                    startActivity(intent);

                                }



                            }
                        });
                        builder.show();

                    }
                });
                builder.show();

            }
        });

        ((ImageButton)findViewById(R.id.buttonView)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentMarkLocId!=null){
                    Intent intent = new Intent(MainActivity.this, DetailLocationActivity.class);
                    intent.putExtra("locationId", mCurrentMarkLocId);
                    intent.putExtra("title",getResources().getString(R.string.loc_tourist_site_name));
                    startActivity(intent);
                }
                else
                {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(getResources().getString(R.string.warn_title))
                            .setMessage(getResources().getString(R.string.warn_select_marker))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }
                            )
                            .show();
                }
            }
        });


        ((ImageButton)findViewById(R.id.buttonLocation)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (mGoogleApiClient == null) {
                    mGoogleApiClient = new GoogleApiClient.Builder(getBaseContext())
                            .addConnectionCallbacks(MainActivity.this)
                            .addOnConnectionFailedListener(MainActivity.this)
                            .addApi(LocationServices.API)
                            .build();
                }

                if (mGoogleApiClient != null) {
                    mGoLastLocation=true;
                    if (!mGoogleApiClient.isConnected())
                        mGoogleApiClient.connect();
                    else
                        goLastLocation();
                }

            }
        });

        SupportMapFragment mappa=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
        mappa.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.i(LOG_TAG,"getMapAsync");
                MainActivity.this.googleMap=googleMap;
                googleMap.setOnMarkerClickListener(MainActivity.this);

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(CyBssConstants.CAROVIGNO_LATLNG)
                        .zoom(15)                  // Sets the zoom
                        .bearing(0)                // Sets the orientation of the camera to east
                        .tilt(0)                   // Sets the tilt of the camera
                        .build();                  // Creates a CameraPosition from the builder

                FindLocationTask task=new FindLocationTask( MainActivity.this,FindLocationTask.TOURIST_SITES,null);
                task.addFindLocationResultLister(MainActivity.this);
                task.executeOnExecutor(FindLocationTask.THREAD_POOL_EXECUTOR,"");

                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


            }
        });

        // Load Spinner Map Types
        String[] mapTypes=new String[]{
                getResources().getString(R.string.map_type_normal),
                getResources().getString(R.string.map_type_satellite),
                getResources().getString(R.string.map_type_hybrid)
        };

        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,mapTypes);
        Spinner mapTypesSpinner=(Spinner)findViewById(R.id.spinnerMapTypes);
        if (mapTypesSpinner!=null) {
            mapTypesSpinner.setAdapter(arrayAdapter);

            mapTypesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.i(LOG_TAG, "onItemSelected:" + position + ";" + id);
                    String mapType=(String)parent.getAdapter().getItem(position);
                    if (mapType!=null){
                        if (mapType.equals(getResources().getString(R.string.map_type_hybrid))){
                            if (googleMap!=null)
                                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        }
                        else
                            if (mapType.equals(getResources().getString(R.string.map_type_satellite))){
                                if (googleMap!=null)
                                    googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                            }
                        else
                            {
                                if (googleMap!=null)
                                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            }

                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        checkPermission();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient!=null)
            mGoogleApiClient.disconnect();
    }

    protected void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String[] permissions = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
                requestPermissions(permissions, REQUEST_PERMISSION);
            } else {
                // Message Box
                Log.i(LOG_TAG, "checkPermission: No Permission found !");
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
            if (mGoLastLocation)
                goLastLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(LOG_TAG, "onRequestPermissionsResult");
        switch(requestCode){
            case REQUEST_PERMISSION:
                Log.i(LOG_TAG, "onRequestPermissionsResult:Request Permission:"+grantResults.length);
                boolean granted=false;
                for(int i=0;i<grantResults.length;i++)
                    if (grantResults[i]==PackageManager.PERMISSION_GRANTED)
                        granted=true;
                // Toast
                if (granted) {
                    Toast.makeText(this, getResources().getString(R.string.toast_loc_permission), Toast.LENGTH_LONG).show();
                    if (mGoLastLocation)
                        goLastLocation();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            SettingDialog settingDialog=new SettingDialog();
            settingDialog.show(getSupportFragmentManager(),"SettingDialog");
            return true;
        }

        if (id == R.id.action_info) {
            InfoDialog infoDialog=InfoDialog.newInstance();
            infoDialog.show(getSupportFragmentManager(),"InfoDialog");
            return true;
        }


        if (id == R.id.menu_item_share){
            Log.i(LOG_TAG, "onItemShare");

            if (mCurrentMarkLocId!=null){

                Intent shareIntent=new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String uri="http://maps.google.com/maps?&q="+mCurrentLatitude+","+mCurrentLongitude;

                shareIntent.putExtra(Intent.EXTRA_SUBJECT,mCurrentLocName+" "+getResources().getString(R.string.share_suffix_label));
                shareIntent.putExtra(Intent.EXTRA_TEXT,uri+" - "+mCurrentLocName+" "+getResources().getString(R.string.share_suffix_label));

                startActivity(Intent.createChooser(shareIntent,getResources().getString(R.string.share_button_label)));
            }
            else
            {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getResources().getString(R.string.warn_title))
                        .setMessage(getResources().getString(R.string.warn_select_marker))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }
                        )
                        .show();
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void reiceveFindLocationResult(List<CyLocation> locations) {
        if (googleMap!=null) {
            if (locations!=null)
                for (CyLocation loc:locations) {
                    MarkerOptions marker = new MarkerOptions()
                            .position(new LatLng(loc.latitude, loc.longitude))
                            .title(loc.name)
                            .snippet(new Long(loc.id).toString());
                    googleMap.addMarker(marker);
                }
        }
    }

    @Override
    public void reiceveFindLocationError(String errorMessage) {
        Log.e(LOG_TAG,"reiceveFindLocationError:"+errorMessage);
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.error_title))
                .setMessage(getResources().getString(R.string.error_check_connection))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }
                )
                .show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.e(LOG_TAG,"onMarkerClick:"+marker.getSnippet());
        if (marker!=null) {
            if (marker.getSnippet()!=null&&(!marker.getSnippet().startsWith("lat")))
            {
                mCurrentMarkLocId = marker.getSnippet();
                mCurrentLatitude = marker.getPosition().latitude;
                mCurrentLongitude = marker.getPosition().longitude;
                mCurrentLocName = marker.getTitle();
            }
        }
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(LOG_TAG,"onConnected");
        checkPermission();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(LOG_TAG,"onConnectionSuspended");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_TAG,"onConnectionFailed");

    }

    private void goLastLocation(){
        Log.e(LOG_TAG,"goLastLocation");
        if (mGoogleApiClient!=null){
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                checkPermission();
                return;
            }
                else
            {

                Location location=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                Log.e(LOG_TAG,"goLastLocation-location:"+location);
                if (location!=null) {

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(),location.getLongitude()))
                            .zoom(17)                  // Sets the zoom
                            .bearing(0)                // Sets the orientation of the camera to east
                            .tilt(0)                   // Sets the tilt of the camera
                            .build();                  // Creates a CameraPosition from the builder


                    if (googleMap != null) {
                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        MarkerOptions marker = new MarkerOptions()
                                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pinorange48))
                                .title(getResources().getString(R.string.position_markup_title))
                                .snippet("lat="+location.getLatitude()+
                                        ",lon="+location.getLongitude()+
                                        ",alt="+location.getAltitude());

                        if (currentMarker!=null)
                            currentMarker.remove();

                        currentMarker=googleMap.addMarker(marker);

                    }
                } // location!=null

            }

        }
    }

}

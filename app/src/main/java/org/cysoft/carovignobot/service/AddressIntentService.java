package org.cysoft.carovignobot.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import org.cysoft.carovignobot.R;
import org.cysoft.carovignobot.common.CyBssConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by NS293854 on 31/05/2016.
 */
public class AddressIntentService extends IntentService{

    private final String LOG_TAG=this.getClass().getName();

    //protected ResultReceiver mReceiver=null;

    public AddressIntentService(String name) {
        super(name);
    }
    public AddressIntentService() {
        super("CarovignoBot.AddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(LOG_TAG, "onHandleIntent");
        String errorMessage = "";
        Location location = intent.getParcelableExtra(
                CyBssConstants.LOCATION_DATA_EXTRA);

        //mReceiver=intent.getParcelableExtra(CyBssConstants.RECEIVER);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address.
                    1);

        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available);
            Log.e(LOG_TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(LOG_TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(LOG_TAG, errorMessage);
            }
            deliverResult(CyBssConstants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(LOG_TAG, getString(R.string.address_found));
            deliverResult(CyBssConstants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));
        }



    }

    private void deliverResult(int resultCode, String message) {
            Log.i(LOG_TAG,"deliverResult:"+message);
            Intent broadcast=new Intent(getResources().getString(R.string.address_broadcast));
            broadcast.putExtra(getResources().getString(R.string.address_broadcast_label),message);
            sendBroadcast(broadcast);
            /*
            Bundle bundle = new Bundle();
            bundle.putString(CyBssConstants.RESULT_DATA_KEY, message);
            if (mReceiver!=null)
                mReceiver.send(resultCode, bundle);
            */
        }

}

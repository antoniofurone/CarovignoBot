package org.cysoft.carovignobot.common;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by NS293854 on 31/05/2016.
 */
public final class CyBssConstants {

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME =
            "com.google.android.gms.location.sample.locationaddress";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME +
            ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";


    public static final int EARTH_RADIUS = 6371; // Radius of the earth

    public static final int DEFAULT_REFRESH_INTERVAL=120;

    public static final LatLng CAROVIGNO_LATLNG = new LatLng(40.706936,17.657968 );


}

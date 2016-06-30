package org.cysoft.carovignobot.task;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.cysoft.carovignobot.R;
import org.cysoft.carovignobot.common.CyBssException;
import org.cysoft.carovignobot.common.SimpleHttpClient;
import org.cysoft.carovignobot.model.CyLocation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Created by NS293854 on 13/05/2016.
 */
public class FindLocationTask  extends AsyncTask<String,Void,List<CyLocation>>{

    public final static String TOURIST_SITES="TouristSiteLocation";
    public final static String STORY="StoryLocation";
    public final static String EVENT="EventLocation";

    private final static int NUM_MAX_LOCS=300;

    private final String LOG_TAG=this.getClass().getName();

    private String mLocationType="";
    private Location mLocationRif=null;
    private Context mContext;

    private String errorMessage="";
    private boolean isInError=false;

    public FindLocationTask(Context context, String locationType, Location locationRif){
        mContext=context;
        mLocationType=locationType;
        mLocationRif=locationRif;

    }


    private List<FindLocationResultListener> listeners=new ArrayList<FindLocationResultListener>();

    public synchronized void addFindLocationResultLister(FindLocationResultListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeFindLocationResultLister(FindLocationResultListener listener){
        listeners.remove(listener);
    }


    @Override
    protected List<CyLocation> doInBackground(String... params) {
        isInError=false;
        errorMessage="";

        String searchString="";
        if (params.length>0 && params[0]!=null && !params[0].equals("") )
            searchString="%"+params[0]+"%";
        Log.i(FindLocationTask.class.getName(), "Search Param=" + searchString);

        List<CyLocation> locations=null;

        try {

            // name
            String url= mContext.getResources().getString(R.string.core_url)+"/rest/location/find?name="
                    + URLEncoder.encode(searchString, "UTF-8")+"&locationType="+mLocationType;
            Log.i(this.getClass().getName(),"url="+url);

            locations=new ArrayList<CyLocation>();

            /*
            Properties systemProperties = System.getProperties();
            systemProperties.setProperty("http.proxyHost","10.29.176.1");
            systemProperties.setProperty("http.proxyPort","8080");

            Authenticator authenticator = new Authenticator() {

                public PasswordAuthentication getPasswordAuthentication() {
                    return (new PasswordAuthentication("ns293854",
                            "a230703$".toCharArray()));
                }
            };
            Authenticator.setDefault(authenticator);
            */

            SimpleHttpClient httpClient=new SimpleHttpClient(url);

            String language="";

            if (Locale.getDefault().getCountry().equalsIgnoreCase("IT"))
                language="it";
            String response=httpClient.getResult(language);

            //Log.i(this.getClass().getName(),"response="+response);
            JSONObject jsonResponse = new JSONObject(response);
            String resultCode=jsonResponse.getString("resultCode");

            //Log.i(LOG_TAG,"resultCode="+resultCode);
            if (!resultCode.equals("00"))
                throw new CyBssException("WsRest Result code <> '00'");

            JSONArray jsonArray=jsonResponse.getJSONArray("locations");

            Map<Long,String> hMap=new LinkedHashMap<Long,String>();
            for (int i=0;i<jsonArray.length();i++) {
                JSONObject jsonLoc = jsonArray.getJSONObject(i);

                CyLocation location=getLocationfromJson(jsonLoc);

                locations.add(location);
                hMap.put(location.id, location.name);
                if (locations.size()>NUM_MAX_LOCS)
                   break;
            } // end for
            // end name

            // description
            url=mContext.getResources().getString(R.string.core_url)+"/rest/location/find?description="
                    + URLEncoder.encode(searchString, "UTF-8")+"&locationType="+mLocationType;
            Log.i(this.getClass().getName(),"url="+url);
            httpClient=new SimpleHttpClient(url);
            response=httpClient.getResult(language);

            //Log.i(this.getClass().getName(),"response="+response);
            jsonResponse = new JSONObject(response);
            resultCode=jsonResponse.getString("resultCode");

            //Log.i(LOG_TAG,"resultCode="+resultCode);
            if (!resultCode.equals("00"))
                throw new CyBssException("WsRest Result code <> '00'");

            jsonArray=jsonResponse.getJSONArray("locations");
            for (int i=0;i<jsonArray.length();i++) {
                JSONObject jsonLoc = jsonArray.getJSONObject(i);

                CyLocation location=getLocationfromJson(jsonLoc);
                if (!hMap.containsKey(location.id))
                    locations.add(location);

                hMap.put(location.id, location.name);
                if (locations.size()>NUM_MAX_LOCS)
                    break;
            } // end for
            // end description

            if (mLocationRif!=null)
                Collections.sort(locations);

            // debug
            /*
            for(int i=0;i<50;i++){
                CyLocation location=new CyLocation();
                location.id=i;
                location.name="Name"+i;
                location.description="Description"+i;
                locations.add(location);
            }
            */
            // end debug


        } catch (UnsupportedEncodingException | CyBssException |JSONException e) {
            e.printStackTrace();
            Log.e(this.getClass().getName(),e.getMessage());
            isInError=true;
            errorMessage=e.getMessage();
        }


        return locations;
    }

    private CyLocation getLocationfromJson(JSONObject jsonLoc)
        throws JSONException
    {
        CyLocation location=new CyLocation();

        location.id=jsonLoc.getLong("id");
        location.name=jsonLoc.getString("name");
        location.description=jsonLoc.getString("description");
        location.latitude=jsonLoc.getDouble("latitude");
        location.longitude=jsonLoc.getDouble("longitude");

        if (mLocationType.equals(STORY))
            location.name = location.description.length() > 30 ? location.description.substring(0, 30) + " [...]" : location.description;

        if (mLocationRif!=null) {
            location.calcDistance(mLocationRif);
            location.formattedDistance=" ";
            if (Locale.getDefault().getCountry().equalsIgnoreCase("IT"))
                location.formattedDistance+= NumberFormat.getNumberInstance(Locale.ITALIAN).format((int)location.distance);
            else
                location.formattedDistance+= NumberFormat.getNumberInstance(Locale.US).format((int)location.distance);
            location.formattedDistance+=" mt ";
        }

        location.description = (location.description.length() > 100 ? location.description.substring(0, 100) + " [...]" : location.description);

        return location;
    }


    @Override
    protected void onPostExecute(List<CyLocation> locations) {
        super.onPostExecute(locations);
        if (!isInError)
            for(FindLocationResultListener listener:listeners)
                listener.reiceveFindLocationResult(locations);
        else
            for(FindLocationResultListener listener:listeners)
                listener.reiceveFindLocationError(errorMessage);
    }

}

package org.cysoft.carovignobot.task;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.cysoft.carovignobot.R;
import org.cysoft.carovignobot.common.CyBssException;
import org.cysoft.carovignobot.common.SimpleHttpClient;
import org.cysoft.carovignobot.model.CyFile;
import org.cysoft.carovignobot.model.CyLocation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by NS293854 on 02/06/2016.
 */
public class GetLocationTask extends AsyncTask<String,Void,CyLocation> {

    private final String LOG_TAG=this.getClass().getName();

    private String errorMessage="";
    private boolean isInError=false;

    private Context mContext;


    private List<GetLocationResultListener> listeners=new ArrayList<GetLocationResultListener>();
    public synchronized void addGetLocationResultLister(GetLocationResultListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeGetLocationResultLister(GetLocationResultListener listener){
        listeners.remove(listener);
    }

    public GetLocationTask(Context context){
        mContext=context;
    }


    @Override
    protected CyLocation doInBackground(String... params) {
        CyLocation location=null;

        isInError=false;
        errorMessage="";

        String id="";
        if (params.length>0 && params[0]!=null && !params[0].equals("") )
            id=params[0];

        String url=mContext.getResources().getString(R.string.core_url)+"/rest/location/"+id+"/get";
        Log.i(this.getClass().getName(),"url="+url);


        SimpleHttpClient httpClient=new SimpleHttpClient(url);

        String language="";

        if (Locale.getDefault().getCountry().equalsIgnoreCase("IT"))
            language="it";

        try {
            String response=httpClient.getResult(language);
            //Log.i(this.getClass().getName(),"response="+response);
            JSONObject jsonResponse = new JSONObject(response);

            String resultCode=jsonResponse.getString("resultCode");

            //Log.i(LOG_TAG,"resultCode="+resultCode);
            if (!resultCode.equals("00"))
                throw new CyBssException("WsRest Result code <> '00'");

            JSONObject jsonLocation=jsonResponse.getJSONObject("location");
            location=getLocationfromJson(jsonLocation);

            url=mContext.getResources().getString(R.string.core_url)+"/rest/location/"+id+"/getFiles";
            Log.i(this.getClass().getName(),"url="+url);
            httpClient=new SimpleHttpClient(url);
            response=httpClient.getResult(language);
            Log.i(this.getClass().getName(),"response="+response);
            jsonResponse = new JSONObject(response);
            resultCode=jsonResponse.getString("resultCode");
            if (!resultCode.equals("00"))
                throw new CyBssException("WsRest Result code <> '00'");

            JSONArray jsonArray=jsonResponse.getJSONArray("files");
            for (int i=0;i<jsonArray.length();i++) {
                JSONObject jsonFile = jsonArray.getJSONObject(i);
                CyFile file=getFilefromJson(jsonFile);
                location.files.add(file);
            }



            } catch (CyBssException | JSONException e) {
            e.printStackTrace();

            e.printStackTrace();
            Log.e(this.getClass().getName(),e.getMessage());
            isInError=true;
            errorMessage=e.getMessage();
        }

        return location;
    }

    private CyFile getFilefromJson(JSONObject jsonFile)
            throws JSONException
    {
        CyFile file=new CyFile();
        file.id=jsonFile.getLong("id");
        file.name=jsonFile.getString("name");
        file.length=jsonFile.getLong("length");
        file.contentType=jsonFile.getString("contentType");
        file.fileType=jsonFile.getString("fileType");
        file.note=jsonFile.getString("note");
        file.entityId=jsonFile.getLong("entityId");

        return file;
    }


    private CyLocation getLocationfromJson(JSONObject jsonLoc)
            throws JSONException {
        CyLocation location = new CyLocation();

        location.id = jsonLoc.getLong("id");
        location.name = jsonLoc.getString("name");
        location.description = jsonLoc.getString("description");
        location.latitude = jsonLoc.getDouble("latitude");
        location.longitude = jsonLoc.getDouble("longitude");
        location.creationDate = jsonLoc.getString("creationDate");
        location.locationType = jsonLoc.getString("locationType");

        if (location.locationType.equals(FindLocationTask.STORY))
            location.name = location.description.length() > 30 ? location.description.substring(0, 30) + " [...]" : location.description;

        return location;
    }

    @Override
    protected void onPostExecute(CyLocation location) {
        super.onPostExecute(location);

        if (!isInError)
            for(GetLocationResultListener listener:listeners)
                listener.reiceveGetLocationResult(location);
        else
            for(GetLocationResultListener listener:listeners)
                listener.reiceveGetLocationError(errorMessage);
    }
}

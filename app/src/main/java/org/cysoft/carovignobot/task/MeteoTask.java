package org.cysoft.carovignobot.task;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.cysoft.carovignobot.common.CyBssException;
import org.cysoft.carovignobot.common.SimpleHttpClient;
import org.cysoft.carovignobot.model.CyLocation;
import org.cysoft.carovignobot.model.Meteo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by NS293854 on 07/06/2016.
 */
public class MeteoTask extends AsyncTask<CyLocation,Void,Meteo> {

    private final String LOG_TAG=getClass().getName();

    private Context mContext;
    private MeteoListener mListener;

    public MeteoTask(Context context,MeteoListener listener ){
        mContext=context;
        mListener=listener;
    }

    @Override
    protected Meteo doInBackground(CyLocation... params) {
        if (params.length<1) {
            Log.e(LOG_TAG, "Location not setted !");
            return null;
        }
        CyLocation location=params[0];

        String language="";
        if (Locale.getDefault().getCountry().equalsIgnoreCase("IT"))
            language="it";

        Meteo ret=null;

        String url="http://api.openweathermap.org/data/2.5/weather?lat="+location.latitude+"&lon="+location.longitude;
        url+="&appId=6110672a97c49db509d77831960ac7ea";
        if (!language.equals(""))
            url+="&lang=it";
        url+="&units=metric";

        SimpleHttpClient httpClient=new SimpleHttpClient(url);
        try {
            String response=httpClient.getResult();
            Log.i(this.getClass().getName(),"response="+response);
            JSONObject jsonResponse = new JSONObject(response);


            JSONArray jsonArray=jsonResponse.getJSONArray("weather");
            JSONObject item= (JSONObject) jsonArray.get(0);
            String description=item.getString("description");
            String icon=item.getString("icon");

            JSONObject main=jsonResponse.getJSONObject("main");
            double temp=main.optDouble("temp");

            JSONObject wind=jsonResponse.getJSONObject("wind");
            double speed=wind.optDouble("speed");

            ret=new Meteo();
            ret.description=description;
            ret.temperatura=temp;
            ret.icon=icon;
            ret.windSpeed=speed;

            if (Locale.getDefault().getCountry().equalsIgnoreCase("IT"))
                ret.formattedTemp= NumberFormat.getNumberInstance(Locale.ITALIAN).format(ret.temperatura)+" °C";
            else
                ret.formattedTemp= NumberFormat.getNumberInstance(Locale.US).format(ret.temperatura)+" °C";

            if (Locale.getDefault().getCountry().equalsIgnoreCase("IT"))
                ret.formattedWindSpeed= NumberFormat.getNumberInstance(Locale.ITALIAN).format(ret.windSpeed)+" m/s";
            else
                ret.formattedWindSpeed= NumberFormat.getNumberInstance(Locale.US).format(ret.windSpeed)+" m/s";


        } catch (CyBssException | JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
            return null;
        }

        return ret;
    }

    @Override
    protected void onPostExecute(Meteo meteo) {
        super.onPostExecute(meteo);
        if (mListener!=null && meteo!=null)
            mListener.reiceveMeteo(meteo);
    }
}

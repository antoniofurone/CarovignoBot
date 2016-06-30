package org.cysoft.carovignobot.model;

import android.os.AsyncTask;

/**
 * Created by NS293854 on 07/06/2016.
 */
public class Meteo {

    public String description;
    public double temperatura;
    public String formattedTemp;
    public double windSpeed;
    public String formattedWindSpeed;
    public String icon;

    @Override
    public String toString() {
        return "Meteo{" +
                "description='" + description + '\'' +
                ", temperatura=" + temperatura +
                ", icon='" + icon + '\'' +
                '}';
    }
}

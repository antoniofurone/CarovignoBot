package org.cysoft.carovignobot.model;

import android.location.Location;

import org.cysoft.carovignobot.common.CyBssConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NS293854 on 13/05/2016.
 */
public class CyLocation implements Comparable<CyLocation>{
    public long id;
    public String name;
    public String creationDate;
    public String description;
    public String locationType;
    public double latitude;
    public double longitude;
    public double distance;
    public String formattedDistance=" ?? ";


    public List<CyFile> files=new ArrayList<CyFile>();

    public void calcDistance(Location locationRif){
        Double latDistance = Math.toRadians(latitude - locationRif.getLatitude());
        Double lonDistance = Math.toRadians(longitude- locationRif.getLongitude());
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(locationRif.getLatitude())) * Math.cos(Math.toRadians(latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        distance = CyBssConstants.EARTH_RADIUS * c * 1000; // convert to meters
    }

    @Override
    public int compareTo(CyLocation another) {
        if (this.distance<another.distance)
            return -1;
        else
            return 1;
    }

    @Override
    public String toString() {
        return "CyLocation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", description='" + description + '\'' +
                ", locationType='" + locationType + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}

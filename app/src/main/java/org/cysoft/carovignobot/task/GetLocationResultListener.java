package org.cysoft.carovignobot.task;

import org.cysoft.carovignobot.model.CyLocation;

import java.util.List;

/**
 * Created by NS293854 on 13/05/2016.
 */
public interface GetLocationResultListener {
    public void reiceveGetLocationResult(CyLocation location);
    public void reiceveGetLocationError(String errorMessage);
}

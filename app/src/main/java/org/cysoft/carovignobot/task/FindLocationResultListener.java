package org.cysoft.carovignobot.task;

import org.cysoft.carovignobot.model.CyLocation;

import java.util.List;

/**
 * Created by NS293854 on 13/05/2016.
 */
public interface FindLocationResultListener {
    public void reiceveFindLocationResult(List<CyLocation> locations);
    public void reiceveFindLocationError(String errorMessage);
}

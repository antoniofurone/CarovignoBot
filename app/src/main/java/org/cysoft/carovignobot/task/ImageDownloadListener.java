package org.cysoft.carovignobot.task;

import android.graphics.Bitmap;

import org.cysoft.carovignobot.model.CyLocation;

import java.util.List;

/**
 * Created by NS293854 on 13/05/2016.
 */
public interface ImageDownloadListener {
    public void reiceveImageDownload(List<Bitmap> bitmapList);

}

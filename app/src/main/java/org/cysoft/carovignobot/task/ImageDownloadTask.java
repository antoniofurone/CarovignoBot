package org.cysoft.carovignobot.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.cysoft.carovignobot.R;
import org.cysoft.carovignobot.model.CyFile;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by NS293854 on 02/06/2016.
 */
public class ImageDownloadTask extends AsyncTask<Void,Void,List<Bitmap>> {
    private List<CyFile> mFiles=null;
    private ImageDownloadListener mListener=null;
    private Context mContext;

    private final String LOG_TAG=this.getClass().getName();

    public ImageDownloadTask(Context context,List<CyFile> files, ImageDownloadListener listener){
        mFiles=files;
        mListener=listener;
    }

    @Override
    protected List<Bitmap> doInBackground(Void... params) {

        List<Bitmap> bitmapList = new ArrayList<Bitmap>();

        for(CyFile file:mFiles){
            try {
                if (file.isPhoto())
                    bitmapList.add(urlImageToBitmap(mContext.getResources().getString(R.string.core_url)+
                            "/fileservice/file/"+file.id+"/download"));
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG,e.getMessage());
            }
        }

        return bitmapList;
    }

    private Bitmap urlImageToBitmap(String imageUrl) throws Exception {
        Bitmap result = null;
        Log.i(LOG_TAG,"urlImageToBitmap:"+imageUrl);
        URL url = new URL(imageUrl);
        if(url != null) {
            result = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        }
        return result;
    }


    @Override
    protected void onPostExecute(List<Bitmap> bitmapList) {
        super.onPostExecute(bitmapList);
        mListener.reiceveImageDownload(bitmapList);
    }
}

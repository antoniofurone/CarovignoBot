package org.cysoft.carovignobot.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.cysoft.carovignobot.R;
import org.cysoft.carovignobot.model.CyFile;
import org.cysoft.carovignobot.model.CyLocation;
import org.cysoft.carovignobot.task.FindLocationTask;
import org.cysoft.carovignobot.task.GetLocationResultListener;
import org.cysoft.carovignobot.task.GetLocationTask;

import java.util.ArrayList;
import java.util.List;

public class ImageViewActivity extends AppCompatActivity
implements GetLocationResultListener{

    private final String LOG_TAG=getClass().getName();

    private long locationId;
    private long selectedPhotoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        String title=getIntent().getStringExtra("title");
        if (title!=null && !title.equals(""))
            setTitle(title);

        locationId=getIntent().getLongExtra("locationId",0);
        selectedPhotoId=getIntent().getLongExtra("selectedPhotoId",0);
        //Log.i(LOG_TAG,"selectedPhotoId="+ selectedPhotoId);

    }

    @Override
    protected void onStart() {
        super.onStart();

        GetLocationTask task=new GetLocationTask(this);
        task.addGetLocationResultLister(this);

        task.executeOnExecutor(FindLocationTask.THREAD_POOL_EXECUTOR,new Long(locationId).toString());

    }

    @Override
    public void reiceveGetLocationResult(CyLocation location) {
        Log.e(LOG_TAG,"reiceveGetLocationResult");

        ViewPager pagerImages = (ViewPager) findViewById(R.id.pagerImages);

        if (pagerImages  != null) {
            List<CyFile> photos=new ArrayList<CyFile>();
            for(CyFile file:location.files)
                if (file.isPhoto())
                    photos.add(file);
            ImagePagerAdapter adapter=new ImagePagerAdapter(getBaseContext(), photos);
            pagerImages.setAdapter(adapter);

            int position=-1;
            for(int i=0;i<photos.size();i++){
                if (photos.get(i).id==selectedPhotoId){
                   position=i;
                   break;
                }
            }
            if (position!=-1)
               pagerImages.setCurrentItem(position);

        }
    }

    @Override
    public void reiceveGetLocationError(String errorMessage) {

        Log.e(LOG_TAG,"reiceveGetLocationError:"+errorMessage);

        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.error_title))
                .setMessage(getResources().getString(R.string.error_check_connection))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }
                )
                .show();

    }
}

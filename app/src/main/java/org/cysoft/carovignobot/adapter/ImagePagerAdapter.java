package org.cysoft.carovignobot.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.cysoft.carovignobot.R;

import com.squareup.picasso.Picasso;

import org.cysoft.carovignobot.model.CyFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NS293854 on 06/06/2016.
 */
public class ImagePagerAdapter extends PagerAdapter {

    private final String LOG_TAG=getClass().getName();


    private Context mContext=null;
    private List<CyFile> mPhotos=new ArrayList<CyFile>();

    public ImagePagerAdapter(Context context,List<CyFile> files){
        mContext=context;

        if (files!=null){
            for(CyFile file:files)
                if (file.isPhoto())
                    mPhotos.add(file);
        }

    }

    @Override
    public int getCount() {
        return mPhotos.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);


        ImageView imageView = new ImageView(mContext);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(imageParams);

        String url= mContext.getResources().getString(R.string.core_url)+
                "/fileservice/file/"+ mPhotos.get(position).id+"/download";

        //Log.i(LOG_TAG,"url="+url);

        Picasso.with(mContext)
                .load(url)
                //.resize(400,400)
                //.centerCrop()
                .into(imageView);

        layout.addView(imageView);

        AnimatorSet set=new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(imageView,"alpha",0f,0.25f,0.5f,0.75f,1f).setDuration(1000),
        ObjectAnimator.ofFloat(imageView,"rotationY",0,360).setDuration(1000));

        set.start();

        container.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout)object);

    }

}

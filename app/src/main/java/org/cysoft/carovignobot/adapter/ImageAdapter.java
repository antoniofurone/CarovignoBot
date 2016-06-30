package org.cysoft.carovignobot.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.cysoft.carovignobot.R;
import org.cysoft.carovignobot.WebMapActivity;
import org.cysoft.carovignobot.model.CyFile;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.BlurMaskFilter.*;

/**
 * Created by NS293854 on 02/06/2016.
 */
public class ImageAdapter extends BaseAdapter {

    private final String LOG_TAG=getClass().getName();

    private Context context;
    private List<CyFile> mPhotos;
    private String mTitle;

    public ImageAdapter(Context context, String title, List<CyFile> files) {
        this.context = context;
        this.mPhotos = new ArrayList<CyFile>();
        this.mTitle=title;

        if (files!=null){
            for(CyFile file:files)
                if (file.isPhoto())
                    mPhotos.add(file);
        }

    }

    public int getCount() {
        return this.mPhotos.size();
    }

    public Object getItem(int position) {
        return mPhotos.get(position);
    }

    public long getItemId(int position) {
        return mPhotos.get(position).id;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        final float scale = context.getResources().getDisplayMetrics().density;

        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(this.context);
            imageView.setLayoutParams(new GridView.LayoutParams((int)(120*scale), (int)(120*scale)));
            //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        String url=context.getResources().getString(R.string.core_url)+
                "/fileservice/file/"+ mPhotos.get(position).id+"/download";
        //Log.i(LOG_TAG,"url="+url);

        Picasso.with(context)
                .load(url)
                .resize(400,400)
                .centerCrop()
                //.transform(new CircleTransform())
                .into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context,ImageViewActivity.class);

                intent.putExtra("title",mTitle);
                intent.putExtra("locationId",mPhotos.get(position).entityId);
                intent.putExtra("selectedPhotoId",mPhotos.get(position).id);

                context.startActivity(intent);
            }
        });

        AnimatorSet set=new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(imageView,"alpha",0f,0.25f,0.5f,0.75f,1f).setDuration(1500),
                ObjectAnimator.ofFloat(imageView,"rotationY",0,360).setDuration(1500));

        set.start();

        return imageView;
    }

    class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }




}

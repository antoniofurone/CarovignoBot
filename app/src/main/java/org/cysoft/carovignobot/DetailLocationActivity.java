package org.cysoft.carovignobot;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import org.cysoft.carovignobot.model.CyFile;
import org.cysoft.carovignobot.model.CyLocation;

public class DetailLocationActivity extends AppCompatActivity {
    private final String LOG_TAG = this.getClass().getName();

    private DetailLocationFragment detailFragment=null;


    // Facebook
    CallbackManager callbackManager;
    ShareDialog shareDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_location);

        //facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {

            @Override
            public void onSuccess(Sharer.Result result) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }
        });
        // end facebook

        String locationId= getIntent().getExtras().getString("locationId");
        Log.i(LOG_TAG,"locationId="+locationId);

        detailFragment=(DetailLocationFragment)getSupportFragmentManager().findFragmentById(R.id.detailFrament);
        detailFragment.setLocationId(locationId);

        String title= (String)getIntent().getExtras().get("title");
        setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_item_share && detailFragment!=null && detailFragment.location!=null){
            Log.i(LOG_TAG, "onItemShare");

            Intent shareIntent=new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String uri="http://maps.google.com/maps?&q="+detailFragment.location.latitude+","+detailFragment.location.longitude;

            shareIntent.putExtra(Intent.EXTRA_SUBJECT,detailFragment.location.name+" "+getResources().getString(R.string.share_suffix_label));
            String bodyMessage=uri+" - "+
                    detailFragment.location.name+" "+getResources().getString(R.string.share_suffix_label)+
                    " - "+detailFragment.location.description;

            if (detailFragment.location.files!=null){
                for (CyFile file:detailFragment.location.files)
                    bodyMessage+=" - "+getString(R.string.core_url) +
                            "/fileservice/file/" + file.id + "/download";
            }

            shareIntent.putExtra(Intent.EXTRA_TEXT,bodyMessage);
            startActivity(Intent.createChooser(shareIntent,getResources().getString(R.string.share_button_label)));

            return true;
        }

        if (id == R.id.menu_item_facebook && detailFragment!=null && detailFragment.location!=null) {
            Log.i(LOG_TAG, "onItemShare");

            int indexPhoto=-1;
            if (detailFragment.location.files!=null && !detailFragment.location.files.isEmpty()){
                for(int i=0;i<detailFragment.location.files.size();i++)
                    if (detailFragment.location.files.get(i).isPhoto()){
                        indexPhoto=i;
                        break;
                    }
            }

            if (ShareDialog.canShow(ShareLinkContent.class)) {

                String description=detailFragment.location.name +
                        " " + getResources().getString(R.string.share_suffix_label) +
                        " - " + detailFragment.location.description;

                ShareLinkContent.Builder builder=new ShareLinkContent.Builder();
                builder.setContentTitle(detailFragment.location.name)
                        .setContentDescription(description)
                        .setContentUrl(Uri.parse("http://maps.google.com/maps?&q=" + detailFragment.location.latitude + "," + detailFragment.location.longitude))
                        .setQuote(description)
                        .setShareHashtag(new ShareHashtag.Builder()
                                .setHashtag("#CarovignoBot")
                                .build());


                if (indexPhoto>=0)
                    builder.setImageUrl(Uri.parse(getBaseContext().getResources().getString(R.string.core_url)+
                            "/fileservice/file/"+ detailFragment.location.files.get(indexPhoto).id+"/download"));

                ShareLinkContent linkContent = builder.build();
                shareDialog.show(linkContent);
                }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

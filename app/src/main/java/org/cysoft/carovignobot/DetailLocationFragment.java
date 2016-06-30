package org.cysoft.carovignobot;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.cysoft.carovignobot.adapter.BitmapImageAdapter;
import org.cysoft.carovignobot.adapter.ImageAdapter;
import org.cysoft.carovignobot.model.CyFile;
import org.cysoft.carovignobot.model.CyLocation;
import org.cysoft.carovignobot.model.Meteo;
import org.cysoft.carovignobot.task.FindLocationTask;
import org.cysoft.carovignobot.task.GetLocationResultListener;
import org.cysoft.carovignobot.task.GetLocationTask;
import org.cysoft.carovignobot.task.ImageDownloadListener;
import org.cysoft.carovignobot.task.MeteoListener;
import org.cysoft.carovignobot.task.MeteoTask;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailLocationFragment extends Fragment
    implements GetLocationResultListener,ImageDownloadListener,MeteoListener
{

    private final String LOG_TAG=this.getClass().getName();

    private String mLocationId;

    public CyLocation location=null;

    public DetailLocationFragment() {
        // Required empty public constructor
    }

    public void setLocationId(String locationId){
        mLocationId=locationId;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_detail_location, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        GetLocationTask task=new GetLocationTask(getContext());
        task.addGetLocationResultLister(this);
        task.executeOnExecutor(FindLocationTask.THREAD_POOL_EXECUTOR,mLocationId);

    }

    @Override
    public void reiceveGetLocationResult(final CyLocation pLocation) {
        Log.i(LOG_TAG,"reiceveGetLocationResult");
        this.location=pLocation;

        MeteoTask task=new MeteoTask(getContext(),this);
        task.executeOnExecutor(FindLocationTask.THREAD_POOL_EXECUTOR,location);

        if (getActivity()!=null) {
            TextView txtName = (TextView) getActivity().findViewById(R.id.txtName);
            if (txtName != null)
                txtName.setText(location.name);
        }

        if (getActivity()!=null) {
            ImageButton btnMap = (ImageButton) getActivity().findViewById(R.id.buttonMaps);
            if (btnMap!=null)
                btnMap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri gmmIntentUri = Uri.parse("geo:" + location.latitude + "," + location.longitude + "?q=" +
                                +location.latitude + "," + location.longitude + "(" + location.name + ")");

                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null)
                            startActivity(mapIntent);
                    }
                });
        }

        if (getActivity()!=null) {
            TextView txtDescription = (TextView) getActivity().findViewById(R.id.txtDescription);
            if (txtDescription != null)
                txtDescription.setText(location.description);
        }

            //ImageDownloadTask downloadTask=new ImageDownloadTask(getString(R.string.core_url),location.files,this);
            //downloadTask.executeOnExecutor(FindLocationTask.THREAD_POOL_EXECUTOR);

        if (getActivity()!=null) {
            GridView imageGrid = (GridView) getActivity().findViewById(R.id.gridImages);
            if (imageGrid != null)
                imageGrid.setAdapter(new ImageAdapter(getContext(), getActivity().getTitle().toString(), location.files));
        }

        if (getActivity()!=null) {
            TextView txtCreationDate = (TextView) getActivity().findViewById(R.id.txtCreationDate);
            if (txtCreationDate != null)
                if (location.locationType.equals(FindLocationTask.STORY))
                    txtCreationDate.setText(" " + location.creationDate + " ");
                else
                    txtCreationDate.setVisibility(View.INVISIBLE);
        }

        if (getActivity()!=null){
            GridLayout gridLayoutFiles = (GridLayout) getActivity().findViewById(R.id.gridLayoutFiles);
            if (gridLayoutFiles != null) {
                int row = 1;
                for (final CyFile file : location.files) {
                    if (!file.isPhoto()) {
                        ImageButton imageButton = new ImageButton(this.getContext());
                        imageButton.setImageResource(android.R.drawable.ic_menu_view);
                        //imageButton.setBackgroundColor(Color.TRANSPARENT);
                        imageButton.setBackgroundResource(R.color.buttonBackgroundColor);

                        imageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String url = getString(R.string.core_url) +
                                        "/fileservice/file/" + file.id + "/download";
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(url));
                                startActivity(intent);
                            }
                        });


                        GridLayout.Spec rowSpec = GridLayout.spec(row, 1);
                        GridLayout.Spec colspanSpec = GridLayout.spec(1, 1);
                        GridLayout.LayoutParams gridLayoutParam = new GridLayout.LayoutParams(rowSpec, colspanSpec);
                        gridLayoutFiles.addView(imageButton, gridLayoutParam);

                        colspanSpec = GridLayout.spec(2, 1);
                        gridLayoutParam = new GridLayout.LayoutParams(rowSpec, colspanSpec);

                        TextView textView = new TextView(this.getContext());
                        if (file.note != null && !file.note.equals(""))
                            textView.setText(file.note);
                        else if (file.fileType != null && !file.fileType.equals(""))
                            textView.setText(file.fileType);
                        else
                            textView.setText(file.contentType);
                        gridLayoutFiles.addView(textView, gridLayoutParam);

                        row++;
                    }

                } // end for files
            }
        } // end if Avtivity
    }

    @Override
    public void reiceveGetLocationError(String errorMessage) {
        Log.e(LOG_TAG,"reiceveGetLocationError:"+errorMessage);

        new AlertDialog.Builder(getContext())
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



    @Override
    public void reiceveImageDownload(List<Bitmap> bitmapList) {
        if (getActivity() != null) {
            GridView imageGrid = (GridView) getActivity().findViewById(R.id.gridImages);
            if (imageGrid != null)
                if (bitmapList.size()>0) {
                    imageGrid.setAdapter(new BitmapImageAdapter(getContext(), bitmapList));
                    imageGrid.setVisibility(View.VISIBLE);
                }
                else {
                    imageGrid.setVisibility(View.INVISIBLE);
                }
        }
    }

    @Override
    public void reiceveMeteo(Meteo meteo) {
        Log.e(LOG_TAG,"reiceveMeteo:"+meteo);


            if (getActivity()!=null) {
                ImageView meteoIcon = (ImageView) getActivity().findViewById(R.id.meteoIcon);
                if (meteoIcon != null)
                    Picasso.with(getContext())
                            .load("http://openweathermap.org/img/w/" + meteo.icon + ".png")
                            .resize(100, 100)
                            .centerCrop()
                            .into(meteoIcon);
            }

            if (getActivity()!=null) {
                TextView tempView = (TextView) getActivity().findViewById(R.id.meteoTemp);
                if (tempView != null)
                    tempView.setText(meteo.formattedTemp);
            }

            if (getActivity()!=null) {
                TextView descrView = (TextView) getActivity().findViewById(R.id.meteoDescr);
                if (descrView != null)
                    descrView.setText(meteo.description);
            }

            if (getActivity()!=null) {
                TextView windSpeed = (TextView) getActivity().findViewById(R.id.meteoWindSpeed);
                if (windSpeed != null)
                    windSpeed.setText(meteo.formattedWindSpeed);
            }
        }
}

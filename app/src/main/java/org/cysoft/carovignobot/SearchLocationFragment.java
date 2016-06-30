package org.cysoft.carovignobot;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import org.cysoft.carovignobot.model.CyLocation;
import org.cysoft.carovignobot.task.FindLocationResultListener;
import org.cysoft.carovignobot.task.FindLocationTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchLocationFragment extends Fragment
        implements FindLocationResultListener

{

    private ListView mListView=null;
    private TextView mTextPosition=null;
    private Location mCurrentLocation=null;
    private EditText mSearchText=null;
    private ImageButton mImageButton=null;


    private String mLocationType=FindLocationTask.TOURIST_SITES;
    private boolean mAutomaticRefresh=true;

    private final String LOG_TAG=this.getClass().getName();

    public SearchLocationFragment() {
        // Required empty public constructor
    }

    public void setLocationType(String locationType){
        if (locationType.equals(getResources().getString(R.string.loc_event_name)))
            mLocationType=FindLocationTask.EVENT;
        else
            if (locationType.equals(getResources().getString(R.string.loc_story_name)))
                mLocationType=FindLocationTask.STORY;
            else
                mLocationType=FindLocationTask.TOURIST_SITES;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_search_location, container, false);

        mListView=(ListView)view.findViewById(R.id.listLocation);
        mTextPosition=(TextView)view.findViewById(R.id.textPosition);
        mSearchText=(EditText) view.findViewById(R.id.searchText);
        mImageButton=(ImageButton) view.findViewById(R.id.searchButton);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAutomaticRefresh=true;
                loadLocations();
            }
        });


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (parent.getAdapter()!=null) {
                    Map<String, String> locMap = (Map<String, String>) parent.getAdapter().getItem(position);

                    Intent intent = new Intent(getActivity(), DetailLocationActivity.class);
                    intent.putExtra("locationId", locMap.get("id"));
                    intent.putExtra("title",getActivity().getTitle());
                    startActivity(intent);
                }
            }
        });

        return view;
    }



    @Override
    public void onStart() {
        super.onStart();
        loadLocations();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void loadLocations(){

        FindLocationTask task=new FindLocationTask(getContext(),mLocationType,mCurrentLocation);
        task.addFindLocationResultLister(this);

        task.executeOnExecutor(FindLocationTask.THREAD_POOL_EXECUTOR,mSearchText!=null?mSearchText.getText().toString():"");
    }

    @Override
    public void reiceveFindLocationResult(List<CyLocation> locations) {

        List<Map<String,String>> locsMap=new ArrayList<>();

        if (locations!=null) {
            for (int i = 0; i < locations.size(); i++) {
                Map<String, String> loc = new HashMap<>();
                loc.put("id",new Long(locations.get(i).id).toString());
                loc.put("name", locations.get(i).name);
                loc.put("description", locations.get(i).description);
                loc.put("distance", locations.get(i).formattedDistance);
                locsMap.add(loc);
            }


            /*
            SimpleAdapter locsAdapter = new SimpleAdapter(getActivity(), locsMap, R.id.simple_list_item_2,
                    new String[]{"name", "description"},
                    new int[]{android.R.id.text1, android.R.id.text2}
            );
            */

            if (getContext()!=null) {
                SimpleAdapter locsAdapter = new SimpleAdapter(getContext(), locsMap, R.layout.search_result_list,
                        new String[]{"name", "distance", "description"},
                        new int[]{R.id.srName, R.id.srDistance, R.id.srDescription}
                );

                if (mListView != null) {
                    mListView.setAdapter(locsAdapter);
                    Toast.makeText(getContext(), getResources().getString(R.string.toast_update_list), Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    @Override
    public void reiceveFindLocationError(String errorMessage) {
        Log.e(LOG_TAG,"reiceveFindLocationError:"+errorMessage);
        mAutomaticRefresh=false;
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

    public void setTextLocation(String textLocation){
        mTextPosition.setText(textLocation);
    }

    public void setCurrentLocation(Location location){
        mCurrentLocation=location;
        if (mAutomaticRefresh)
            loadLocations();
    }

}

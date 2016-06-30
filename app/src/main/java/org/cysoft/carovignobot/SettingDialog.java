package org.cysoft.carovignobot;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.cysoft.carovignobot.common.CyBssConstants;

/**
 * Created by NS293854 on 17/06/2016.
 */
public class SettingDialog extends DialogFragment {

    public static SettingDialog newInstance (){
        SettingDialog dialog=new SettingDialog();
        return dialog;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View dialogView= LayoutInflater.from(getContext()).inflate(R.layout.dialog_setting,null);

        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());

        builder.setView(dialogView);
        builder.setTitle(getResources().getString(R.string.setting_dialog_name));

        SharedPreferences sharedPref = getContext().getSharedPreferences(
                getString(R.string.app_key_store), Context.MODE_PRIVATE);

        Integer refreshInterval = sharedPref.getInt(getString(R.string.refresh_interval), CyBssConstants.DEFAULT_REFRESH_INTERVAL);
        final EditText txtInterval=(EditText) dialogView.findViewById(R.id.txtInterval);
        txtInterval.setText(refreshInterval.toString());


        builder.setPositiveButton(getResources().getString(R.string.btn_label_save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.app_key_store),
                        Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(getString(R.string.refresh_interval), new Integer(txtInterval.getText().toString()).intValue());
                editor.commit();
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.btn_label_close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });


        return builder.create();
    }


}

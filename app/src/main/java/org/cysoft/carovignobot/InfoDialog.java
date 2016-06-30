package org.cysoft.carovignobot;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by NS293854 on 17/06/2016.
 */
public class InfoDialog extends DialogFragment {

    public static InfoDialog newInstance (){
        InfoDialog dialog=new InfoDialog();
        return dialog;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View dialogView= LayoutInflater.from(getContext()).inflate(R.layout.dialog_info,null);

        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());

        builder.setView(dialogView);
        builder.setTitle(getResources().getString(R.string.info_dialog_name));

        builder.setPositiveButton(getResources().getString(R.string.btn_label_close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();
    }
}

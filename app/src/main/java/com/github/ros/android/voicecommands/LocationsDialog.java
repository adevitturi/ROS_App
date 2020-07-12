package com.github.ros.android.voicecommands;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

/** Creates dialogs for editing and adding new locations.  */
public class LocationsDialog {
    private Context context;
    private List<OnDialogEventListener> listeners = new ArrayList<>();

    /** Registers listener to the dialog events. */
    public void registerListener(OnDialogEventListener listener) {
        if(listeners.contains(listener)){
            return;
        }
        listeners.add(listener);
    }

    /** Unregisters listeners. */
    public void unregisterListener(OnDialogEventListener listener) {
        listeners.remove(listener);
    }

    public LocationsDialog(Context context) {
        this.context = context;
    }

    /** Creates an empty dialog to create a new location. */
    public Dialog createEmptyDialog(){
        LayoutInflater inflater = LayoutInflater.from(context);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Locations").setMessage("Add a new location");
        View view = inflater.inflate(R.layout.location_dialog, null);
        alertDialog.setView(view)
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String[] params = new String[4];
                        params[0] = ((EditText)view.findViewById(R.id.name)).getText().toString();
                        params[1] = ((EditText)view.findViewById(R.id.xvalue)).getText().toString();
                        params[2] = ((EditText)view.findViewById(R.id.yvalue)).getText().toString();
                        params[3] = ((EditText)view.findViewById(R.id.zvalue)).getText().toString();

                        if(params[0].isEmpty()){
                            Toast.makeText(context, "Name can't be empty.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        for(int i=1;i<4;i++){
                            if(params[i].isEmpty()) {
                                params[i]="0";
                            }
                        }

                        for(OnDialogEventListener listener:listeners) {
                            listener.onCreate(params);
                        }
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return alertDialog.create();
    }

    /** Creates a fileld dialog to edit a location's values. */
    public Dialog createFilledDialog(String[] params){
        LayoutInflater inflater = LayoutInflater.from(context);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Locations").setMessage("Edit location " + params[0] +":");
        View view = inflater.inflate(R.layout.location_dialog_edit, null);
        ((EditText)view.findViewById(R.id.xvalue)).setText(params[1]);
        ((EditText)view.findViewById(R.id.yvalue)).setText(params[2]);
        ((EditText)view.findViewById(R.id.zvalue)).setText(params[3]);
        alertDialog.setView(view)
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String[] newParams = new String[4];
                        newParams[0] = params[0];
                        newParams[1] = ((EditText)view.findViewById(R.id.xvalue)).getText().toString();
                        newParams[2] = ((EditText)view.findViewById(R.id.yvalue)).getText().toString();
                        newParams[3] = ((EditText)view.findViewById(R.id.zvalue)).getText().toString();
                          for(int i=1;i<4;i++){
                            if(newParams[i].isEmpty()) {
                                newParams[i]="0";
                            }
                        }
                        for(OnDialogEventListener listener:listeners) {
                            listener.onEdit(newParams);
                        }
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                AlertDialog.Builder deteleDialog = new AlertDialog.Builder(context);
                deteleDialog.setTitle("Delete").setMessage("Delete location?")
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                for(OnDialogEventListener listener:listeners) {
                                    listener.onDelete(params[0]);
                                }
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).create().show();
                dialog.cancel();
            }
        });
        return alertDialog.create();
    }
}

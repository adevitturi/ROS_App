package com.github.ros.android.helloros;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.LocationViewHolder> implements View.OnClickListener {
    private Context context;
    private SharedPreferences preferences;
    private LocationsDialog locationsDialog;
    private final List<String> dataset = new ArrayList<>();

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private TextView title;
        private TextView xvalue;
        private TextView yvalue;
        private TextView zvalue;

        public LocationViewHolder(View v) {
            super(v);
            this.title = v.findViewById(R.id.location);
            this.xvalue = v.findViewById(R.id.x_value);
            this.yvalue = v.findViewById(R.id.y_value);
            this.zvalue = v.findViewById(R.id.z_value);
        }

        public void setTitle (String title) {
           this.title.setText(title);
        }

        public void setValues (String x, String y, String z) {
            this.xvalue.setText(x);
            this.yvalue.setText(y);
            this.zvalue.setText(z);
        }

        public void setLocation(TargetLocation location) {
            setTitle(location.getName());
            setValues(location.getX(), location.getY(), location.getZ());
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public LocationsAdapter(LocationsDialog locationsDialog, Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.locationsDialog = locationsDialog;
        updateDataSet();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public LocationsAdapter.LocationViewHolder onCreateViewHolder(ViewGroup parent,
                                                                  int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.location, parent, false);
        v.setOnClickListener(this);
        LocationViewHolder vh = new LocationViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(LocationViewHolder holder, int position) {
        String key = dataset.get(position);
        String bundle = preferences.getString(key, "");
        TargetLocation location = new TargetLocation(bundle);
        holder.setLocation(location);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataset.size();
    }

    @Override
    public void onClick(View view) {
        String[] params = new String[4];
        params[0] = ((TextView)view.findViewById(R.id.location)).getText().toString();
        params[1] = ((TextView)view.findViewById(R.id.x_value)).getText().toString();
        params[2] = ((TextView)view.findViewById(R.id.y_value)).getText().toString();
        params[3] = ((TextView)view.findViewById(R.id.z_value)).getText().toString();
        locationsDialog.createFilledDialog(params).show();
    }

    public void updateDataSet(){
        Set<String> keyset = preferences.getStringSet(context.getString(R.string.app_key_set),new LinkedHashSet<>());
        dataset.clear();
        dataset.addAll(keyset);
    }

}
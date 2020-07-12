package com.github.ros.android.voicecommands;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import org.ros.android.MessageCallable;
import org.ros.android.view.RosTextView;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import std_msgs.Float32;

/** Fragment to send voice commands and visualize the remaining distance to a goal. */
public class CommandFragment extends Fragment implements View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private ImageView recordingButton;
    private RosTextView<std_msgs.Float32> rosTextView;
    private SharedPreferences preferences;
    private NodeConfiguration nodeConfiguration;
    private NodeMainExecutor nodeMainExecutor;
    private boolean initialized = false;
    private String distanceTopic;
    private String namespace;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Gets default shared preferences.
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        View view = inflater.inflate(R.layout.command_fragment, container, false);
        recordingButton = view.findViewById(R.id.recording_button);
        recordingButton.setOnClickListener(this);

        // Gets distance topic.
        distanceTopic = preferences.getString(getString(R.string.topic_key),
                MainActivity.DEFAULT_GOAL_TOPIC) + "_distance";
        // Gets namespace.
        namespace = preferences.getString(getString(R.string.namespace_key),
                MainActivity.DEFAULT_NAMESPACE);
        rosTextView = (RosTextView<Float32>) view.findViewById(R.id.text);

        ((MainActivity)getActivity()).getNodeMainExec().observe(this, (mainExec) -> {
            this.nodeMainExecutor = mainExec;
            if(nodeMainExecutor != null && nodeConfiguration!=null) {
                setUpRosTextView();
            }
        });
        ((MainActivity)getActivity()).getNodeConfig().observe(this, (nodeConfig) -> {
            this.nodeConfiguration = nodeConfig;
            if(nodeMainExecutor != null && nodeConfiguration!=null) {
                setUpRosTextView();
            }
        });
        return view;
    }

    /** Sets up the textview that subscribes to the goal remaining distance. */
    public void setUpRosTextView() {
        if(!initialized){
            rosTextView.setTopicName(namespace+"/"+distanceTopic);
            rosTextView.setMessageType(std_msgs.Float32._TYPE);
            rosTextView.setMessageToStringCallable(new MessageCallable<String, Float32>() {
                @Override
                public String call(std_msgs.Float32 message) {
                    try{
                        return getActivity().getString(R.string.distance_str,message.getData());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "Error.";
                    }
                }
            });
            nodeMainExecutor.execute(rosTextView, nodeConfiguration);
            initialized = true;
        }
    }

    /** Starts the voice command recording. */
    @Override
    public void onClick(View view) {
        ((MainActivity) getActivity()).startRecording();
    }

    /** Reinitializes the text view to display the distance if the topic or namespace change. */
    private void reInitRosTextView(){
        if(!initialized) {
            return;
        }
        String newTopic = preferences.getString(getString(R.string.topic_key),
                MainActivity.DEFAULT_GOAL_TOPIC) + "_distance";
        String newNamespace = preferences.getString(getString(R.string.namespace_key),
                MainActivity.DEFAULT_NAMESPACE);

        if (newTopic.equals(distanceTopic) && newNamespace.equals(namespace)) {
            return;
        }
        distanceTopic = newTopic;
        namespace = newNamespace;
        nodeMainExecutor.shutdownNodeMain(rosTextView);
        initialized = false;
        setUpRosTextView();
    }

    @Override
    public void onResume() {
        super.onResume();
        preferences.registerOnSharedPreferenceChangeListener(this);
        reInitRosTextView();
    }

    @Override
    public void onPause(){
        super.onPause();
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        if(nodeMainExecutor != null && nodeConfiguration!=null) {
            nodeMainExecutor.shutdownNodeMain(rosTextView);
        }
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals(getString(R.string.topic_key)) || s.equals(getString(R.string.namespace_key))){
            reInitRosTextView();
        }
    }
}

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

public class CommandFragment extends Fragment implements View.OnClickListener {
    private ImageView recordingButton;
    private RosTextView<std_msgs.Float32> rosTextView;
    private SharedPreferences preferences;
    private NodeConfiguration nodeConfiguration;
    private NodeMainExecutor nodeMainExecutor;
    private boolean initialized = false;
    private String distanceTopic;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        View view = inflater.inflate(R.layout.command_fragment, container, false);
        recordingButton = view.findViewById(R.id.recording_button);
        recordingButton.setOnClickListener(this);

        distanceTopic = preferences.getString(getString(R.string.topic_key),
                MainActivity.DEFAULT_GOAL_TOPIC) + "_distance";
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

    public void setUpRosTextView() {
        if(!initialized){
            rosTextView.setTopicName(distanceTopic);
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

    @Override
    public void onClick(View view) {
        ((MainActivity) getActivity()).startRecording();
    }

    private void reInitRosTextView(){
        if(!initialized) {
            return;
        }
        String newTopic = preferences.getString(getString(R.string.topic_key),
                MainActivity.DEFAULT_GOAL_TOPIC) + "_distance";
        if (!newTopic.equals(distanceTopic)) {
            distanceTopic = newTopic;
            nodeMainExecutor.shutdownNodeMain(rosTextView);
            initialized = false;
            setUpRosTextView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        reInitRosTextView();
    }

    @Override
    public void onDestroy() {
        if(nodeMainExecutor != null && nodeConfiguration!=null) {
            nodeMainExecutor.shutdownNodeMain(rosTextView);
        }
        super.onDestroy();
    }
}

package com.github.ros.android.voicecommands;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.util.List;
import java.util.Locale;

/** Activity for sending voice commands that communicate with ROS. */
public class MainActivity extends RosAppCompat implements SharedPreferences.OnSharedPreferenceChangeListener{
  private static final int SPEECH_CODE = 73;
  public static final String DEFAULT_GOAL_TOPIC = "goal_assistant";
  public static final String DEFAULT_NAMESPACE = "create1";

  private StopPublisher stopPublisher;
  private GoalPublisher goalPublisher;
  private ResultListener resultListener;
  private SharedPreferences preferences;
  private ViewPager viewPager;
  private boolean isPublisherRunning = false;
  private SpeechValidator speechValidator;
  private MutableLiveData<NodeMainExecutor> nodeMainExecutorMutableLiveData = new MutableLiveData<>();
  private MutableLiveData<NodeConfiguration> nodeConfigurationMutableLiveData= new MutableLiveData<>();

  public MainActivity() {
    super("Android ROS Assistant", "ROS Assistant");
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    SlidePageAdapter slidePageAdapter;
    slidePageAdapter = new SlidePageAdapter(getSupportFragmentManager());
    viewPager = findViewById(R.id.pager);
    viewPager.setAdapter(slidePageAdapter);

    preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    preferences.registerOnSharedPreferenceChangeListener(this);

    // Set topic and namespace default to prefs.
    if (!preferences.contains(getString(R.string.topic_key))){
      preferences.edit().putString(getString(R.string.topic_key), DEFAULT_GOAL_TOPIC);
    }
    if (!preferences.contains(getString(R.string.namespace_key))){
      preferences.edit().putString(getString(R.string.namespace_key), DEFAULT_NAMESPACE);
    }
    speechValidator = new SpeechValidator(this);
  }

  /** Sets up ROS params, publishers and subscribers. */
  @Override
  protected void init(NodeMainExecutor nodeMainExecutor) {
    NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
    nodeConfiguration.setMasterUri(getMasterUri());
    Handler mainHandler = new Handler(getMainLooper());
    mainHandler.post(()->{
      this.nodeMainExecutorMutableLiveData.setValue(nodeMainExecutor);
      this.nodeConfigurationMutableLiveData.setValue(nodeConfiguration);
      setUpPublishers();
      setUpSubscriber();
    });
  }

  /** Starts the speech recognition API. */
  public void startRecording() {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);

    if(intent.resolveActivity(getPackageManager())!=null) {
      startActivityForResult(intent, SPEECH_CODE);
    } else {
      Toast.makeText(this, "Speech recognition not supported on this device.", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if (requestCode == MASTER_CHOOSER_REQUEST_CODE) {
      super.onActivityResult(requestCode, resultCode, data);
      return;
    }
    /** Gets the payload from the speech recognition API. */
    if(requestCode == SPEECH_CODE) {
      if(resultCode == RESULT_OK && data != null) {
        List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        String speechPayload = results.get(0);
        if (speechValidator.hasStopCommand(speechPayload)) {
          stopPublisher.publishMessage();
        } else {
          try {
            TargetLocation payloadLocation = speechValidator.validateAndGetLocation(speechPayload);
            double x = Double.parseDouble(payloadLocation.getX());
            double y = Double.parseDouble(payloadLocation.getY());
            double z = Double.parseDouble(payloadLocation.getZ());
            goalPublisher.publishMessage(x, y, z);
            Toast.makeText(this, "Moving to location: " + payloadLocation.getName()
                    + "...", Toast.LENGTH_SHORT);
          } catch (Exception e) {
            Toast.makeText(this, "Invalid voice command.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
          }
        }
      }
    }
  }

  /** Creates Options Menu. */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main_menu, menu);
    return true;
  }

  /** Action upon pressing the settings button on the Options Menu. */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.settings) {
      navToTab(SlidePageAdapter.SETTINGS_PAGE);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /** Navigates to a specific ViewPager Tab. */
  public void navToTab(int index) {
    viewPager.setCurrentItem(index);
  }

  /** Sets up and executes ROS publishers nodes. */
  private void setUpPublishers() {
    String topic = preferences.getString(getString(R.string.topic_key), DEFAULT_GOAL_TOPIC);
    String namespace = preferences.getString(getString(R.string.namespace_key), DEFAULT_NAMESPACE);
    goalPublisher = new GoalPublisher(namespace+"/"+topic);
    stopPublisher = new StopPublisher(namespace+"/"+topic + "_cancel");
    nodeMainExecutorMutableLiveData.getValue().execute(goalPublisher,
            nodeConfigurationMutableLiveData.getValue());
    nodeMainExecutorMutableLiveData.getValue().execute(stopPublisher,
            nodeConfigurationMutableLiveData.getValue());
    isPublisherRunning = true;
  }

  /** Sets up and executes ROS subscriber node. */
  private void setUpSubscriber() {
    String topic = preferences.getString(getString(R.string.topic_key), DEFAULT_GOAL_TOPIC) + "_result";
    String namespace = preferences.getString(getString(R.string.namespace_key), DEFAULT_NAMESPACE);
    resultListener = new ResultListener(namespace+"/"+topic, this);
    nodeMainExecutorMutableLiveData.getValue().execute(resultListener,
            nodeConfigurationMutableLiveData.getValue());
  }

  /** Gets the main executor. */
  public LiveData<NodeMainExecutor> getNodeMainExec() {
    return nodeMainExecutorMutableLiveData;
  }

  /** Gets the node configuration. */
  public LiveData<NodeConfiguration> getNodeConfig() {
    return nodeConfigurationMutableLiveData;
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
    if((s.equals(getString(R.string.topic_key)) || s.equals(getString(R.string.namespace_key)))
            && isPublisherRunning){
      nodeMainExecutorMutableLiveData.getValue().shutdownNodeMain(goalPublisher);
      nodeMainExecutorMutableLiveData.getValue().shutdownNodeMain(stopPublisher);
      setUpPublishers();
      setUpSubscriber();
    }
  }
}

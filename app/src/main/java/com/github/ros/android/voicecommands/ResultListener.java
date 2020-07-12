package com.github.ros.android.voicecommands;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

/** Node that subscribes to goal results and displays its payload. */
public class ResultListener extends AbstractNodeMain {
  private final String topic;
  private final Context context;
  private final Handler mainHandler;

  public ResultListener(String topic, Context context) {
    this.topic = topic;
    this.context = context;
    mainHandler = new Handler(context.getMainLooper());
  }

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("android_assistant/result_listener");
  }

  @Override
  public void onStart(ConnectedNode connectedNode) {
    final Log log = connectedNode.getLog();
    Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber(topic, std_msgs.String._TYPE);
    subscriber.addMessageListener(new MessageListener<std_msgs.String>() {
      @Override
      public void onNewMessage(std_msgs.String message) {
        mainHandler.post(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(context, message.getData(), Toast.LENGTH_LONG).show();
          }
        });
      }
    });
  }
}

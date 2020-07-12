package com.github.ros.android.voicecommands;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import std_msgs.Float32;

/** A node to publish cancel-goal messages. */
public class StopPublisher extends AbstractNodeMain {
  private String topic_name;
  private Publisher<Float32> publisher;

  public StopPublisher(String topic)
  {
    topic_name = topic;
  }

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("android_assistant/stop_publisher");
  }

  @Override
  public void onStart(final ConnectedNode connectedNode) {
    publisher = connectedNode.newPublisher(topic_name, Float32._TYPE);
  }

  public void setTopic(String topic) {
    this.topic_name = topic;
  }

  public void publishMessage() {
    Float32 cmd = publisher.newMessage();
    cmd.setData(0);
    publisher.publish(cmd);
  }
}

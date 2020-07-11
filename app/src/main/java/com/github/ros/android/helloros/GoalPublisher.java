package com.github.ros.android.helloros;

import org.ros.concurrent.CancellableLoop;
import org.ros.internal.message.RawMessage;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

import geometry_msgs.Vector3;
import geometry_msgs.Vector3Stamped;

public class GoalPublisher extends AbstractNodeMain {
  private String topic_name;
  private Publisher<Vector3> publisher;

  public GoalPublisher(String topic)
  {
    topic_name = topic;
  }

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("android_assistant/goal_publisher");
  }

  @Override
  public void onStart(final ConnectedNode connectedNode) {
    publisher = connectedNode.newPublisher(topic_name, Vector3._TYPE);
  }

  public void setTopic(String topic) {
    this.topic_name = topic;
  }

  public void publishMessage(double x, double y, double z) {
    Vector3 cmd = publisher.newMessage();
    cmd.setX(x);
    cmd.setY(y);
    cmd.setZ(z);
    publisher.publish(cmd);
  }
}

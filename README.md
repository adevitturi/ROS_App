# ROS - Android Assistant App.
An application to send messages to ROS using your voice.

## Build and install
Build and install the app with Android Studio.

## Using the app
The first screen requests a master URI to connect to a ROS Master. Write the $ROS_MASTER_URI in which the master is running and press the connect button. 

## Topics
The base topic can be set in the settings page. E.g. if we chose the topic "goal_assistant" and namespace "create1", this will produce the following:
* /create1/goal_assistant: topic used to publish Vector3 messages with the goals commanded by the app.
* /create1/goal_assistant_cancel: this is used to publish a message to cancel the current goal, when a "stop command" is executed.
* /create1/goal_assistant_distance: the app subscribes to this topic to display the remaining distance to the current goal, in meters.
* /create1/goal_assistant_result: the app subscribes to this topic to display the action results.

## Communicating with move base
To control a robot using move base, [this package](https://github.com/adevitturi/create_autonomy) is required.


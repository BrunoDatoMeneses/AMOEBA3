# AMOEBA with ROS
AMOEBAonAMAK come with utilities to allow it to communicate with ROS using [rosbridge](http://wiki.ros.org/rosbridge_suite). Rosbridge allow for simpler integration of existing java application into ROS, and is up-to-date (wich is not the case of rosjava).

For anything ros-related, look at the `ros` package in `AMOEBAonAMAK`.

Make sure you've read [How to use AMOEBA](usage.md).

# Preparation
You should have a `.msg` and `.xml` files. Make sure they have the same name (except for the extension).

The .msg is a [ROS Message Type](http://wiki.ros.org/msg), setup your ROS project [to use it](http://wiki.ros.org/ROS/Tutorials/CreatingMsgAndSrv).

Add [rosbridge](http://wiki.ros.org/rosbridge_suite) to your project.

# Compile AMOEBA
Do
```
mvn clean compile assembly:single -Dmain.class=ros.Main
```
You'll find the jar in AMOEBAonAMAK/target

# Run
Launch your project, including rosbridge-server.

Launch `AMOEBAonAMAK/ros.Main` with the arguments `RosbridgeWebsocketURI ConfigFilePath RosToAmoebaTopic AmoebaToRosTopic MsgType`. In our example, and using the provided jar for Amoeba, the command is :
```
java -jar amoeba-ros.jar ws://localhost:9090 MyAmoebaConfig.xml /amoeba_request /amoeba_result your_ros_package/MyAmoebaMessageType
``` 
# Demo
A demo is provided, you'll find it in `documentation/ros_demo`:
Scripts are provided, for ease of use.
- `setup.sh` compile amoeba and the catkin workspace.
- `start.sh` launch the demo.
- `stop.sh` stop ROS process created by the demo.
- `clean.sh` remove files created by the above scripts.

The demo is a turtlesim doing ovals. First we teach (blue background), then we ask amoeba to move the turtle (rose background). Keep in mind that the goal of the demo is to show exemple of ros integration with amoeba. It's not a demonstration of amoeba capabilities, so if the turtle does not learn, it's not a big problem.

You can check that something is learned by looking at amoeba's window.

The python node is at `documentation/ros_demo/catkin_ws/src/amoeba_demo/src/script/demo.py`

In this demo, there's 2 amoebas (one for linear velocity, one for angular velocity), connected to ROS via rosbridge, and communicating with our python node via the topics `/amoeba_lin`, `/amoeba_lin_res`, `/amoeba_rot`, `/amoeba_rot_res`.

 `/amoeba_lin` and `/amoeba_rot` are where we publish our request to the amoebas, and  `/amoeba_lin_res` and `/amoeba_rot_res` is where we retrieve the result.

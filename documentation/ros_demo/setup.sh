#/bin/sh

(cd ../.. && mvn clean compile assembly:single -Dmain.class=ros.Main && cp AMOEBAonAMAK/target/AMOEBAonAMAK-1.0-jar-with-dependencies.jar documentation/ros_demo/amoeba.jar)
(cd catkin_ws/ && catkin_make)
echo "Done. If there's no error, you can do ./start.sh"

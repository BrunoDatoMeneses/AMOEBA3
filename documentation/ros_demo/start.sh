#/bin/sh

. ./catkin_ws/devel/setup.sh
roslaunch amoeba_demo turtle_demo.launch &
 ./start_amoebas.sh &

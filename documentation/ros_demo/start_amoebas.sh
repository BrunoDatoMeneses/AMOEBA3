#!/bin/sh

java -jar amoeba.jar ws://localhost:9090 Amoeba_ros_demo.xml /amoeba_lin /amoeba_lin_res amoeba_demo/Amoeba_msg &
java -jar amoeba.jar ws://localhost:9090 Amoeba_ros_demo.xml /amoeba_rot /amoeba_rot_res amoeba_demo/Amoeba_msg &


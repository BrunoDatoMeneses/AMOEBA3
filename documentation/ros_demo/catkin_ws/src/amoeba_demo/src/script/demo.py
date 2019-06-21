#!/usr/bin/python

import time
import rospy
from geometry_msgs.msg import Twist
from amoeba_demo.msg import Amoeba_msg
from turtlesim.msg import Pose

learn = True
tx = 0
ty = 0
theta = 0

lin_vel = 0
ang_vel = 0


def control():
    global learn
    time.sleep(5)
    rospy.loginfo("Learning ...")
    for i in range(60):
	forward(1)
	rotate(1)
    learn = False
    rospy.loginfo("replaying ...")
    while not rospy.is_shutdown():
	askAmoeba()
	
def forward(value):
    vel_msg = Twist()
    vel_msg.linear.x = value
    vel_msg.linear.y = 0
    vel_msg.linear.z = 0
    vel_msg.angular.x = 0
    vel_msg.angular.y = 0
    vel_msg.angular.z = 0
    pubTurtle.publish(vel_msg)
    if learn :
	pub(pubAmoebaLin, vel_msg.linear.x)
    rate.sleep()

def rotate(value):
    vel_msg = Twist()
    vel_msg.linear.x = 0
    vel_msg.linear.y = 0
    vel_msg.linear.z = 0
    vel_msg.angular.x = 0
    vel_msg.angular.y = 0
    vel_msg.angular.z = value
    pubTurtle.publish(vel_msg)
    if learn :
	pub(pubAmoebaRot, vel_msg.angular.z)
    rate.sleep()

def askAmoeba():
    global lin_vel, ang_vel
    lin_vel = 0
    ang_vel = 0
    pub(pubAmoebaLin, 0)
    pub(pubAmoebaRot, 0)
    rate.sleep()
    vel_msg = Twist()
    vel_msg.linear.x = lin_vel
    vel_msg.linear.y = 0
    vel_msg.linear.z = 0
    vel_msg.angular.x = 0
    vel_msg.angular.y = 0
    vel_msg.angular.z = ang_vel
    pubTurtle.publish(vel_msg)
    rate.sleep()

def pub(publisher, oracle):
    msg = Amoeba_msg()
    msg.learn = learn
    msg.px = tx
    msg.py = ty
    msg.theta = theta
    msg.oracle = oracle
    publisher.publish(msg)

def callback(data):
    global tx, ty, theta
    tx = data.x * 10
    ty = data.y * 10
    theta = data.theta * 10

def callbackLin(data):
    global lin_vel
    lin_vel = data.oracle


def callbackRot(data):
    global ang_vel
    ang_vel = data.oracle


if __name__ == '__main__':
    print("Start control ...")
    try:
	rospy.init_node('control', anonymous=True)
	pubTurtle = rospy.Publisher('turtle1/cmd_vel', Twist, queue_size=10)
	pubAmoebaLin = rospy.Publisher('amoeba_lin', Amoeba_msg, queue_size=10)
	pubAmoebaRot = rospy.Publisher('amoeba_rot', Amoeba_msg, queue_size=10)
	rospy.Subscriber("turtle1/pose", Pose, callback)
	rospy.Subscriber("amoeba_lin_res", Amoeba_msg, callbackLin)
	rospy.Subscriber("amoeba_rot_res", Amoeba_msg, callbackRot)
	rate = rospy.Rate(1) # 1hz
        control()
    except rospy.ROSInterruptException:
        pass

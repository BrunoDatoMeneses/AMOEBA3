# Install :
Install python dependencies :
```
pip3 install requirements.txt
```
Optional : check [openai gym](https://gym.openai.com/) documentation for a full install.

Run the setup script [setup.sh](setup.sh) (linux only)

You're good to go, check that py4j is correctly working with [basic_demo.py](basic_demo.py) :
```
python3 basic_demo.py
```
You should get the classic amoeba's GUI, and random context appearing.

Then you can look at some actual learning, inside [learn_gym.py](learn_gym.py).


# Optional : Ros2Learn
[Ros2Learn](https://github.com/AcutronicRobotics/ros2learn) provide some tools for machine learning on robots, using Ros2, openAI gym, and gazebo. 

Install Ros2 and Ros2learn, follow Ros2Learn instruction. Make sure your version of gazebo is at least 9.9.

In your python script, import gym_gazebo2. Before running your python code make sure that you properly loaded Ros2 and Ros2learn environment using their provision scripts.

You can now use gym environment provided by Ros2learn as regular gym environment.
```Python
env = gym.make('MARA-v0')
```


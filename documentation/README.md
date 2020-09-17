# Running Experimentations

## Prerequisites

Install java JRE 8
https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html


## Allowed parameters

number_of_joints: `2, 3, 6, 10, 20, 30`

learning_situations: `any positive integer (1000 is used in the paper)` 

exploitation_situations: `any positive integer (200 is used in the paper)`

number_of_episodes: `any positive integer (15 is used in the paper)`

precision_range (%): `any positive integer between 1 and 10 (1 and 3 are used in the paper)`

neighborhood_size: `2, 4, 6, 8, 10, 12, 14, 16`

arm_length: `any positive integer (50 is used in the paper)`

## Run experimentation with UI

```
java -jar experimentationWithUi.jar <number_of_joints> <learning_situations> <exploitation_situations> <precision_range> <neighborhood_size> <arm_length>
```

Example: ``-jar experimentationWithUi.jar 2 1000 200 3 2 50``

To start the learning, select the speed on the bottom left of the `Robot Arm Simulation` window. Step by step running is also possible. You can see the Context Agents by clicking on the button `Allow Rendering` in the `Multi-Agent System` window. ATTENTION: do no use the speed selection on the `Multi-Agent System` window.

During exploitation, one simulation step shows the target goal and the next simulation step shows the result of the exploitation.

After the exploitation is done, the goal error is displayed on the console and you can set the speed to `Stop` or close the windows.



## Run experimentation with several learning episodes

```
java -jar experimentation.jar <number_of_joints> <learning_situations> <exploitation_situations> <number_of_episodes> <precision_range> <neighborhood_size> <arm_length>
```
Example: ``-jar experimentation.jar 2 1000 200 15 3 2 50``


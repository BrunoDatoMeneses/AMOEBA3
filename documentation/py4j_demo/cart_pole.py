import gym
import subprocess
import time
import numpy as np

from py4j.java_gateway import JavaGateway, GatewayParameters, set_field


def chose_next_action(amoeba, state):
    proposition = [
        amoeba.request({"p0": state[0]*100, "p1": state[1]*100, "p2": state[2]*100, "p3": state[3]*100, "a0": 1.0, "a1": 0.0, "oracle": 0.0}),
        amoeba.request({"p0": state[0]*100, "p1": state[1]*100, "p2": state[2]*100, "p3": state[3]*100, "a0": 0.0, "a1": 1.0, "oracle": 0.0})
    ]
    return np.argmax(proposition)


def learn_amoeba(amoeba, state, action, reward):
    a0 = 1.0 if action == 0 else 0.0
    a1 = 1.0 if action == 1 else 0.0
    #a2 = 1.0 if action == 2 else 0.0
    amoeba.learn({"p0": state[0]*100, "p1": state[1]*100, "p2": state[2]*100, "p3": state[3]*100, "a0": a0, "a1": a1, "oracle": reward})


if __name__ == '__main__':
    # Make sure to run setup.sh at least once before running this script

    subprocess.Popen(["java", "-jar", "amoeba.jar"])
    time.sleep(2)

    gateway = JavaGateway(gateway_parameters=GatewayParameters(auto_convert=True, auto_field=True))
    gateway.jvm.py4j.Main.Control.setComandLine(True)

    amoeba = gateway.jvm.kernel.AMOEBA("/home/daavve/AMOEBA3/documentation/py4j_demo/cart_pole.xml", None)
    set_field(amoeba.saver, "autoSave", False)

    env = gym.make('CartPole-v1')
    env.reset()

    # Initialize variables to track rewards
    reward_list = []
    ave_reward_list = []

    episodes = 1000
    epsilon = 0.3
    min_eps = 0
    reduction = 0.01
    for i in range(episodes):
        # Initialize parameters
        done = False
        tot_reward, reward = 0, 0
        state = env.reset()

        state_action_list = []

        while not done:
            # Render environment for last five episodes
            env.render()
            #if i >= (episodes - 20):
            #    env.render()

            # Determine next action - epsilon greedy strategy
            if np.random.random() < 1 - epsilon:
                action = chose_next_action(amoeba, state)
            else:
                action = np.random.randint(0, env.action_space.n)

            # Get next state and reward
            state2, reward, done, info = env.step(action)

            state_action_list.append((state, action))

            state = state2

            # Update variables
            tot_reward += reward

        for state, action in state_action_list:
            learn_amoeba(amoeba, state, action, tot_reward)
        print('Episode {}  Reward: {}'.format(i + 1, tot_reward))

        # Decay epsilon
        if epsilon > min_eps:
            epsilon -= reduction

        # Track rewards
        reward_list.append(tot_reward)

        if (i + 1) % 10 == 0:
            ave_reward = np.mean(reward_list)
            reward_list = []

        if (i + 1) % 10 == 0:
            print('Episode {} Average Reward: {} Epsilon: {}'.format(i + 1, ave_reward, epsilon))

    env.close()



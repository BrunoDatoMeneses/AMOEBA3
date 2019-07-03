import gym
import subprocess
import time
import numpy as np

from py4j.java_gateway import JavaGateway, GatewayParameters, set_field


def msg_obj(obs:list, act:list, oracle) -> dict:
    msg = dict()
    for i, o in enumerate(obs):
        msg["p%d" % i] = float(o*100.0)
    for i, a in enumerate(act):
        msg["a%d" % i] = float(a)
    msg["oracle"] = float(oracle)
    return msg


def chose_next_action(amoeba, state):
    proposition = []
    for i in range(env.action_space.n):
        act = [0.0]*env.action_space.n
        act[i] = 1
        proposition.append(amoeba.request(msg_obj(state, act, 0)))

    return np.argmax(proposition)


def learn_amoeba(amoeba, state, action, reward):
    act = [0.0]*env.action_space.n
    act[action] = 1
    amoeba.learn(msg_obj(state, act, reward))


if __name__ == '__main__':
    # Make sure to run setup.sh at least once before running this script

    subprocess.Popen(["java", "-jar", "amoeba.jar"])
    time.sleep(2)

    gateway = JavaGateway(gateway_parameters=GatewayParameters(auto_convert=True, auto_field=True))
    #gateway.jvm.py4j.Main.Control.setComandLine(True)

    amoeba = gateway.jvm.kernel.AMOEBA()
    backup_sys = gateway.jvm.kernel.backup.BackupSystem(amoeba)
    file = gateway.jvm.java.io.File("lunar_lander.xml")
    backup_sys.load(file)

    env = gym.make('LunarLander-v2')
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
            env.step(action)
            env.step(action)
            env.step(action)
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

        print_delta = 10
        if (i + 1) % print_delta == 0:
            ave_reward = np.mean(reward_list)
            reward_list = []
            print('Episode {}-{} Average Reward: {} Epsilon: {}'.format(i - print_delta + 1, i + 1, ave_reward, epsilon))

    env.close()



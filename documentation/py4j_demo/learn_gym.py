import os
import gym
#import gym_gazebo2
import subprocess
import time
import math
import numpy as np
import matplotlib.pyplot as plt

from py4j.java_gateway import JavaGateway, GatewayParameters, set_field

MULTIPLICATOR = 1000.0

def gen_file(percepts:list):
    """
    Create the config file for amoeba
    :param percepts: list of tuple (percept:string, enum:bool)
    :return config file name
    """
    filename = "config.xml"
    with open(filename, "w") as xmlFile:
        start = """<?xml version="1.0" encoding="UTF-8"?>
<System>

    <Configuration>	
        <Learning allowed = "true" creationOfNewContext = "true" loadPresetContext = "false"></Learning>	
    </Configuration>

    <StartingAgents>
"""
        xmlFile.write(start)
        for percept, enum in percepts:
            xmlFile.write("\t\t<Sensor Name=\"%s\" Enum=\"%s\" />\n" % (percept, "true" if enum else "false"))
        end = """
        <Controller Name="Controller">
            <ErrorMargin ErrorAllowed="2000.0" AugmentationFactorError="5.0" DiminutionFactorError="0.4" MinErrorAllowed="0.1" NConflictBeforeAugmentation="40" NSuccessBeforeDiminution="80" />

        </Controller> 
    </StartingAgents>

</System>
"""
        xmlFile.write(end)
        return os.path.realpath(xmlFile.name)


def percepts_from_env(env) -> list:
    percepts = []
    for i in range(env.observation_space.shape[0]):
        percepts.append(("p%d" % i, False))
    if isinstance(env.action_space, gym.spaces.discrete.Discrete):
        n = 1
        enum = True
    else:
        n = env.action_space.shape[0]
        enum = False
    for i in range(n):
        percepts.append(("a%d" % i, enum))
    return percepts


def msg_obj(obs:list, act:list, oracle) -> dict:
    msg = dict()
    for i, o in enumerate(obs):
        msg["p%d" % i] = float(o*MULTIPLICATOR)
    for i, a in enumerate(act):
        msg["a%d" % i] = float(a*MULTIPLICATOR)
    msg["oracle"] = float(oracle)
    return msg


def chose_next_action(amoeba, state, env):
    if isinstance(env.action_space, gym.spaces.discrete.Discrete):
        # Special case for Discrete, work better than maximize
        #proposition = []
        #for i in range(env.action_space.n):
        #    act = [i]
        #    proposition.append(amoeba.request(msg_obj(state, act, 0)))
        #return np.argmax(proposition)
        # ----------------------------------------------------
        n = 1
    else:
        n = env.action_space.shape[0]

    action = [0.0]*n
    msg = msg_obj(state, [], 0)
    msg.pop("oracle", None)
    res = amoeba.maximize(msg)

    if (res["oracle"] == -math.inf) or (res is None):
        return random_action()

    for i in range(n):
        action[i] = res["a%d" % i]/MULTIPLICATOR

    if isinstance(env.action_space, gym.spaces.discrete.Discrete) :
        return int(round(action[0]))
    else:
        return action


def learn_amoeba(amoeba, state, action, reward, env):
    if isinstance(env.action_space, gym.spaces.discrete.Discrete):
        act = [action]
        amoeba.learn(msg_obj(state, act, reward))
    else:
        amoeba.learn(msg_obj(state, action, reward))


def random_action():
    #nb_random_action += 1
    return env.action_space.sample()


def render():
    if i > 0:
        env.render()


if __name__ == '__main__':

    plt.ion()

    # Make sure to run setup.sh at least once before running this script
    subprocess.Popen(["java", "-jar", "amoeba.jar"])
    time.sleep(2)

    gateway = JavaGateway(gateway_parameters=GatewayParameters(auto_convert=True, auto_field=True))
    #gateway.jvm.py4j.Main.Control.setComandLine(True)
    gateway.jvm.py4j.Main.Control.setLogLevel("INFORM")

    env = gym.make('CartPole-v0')
    env.reset()
    percepts = percepts_from_env(env)
    filename = gen_file(percepts_from_env(env))

    amoeba = gateway.jvm.kernel.ELLSA()
    backup_sys = gateway.jvm.kernel.backup.BackupSystem(amoeba)
    file = gateway.jvm.java.io.File(filename)
    backup_sys.load(file)

    # Initialize variables to track rewards
    reward_list = []
    ave_reward_list = []
    nb_random_action = 0

    episodes = 1000
    epsilon = 0.3
    min_eps = 0.02
    reduction = 0.01
    for i in range(episodes):
        # Initialize parameters
        done = False
        tot_reward, reward = 0, 0
        nb_random_action = 0
        state = env.reset()
        render()

        state_action_list = []

        j = 0
        while not done:

            if np.random.random() < 1 - epsilon:
                action = chose_next_action(amoeba, state, env)
            else:
                action = random_action()

            state_action_list.append((state, action))

            reward = 0
            # Get next state and reward
            # j += 1
            # if j >= 200:
            #     done = True
            #     r = -100
            # else:
            state2, r, done, info = env.step(action)
            render()
            reward += r

            learn_amoeba(amoeba, state, action, reward, env)

            tot_reward += reward

            state = state2

        # for state, action in state_action_list:
        #     learn_amoeba(amoeba, state, action, tot_reward, env)
        print('Episode {}  Reward: {}  Random actions: {}/{}  Info: {}'.format(i + 1, tot_reward, nb_random_action, i, info))

        # Decay epsilon
        if epsilon > min_eps:
            epsilon -= reduction

        # Track rewards
        reward_list.append(tot_reward)

        print_delta = 10
        if (i + 1) % print_delta == 0:
            ave_reward = np.mean(reward_list)
            ave_reward_list.append(ave_reward)
            reward_list = []
            print(
                'Episode {}-{} Average Reward: {} Epsilon: {}'.format(i - print_delta + 1, i + 1, ave_reward, epsilon))
            plt.clf()
            plt.plot(ave_reward_list)
            plt.pause(0.1)

    plt.show()
    plt.pause(100000)
    env.close()



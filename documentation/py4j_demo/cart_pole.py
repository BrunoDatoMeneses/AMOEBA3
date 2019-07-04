import gym
import subprocess
import time
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
    return filename


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
        proposition = []
        for i in range(env.action_space.n):
            act = [0.0]*env.action_space.n
            act[i] = 1.0
            proposition.append(amoeba.request(msg_obj(state, act, 0)))
        action = np.argmax(proposition)
    else:
        res = amoeba.maximise(msg_obj(state, []))
        n = env.action_space.shape[0]
        action = [0.0]*n
        for i in range(n):
            action[i] = res["a%d" % i]/MULTIPLICATOR

    return action


def learn_amoeba(amoeba, state, action, reward, env):
    if isinstance(env.action_space, gym.spaces.discrete.Discrete):
        act = [0.0] * env.action_space.n
        act[action] = 1.0
        amoeba.learn(msg_obj(state, act, reward))
    else:
        amoeba.learn(msg_obj(state, action, reward))


if __name__ == '__main__':

    plt.ion()

    # Make sure to run setup.sh at least once before running this script
    subprocess.Popen(["java", "-jar", "amoeba.jar"])
    time.sleep(2)

    gateway = JavaGateway(gateway_parameters=GatewayParameters(auto_convert=True, auto_field=True))
    gateway.jvm.py4j.Main.Control.setComandLine(True)

    env = gym.make('CartPole-v1')
    env.reset()
    percepts = percepts_from_env(env)
    filename = gen_file(percepts_from_env(env))

    amoeba = gateway.jvm.kernel.AMOEBA()
    backup_sys = gateway.jvm.kernel.backup.BackupSystem(amoeba)
    file = gateway.jvm.java.io.File(filename)
    backup_sys.load(file)

    # Initialize variables to track rewards
    reward_list = []
    ave_reward_list = []

    episodes = 1000
    epsilon = 0
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
            #env.render()
            #if i >= (episodes - 20):
            #    env.render()

            # Determine next action - epsilon greedy strategy
            if np.random.random() < 1 - epsilon:
                action = chose_next_action(amoeba, state, env)
            else:
                action = np.random.randint(0, env.action_space.n)

            # Get next state and reward
            state2, reward, done, info = env.step(action)

            state_action_list.append((state, action))

            state = state2

            # Update variables
            tot_reward += reward

        for state, action in state_action_list:
            learn_amoeba(amoeba, state, action, tot_reward, env)
        print('Episode {}  Reward: {}'.format(i + 1, tot_reward))

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


    env.close()



# How to use AMOEBA

## What AMOEBA can do :
AMOEBA is a learning function, and it does that by building a model of the function.

The inputs of that function can be any amount of numerical variables (usually continuous, like the x and y coordinate in an Euclidian space), we call these variables Percepts. The output (result) of our function is a single, numerical variable (like whether or not that point in our Euclidian space has the colour red), we call this result Prediction, and the correct prediction is called Oracle.

 Amoeba can be asked to learn, or to provide a prediction, at any time. Making it suitable for real time usage and lifelong learning.

# Preparations
First, make sure that you properly [installed AMOEBAonAMAK](installing.md), preferably with eclipse. 

Depending on your problem, determine what AMOEBA should learn, and from what. But keep in mind that an amoeba predict one variable, if you have multiple variables to predict, you'll need multiple amoebas.

## Create a config file
A config file is a xml file used to initialize your amoeba. Most importantly it contain the list of percepts.

You can use `utils/genFiles.py` to generate your xml file based on a list of percept. `python genFiles.py MySystem false px py` will create `MySystem.xml` (and `MySystem.msg`, but ignore it if you don't use [AMOEBA and ROS](rosbridge.md)) containing something looking like the following example :
(if you don't want to use python, a java class doing the same job is available at `utils.XmlConfigGenerator`)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<System>

    <!-- General config options -->
    <Configuration>	
        <Learning allowed = "true" creationOfNewContext = "true" loadPresetContext = "false"></Learning>	
    </Configuration>
	
    <StartingAgents>
        <!-- List your percepts here -->
        <Sensor Name="px" Enum="false" />
        <Sensor Name="py" Enum="false" />
	
        <Controller Name="Controller">
            <ErrorMargin ErrorAllowed="2000.0" AugmentationFactorError="5.0" DiminutionFactorError="0.4" MinErrorAllowed="0.1" NConflictBeforeAugmentation="40" NSuccessBeforeDiminution="80" />
        </Controller> 
    </StartingAgents>
	
</System>
```
With :
- **Configuration** make sure `Learning` and `creationOfNewContext` are at `true` if you want to train your model.
- **Sensor** represent one percept, make sure to remember the name, we will use it later. `Enum` tell AMOEBA whether or not the variable linked to this percept is continuous, or discrete. 
- **Controller** these are hyperparameter for the AMOEBA, they affect how fast and how well an amoeba learn. They can be dynamically adjusted during training, AMOEBA even does it automatically ! You still might want to adjust `MinErrorAllowed` and `MinInexactAllowed`, as they're dependent on your problem.

If you want to use AMOEBA with ROS, continue [here](rosbridge.md).

## Create your studied system java class
We have to tell AMOEBA what we want it to learn. Although we can hand-feed an amoeba with our data, providing a studied system allow to make full use of the graphical user interface.

Your studied system must implement the `StudiedSystem` interface. Most importantly :
- `public void playOneStep()` advance the simulation of the studied system by one step (for example, read the next data point in a file).
- `public HashMap<String, Double> getOutput()` give the value of percepts and oracle for the current step.

The hash map returned by `getOutput()` MUST have this structure :
```
{
    "contextName1":value1,
    "contextName2":value2,
    ...
    "oracle":expectedPrediction
}
```
Where the keys ("contextNameX") are the ones set in the config file, and values the value of that percept at that step. 

For example it can look like :
```Java
public HashMap<String, Double> getOutput(){
    HashMap<String, Double> ret = new HashMap<>();
    ret.put("px",x);
    ret.put("py",y);
    ret.put("oracle", computeOracleFor(x,y));
    return ret;
}
```
Where `px` and `py` are the name of our 2 percepts, `x` and `y` where set in `playOneStep()`, and `computeOracleFor(x,y)` return the oracle for the point (x,y).

Usage example with a basic learning loop :
```Java
for(int i = 0; i < 1000; i++){
    studiedSystem.playOneStep();
    amoeba.learn(studiedSystem.getOutput())
}
```
There's a 3rd method in StudiedSystem : `public double requestOracle(HashMap<String, Double> request)` it is used when the user ask amoeba (via the GUI) to learn at a specific point. The GUI will ask the studied system what the oracle is at that point, then ask the amoeba to learn that point. If you don't use that feature, no need to put any meaningful code in that method. 

# Your main method
Your main method is where you'll instantiate, configure, and use your amoeba.
```Java
public static void main(String[] args) {
    //TODO your code for creating an amoeba
}
```
## Global configuration
AMAK offers some configuration that must be changed before the creation of a new amoeba. They are in `AMAKFX.src.fr.irit.smac.amak.Configuration.java` and the most important are :
- `allowedSimultaneousAgentsExecution` : control multithreading of agents.
- `commandLineMode` : tell whether or not to use the graphical user interface.
```Java
public static void main(String[] args) {
    //global configuration
    Configuration.commandLineMode = false;
    Configuration.allowedSimultaneousAgentsExecution = 4;
}
```
## Instantiating an amoeba
There's multiple ways to instantiate an amoeba, the easiest is :
```Java
public static void main(String[] args) {
    //global configuration
    Configuration.commandLineMode = false;
    Configuration.allowedSimultaneousAgentsExecution = 4;

    //intantiate an amoeba
    StudiedSystem studiedSystem = new myStudiedSystem();
    AMOEBA amoeba = new AMOEBA("path/to/my/configFile.xml", studiedSystem);
}
```
With that you get a perfectly functioning amoeba, but it can only be [controlled with the GUI](gui.md), which can be very limiting.

## Using an amoeba
Like shown before, you can control amoeba's learning with a very simple loop. And the same can be done when asking amoeba for predictions :
```Java
public static void main(String[] args) {
    //global configuration
    Configuration.commandLineMode = false;
    Configuration.allowedSimultaneousAgentsExecution = 4;

    //intantiate an amoeba
    StudiedSystem studiedSystem = new myStudiedSystem();
    AMOEBA amoeba = new AMOEBA("path/to/my/configFile.xml", studiedSystem);

    //learn 1000 points
    for(int i = 0; i < 1000; i++){
        studiedSystem.playOneStep();
        amoeba.learn(studiedSystem.getOutput());
    }

    //make prediction for 1000 other points
    for(int i = 0; i < 1000; i++){
        studiedSystem.playOneStep();
        System.out.println(
            amoeba.request(studiedSystem.getOutput())
        );
    }
}
```
We can ask amoeba how well it performed on the last prediction with `amoeba.getHeadAgent().getCriticity()`.

For more example on how to use amoeba, check `AdvancedMain.java`, `MinimalMain.java`, and `Main.java` in the `experiments` package.
# Usage
*This is a markdown version of the example in Main.java*

## Basic usage
Set AMAK configuration before creating an AMOEBA
```Java
Configuration.commandLineMode = false;
Configuration.allowedSimultaneousAgentsExecution = 2;
```
Create a World, a Studied System, and an AMOEBA
```Java
World world = new World();
StudiedSystem studiedSystem = new F_XY_System(50.0);
AMOEBA amoeba = new AMOEBA(world, studiedSystem);
```
*Note : if you plan on only using the learn method, you can leave studiedSystem at null when creating an amoeba.*

A window appeared, allowing to control the simulation, but if you try to run it, it will crash (there's no percepts !). We need to load a configuration :

Create a backup system for the AMOEBA and load a configuration matching the studied system.
```Java
IBackupSystem backupSystem = new BackupSystem(amoeba);
File file = new File("resources\\twoDimensionsLauncher.xml");
backupSystem.loadXML(file);
```
The amoeba is ready to be used.

## Controling amoeba and learning
AMOEBA extends AMAK's Amas class, you can control the simulation with the methods it provide. If at the creation of the amoeba a studied system was provided, you can launch the simulation and amoeba will learn.

We provide an additionnal way to learn with amoeba, as described in IAMOEBA.
Here is an exemple :

We deny the possibility to change simulation speed with the UI, and allow rendering.
```Java
amoeba.allowGraphicalScheduler(false);
amoeba.setRenderUpdate(true);
```
We run some learning cycles.
```Java
int nbCycle = 1000;
for (int i = 0; i < nbCycle; ++i) {
    studiedSystem.playOneStep();
    amoeba.learn(studiedSystem.getOutput());
}
```
Rendering the UI slow a lot the simulation, we can partially deactivate it during runtime with
```Java
amoeba.setRenderUpdate(false);
```
and then activate it back with
```Java
amoeba.setRenderUpdate(true);
```
*Note that when rendering is deactivated, graph and some statistics are no longer updated.*

But after activating rendering we need to run a cycle to update the render of contexts agents. For this example we use a request call to avoid change in contexts.
```Java
amoeba.request(studiedSystem.getOutput());
```
You can ignore this step if you run the simulation after activating rendering.
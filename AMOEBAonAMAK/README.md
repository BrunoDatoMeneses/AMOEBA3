# AMOEBA on AMAK
An AMOEBA3 port on AMAK.

## Install with Eclipse
Import AMOEBAonAMAK and amak as eclipse projects, add amak as a project dependency for AMOEBAonAMAK. Add *jdom2* and *apache commons math* as jar dependencies.

## Quick start
```Java
// Create a world and a studied system for your amoeba
World world = new World();
StudiedSystem studiedSystem = new F_XY_System(50.0);
AMOEBA amoeba = new AMOEBA(world, studiedSystem);
// Create a backup system for the amoeba
IBackupSystem backupSystem = new BackupSystem(amoeba);
// Load a configuration matching the studied system
File file = new File("resources\\twoDimensionsLauncher.xml");
backupSystem.loadXML(file);
// The amoeba is ready to be used
```
You can find a more complete exemple at `experiment.Main.java`.
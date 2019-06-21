# Installing and build AMOEBAonAMAK

## Dependencies
- maven (tested with 3.6.0) or eclipse (tested with 2019)
- Java 8 or more (tested with Java 8 and 11)

## Build AMOEBAonAMAK
Clone the repo : `git clone https://github.com/Hugi-R/AMOEBA3.git`

To get the latest, but less stable, version : `git clone https://github.com/Hugi-R/AMOEBA3.git -b dev`

### Build with maven
Compile and run tests :

`mvn clean test` or `mvn test`

Build an executable jar with all dependencies :

```
mvn clean compile assembly:single
```

You can change the main class of the executable jar with the argument `-Dmain.class=fully.qualified.Main`.

Example :
```
mvn clean compile assembly:single -Dmain.class=ros.Main
```

Results are in `target` folder.

You may have to tweak the `pom.xml` to make it compatible with your maven/java setup, or to chose the starting class of your executable jar.

For example, if you use jdk-1.8 (java 8) you have to replace 
```
<release>8</release>
```
in `maven-compiler-plugin` by
```
<source>1.8</source>
<target>1.8</target>
```

The resulting jar is not platform agnostic.

### Build with eclipse
`File > Open project from file system`, select the folder `AMOEBA3`, eclipse should detect 3 maven projects :
```
AMOEBA-parent
  | AMAKFX
  | AMOEBAonAMAK
```
`Finish`

Make sure it works by right-clicking on `AMOEBA-parent` > `Run as > Maven test`. You should get a `BUILD SUCCES`.

Make sure it works with JavaFX : right-click on `AMAKFX.src.fr.irit.smac.amak.examples.philosophers.PhilosopherLaunchExample.java` or any other example and `Run as > Java application`. Do the same for `AMOEBAonAMAK.src.experiments.MinimalMain.java`. You should get a new window, and no error in the console.

## Troubleshooting
 I get `Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.8.0:compile (default-compile) on project AMAKFX: Fatal error compiling: invalid flag: --release -> [Help 1]` ? In AMOEBA-parent's pom.xml, replace 
```
<release>8</release>
```
in `maven-compiler-plugin` by
```
<source>1.8</source>
<target>1.8</target>
```
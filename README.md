# AMAKFX and AMOEBAonAMAK 

Tested with OpenJDK 11, should work with Java 8.

Check [the documentation directory](documentation) for more detailed and in depth instruction and explanation.

Check AMAKFX's [README](AMAKFX/README.md) for more detail on how to use it.

# Quick start :
## Maven build Jar with dependencies
`mvn clean compile assembly:single`
Note that the resulting jars are not platform independent. 

## Use with Eclipse
Clone this repo and import it as a Maven project.
There is one parent project `AMOEBA-parent` and two modules `AMOEBAonAMAK` and `AMAKFX`.

You should get a structure like this :
```
AMOEBA-parent
  | AMAKFX
  | AMOEBAonAMAK
```

Check AMAKFX's [README](AMAKFX/README.md) for more detail on how to use it.

Check [the documentation directory](documentation) for more detailed and in depth instruction and explanation on AMOEBA.

### Amoeba Quick start
```Java
StudiedSystem studiedSystem = new F_XY_System(50.0);
AMOEBA amoeba = new AMOEBA("resources/twoDimensionsLauncher.xml", studiedSystem);
```
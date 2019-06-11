# AMAKFX and AMOEBAonAMAK 

Tested with openjdk 11.

## Build Jar with dependencies
`mvn clean compile assembly:single`
Note that the resulting jars are not platform independant. 

## Use with Eclipse
Clone this repo and import it as a Maven project.
There is one parent project `AMOEBA-parent` and two modules `AMOEBAonAMAK` and `AMAKFX`.

You should get a structure like this :
```
AMOEBA-parent
  | AMAKFX
  | AMOEBAonAMAK
```
For more detail check the `documentation` directory.


# Using Py4j
[Py4j](https://www.py4j.org/) is a Python/Java tool allowing control java code with python.

Demos are available in the [py4j_demo](py4j_demo) directory.

A minimalistic main is provided at `py4j.Main.java`. Please note that py4j use socket, so only one process of that main can be executed at a given time. You can have multiple ellsa on a same process, but only one GUI on the same process. If you need to launch multiple ellsas with GUI at a same time, check py4j official documentation on how to change sockets.

To compile AMOEBA as an executable jar with this main, do :
```
mvn clean compile assembly:single -Dmain.class=py4j.Main
```
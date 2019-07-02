#/bin/sh

(cd ../.. && mvn clean compile assembly:single -Dmain.class=py4j.Main && cp AMOEBAonAMAK/target/AMOEBAonAMAK-1.0-jar-with-dependencies.jar documentation/py4j_demo/amoeba.jar)
echo "Done. If there's no error."

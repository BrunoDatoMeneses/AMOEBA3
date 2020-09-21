import subprocess
import time

from py4j.java_gateway import JavaGateway, GatewayParameters

if __name__ == '__main__':
    # Make sure to run setup.sh at least once before running this script

    subprocess.Popen(["java", "-jar", "ellsa.jar"])
    time.sleep(2)

    gateway = JavaGateway(gateway_parameters=GatewayParameters(auto_convert=True))
    ellsa = gateway.jvm.kernel.ELLSA("/home/daavve/AMOEBA3/AMOEBAonAMAK/resources/twoDimensionsLauncher.xml", None)
    for i in range(100):
        ellsa.learn({"px0": float(i), "px1": float(i), "oracle": float(i)})
        print(i, " ", ellsa.getAction())

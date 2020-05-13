package experiments.roboticArm;

public class RobotController {

    int jointsNumber;

    public RobotController(int jointsNb){
        jointsNumber = jointsNb;
    }

    public void setJoint(int jointIndice, int cycle, double[] anglesBase, double[] angles) {
        //setContinuousSinusoidJoints(jointIndice, cycle, anglesBase, angles);
        setRandomJoints(jointIndice, angles);
    }

    private void setContinuousSinusoidJoints(int jointIndice, double cycle, double[] anglesBase, double[] angles) {
        anglesBase[jointIndice] = 10*Math.sin(0.0001* cycle * Math.PI );
        if (jointIndice == 0) angles[jointIndice] = anglesBase[jointIndice] + (7.0/(double)jointsNumber)*Math.sin((0.01)* cycle * Math.PI    +  ((jointsNumber-jointIndice)*0.5) );
        else angles[jointIndice] = (7.0/(double)jointsNumber) * Math.sin(0.01* cycle * Math.PI    +  ((jointsNumber-jointIndice)*0.5) );
    }

    private void setRandomJoints(int jointIndice, double[] angles) {
        angles[jointIndice] = Math.random()* 2 * Math.PI;
    }

}

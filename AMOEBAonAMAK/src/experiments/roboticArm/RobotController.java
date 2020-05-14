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

    public void setJointsFromRequest(double[] currentAngles, double[] goalAngles, double variation){

        for(int i=0;i<jointsNumber;i++){

            double difference = goalAngles[i]-currentAngles[i];
            double deltaTheta;

            if(Math.abs(difference)> Math.PI){
                deltaTheta = difference>0 ? - variation :  variation;
            }else{
                deltaTheta = difference>0 ?  variation : - variation;
            }
            System.out.println(i + " Goal " + goalAngles[i] + " Current " + currentAngles[i] + " diff " + difference + " delta " + deltaTheta);
            currentAngles[i] += deltaTheta;
        }

    }

}

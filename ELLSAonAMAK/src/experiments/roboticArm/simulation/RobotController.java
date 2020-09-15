package experiments.roboticArm.simulation;

import utils.TRACE;
import utils.TRACE_LEVEL;

public class RobotController {

    int jointsNumber;
    public double[] activeLearningSituation = null;
    int pseudoRandomCounter = 0;

    public RobotController(int jointsNb){
        jointsNumber = jointsNb;
    }

    public void setJoint(int jointIndice, int cycle, double[] anglesBase, double[] angles) {

        setRandomJoints(jointIndice, angles);
    }



    private void setRandomJoints(int jointIndice, double[] angles) {

        if(jointIndice==0){
            angles[jointIndice] = Math.random()* 2 * Math.PI;
        }else{
            double dispersion = 2.5593*Math.pow(jointsNumber,-0.479); //Math.PI/6 for 30 joints, Math.PI/4 for 10 joints , Math.PI/2 for 3 joints
            angles[jointIndice] = modulo2PI(gaussianRandom(0.0,dispersion));
        }
    }

    public void setJoint(int jointIndice, double[] angles, double value) {

        angles[jointIndice] = value;
    }


    private double gaussianRandom(double mean, double dispersion){
        java.util.Random r = new java.util.Random();
        return  (r.nextGaussian() * dispersion) + mean;
    }


    public void setJointsFromRequest(double[] currentAngles, double[] goalAngles, double variation){

        for(int i=0;i<jointsNumber;i++){
            currentAngles[i] = maxMin2PI( goalAngles[i]);
        }
    }

    public double maxMin2PI(double angle){

        if(angle<0.0){
            TRACE.print(TRACE_LEVEL.DEBUG,"----------> ERROR " + angle);
            return 0.0;
        }else if(angle>Math.PI*2){
            TRACE.print(TRACE_LEVEL.DEBUG,"----------> ERROR " + angle);
            return Math.PI*2;
        }else{
            return angle;
        }

    }

    public double modulo2PI(double angle){
        double newAngle = angle;
        if(newAngle> 2* Math.PI){
            while(newAngle > 2* Math.PI){
                newAngle -= 2* Math.PI;
            }

        }else if(newAngle < 0){
            while(newAngle <0){
                newAngle += 2* Math.PI;
            }
        }

        return newAngle;
    }

}

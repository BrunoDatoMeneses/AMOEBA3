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
        //angles[jointIndice] = Math.random()* 2 * Math.PI;
        if(jointIndice == 1){
            angles[jointIndice] = addConstrains(Math.random()*  Math.PI, 2 * Math.PI / 10) ;
        }else{
            angles[jointIndice] = addConstrains(Math.random()* 2 * Math.PI, 2 * Math.PI / 10) ;
        }

        /*if(jointIndice==0){
            angles[jointIndice] = -Math.PI / 2;
        }
        if(jointIndice==1){
            angles[jointIndice] = modulo2PI(angles[jointIndice] +Math.PI / 4);
        }*/
        //System.out.println(jointIndice + " " + angles[jointIndice]);
    }

    private double addConstrains(double angleValue, double limit){
        /*if (Math.PI - limit < angleValue && angleValue < Math.PI){
            return Math.PI - limit;
        }
        else if (Math.PI  < angleValue && angleValue < Math.PI + limit){
            return Math.PI + limit;
        }
        else{
            return angleValue;
        }*/
        return angleValue;
    }

    public void setJointsFromRequest(double[] currentAngles, double[] goalAngles, double variation){

        for(int i=0;i<jointsNumber;i++){



            double difference = modulo2PI(goalAngles[i] ) -currentAngles[i];
            double deltaTheta;

            if(Math.abs(difference)> Math.PI){
                deltaTheta = difference>0 ? - variation :  variation;
            }else{
                deltaTheta = difference>0 ?  variation : - variation;
            }
            //System.out.println(i + " Goal " + goalAngles[i] + " Current " + currentAngles[i] + " diff " + difference + " delta " + deltaTheta);
            currentAngles[i] += deltaTheta;
            //currentAngles[i] = modulo2PI(currentAngles[i] );
            //currentAngles[i] = addConstrains(modulo2PI(currentAngles[i] ), 2 * Math.PI / 10) ;
            currentAngles[i] = goalAngles[i];
            if(currentAngles[i] > Math.PI*2 || currentAngles[i]< 0){
                System.out.println("-----------------\n-------------\n----------------\n----------------- ERROR " + currentAngles[i]);
            }
        }

    }


    private double modulo2PI(double angle){
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

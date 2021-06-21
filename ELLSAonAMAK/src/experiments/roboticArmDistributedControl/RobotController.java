package experiments.roboticArmDistributedControl;

import utils.RAND_REPEATABLE;
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

        //setContinuousSinusoidJoints(jointIndice, cycle, anglesBase, angles);
        setRandomJoints(jointIndice, angles);



        //setRandomJoints(jointIndice, angles);

    }

    private void setContinuousSinusoidJoints(int jointIndice, double cycle, double[] anglesBase, double[] angles) {
        anglesBase[jointIndice] = 10*Math.sin(0.0001* cycle * Math.PI );
        if (jointIndice == 0) angles[jointIndice] = anglesBase[jointIndice] + (7.0/(double)jointsNumber)*Math.sin((0.01)* cycle * Math.PI    +  ((jointsNumber-jointIndice)*0.5) );
        else angles[jointIndice] = (7.0/(double)jointsNumber) * Math.sin(0.01* cycle * Math.PI    +  ((jointsNumber-jointIndice)*0.5) );

        angles[jointIndice] = modulo2PI(angles[jointIndice]);
    }

    private void setRandomJoints(int jointIndice, double[] angles) {
        /*if(Math.random()<0.05) angles[jointIndice] = Math.random()* 2 * Math.PI;
        else angles[jointIndice] = modulo2PI(angles[jointIndice] + ((Math.random() * Math.PI / 16) - (Math.random() *Math.PI / 5)));*/

        //angles[jointIndice] = addConstrains(Math.random()* 2 * Math.PI,Math.PI/4);
        //angles[jointIndice] = modulo2PI(gaussianRandom(0.0,Math.PI/2));

        double gaussianRandomWeightJoint = (jointIndice+1)*(jointIndice+1)/(jointsNumber*jointsNumber);
        //angles[jointIndice] = addConstrains(modulo2PI(( gaussianRandomWeightJoint*gaussianRandom(0.0,Math.PI/(32))) +  ((1-gaussianRandomWeightJoint)*(Math.random()* 2 * Math.PI))), Math.PI/4)  ;
        if(jointIndice==0){
            angles[jointIndice] = RAND_REPEATABLE.random()* 2 * Math.PI;
        }else{
            //double dispersion = Math.PI/2;
            //double dispersion = -jointsNumber*Math.PI/240 + 7*Math.PI/24; //Math.PI/6 for 30 joints and Math.PI/4 for 10 joints
            double dispersion = 2.5593*Math.pow(jointsNumber,-0.479); //Math.PI/6 for 30 joints, Math.PI/4 for 10 joints , Math.PI/2 for 3 joints
            angles[jointIndice] = modulo2PI(gaussianRandom(0.0,dispersion));
        }

        addConstrains(maxMin2PI( angles[jointIndice]), jointIndice);

        /*if(jointIndice == 0){
            angles[jointIndice] = Math.random()* 2 * Math.PI;
            //angles[jointIndice] = Math.random()<0.5 ? Math.random()<0.5 ? Math.PI/4 : 3*Math.PI/4 : Math.random()<0.5 ? Math.PI : 0.0 ;
            //angles[jointIndice] = addConstrains(Math.random()*  Math.PI, 2 * Math.PI / 10) ;
        }else{
            angles[jointIndice] = 3 * Math.PI / 4 + ((Math.random() * Math.PI / 16) - Math.PI / 32);
            //angles[jointIndice] = Math.PI/4 ;
            //angles[jointIndice] = addConstrains(Math.random()* 2*   Math.PI, 2 * Math.PI / 10) ;
            //angles[jointIndice] = addConstrains(Math.random()* 2 * Math.PI, 2 * Math.PI / 10) ;
        }*/

        /*if(jointIndice==0){
            angles[jointIndice] = -Math.PI / 2;
        }
        if(jointIndice==1){
            angles[jointIndice] = modulo2PI(angles[jointIndice] +Math.PI / 4);
        }*/
        //System.out.println(jointIndice + " " + angles[jointIndice]);
    }

    private void setJoints(int jointIndice, double[] angles, double value) {
        angles[jointIndice] = maxMin2PI(value);
    }

    private void setJointsAroundActiveLearningSituation(int jointIndice, double[] angles, double value) {
        angles[jointIndice] = maxMin2PI(value) + (RAND_REPEATABLE.random()-1)*Math.PI/4 ;
    }



    private double gaussianRandom(double mean, double dispersion){
        //java.util.Random r = RAND_REPETABLE.getGeneratorWithoutSeed();
        return  (RAND_REPEATABLE.generator.nextGaussian() * dispersion) + mean;
    }

    private double addConstrains(double angleValue, int indice){
        double limit =  1 * Math.PI / 6;
        if(indice == 0){
            return angleValue;
        }else{
            if (Math.PI - limit < angleValue && angleValue < Math.PI){
                return Math.PI - limit;
            }
            else if (Math.PI  < angleValue && angleValue < Math.PI + limit){
                return Math.PI + limit;
            }
            else{
                return angleValue;
            }
        }

        //return angleValue;
    }

    private double addConstrainsWithLimit(double angleValue, double limit){
        if (Math.PI - limit < angleValue && angleValue < Math.PI){
            return Math.PI - limit;
        }
        else if (Math.PI  < angleValue && angleValue < Math.PI + limit){
            return Math.PI + limit;
        }
        else{
            return angleValue;
        }
        //return angleValue;
    }

    public void setJointsFromRequest(double[] currentAngles, double[] goalAngles, double variation){

        for(int i=0;i<jointsNumber;i++){



            /*double difference = modulo2PI(goalAngles[i] ) -modulo2PI(currentAngles[i]);
            double deltaTheta;

            if(Math.abs(difference)> Math.PI){
                deltaTheta = difference>0 ? - variation :  variation;
            }else{
                deltaTheta = difference>0 ?  variation : - variation;
            }

            currentAngles[i] += deltaTheta;*/




            currentAngles[i] = addConstrains(maxMin2PI( goalAngles[i]), i);



        }

    }

    public double maxMin2PI(double angle){

        if(angle<0.0){
            TRACE.print(TRACE_LEVEL.ERROR,"----------> ERROR " + angle);
            return 0.0;
        }else if(angle>Math.PI*2){
            TRACE.print(TRACE_LEVEL.ERROR,"----------> ERROR " + angle);
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

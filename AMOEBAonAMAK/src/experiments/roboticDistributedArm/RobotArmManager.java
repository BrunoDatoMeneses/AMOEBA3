package experiments.roboticDistributedArm;

import agents.percept.Percept;
import kernel.ELLSA;
import utils.Pair;
import utils.TRACE;
import utils.TRACE_LEVEL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.OptionalDouble;

public class RobotArmManager {

    int jointsNb;
    public int trainingCycles;
    public int requestCycles;
    double[] l;
    double[] joints;
    ELLSA[] ellsas;
    RobotController controller;

    double xPos;
    double yPos;
    double[] poseGoal;
    double[] futurePoseGoal;
    double[] goalAngles;

    int learningCycle;
    int requestCycle;
    double goalErrors;
    ArrayList<Double> allGoalErrors ;
    public OptionalDouble averageError;
    Double errorDispersion;

    ArrayList<Pair<Double,Double>> learnedPositions;
    boolean showSubrequest = false;

    public boolean finished = false;
    public boolean plotRequestError = false;

    public double maxError;
    public int errorRequests = 0;

    Pair<Double,Double>[] starts;
    Pair<Double,Double>[]  ends;

    boolean allRequestsFinished = true;
    int jointIndiceForRequests;

    boolean newGoal = true;
    int requestControlCycles = 5;
    int requestControlCycle;

    public RobotArmManager(int jointsNumber, double[] jointDistances, ELLSA[] els, RobotController robotController, int trainingCycleNb, int requestCycleNb){

        jointsNb = jointsNumber;
        l = jointDistances;
        ellsas = els;
        controller = robotController;
        poseGoal = new double[2];
        futurePoseGoal = new double[2];
        poseGoal[0] = 0.0;
        poseGoal[1] = 0.0;
        futurePoseGoal[0] = 0.0;
        futurePoseGoal[1] = 0.0;
        trainingCycles = trainingCycleNb;
        requestCycles = requestCycleNb;
        learningCycle = 0;
        requestCycle = 0;
        learnedPositions = new ArrayList<>();
        goalErrors = 0.0;
        allGoalErrors = new ArrayList<>();
    }

    public double[] forwardKinematics(double[] jointsAngles, int joint){


        double[] position = new double[3];
        joints = jointsAngles;


        double[][] T = TRZ(0,1) ;

        int i = 2;
        while (i<=joint){

            T = product(T,TRZ(i-1,i));
            i++;
        }

        position[0] = T[0][3];
        position[1] = T[1][3];
        position[2] = T[2][3];



        return position;
    }



    public void learn(double[] jointsAngles){


        double[] position = new double[3];
        joints = jointsAngles;


        double[][] T = TRZ(0,1) ;

        int i = 2;
        while (i<=jointsNb){

            T = product(T,TRZ(i-1,i));
            i++;
        }

        position[0] = T[0][3];
        position[1] = T[1][3];
        position[2] = T[2][3];

        //System.out.println("px " + position[0] + " py " + position[1]);
        if(position[1]>0.0 || true){
            HashMap<String, Double> out = new HashMap<String, Double>();

            double[] anglesToLearn = new double[jointsAngles.length];
            for(int k = 0;k<jointsAngles.length;k++){
                anglesToLearn[k] = angleConvertionForLearning(jointsAngles[k]);
            }

            for(int l=0;l<anglesToLearn.length;l++){


                double result = anglesToLearn[l];

                if(l!=0){
                    out.put("pxOrigin",starts[l].getA());
                    out.put("pyOrigin",starts[l].getB());
                }
                out.put("pxGoal",ends[l].getA());
                out.put("pyGoal",ends[l].getB());



                out.put("oracle",result);
                //System.out.println(out0);
                ellsas[l].learn(out);

                //System.out.println(l + " " + out);
            }


            learningCycle++;
        }



    }




    private double[][] TRZ(int i_1, int i){
        double[][] transformationMatrix = new double[4][4];
        for(int j=0;j<4;j++) {
            for (int k = 0; k < 4; k++) {
                transformationMatrix[j][k]=0.0;
            }
        }
        transformationMatrix[0][0] = Math.cos(joints[i_1]);
        transformationMatrix[1][0] = Math.sin(joints[i_1]);
        transformationMatrix[0][1] = -Math.sin(joints[i_1]);
        transformationMatrix[1][1] = Math.cos(joints[i_1]);
        transformationMatrix[0][3] = l[i_1]*Math.cos(joints[i_1]);
        transformationMatrix[1][3] = l[i_1]*Math.sin(joints[i_1]);
        transformationMatrix[2][2] = 1;
        transformationMatrix[3][3] = 1;


        return transformationMatrix;
    }

    private double[][] product(double[][] m1, double[][] m2){

        double[][] productResult = new double[4][4];

        for(int i=0;i<4;i++){
            for(int j=0;j<4;j++){
                productResult[i][j]=0;
                for(int k=0;k<4;k++)
                {
                    productResult[i][j]+=m1[i][k]*m2[k][j];
                }
            }
        }

        return productResult;

    }

    public HashMap<String, Double> getOutput() {
        HashMap<String, Double> out = new HashMap<String, Double>();



        return out;
    }


    public double[] request(double[] jointsAngles, double[] goalPosition, int cycle){ // TODO

        double[] goalJoints = new double[jointsNb];
        double[] requestJoints = new double[jointsNb];
        Pair<Double,Double>[] workingStarts = new Pair[jointsNb];
        //Pair<Double,Double>[] workingEnds = new Pair[jointsNb];

        joints = jointsAngles;



        workingStarts[0] = new Pair<>(0.0,0.0);


        for(int j = requestJoints.length-1;j>=0;j--){

            HashMap<String, Double> out = new HashMap<String, Double>();
            if(j!=0){
                out.put("pxOrigin",starts[j].getA());
                out.put("pyOrigin",starts[j].getB());
            }
            if(j==requestJoints.length-1){
                out.put("pxGoal",goalPosition[0]);
                out.put("pyGoal",goalPosition[1]);
            }else{
                out.put("pxGoal",ends[j].getA() + (goalPosition[0] - ends[requestJoints.length-1].getA()));
                out.put("pyGoal",ends[j].getB() + (goalPosition[1] - ends[requestJoints.length-1].getB()));
            }

            double angle = ellsas[j].request(out);
            goalJoints[j] = maxMin2PI(angleConvertionForRequest(angle));

            joints[j] = goalJoints[j];

            //System.out.println(i + "\n" + out + "\n" + goalJoints[i]);


            for (int i = 0;i<jointsNb;i++){

                starts[i] = new Pair<>(xPos,yPos);
                double[] position = forwardKinematics(joints,i+1);
                xPos = position[0];
                yPos = position[1];
                ends[i] = new Pair<>(xPos,yPos);




            }

        }


        /*for(int i=0;i<requestJoints.length;i++){

            HashMap<String, Double> out = new HashMap<String, Double>();
            if(i!=0){
                out.put("pxOrigin",workingStarts[i].getA());
                out.put("pyOrigin",workingStarts[i].getB());
            }
            out.put("pxGoal",goalPosition[0]);
            out.put("pyGoal",goalPosition[1]);


            double angle = ellsas[i].request(out);
            goalJoints[i] = maxMin2PI(angleConvertionForRequest(angle));

            //System.out.println(i + "\n" + out + "\n" + goalJoints[i]);

            double[] position = forwardKinematics(goalJoints,i+1);
            double endXPos = position[0];
            double endYPos = position[1];
            //workingEnds[i] = new Pair<>(endXPos,endYPos);
            if(i<requestJoints.length-1){
                workingStarts[i+1] = new Pair<>(endXPos,endYPos);
            }

        }*/



        requestCycle++;



        return goalJoints;
    }

    public double[] indiceRequest(double[] jointsAngles, double[] goalPosition, int cycle,int jointIndice){ // TODO

        double[] goalJoints = new double[jointsNb];
        double[] requestJoints = new double[jointsNb];

        joints = jointsAngles;





        HashMap<String, Double> out = new HashMap<String, Double>();
        if(jointIndice!=0){
            out.put("pxOrigin",starts[jointIndice].getA());
            out.put("pyOrigin",starts[jointIndice].getB());
        }
        if(jointIndice==requestJoints.length-1){

            double xSubGoalVectorFromStart = goalPosition[0] - starts[jointIndice].getA();
            double ySubGoalVectorFromStart = goalPosition[1] - starts[jointIndice].getB();
            double norm = Math.sqrt( Math.pow(xSubGoalVectorFromStart,2) + Math.pow(ySubGoalVectorFromStart,2));
            double ratio = (PARAMS.armBaseSize/jointsNb)/norm;

            out.put("pxGoal",starts[jointIndice].getA() + ratio*xSubGoalVectorFromStart);
            out.put("pyGoal",starts[jointIndice].getB() + ratio*ySubGoalVectorFromStart);
        }else{
            double xSubGoalPos = ends[jointIndice].getA() + (goalPosition[0] - ends[requestJoints.length-1].getA());
            double ySubGoalPos = ends[jointIndice].getB() + (goalPosition[1] - ends[requestJoints.length-1].getB());
            double xSubGoalVectorFromStart = xSubGoalPos - starts[jointIndice].getA();
            double ySubGoalVectorFromStart = ySubGoalPos - starts[jointIndice].getB();
            double norm = Math.sqrt( Math.pow(xSubGoalVectorFromStart,2) + Math.pow(ySubGoalVectorFromStart,2));
            double ratio = (PARAMS.armBaseSize/jointsNb)/norm;

            out.put("pxGoal",starts[jointIndice].getA() + ratio*xSubGoalVectorFromStart);
            out.put("pyGoal",starts[jointIndice].getB() + ratio*ySubGoalVectorFromStart);



        }

        double angle = ellsas[jointIndice].request(out);
        goalJoints[jointIndice] = maxMin2PI(angleConvertionForRequest(angle));

        joints[jointIndice] = goalJoints[jointIndice];

        //System.out.println(i + "\n" + out + "\n" + goalJoints[i]);


        /*for(int i=0;i<requestJoints.length;i++){

            HashMap<String, Double> out = new HashMap<String, Double>();
            if(i!=0){
                out.put("pxOrigin",workingStarts[i].getA());
                out.put("pyOrigin",workingStarts[i].getB());
            }
            out.put("pxGoal",goalPosition[0]);
            out.put("pyGoal",goalPosition[1]);


            double angle = ellsas[i].request(out);
            goalJoints[i] = maxMin2PI(angleConvertionForRequest(angle));

            //System.out.println(i + "\n" + out + "\n" + goalJoints[i]);

            double[] position = forwardKinematics(goalJoints,i+1);
            double endXPos = position[0];
            double endYPos = position[1];
            //workingEnds[i] = new Pair<>(endXPos,endYPos);
            if(i<requestJoints.length-1){
                workingStarts[i+1] = new Pair<>(endXPos,endYPos);
            }

        }*/







        return joints;
    }

    public Pair<Pair<Double,Double>[],Pair<Double,Double>[]> decideAndAct(int cycle, double[] anglesBase, double[] angles){

        xPos = 0.0;
        yPos = 0.0;
        starts = new Pair[jointsNb];
        ends = new Pair[jointsNb];

        if(learningCycle <trainingCycles){


            poseGoal[0] = futurePoseGoal[0] ;
            poseGoal[1] = futurePoseGoal[1] ;

            xPos = 0.0;
            yPos = 0.0;
            for (int i = 0;i<jointsNb;i++){

                controller.setJoint(i, cycle, anglesBase, angles);
                starts[i] = new Pair<>(xPos,yPos);
                double[] position = forwardKinematics(angles,i+1);
                xPos = position[0];
                yPos = position[1];
                ends[i] = new Pair<>(xPos,yPos);




            }


            learnedPositions.add(ends[jointsNb-1]);
            if(learningCycle%50==0)  TRACE.print(TRACE_LEVEL.SUBCYCLE,"LEARNING [" + learningCycle + "] ");
            if(controller.pseudoRandomCounter>0) {
                System.out.println("PSEUDO RANDOM");
                controller.pseudoRandomCounter --;
            }
            learn(angles);



        }else if (requestCycle < requestCycles){
            /*amoebas[0].data.isSelfLearning = false;
            amoebas[1].data.isSelfLearning = false;*/

            //System.out.println("ANGLES BEFORE");
            /*for(int k=0;k<jointsNb;k++){
                //System.out.println(angles[k]);
            }*/

            if(newGoal){
                //System.out.println("NEW GOAL");
                newGoal = false;
                requestControlCycle = 0;
                double randomAngle = Math.random()*Math.PI*2;
                double randomRadius;

                if(jointsNb ==1){
                    randomRadius = PARAMS.armBaseSize;
                }else{
                    randomRadius = Math.random()*(maxError/2);
                }


                poseGoal[0] = randomRadius*Math.cos(randomAngle);
                poseGoal[1] = randomRadius*Math.sin(randomAngle);

                /*int j = (int)(Math.random() * learnedPositions.size());
                poseGoal[0] = learnedPositions.get(j).getA();
                poseGoal[1] = learnedPositions.get(j).getB();*/

                //System.out.println(poseGoal[0] + " " + poseGoal[1]);
                xPos = 0.0;
                yPos = 0.0;
                for (int i = 0;i<jointsNb;i++){

                    controller.setJoint(i, cycle, anglesBase, angles);
                    starts[i] = new Pair<>(xPos,yPos);
                    double[] position = forwardKinematics(angles,i+1);
                    xPos = position[0];
                    yPos = position[1];
                    ends[i] = new Pair<>(xPos,yPos);

                }

            }else{
                xPos = 0.0;
                yPos = 0.0;
                for (int i = 0;i<jointsNb;i++){

                    starts[i] = new Pair<>(xPos,yPos);
                    double[] position = forwardKinematics(angles,i+1);
                    xPos = position[0];
                    yPos = position[1];
                    ends[i] = new Pair<>(xPos,yPos);

                }


                if(allRequestsFinished){
                    if(requestCycle%50==0)  TRACE.print(TRACE_LEVEL.SUBCYCLE,"REQUEST [" + requestCycle + "] ");
                    jointIndiceForRequests = jointsNb-1;
                    allRequestsFinished = false;
                }else{
                    jointIndiceForRequests--;
                }

                //System.out.println(jointIndiceForRequests);
                goalAngles = indiceRequest(angles, poseGoal, cycle,jointIndiceForRequests);

                plotRequestError = true;
                //System.out.println("[" + cycle + "]");
                //System.out.println(poseGoal[0] + " " + poseGoal[1] + " / " + angles[0] + " " + angles[1]  + " -> " + goalAngles[0] + " " + goalAngles[1]);
                controller.setJointsFromRequest(angles, goalAngles, Math.PI/100);
                //System.out.println(poseGoal[0] + " " + poseGoal[1] + " -> " + angles[0] + " " + angles[1] + " <- " + goalAngles[0] + " " + goalAngles[1]);

                xPos = 0.0;
                yPos = 0.0;
                for (int i = 0;i<jointsNb;i++){

                    starts[i] = new Pair<>(xPos,yPos);
                    double[] position = forwardKinematics(angles,i+1);
                    xPos = position[0];
                    yPos = position[1];
                    ends[i] = new Pair<>(xPos,yPos);

                }
                if(jointIndiceForRequests==0){
                //if(jointIndiceForRequests==jointsNb-1){
                    requestControlCycle++;
                    allRequestsFinished = true;

                    if(requestControlCycle == requestControlCycles){
                        requestCycle++;
                        newGoal = true;
                    }

                }

                if(allRequestsFinished){
                    String anglesString = " ";
                    String goalanglesString = " ";
                    for (int i = 0;i<jointsNb;i++){

                        anglesString += angles[i] + " ";
                        goalanglesString += goalAngles[i] + " ";
                    }



                    TRACE.print(TRACE_LEVEL.DEBUG,"[" + cycle + "] " + poseGoal[0] + " " + poseGoal[1] + " -> " +  anglesString + " <- " + goalanglesString + ends[jointsNb-1]);

                    double currentError = 0.0;
                    //double currentError = Math.sqrt( Math.pow(poseGoal[0]-ends[jointsNb-1].getA(),2) +  Math.pow(poseGoal[1]-ends[jointsNb-1].getB(),2))/ Math.sqrt( Math.pow(poseGoal[0],2) +  Math.pow(poseGoal[1],2));
                /*if(PARAMS.dimension == 11){
                    currentError = Math.sqrt( Math.pow(poseGoal[0]-ends[jointsNb-1].getA(),2) +  Math.pow(poseGoal[1]-ends[jointsNb-1].getB(),2))/1000.0;
                }else if(PARAMS.dimension == 4){
                    currentError = Math.sqrt( Math.pow(poseGoal[0]-ends[jointsNb-1].getA(),2) +  Math.pow(poseGoal[1]-ends[jointsNb-1].getB(),2))/500.0;
                }else if(PARAMS.dimension == 3){
                    currentError = Math.sqrt( Math.pow(poseGoal[0]-ends[jointsNb-1].getA(),2) +  Math.pow(poseGoal[1]-ends[jointsNb-1].getB(),2))/360.0;
                }else if(PARAMS.dimension == 2 && PARAMS.nbJoints==1){
                    currentError = Math.sqrt( Math.pow(poseGoal[0]-ends[jointsNb-1].getA(),2) +  Math.pow(poseGoal[1]-ends[jointsNb-1].getB(),2))/360.0;
                }else if(PARAMS.dimension == 2 && PARAMS.nbJoints==2){
                    currentError = Math.sqrt( Math.pow(poseGoal[0]-ends[jointsNb-1].getA(),2))/360.0;
                }*/
                    if(PARAMS.dimension == 2 && PARAMS.nbJoints==2){
                        currentError = Math.sqrt( Math.pow(poseGoal[0]-ends[jointsNb-1].getA(),2))/maxError;
                    }else{
                        currentError = Math.sqrt( Math.pow(poseGoal[0]-ends[jointsNb-1].getA(),2) +  Math.pow(poseGoal[1]-ends[jointsNb-1].getB(),2))/maxError;
                    }


                    TRACE.print(TRACE_LEVEL.INFORM,"ERROR " + currentError + " [" + requestCycle + "]");
                    goalErrors += currentError;
                    allGoalErrors.add(new Double(currentError));
                }
                }



            if(requestCycle == requestCycles-1){
                goalErrors /= requestCycles;
                averageError = allGoalErrors.stream().mapToDouble(a->a).average();
                errorDispersion = allGoalErrors.stream().mapToDouble(a->Math.pow((a-averageError.getAsDouble()),2)).sum();

            }


            //System.out.println("ANGLES AFTER");
            /*for(int k=0;k<jointsNb;k++){
                System.out.println(angles[k]);
            }*/
        }else{
            finished = true;
            TRACE.print(TRACE_LEVEL.ERROR,averageError.getAsDouble() + " [ " + Math.sqrt(errorDispersion/allGoalErrors.size()) + " ]      -    " + goalErrors);
            xPos = 0.0;
            yPos = 0.0;
            for (int i = 0;i<jointsNb;i++){

                controller.setJoint(i, cycle, anglesBase, angles);
                starts[i] = new Pair<>(xPos,yPos);
                double[] position = forwardKinematics(angles,i+1);
                xPos = position[0];
                yPos = position[1];
                ends[i] = new Pair<>(xPos,yPos);

            }

        }



        return new Pair<>(starts, ends);
    }

    public double[] getGoal(){
        return poseGoal;
    }

    private HashMap<String, Double> convertRequestPerceptToString(HashMap<Percept, Double> selfRequest) {
        HashMap<String,Double> newRequest = new HashMap<String,Double>();

        for(Percept pct : selfRequest.keySet()) {
            newRequest.put(pct.getName(), selfRequest.get(pct));
        }
        return newRequest;
    }

    private double angleConvertionForLearning(double value){
        double multilpicator = 100;//*jointsNb/2;
        if(value<Math.PI){
            return ((2*Math.PI) + value)*multilpicator;
        }else{
            return value*multilpicator;
        }
        //return value*multilpicator;

    }

    private double angleConvertionForRequest(double value){
        double multilpicator = 100;//*jointsNb/2;
        if(value/multilpicator>2*Math.PI){
            if(value/multilpicator>3*Math.PI){
                //System.out.println(value/multilpicator);
                errorRequests++;
                return Math.PI;
            }else{
                return controller.modulo2PI(value/multilpicator);
            }
        }else{
            if(value/multilpicator<Math.PI){
                //System.out.println(value/multilpicator);
                errorRequests++;
                return  Math.PI;
            }else{
                return value/multilpicator;
            }

        }
        //return value/multilpicator;
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

}

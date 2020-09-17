package experiments.roboticArm.simulation;

import agents.context.Context;
import agents.head.EndogenousRequest;
import agents.percept.Percept;
import experiments.roboticArm.launchers.PARAMS;
import kernel.ELLSA;
import utils.Pair;
import utils.TRACE;
import utils.TRACE_LEVEL;

import java.util.*;

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
    public double goalErrors;
    public ArrayList<Double> allGoalErrors ;
    public OptionalDouble averageError;
    public Double errorDispersion;

    ArrayList<Pair<Double,Double>> learnedPositions;

    public boolean finished = false;
    public boolean plotRequestError = false;

    public double maxError;
    public int errorRequests = 0;

    public RobotArmManager(int jointsNumber, double[] jointDistances, ELLSA[] ambs, RobotController robotController, int trainingCycleNb, int requestCycleNb){

        jointsNb = jointsNumber;
        l = jointDistances;
        ellsas = ambs;
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


        HashMap<String, Double> out0 = new HashMap<String, Double>();

        double[] anglesToLearn = new double[jointsAngles.length];
        for(int k = 0;k<jointsAngles.length;k++){
            anglesToLearn[k] = angleConvertionForLearning(jointsAngles[k]);
        }

        if(PARAMS.nbJoints>1){


            if(PARAMS.nbJoints>=3){

                double result = anglesToLearn[PARAMS.nbJoints-1];

                out0.put("px",position[0]);
                out0.put("py",position[1]);
                for(int j=1;j<PARAMS.nbJoints-1;j++){
                    out0.put("ptheta"+j,anglesToLearn[j]);
                }
                out0.put("ptheta"+(PARAMS.nbJoints-1),anglesToLearn[0]);

                out0.put("oracle",result);
                //System.out.println(out0);
                ellsas[0].learn(out0);

            }else{

                double result = anglesToLearn[PARAMS.nbJoints-1];


                if(PARAMS.dimension == 3 ){
                    out0.put("px",position[0]);
                    out0.put("py",position[1]);
                }
                else if(PARAMS.dimension == 2){
                    out0.put("px",position[0]);
                }

                out0.put("ptheta0",anglesToLearn[0]);

                out0.put("oracle",result);

                ellsas[0].learn(out0);


                if(controller.pseudoRandomCounter>0){
                    ellsas[0].resetSubrequest();
                }
                EndogenousRequest subVoidRequest= ellsas[0].getSubrequest();
                futurePoseGoal[0] = 0.0;
                futurePoseGoal[1] = 0.0;


            }



        }else{
            double result = anglesToLearn[0];
            out0.put("px",position[0]);
            out0.put("py",position[1]);
            out0.put("oracle",result);
            ellsas[0].learn(out0);
        }


        learningCycle++;
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

        double[][] prodcutResult = new double[4][4];

        for(int i=0;i<4;i++){
            for(int j=0;j<4;j++){
                prodcutResult[i][j]=0;
                for(int k=0;k<4;k++)
                {
                    prodcutResult[i][j]+=m1[i][k]*m2[k][j];
                }
            }
        }

        return prodcutResult;

    }

    public HashMap<String, Double> getOutput() {
        HashMap<String, Double> out = new HashMap<String, Double>();



        return out;
    }


    public double[] request(double[] jointsAngles, double[] goalPosition, int cycle){ // TODO

        double[] goalJoints = new double[jointsNb];
        double[] requestJoints = new double[jointsNb];
        joints = jointsAngles;

        HashMap<String, Double> out0 = new HashMap<String, Double>();
        HashMap<String, Double> out1 = new HashMap<String, Double>();
        HashMap<String, Double> out2 = new HashMap<String, Double>();


        if(PARAMS.nbJoints>1){


            if(PARAMS.nbJoints>=3){


                out2.put("px",goalPosition[0]);
                out2.put("py",goalPosition[1]);
                HashMap<String,Double> actions1 = ellsas[0].requestWithLesserPercepts(out2);
                requestJoints[PARAMS.nbJoints-1] = actions1.get("action");

                for(int j=1;j<PARAMS.nbJoints-1;j++){
                    requestJoints[j] = actions1.get("ptheta"+j);
                }
                requestJoints[0] = actions1.get("ptheta"+(PARAMS.nbJoints-1));
                //System.out.println(actions1);

            }else{
                if(PARAMS.dimension ==3){
                    out2.put("px",goalPosition[0]);
                    out2.put("py",goalPosition[1]);
                }else if (PARAMS.dimension ==2){
                    out2.put("px",goalPosition[0]);
                }



                HashMap<String,Double> actions1 = ellsas[0].requestWithLesserPercepts(out2);


                Context bestContext = ellsas[0].getHeadAgent().getBestContext();
                if(bestContext!=null){
                    TRACE.print(TRACE_LEVEL.DEBUG, "B",bestContext, bestContext.getConfidence());
                    for(Percept pct : ellsas[0].getPercepts()){
                        TRACE.print(TRACE_LEVEL.DEBUG, new ArrayList<>(Arrays.asList(pct.getName(),bestContext.getRanges().get(pct).getStart()+"",bestContext.getRanges().get(pct).getEnd()+"")));
                    }


                }else{
                    TRACE.print(TRACE_LEVEL.DEBUG, new ArrayList<>(Arrays.asList("B",bestContext+"")));

                }

                TRACE.print(TRACE_LEVEL.DEBUG, new ArrayList<>(Arrays.asList("A",""+bestContext, ""+ ellsas[0].getHeadAgent().getActivatedContexts())));
                for(Context ctxt: ellsas[0].getHeadAgent().getActivatedContexts()){
                    TRACE.print(TRACE_LEVEL.DEBUG, new ArrayList<>(Arrays.asList(ctxt.getName(),""+ctxt.getConfidence())));
                }
                TRACE.print(TRACE_LEVEL.DEBUG, new ArrayList<>(Arrays.asList("N", ellsas[0].getHeadAgent().getActivatedNeighborsContexts()+"")));


                requestJoints[1] = actions1.get("action");
                requestJoints[0] = actions1.get("ptheta0");



                TRACE.print(TRACE_LEVEL.DEBUG,"PERCEPTIONS " + out2);
                TRACE.print(TRACE_LEVEL.DEBUG,"ELLSA1");
                TRACE.print(TRACE_LEVEL.DEBUG,"ACTION 0 " + actions1.get("action"));
                TRACE.print(TRACE_LEVEL.DEBUG,"ACTION 1 " + actions1.get("ptheta0"));
                TRACE.print(TRACE_LEVEL.DEBUG,"JOINT 0 " + goalJoints[0]/Math.PI);
                TRACE.print(TRACE_LEVEL.DEBUG,"JOINT 1 " + goalJoints[1]/Math.PI);


            }




        }else{
            out0.put("px",goalPosition[0]);
            out0.put("py",goalPosition[1]);
            requestJoints[0]= ellsas[0].request(out0);
        }



        requestCycle++;

        for(int k = 0;k<jointsNb;k++){
            goalJoints[k] = angleConvertionForRequest(requestJoints[k]);
        }

        return goalJoints;
    }

    public Pair<Pair<Double,Double>[],Pair<Double,Double>[]> decideAndAct(int cycle, double[] anglesBase, double[] angles){

        xPos = 0.0;
        yPos = 0.0;
        Pair<Double,Double>[] starts = new Pair[jointsNb];
        Pair<Double,Double>[]  ends = new Pair[jointsNb];

        if(learningCycle <trainingCycles){

            poseGoal[0] = futurePoseGoal[0] ;
            poseGoal[1] = futurePoseGoal[1] ;

            for (int i = 0;i<jointsNb;i++){

                controller.setJoint(i, cycle, anglesBase, angles);
                starts[i] = new Pair<>(xPos,yPos);
                double[] position = forwardKinematics(angles,i+1);
                xPos = position[0];
                yPos = position[1];
                ends[i] = new Pair<>(xPos,yPos);

            }

            learnedPositions.add(ends[jointsNb-1]);
            if(learningCycle%50==0)  {TRACE.print(TRACE_LEVEL.SUBCYCLE,"LEARNING [ " + learningCycle + " ] ");

            }
            if(controller.pseudoRandomCounter>0) {
                System.out.println("PSEUDO RANDOM");
                controller.pseudoRandomCounter --;
            }
            learn(angles);



        }else if (requestCycle < requestCycles){

            if(cycle%2 == 0){

                double randomAngle = Math.random()*Math.PI*2;
                double randomRadius;

                if(jointsNb ==1){
                    randomRadius = PARAMS.extendedArmLength;
                }else{
                    randomRadius = Math.random()*(maxError/2);
                }

                poseGoal[0] = randomRadius*Math.cos(randomAngle);
                poseGoal[1] = randomRadius*Math.sin(randomAngle);


                for (int i = 0;i<jointsNb;i++){

                    controller.setJoint(i, cycle, anglesBase, angles);
                    starts[i] = new Pair<>(xPos,yPos);
                    double[] position = forwardKinematics(angles,i+1);
                    xPos = position[0];
                    yPos = position[1];
                    ends[i] = new Pair<>(xPos,yPos);

                }

            }else{
                if(requestCycle%50==0)  TRACE.print(TRACE_LEVEL.SUBCYCLE,"EXPLOITATION [" + requestCycle + "] ");

                goalAngles = request(angles, poseGoal, cycle);

                plotRequestError = true;
                controller.setJointsFromRequest(angles, goalAngles, Math.PI/100);


                for (int i = 0;i<jointsNb;i++){

                    starts[i] = new Pair<>(xPos,yPos);
                    double[] position = forwardKinematics(angles,i+1);
                    xPos = position[0];
                    yPos = position[1];
                    ends[i] = new Pair<>(xPos,yPos);

                }
                String anglesString = " ";
                String goalanglesString = " ";
                for (int i = 0;i<jointsNb;i++){

                    anglesString += angles[i] + " ";
                    goalanglesString += goalAngles[i] + " ";
                }


                TRACE.print(TRACE_LEVEL.DEBUG,"[" + cycle + "] " + poseGoal[0] + " " + poseGoal[1] + " -> " +  anglesString + " <- " + goalanglesString + ends[jointsNb-1]);

                double currentError = 0.0;

                if(PARAMS.dimension == 2 && PARAMS.nbJoints==2){
                    currentError = Math.sqrt( Math.pow(poseGoal[0]-ends[jointsNb-1].getA(),2))/maxError;
                }else{
                    currentError = Math.sqrt( Math.pow(poseGoal[0]-ends[jointsNb-1].getA(),2) +  Math.pow(poseGoal[1]-ends[jointsNb-1].getB(),2))/maxError;
                }


                TRACE.print(TRACE_LEVEL.INFORM,"ERROR " + currentError + " [" + requestCycle + "]");
                goalErrors += currentError;
                allGoalErrors.add(new Double(currentError));
            }

            if(requestCycle == requestCycles-1){
                goalErrors /= requestCycles;
                averageError = allGoalErrors.stream().mapToDouble(a->a).average();
                errorDispersion = allGoalErrors.stream().mapToDouble(a->Math.pow((a-averageError.getAsDouble()),2)).sum();

            }
        }else{
            finished = true;
            TRACE.print(TRACE_LEVEL.SUBCYCLE,"GOAL ERROR: "+averageError.getAsDouble() + " [ STANDARD DEVIATION: " + Math.sqrt(errorDispersion/allGoalErrors.size()) + " ]");
            for (int i = 0;i<jointsNb;i++){

                controller.setJoint(i,angles,0.0);
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

}

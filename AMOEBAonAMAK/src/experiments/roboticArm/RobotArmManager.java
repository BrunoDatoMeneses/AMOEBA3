package experiments.roboticArm;

import agents.context.Context;
import agents.head.EndogenousRequest;
import agents.percept.Percept;
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
    double goalErrors;
    ArrayList<Double> allGoalErrors ;
    public OptionalDouble averageError;
    Double errorDispersion;

    ArrayList<Pair<Double,Double>> learnedPositions;
    boolean showSubrequest = false;

    public boolean finished = false;
    public boolean plotRequestError = false;

    public double maxError;

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

        //System.out.println("px " + position[0] + " py " + position[1]);
        if(position[1]>0.0 || true){
            HashMap<String, Double> out0 = new HashMap<String, Double>();
            HashMap<String, Double> out1 = new HashMap<String, Double>();

            double[] anglesToLearn = new double[jointsAngles.length];
            for(int k = 0;k<jointsAngles.length;k++){
                anglesToLearn[k] = angleConvertionForLearning(jointsAngles[k]);
            }

            if(PARAMS.nbJoints>1){


                if(PARAMS.nbJoints==30){

                    double result = anglesToLearn[29];

                    out0.put("px",position[0]);
                    out0.put("py",position[1]);
                    for(int j=1;j<29;j++){
                        out0.put("ptheta"+j,anglesToLearn[j]);
                    }
                    out0.put("ptheta29",anglesToLearn[0]);

                    out0.put("oracle",result);
                    //System.out.println(out0);
                    ellsas[0].learn(out0);

                }else if(PARAMS.nbJoints==10){

                    double result = anglesToLearn[9];

                    out0.put("px",position[0]);
                    out0.put("py",position[1]);
                    out0.put("ptheta1",anglesToLearn[1]);
                    out0.put("ptheta2",anglesToLearn[2]);
                    out0.put("ptheta3",anglesToLearn[3]);
                    out0.put("ptheta4",anglesToLearn[4]);
                    out0.put("ptheta5",anglesToLearn[5]);
                    out0.put("ptheta6",anglesToLearn[6]);
                    out0.put("ptheta7",anglesToLearn[7]);
                    out0.put("ptheta8",anglesToLearn[8]);
                    out0.put("ptheta9",anglesToLearn[0]);

                    out0.put("oracle",result);
                    ellsas[0].learn(out0);

                }else if(PARAMS.nbJoints==3){

                    double result = anglesToLearn[0];

                    out0.put("px",position[0]);
                    out0.put("py",position[1]);
                    out0.put("ptheta1",anglesToLearn[1]);
                    out0.put("ptheta2",anglesToLearn[2]);
                    out0.put("oracle",result);
                    ellsas[0].learn(out0);

                }else{

                    double result = anglesToLearn[0];


                    if(PARAMS.dimension == 3 ){
                        out0.put("px",position[0]);
                        out0.put("py",position[1]);
                    }
                    else if(PARAMS.dimension == 2){
                        out0.put("px",position[0]);
                    }

                    int j = PARAMS.nbJoints-1;
                    int k = 0;
                    while(j>0){
                        out0.put("ptheta"+k,anglesToLearn[j]);
                        j--;
                        k++;
                    }
                    out0.put("oracle",result);


                /*System.err.println(out0.get("px"));
                System.err.println(out0.get("oracle"));
                System.err.println(out0.get("ptheta0"));*/
                    ellsas[0].learn(out0);

                    //ellsas[0].learn(out0, new ArrayList<>(Collections.singleton("ptheta0")));




                    result = anglesToLearn[1];
                    if(PARAMS.dimension == 3 ){
                        out1.put("px",position[0]);
                        out1.put("py",position[1]);
                    }
                    else if(PARAMS.dimension == 2){
                        out1.put("px",position[0]);
                    }

                    j = 0;
                    k = 0;
                    while(j<PARAMS.nbJoints-1){
                        out1.put("ptheta"+k,anglesToLearn[j]);
                        j++;
                        k++;
                    }
                    out1.put("oracle",result);

                    ellsas[1].learn(out1);

                    if(controller.pseudoRandomCounter>0){
                        ellsas[0].resetSubrequest();
                    }
                    EndogenousRequest subVoidRequest= ellsas[0].getSubrequest();
                    futurePoseGoal[0] = 0.0;
                    futurePoseGoal[1] = 0.0;
                    /*if(subVoidRequest!= null){

                        ellsas[0].resetSubrequest();
                        controller.pseudoRandomCounter = 5;
                        System.err.println("------------------------------");
                        System.err.println(subVoidRequest);
                        System.err.println(subVoidRequest.getRequest());
                        HashMap<String, Double> request = convertRequestPerceptToString(subVoidRequest.getRequest());
                        futurePoseGoal[0] = request.get("px");
                        futurePoseGoal[1] = request.get("py");
                        //System.err.println("subVoidRequest " + subVoidRequest);
                        HashMap<String,Double> actions = ellsas[0].requestWithLesserPercepts(request);

                        request.put("ptheta0",actions.get("action"));
                        //double action = ellsas[1].request(request);
                        double[] requestAngles = new double[jointsNb];
                        requestAngles[0] = actions.get("action")/100.0;
                        //requestAngles[1] = action/100.0;
                        requestAngles[1] = actions.get("ptheta0")/100.0;
                        //System.err.println("requestAngles " + requestAngles[0] + " " + requestAngles[1]);
                        controller.activeLearningSituation = requestAngles;


                    }*/

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



    }




    private double[][] TRZ(int i_1, int i){
        double[][] transformationMatrix = new double[4][4];
        for(int j=0;i<4;i++) {
            for (int k = 0; j < 4; j++) {
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


        /*out0.put("px",goalPosition[0]);
        out0.put("py",goalPosition[1]);
        out0.put("ptheta",jointsAngles[1]*100.0);
        goalJoints[0]=amoebas[0].request(out0)/100.0;


        out1.put("px",goalPosition[0]);
        out1.put("py",goalPosition[1]);
        out1.put("ptheta",jointsAngles[0]*100.0);
        goalJoints[1]=amoebas[1].request(out1)/100.0;*/

        //goalJoints[1]=amoebas[1].request(out1)/100.0;
        if(PARAMS.nbJoints>1){


            if(PARAMS.nbJoints==30){


                out2.put("px",goalPosition[0]);
                out2.put("py",goalPosition[1]);
                HashMap<String,Double> actions1 = ellsas[0].requestWithLesserPercepts(out2);
                requestJoints[29] = actions1.get("action");

                for(int j=1;j<29;j++){
                    requestJoints[j] = actions1.get("ptheta"+j);
                }
                requestJoints[0] = actions1.get("ptheta29");
                //System.out.println(actions1);

            }else if(PARAMS.nbJoints==10){


                out2.put("px",goalPosition[0]);
                out2.put("py",goalPosition[1]);
                HashMap<String,Double> actions1 = ellsas[0].requestWithLesserPercepts(out2);
                requestJoints[9] = actions1.get("action");
                requestJoints[1] = actions1.get("ptheta1");
                requestJoints[2] = actions1.get("ptheta2");
                requestJoints[3] = actions1.get("ptheta3");
                requestJoints[4] = actions1.get("ptheta4");
                requestJoints[5] = actions1.get("ptheta5");
                requestJoints[6] = actions1.get("ptheta6");
                requestJoints[7] = actions1.get("ptheta7");
                requestJoints[8] = actions1.get("ptheta8");
                requestJoints[0] = actions1.get("ptheta9");
                //System.out.println(actions1);

            }else if(PARAMS.nbJoints==3){


                out2.put("px",goalPosition[0]);
                out2.put("py",goalPosition[1]);
                HashMap<String,Double> actions1 = ellsas[0].requestWithLesserPercepts(out2);
                requestJoints[0] = actions1.get("action");
                requestJoints[1] = actions1.get("ptheta1");
                requestJoints[2] = actions1.get("ptheta2");

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


                HashMap<String,Double> actions2 = ellsas[1].requestWithLesserPercepts(out2);
                TRACE.print(TRACE_LEVEL.DEBUG, new ArrayList<>(Arrays.asList(actions2.toString())));
                TRACE.print(TRACE_LEVEL.DEBUG,"B", ellsas[1].getHeadAgent().getBestContext());
                TRACE.print(TRACE_LEVEL.DEBUG,"A", ellsas[1].getHeadAgent().getActivatedContexts());
                TRACE.print(TRACE_LEVEL.DEBUG,"N", ellsas[1].getHeadAgent().getActivatedNeighborsContexts());


                requestJoints[0] = actions1.get("action");
                requestJoints[1] = actions1.get("ptheta0");



                TRACE.print(TRACE_LEVEL.DEBUG,"PERCEPTIONS " + out2);
                TRACE.print(TRACE_LEVEL.DEBUG,"ELLSA1");
                TRACE.print(TRACE_LEVEL.DEBUG,"ACTION 0 " + actions1.get("action"));
                TRACE.print(TRACE_LEVEL.DEBUG,"ACTION 1 " + actions1.get("ptheta0"));
                TRACE.print(TRACE_LEVEL.DEBUG,"JOINT 0 " + goalJoints[0]/Math.PI);
                TRACE.print(TRACE_LEVEL.DEBUG,"JOINT 1 " + goalJoints[1]/Math.PI);
                TRACE.print(TRACE_LEVEL.DEBUG,"ELLSA2");
                TRACE.print(TRACE_LEVEL.DEBUG,"ACTION 0 " + actions2.get("ptheta0"));
                TRACE.print(TRACE_LEVEL.DEBUG,"ACTION 1 " + actions2.get("action"));
                TRACE.print(TRACE_LEVEL.DEBUG,"JOINT 0 " + actions2.get("ptheta0")/Math.PI);
                TRACE.print(TRACE_LEVEL.DEBUG,"JOINT 1 " + actions2.get("action")/Math.PI);


            /*goalJoints[1] = actions2.get("action")/100.0;
            goalJoints[0] = actions2.get("ptheta0")/100.0;*/

            /*goalJoints[0] = ((actions1.get("action")/100.0) + (actions2.get("ptheta0")/100.0)) / 2;
            goalJoints[1] = ((actions1.get("ptheta0")/100.0) + (actions2.get("action")/100.0)) / 2;*/
            }




        }else{
            out0.put("px",goalPosition[0]);
            out0.put("py",goalPosition[1]);
            requestJoints[0]= ellsas[0].request(out0);
        }


        for(Context ctxt: ellsas[0].getHeadAgent().getActivatedContexts()){
            TRACE.print(TRACE_LEVEL.ERROR,ctxt.getName(),ctxt.getConfidence(),ctxt.centerDistanceFromExperiment);
        }

        Context bestContext = ellsas[0].getHeadAgent().getBestContext();
        if(bestContext!=null) {
            TRACE.print(TRACE_LEVEL.DEBUG, "B", bestContext, bestContext.getConfidence(), bestContext.centerDistanceFromExperiment());

        }

        //amoebas[0].getHeadAgent().getActivatedContexts().get(0).getLocalModel().getProposition()
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
            if(learningCycle%50==0)  TRACE.print(TRACE_LEVEL.SUBCYCLE,"LEARNING [" + learningCycle + "] ");
            if(controller.pseudoRandomCounter>0) {
                System.out.println("PSEUDO RANDOM");
                controller.pseudoRandomCounter --;
            }
            learn(angles);



        }else if (requestCycle < requestCycles){
            /*amoebas[0].data.isSelfLearning = false;
            amoebas[1].data.isSelfLearning = false;*/

            if(cycle%2 == 0){

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
                for (int i = 0;i<jointsNb;i++){

                    controller.setJoint(i, cycle, anglesBase, angles);
                    starts[i] = new Pair<>(xPos,yPos);
                    double[] position = forwardKinematics(angles,i+1);
                    xPos = position[0];
                    yPos = position[1];
                    ends[i] = new Pair<>(xPos,yPos);

                }

            }else{
                if(requestCycle%50==0)  TRACE.print(TRACE_LEVEL.SUBCYCLE,"REQUEST [" + requestCycle + "] ");

                goalAngles = request(angles, poseGoal, cycle);

                plotRequestError = true;
                //System.out.println("[" + cycle + "]");
                //System.out.println(poseGoal[0] + " " + poseGoal[1] + " / " + angles[0] + " " + angles[1]  + " -> " + goalAngles[0] + " " + goalAngles[1]);
                controller.setJointsFromRequest(angles, goalAngles, Math.PI/100);
                //System.out.println(poseGoal[0] + " " + poseGoal[1] + " -> " + angles[0] + " " + angles[1] + " <- " + goalAngles[0] + " " + goalAngles[1]);


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

            if(requestCycle == requestCycles-1){
                goalErrors /= requestCycles;
                averageError = allGoalErrors.stream().mapToDouble(a->a).average();
                errorDispersion = allGoalErrors.stream().mapToDouble(a->Math.pow((a-averageError.getAsDouble()),2)).sum();

            }
        }else{
            finished = true;
            TRACE.print(TRACE_LEVEL.ERROR,averageError.getAsDouble() + " [ " + Math.sqrt(errorDispersion/allGoalErrors.size()) + " ]      -    " + goalErrors);
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
        double multilpicator = 100*jointsNb/2;
        if(value<Math.PI){
            return ((2*Math.PI) + value)*multilpicator;
        }else{
            return value*multilpicator;
        }
        //return value*multilpicator;

    }

    private double angleConvertionForRequest(double value){
        double multilpicator = 100*jointsNb/2;
        if(value/multilpicator>2*Math.PI){
            if(value/multilpicator>3*Math.PI){
                System.err.println(value/multilpicator);
                return Math.PI;
            }else{
                return controller.modulo2PI(value/multilpicator);
            }
        }else{
            if(value/multilpicator<Math.PI){
                System.err.println(value/multilpicator);
                return  Math.PI;
            }else{
                return value/multilpicator;
            }

        }
        //return value/multilpicator;
    }

}

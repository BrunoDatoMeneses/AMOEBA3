package experiments.roboticArm;

import agents.percept.Percept;
import kernel.AMOEBA;
import utils.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.OptionalDouble;

public class RobotArmManager {

    int jointsNb;
    public int trainingCycles;
    public int requestCycles;
    double[] l;
    double[] joints;
    AMOEBA[] amoebas;
    RobotController controller;

    double xPos;
    double yPos;
    double[] poseGoal;
    double[] goalAngles;

    int learningCycle;
    int requestCycle;
    double goalErrors;
    ArrayList<Double> allGoalErrors ;
    OptionalDouble averageError;
    Double errorDispersion;

    ArrayList<Pair<Double,Double>> learnedPositions;


    public RobotArmManager(int jointsNumber, double[] jointDistances, AMOEBA[] ambs, RobotController robotController, int trainingCycleNb, int requestCycleNb){

        jointsNb = jointsNumber;
        l = jointDistances;
        amoebas = ambs;
        controller = robotController;
        poseGoal = new double[2];
        poseGoal[0] = 0.0;
        poseGoal[1] = 0.0;
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
        HashMap<String, Double> out1 = new HashMap<String, Double>();

        if(PARAMS.nbJoints>1){
            double result = jointsAngles[0];

            out0.put("px",position[0]);
            out0.put("py",position[1]);
            int j = PARAMS.nbJoints-1;
            int k = 0;
            while(j>0){
                out0.put("ptheta"+k,jointsAngles[j]*100.0);
                j--;
                k++;
            }
            out0.put("oracle",result*100.0);

            amoebas[0].learn(out0);

            result = jointsAngles[1];
            out1.put("px",position[0]);
            out1.put("py",position[1]);
            j = 0;
            k = 0;
            while(j<PARAMS.nbJoints-1){
                out1.put("ptheta"+k,jointsAngles[j]*100.0);
                j++;
                k++;
            }
            out1.put("oracle",result*100.0);

            amoebas[1].learn(out1);
        }else{
            double result = jointsAngles[0];
            out0.put("px",position[0]);
            out0.put("py",position[1]);
            out0.put("oracle",result*100.0);
            amoebas[0].learn(out0);
        }


        learningCycle++;

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
            out2.put("px",goalPosition[0]);
            out2.put("py",goalPosition[1]);
            ArrayList<String> otherPercepts = new ArrayList<>(Collections.singleton("ptheta0"));
            HashMap<String,Double> actions = amoebas[0].requestWithLesserPercepts(out2, otherPercepts);
            System.out.println(actions);
            System.out.println("B " + amoebas[0].getHeadAgent().getBestContext());
            System.out.println("A " + amoebas[0].getHeadAgent().getActivatedContexts());
            System.out.println("N " + amoebas[0].getHeadAgent().getActivatedNeighborsContexts());
            goalJoints[0] = actions.get("action")/100.0;
            goalJoints[1] = actions.get("ptheta0")/100.0;
        }else{
            out0.put("px",goalPosition[0]);
            out0.put("py",goalPosition[1]);
            goalJoints[0]=amoebas[0].request(out0)/100.0;
        }



        //amoebas[0].getHeadAgent().getActivatedContexts().get(0).getLocalModel().getProposition()
        requestCycle++;

        return goalJoints;
    }

    public Pair<Pair<Double,Double>[],Pair<Double,Double>[]> decideAndAct(int cycle, double[] anglesBase, double[] angles){

        xPos = 0.0;
        yPos = 0.0;
        Pair<Double,Double>[] starts = new Pair[jointsNb];
        Pair<Double,Double>[]  ends = new Pair[jointsNb];

        if(learningCycle <trainingCycles){




            for (int i = 0;i<jointsNb;i++){

                controller.setJoint(i, cycle, anglesBase, angles);;
                starts[i] = new Pair<>(xPos,yPos);
                double[] position = forwardKinematics(angles,i+1);
                xPos = position[0];
                yPos = position[1];
                ends[i] = new Pair<>(xPos,yPos);




            }
            learnedPositions.add(ends[jointsNb-1]);
            System.out.println("LEARNING [" + learningCycle + "] ");
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
                    double maxDistance = 0.0;
                    for(int i = 0;i<jointsNb;i++){
                        maxDistance+= PARAMS.armBaseSize - (i*20);
                    }
                    randomRadius = Math.random()*maxDistance;
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
                goalAngles = request(angles, poseGoal, cycle);

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


                System.out.println("[" + cycle + "] " + poseGoal[0] + " " + poseGoal[1] + " -> " +  anglesString + " <- " + goalanglesString + ends[jointsNb-1]);

                //double currentError = Math.sqrt( Math.pow(poseGoal[0]-ends[jointsNb-1].getA(),2) +  Math.pow(poseGoal[1]-ends[jointsNb-1].getB(),2))/ Math.sqrt( Math.pow(poseGoal[0],2) +  Math.pow(poseGoal[1],2));
                double currentError = Math.sqrt( Math.pow(poseGoal[0]-ends[jointsNb-1].getA(),2) +  Math.pow(poseGoal[1]-ends[jointsNb-1].getB(),2))/360.0;
                System.out.println("ERROR " + currentError + " [" + requestCycle + "]");
                goalErrors += currentError;
                allGoalErrors.add(new Double(currentError));
            }

            if(requestCycle == requestCycles-1){
                goalErrors /= requestCycles;
                averageError = allGoalErrors.stream().mapToDouble(a->a).average();
                errorDispersion = allGoalErrors.stream().mapToDouble(a->Math.pow((a-averageError.getAsDouble()),2)).sum();

            }
        }else{
            System.out.println(averageError.getAsDouble() + " [ " + Math.sqrt(errorDispersion/allGoalErrors.size()) + " ]      -    " + goalErrors);
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

}

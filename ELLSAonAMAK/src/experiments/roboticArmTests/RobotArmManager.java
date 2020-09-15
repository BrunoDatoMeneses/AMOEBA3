package experiments.roboticArmTests;

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

    public RobotArmManager(int jointsNumber, double[] jointDistances, RobotController robotController, int trainingCycleNb, int requestCycleNb){

        jointsNb = jointsNumber;
        l = jointDistances;
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




    public Pair<Pair<Double,Double>[],Pair<Double,Double>[]> decideAndAct(int cycle, double[] anglesBase, double[] angles){

        xPos = 0.0;
        yPos = 0.0;
        starts = new Pair[jointsNb];
        ends = new Pair[jointsNb];


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


        System.out.println("ANGLES");
        for (int i = 0;i<jointsNb;i++){

            System.out.println(i + " " + (angles[i]/Math.PI));




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

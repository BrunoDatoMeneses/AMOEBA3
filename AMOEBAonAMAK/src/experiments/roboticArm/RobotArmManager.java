package experiments.roboticArm;

import kernel.AMOEBA;
import utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class RobotArmManager {

    int jointsNb;
    public int trainingCycles;
    double[] l;
    double[] joints;
    AMOEBA[] amoebas;
    RobotController controller;

    double xPos;
    double yPos;
    double[] poseGoal;
    double[] goalAngles;


    public RobotArmManager(int jointsNumber, double[] jointDistances, AMOEBA[] ambs, RobotController robotController, int trainingCycleNb){

        jointsNb = jointsNumber;
        l = jointDistances;
        amoebas = ambs;
        controller = robotController;
        poseGoal = new double[2];
        poseGoal[0] = 0.0;
        poseGoal[1] = 0.0;
        trainingCycles = trainingCycleNb;
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


        double result = jointsAngles[0];
        out0.put("px",position[0]);
        out0.put("py",position[1]);
        out0.put("ptheta",jointsAngles[1]*100.0);
        out0.put("oracle",result*100.0);
        amoebas[0].learn(out0);

        result = jointsAngles[1];
        out1.put("px",position[0]);
        out1.put("py",position[1]);
        out1.put("ptheta",jointsAngles[0]*100.0);
        out1.put("oracle",result*100.0);
        amoebas[1].learn(out1);



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


    public double[] request(double[] jointsAngles, double[] goalPosition){ // TODO

        double[] goalJoints = new double[jointsNb];
        joints = jointsAngles;

        HashMap<String, Double> out0 = new HashMap<String, Double>();
        HashMap<String, Double> out1 = new HashMap<String, Double>();



        out0.put("px",goalPosition[0]);
        out0.put("py",goalPosition[1]);
        out0.put("ptheta",jointsAngles[1]*100.0);
        goalJoints[0]=amoebas[0].request(out0)/100.0;


        out1.put("px",goalPosition[0]);
        out1.put("py",goalPosition[1]);
        out1.put("ptheta",jointsAngles[0]*100.0);
        goalJoints[1]=amoebas[1].request(out1)/100.0;


        return goalJoints;
    }

    public Pair<Pair<Double,Double>[],Pair<Double,Double>[]> decideAndAct(int cycle, double[] anglesBase, double[] angles){

        xPos = 0.0;
        yPos = 0.0;
        Pair<Double,Double>[] starts = new Pair[jointsNb];
        Pair<Double,Double>[]  ends = new Pair[jointsNb];

        if(cycle<trainingCycles){
            for (int i = 0;i<jointsNb;i++){

                controller.setJoint(i, cycle, anglesBase, angles);;
                starts[i] = new Pair<>(xPos,yPos);
                double[] position = forwardKinematics(angles,i+1);
                xPos = position[0];
                yPos = position[1];
                ends[i] = new Pair<>(xPos,yPos);

            }

            learn(angles);
        }else{

            if(cycle%150 == 0){
                poseGoal[0] = Math.random() < 0.5 ? 20 + Math.random()*120 : - 20 - Math.random()*120;
                poseGoal[1] = Math.random() < 0.5 ? 20 + Math.random()*120 : - 20 - Math.random()*120;
                System.out.println(poseGoal[0] + " " + poseGoal[1]);
                for (int i = 0;i<jointsNb;i++){

                    controller.setJoint(i, cycle, anglesBase, angles);
                    starts[i] = new Pair<>(xPos,yPos);
                    double[] position = forwardKinematics(angles,i+1);
                    xPos = position[0];
                    yPos = position[1];
                    ends[i] = new Pair<>(xPos,yPos);

                }

            }else{
                goalAngles = request(angles, poseGoal);

                System.out.println("[" + cycle + "]");
                System.out.println(poseGoal[0] + " " + poseGoal[1] + " / " + angles[0] + " " + angles[1]  + " -> " + goalAngles[0] + " " + goalAngles[1]);
                controller.setJointsFromRequest(angles, goalAngles, Math.PI/100);
                System.out.println(poseGoal[0] + " " + poseGoal[1] + " -> " + angles[0] + " " + angles[1] + " <- " + goalAngles[0] + " " + goalAngles[1]);

                for (int i = 0;i<jointsNb;i++){

                    starts[i] = new Pair<>(xPos,yPos);
                    double[] position = forwardKinematics(angles,i+1);
                    xPos = position[0];
                    yPos = position[1];
                    ends[i] = new Pair<>(xPos,yPos);

                }

                System.out.println(poseGoal[0] + " " + poseGoal[1] + " -> " + angles[0] + " " + angles[1] + " <- " + goalAngles[0] + " " + goalAngles[1] + " " + ends[jointsNb-1]);
            }


        }



        return new Pair<>(starts, ends);
    }

    public double[] getGoal(){
        return poseGoal;
    }

}

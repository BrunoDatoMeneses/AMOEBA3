package experiments.roboticArm;

import java.util.Arrays;

public class RobotArmManager {

    int jointsNb;
    double[] l;
    double[] joints;

    public RobotArmManager(int jointsNumber, double[] jointDistances){

        jointsNb = jointsNumber;
        l = jointDistances;
    }

    public double[] forwardKinematics(double[] jointsAngles, int joint){

        System.out.println("JOINT "+ joint);
        double[] position = new double[3];
        joints = jointsAngles;

        System.out.println("TR01");
        double[][] T = TRZ(0,1) ;

        int i = 2;
        while (i<=joint){
            System.out.println("TR"+(i-1)+""+i);
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


}

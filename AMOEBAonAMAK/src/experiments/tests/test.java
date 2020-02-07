package experiments.tests;

import java.util.ArrayDeque;
import java.util.Queue;

public class test {


    public static void main(String[] args) {

        int Min = 0;
        int Max = 1000;
        for(int i=0;i<20;i++)
            System.out.println(Min + (int)(Math.random() * ((Max - Min) + 1)));


    }
}

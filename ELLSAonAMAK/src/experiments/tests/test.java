package experiments.tests;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public class test {

    static void nestedLoopOperation(int[] counters, int[] length, int level) {
        if(level == counters.length) performOperation(counters);
        else {
            for (counters[level] = 0; counters[level] < length[level]; counters[level]++) {
                nestedLoopOperation(counters, length, level + 1);
            }
        }
    }

    static void performOperation(int[] counters) {
        String counterAsString = "";
        for (int level = 0; level < counters.length; level++) {
            counterAsString += "[" + level + "] " +counters[level] ;
            if (counters[level] == 0){
                counterAsString +=  " start";
            }else{
                counterAsString +=  " end";
            }
            if (level < counters.length - 1) counterAsString = counterAsString + ",";
        }
        System.out.println(counterAsString);
    }

    public static void main(String[] args) {

        int depth = 3;
        int[] length = new int[depth];
        int[] counters = new int[depth];
        Arrays.fill(counters,0);
        Arrays.fill(length,2);

        nestedLoopOperation(counters, length, 0);

    }
}

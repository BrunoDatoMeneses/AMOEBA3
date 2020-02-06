package experiments.tests;

import java.util.ArrayDeque;
import java.util.Queue;

public class test {


    public static void main(String[] args) {

        Queue<String> testQueue = new ArrayDeque<>();

        testQueue.add("1");
        testQueue.add("2");
        testQueue.add("3");

        System.out.println(testQueue);
        testQueue.poll();
        System.out.println(testQueue);
        System.out.println(testQueue.size());


    }
}

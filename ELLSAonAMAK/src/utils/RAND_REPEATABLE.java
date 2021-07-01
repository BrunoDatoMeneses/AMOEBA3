package utils;

import fr.irit.smac.amak.tools.Log;

import java.util.ArrayList;
import java.util.Random;

public class RAND_REPEATABLE {

    private static Random generator;
    public static int requestsCounts;
    public static int requestsCountsRandom;
    public static int requestsCountsRandomGauss;
    public static int requestsCountsRandomInt;

    public static void setSeed(long seed){
        requestsCounts = 0;
        requestsCountsRandom = 0;
        requestsCountsRandomGauss = 0;
        requestsCountsRandomInt = 0;
        generator = new Random(seed);
    }

    public static double random() {
        requestsCounts++;
        requestsCountsRandom++;
        //return Math.random(); // NOT REPEATABLE
        double value = generator.nextDouble(); // REPEATABLE
        //System.out.println("nextDouble " + value);
        return value;
    }

    public static double randomGauss() {
        requestsCounts++;
        requestsCountsRandomGauss++;
        //return Math.random(); // NOT REPEATABLE
        double value =  generator.nextGaussian(); // REPEATABLE
        //System.out.println("nextGaussian " + value);
        return value;
    }

    public static int randomInt(int bound) {
        requestsCounts++;
        requestsCountsRandomInt++;
        //return Math.random(); // NOT REPEATABLE
        int value = generator.nextInt(bound); // REPEATABLE
        //System.out.println("nextInt " + value);
        return value;
    }


    public static Random getGeneratorWithoutSeed() {
        //return new Random(); // NOT REPEATABLE
        System.out.println("getGeneratorWithoutSeed " );
        return generator; // REPEATABLE
    }

    public static Random getNewGenerator() {
        return new Random(); // NOT REPEATABLE
    }

    public static void main(String[] args) throws Exception {

        RAND_REPEATABLE.setSeed(0);
        ArrayList<Double> array = new ArrayList<>();
        ArrayList<Double> array2 = new ArrayList<>();

        for(int i = 0; i<100;i++){
            array.add(RAND_REPEATABLE.random())  ;
            array.add(RAND_REPEATABLE.randomGauss())  ;
            array.add((double)RAND_REPEATABLE.randomInt(i+1))  ;

        }

        RAND_REPEATABLE.setSeed(0);
        for(int i = 0; i<100;i++){
            array2.add(RAND_REPEATABLE.random())  ;
            array2.add(RAND_REPEATABLE.randomGauss())  ;
            array2.add((double)RAND_REPEATABLE.randomInt(i+1))  ;

        }

        System.out.println(requestsCounts);
    }

}

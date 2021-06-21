package utils;

import java.util.Random;

public class RAND_REPEATABLE {

    public static Random generator;

    public static void setSeed(long seed){
        generator = new Random(seed);
    }

    public static double random() {
        //return Math.random(); // NOT REPEATABLE
        return generator.nextDouble(); // REPEATABLE
    }


    public static Random getGeneratorWithoutSeed() {
        //return new Random(); // NOT REPEATABLE
        return generator; // REPEATABLE
    }

    public static Random getNewGenerator() {
        return new Random(); // NOT REPEATABLE
    }


}

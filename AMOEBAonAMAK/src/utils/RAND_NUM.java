package utils;

public class RAND_NUM {

    public static int randInt(int min, int max){
        return min + (int)(Math.random() * ((max - min) + 1));
    }

    public static boolean oneChanceIn(int chances){
        return randInt(0,chances)<1;
    }
}

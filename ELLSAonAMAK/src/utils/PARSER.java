package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PARSER {
    public static ArrayList<String> convertStringToArraylist(String listOfPercepts){
        ArrayList<String> endList = new ArrayList<>(Arrays.asList(listOfPercepts.split(" ")));
    return endList;
    }

    public static void main(String[] args) {
        System.out.println(convertStringToArraylist("px py pz"));
    }
}

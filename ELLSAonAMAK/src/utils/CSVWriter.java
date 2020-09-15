package utils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class CSVWriter {

    PrintWriter pw;


    public CSVWriter(String folder, String name){
        try {
            pw = new PrintWriter("XP/" + name + ".csv");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // experimentation on calculus
    public CSVWriter(String name){
        try {
            pw = new PrintWriter("/home/daavve/Documents/XP/" + name + ".csv");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void write(Double f1, Double f2){
        pw.println(f1.toString() + ";" + f2.toString());
    }

    public void write(Double f1, Double f2, Double f3){
        pw.println(f1.toString() + ";" + f2.toString() + ";" + f3.toString());
    }

    public void write(Double f1, Double f2, Double f3, Double f4){
        pw.println(f1.toString() + ";" + f2.toString() + ";" + f3.toString() + ";" + f4.toString());
    }

    public void write(ArrayList<String> list){
        String msg = "";
        for(String element : list){
            msg += element + ";";
        }
        pw.println(msg);
    }

    /*public void write(ArrayList<Double> list){
        String msg = "";
        for(Double element : list){
            msg += element.toString() + ";";
        }
        pw.println(msg);
    }*/

    public void close(){
        pw.close();
    }

    public static void main(String[] args) {
        String date = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        CSVWriter test = new CSVWriter("08062020","test");

        test.write(0.0000d, 0.0045d);
        test.write(0.0000d, 0.0045d);
        test.write(new ArrayList<>(Arrays.asList("sds","54654","dfdf5454")));



        test.close();
    }

}

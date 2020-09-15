package utils;

import java.util.ArrayList;
import java.util.List;

public class TRACE {

    public static TRACE_LEVEL minLevel = TRACE_LEVEL.ERROR;

    /*public static void print(TRACE_LEVEL lvl, ArrayList<String> infos) {
        if (lvl.isGE(minLevel)) {
            String message="";

            for(String info : infos) {
                message += " " + info;
            }
            if(minLevel == TRACE_LEVEL.ERROR){
                System.err.println(message);
            }else{
                System.out.println(message);
            }

        }

    }
*/
    public static void print(TRACE_LEVEL lvl, Object... infos) {
        if (lvl.isGE(minLevel)) {
            String message="";

            for(Object info : infos) {
                message += " " + info.toString();
            }
            if(lvl == TRACE_LEVEL.ERROR){
                System.err.println(message);
            }else{
                System.out.println(message);
            }

        }

    }



}

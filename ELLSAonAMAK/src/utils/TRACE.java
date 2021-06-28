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

    public static TRACE_LEVEL convertFromString(String state) {
        switch (state){
            case "OFF":
                return TRACE_LEVEL.OFF;
            case "ERROR":
                return TRACE_LEVEL.ERROR;
            case "SUBCYCLE":
                return TRACE_LEVEL.SUBCYCLE;
            case "CYCLE":
                return TRACE_LEVEL.CYCLE;
            case "NCS":
                return TRACE_LEVEL.NCS;
            case "EVENT":
                return TRACE_LEVEL.EVENT;
            case "STATE":
                return TRACE_LEVEL.STATE;
            case "INFORM":
                return TRACE_LEVEL.INFORM;
            case "DEBUG":
                return TRACE_LEVEL.DEBUG;
            default:
                return TRACE_LEVEL.ERROR;
        }
    }
}

package agents.context;

import agents.percept.Percept;
import utils.Pair;

import java.util.HashMap;

public class VOID {

    public HashMap<Percept, Pair<Double, Double>> bounds;


    public VOID( HashMap<Percept, Pair<Double, Double>> zoneBounds){

        bounds = zoneBounds;

    }


}



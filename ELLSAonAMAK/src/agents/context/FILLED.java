package agents.context;

import agents.percept.Percept;
import utils.Pair;

import java.util.HashMap;

public class FILLED {

    HashMap<Percept, Pair<Double, Double>> bounds;


    public FILLED(HashMap<Percept, Pair<Double, Double>> zoneBounds){

        bounds = zoneBounds;
    }


}



package experiments.tests;

import agents.context.Context;
import agents.context.Range;
import agents.context.VOID;
import agents.percept.Percept;
import experiments.nDimensionsLaunchers.F_N_Manager;
import fr.irit.smac.amak.Configuration;
import javafx.application.Application;
import javafx.stage.Stage;
import kernel.ELLSA;
import kernel.StudiedSystem;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import utils.Pair;
import utils.TRACE_LEVEL;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class TestVoidDetectionFromZone extends Application implements Serializable {

    public static final double oracleNoiseRange = 0.5;
    public static final double learningSpeed = 0.01;
    public static final int regressionPoints = 100;
    public static final int dimension = 2;
    public static final double spaceSize = 50.0	;
    public static final int nbOfModels = 2	;
    public static final int normType = 2	;
    public static final boolean randomExploration = true;
    public static final boolean limitedToSpaceZone = true;
    //public static final double mappingErrorAllowed = 0.07; // BIG SQUARE
    public static double mappingErrorAllowed = 0.05; // MULTI
    public static final double explorationIncrement = 1.0	;
    public static final double explorationWidht = 0.5	;
    public static final boolean setActiveLearning = true	;
    public static final boolean setSelfLearning = false	;
    public static final int nbCycle = 1000;

    public static final boolean setConflictDetection = true ;
    public static final boolean setConcurrenceDetection = true ;
    public static final boolean setVoidDetection = true ;

    public static final boolean setConflictResolution = true ;
    public static final boolean setConcurrenceResolution = true ;

    public static void main(String[] args) throws IOException {


        Application.launch(args);


    }

    @Override
    public void start(Stage arg0) throws Exception {


        // Set AMAK configuration before creating an AMOEBA
        Configuration.multiUI=true;
        Configuration.commandLineMode = true;
        Configuration.allowedSimultaneousAgentsExecution = 1;
        Configuration.waitForGUI = false;
        Configuration.plotMilliSecondsUpdate = 20000;

        ELLSA ellsa = new ELLSA(null,  null);
        StudiedSystem studiedSystem = new F_N_Manager(spaceSize, dimension, nbOfModels, normType, randomExploration, explorationIncrement,explorationWidht,limitedToSpaceZone, oracleNoiseRange);
        ellsa.setStudiedSystem(studiedSystem);
        IBackupSystem backupSystem = new BackupSystem(ellsa);
        File file = new File("resources/threeDimensionsLauncher.xml");
        backupSystem.load(file);


        ellsa.allowGraphicalScheduler(false);
        ellsa.setRenderUpdate(false);
        ellsa.data.PARAM_learningSpeed = learningSpeed;
        ellsa.data.PARAM_numberOfPointsForRegression_ASUPPRIMER = regressionPoints;
        ellsa.data.PARAM_isActiveLearning = setActiveLearning;
        ellsa.data.PARAM_isSelfLearning = setSelfLearning;
        ellsa.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);

        ellsa.setRenderUpdate(false);

        ellsa.getEnvironment().PARAM_minTraceLevel = TRACE_LEVEL.ERROR;



        double tailleCtxt = 10.0;

        HashMap<Percept, Range> manualRanges = new HashMap<>();
        HashMap<Percept, Range> manualRanges1 = new HashMap<>();
        HashMap<Percept, Range> manualRanges2 = new HashMap<>();
        HashMap<Percept, Range> manualRanges3 = new HashMap<>();

        HashMap<Percept, Pair<Double, Double>> zoneBounds =  new HashMap<>();
        for (Percept p : ellsa.getPercepts()) {
            p.setMax(tailleCtxt*10);
            p.setMin(-tailleCtxt*10);
            Range r = new Range(ellsa, -tailleCtxt, tailleCtxt, 0, true, true, p);
            manualRanges.put(p,r);

            Range r1 = new Range(ellsa, -tailleCtxt-20, tailleCtxt-20, 0, true, true, p);
            manualRanges1.put(p,r1);
            Range r2 = new Range(ellsa, -tailleCtxt, tailleCtxt, 0, true, true, p);
            manualRanges2.put(p,r2);
            Range r3 = new Range(ellsa, -tailleCtxt+20, tailleCtxt+20, 0, true, true, p);
            manualRanges3.put(p,r3);

            zoneBounds.put(p, new Pair<>(-3*tailleCtxt, 3*tailleCtxt));

        }

        Context ctxt = new Context(ellsa,manualRanges);
        Context ctxt1 = new Context(ellsa,manualRanges1);
        Context ctxt2 = new Context(ellsa,manualRanges2);
        Context ctxt3 = new Context(ellsa,manualRanges3);

        ArrayList<Context> contexts = new ArrayList<>();
        contexts.add(ctxt1);
        System.out.println(ctxt1.getRanges());
        contexts.add(ctxt2);
        System.out.println(ctxt2.getRanges());
        contexts.add(ctxt3);
        System.out.println(ctxt3.getRanges());
        ArrayList<Percept> computedPercepts = new ArrayList<>();

        System.out.println(zoneBounds);


        ArrayList<VOID> voids = ctxt.getVoidsFromZone(zoneBounds, computedPercepts);
        System.out.println("1CTXT MIDDLE " + voids.size());
        for(VOID voidDetected : voids){
            System.out.println(voidDetected.bounds);
        }

        ArrayList<VOID> voids1 = ctxt1.getVoidsFromZone(zoneBounds, computedPercepts);
        System.out.println("1CTXT SIDE " + voids1.size());
        for(VOID voidDetected : voids1){
            System.out.println(voidDetected.bounds);
        }

        ArrayList<VOID> currentVoids = getVoidsFromContextsAndZone(zoneBounds, contexts);

        System.out.println("3CTXT  " + currentVoids.size());
        for(VOID voidDetected : currentVoids){
            System.out.println(voidDetected.bounds);
        }
    }

    private ArrayList<VOID> getVoidsFromContextsAndZone(HashMap<Percept, Pair<Double, Double>> zoneBounds, ArrayList<Context> contexts) {
        ArrayList<VOID> currentVoids = new ArrayList<>();
        currentVoids.add(new VOID(zoneBounds));

        for(Context testedCtxt : contexts){

            ArrayList<VOID> newVoids = new ArrayList<>();
            for(VOID currentVoid : currentVoids){

                ArrayList<Percept> computedPerceptsInit = new ArrayList<>();
                ArrayList<VOID> voidsToAdd = testedCtxt.getVoidsFromZone(currentVoid.bounds, computedPerceptsInit);
                newVoids.addAll(voidsToAdd);

            }
            currentVoids = newVoids;

        }
        return currentVoids;
    }

}

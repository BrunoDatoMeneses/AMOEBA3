package experiments.tests;

import agents.context.Context;
import agents.context.Experiment;
import agents.context.Range;
import agents.percept.Percept;
import experiments.nDimensionsLaunchers.F_N_Manager;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.ui.VUIMulti;
import gui.EllsaMultiUIWindow;
import javafx.application.Application;
import javafx.stage.Stage;
import kernel.ELLSA;
import kernel.StudiedSystem;
import kernel.World;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import kernel.backup.SaveHelperImpl;
import utils.TRACE_LEVEL;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class TestVoidDetectionFromZoneUI extends Application implements Serializable {

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
    public static final boolean setVoidDetection = false ;
    public static final boolean setVoidDetection2 = true ;

    public static final boolean setConflictResolution = true ;
    public static final boolean setConcurrenceResolution = true ;

    public static void main(String[] args) throws IOException {


        Application.launch(args);


    }

    @Override
    public void start(Stage arg0) throws Exception {


        // Set AMAK configuration before creating an AMOEBA
        Configuration.multiUI=true;
        Configuration.commandLineMode = false;
        Configuration.allowedSimultaneousAgentsExecution = 1;
        Configuration.waitForGUI = true;
        Configuration.plotMilliSecondsUpdate = 20000;

        VUIMulti amoebaVUI = new VUIMulti("2D");
        EllsaMultiUIWindow amoebaUI = new EllsaMultiUIWindow("ELLSA", amoebaVUI, null);
        ELLSA ellsa = new ELLSA(amoebaUI,  amoebaVUI);
        StudiedSystem studiedSystem = new F_N_Manager(spaceSize, dimension, nbOfModels, normType, randomExploration, explorationIncrement,explorationWidht,limitedToSpaceZone, oracleNoiseRange);
        ellsa.setStudiedSystem(studiedSystem);
        IBackupSystem backupSystem = new BackupSystem(ellsa);
        File file = new File("resources/twoDimensionsLauncher.xml");
        backupSystem.load(file);

        ellsa.saver = new SaveHelperImpl(ellsa, amoebaUI);

        ellsa.allowGraphicalScheduler(true);
        ellsa.setRenderUpdate(false);
        ellsa.data.learningSpeed = learningSpeed;
        ellsa.data.numberOfPointsForRegression = regressionPoints;
        ellsa.data.isActiveLearning = setActiveLearning;
        ellsa.data.isSelfLearning = setSelfLearning;
        ellsa.data.isConflictDetection = setConflictDetection;
        ellsa.data.isConcurrenceDetection = setConcurrenceDetection;
        ellsa.data.isVoidDetection = setVoidDetection;
        ellsa.data.isVoidDetection2 = setVoidDetection2;
        ellsa.data.isConflictResolution = setConflictResolution;
        ellsa.data.isConcurrenceResolution = setConcurrenceResolution;
        ellsa.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
        World.minLevel = TRACE_LEVEL.DEBUG;



        double tailleCtxt = 10.0;


        HashMap<Percept, Range> manualRanges1 = new HashMap<>();
        HashMap<Percept, Range> manualRanges2 = new HashMap<>();
        HashMap<Percept, Range> manualRanges3 = new HashMap<>();


        for (Percept p : ellsa.getPercepts()) {
            p.setMax(tailleCtxt*10);
            p.setMin(-tailleCtxt*10);


            Range r1 = new Range(ellsa, -tailleCtxt-20, tailleCtxt-20, 0, true, true, p);
            manualRanges1.put(p,r1);
            Range r2 = new Range(ellsa, -tailleCtxt, tailleCtxt, 0, true, true, p);
            manualRanges2.put(p,r2);
            Range r3 = new Range(ellsa, -tailleCtxt+20, tailleCtxt+20, 0, true, true, p);
            manualRanges3.put(p,r3);



            p.setMin(-15*tailleCtxt);
            p.setMax(15*tailleCtxt);
        }



        ellsa.onSystemCycleBegin();

        //Context ctxt1 = new Context(amoeba,manualRanges1);
        Context ctxt2 = new Context(ellsa,manualRanges2);
        //Context ctxt3 = new Context(amoeba,manualRanges3);


        ArrayList<Context> contexts = new ArrayList<>();
        //contexts.add(ctxt1);
        contexts.add(ctxt2);
        //contexts.add(ctxt3);


        ellsa.getHeadAgent().activatedNeighborsContexts = contexts;
        ellsa.getHeadAgent().NCSDetection_PotentialRequest();
        ellsa.getHeadAgent().testIfrequest();

        ellsa.computePendingAgents();
        ellsa.onSystemCycleEnd();
        ellsa.renderUI();






    }

    private Experiment getExperiment(ELLSA ellsa, Context ctxt1) {
        ArrayList<Percept> percepts = ellsa.getPercepts();
        Experiment exp = new Experiment(ctxt1);
        for (Percept pct : percepts) {
            exp.addDimension(pct, pct.getValue());
        }
        exp.setProposition(0.0);

        return exp;
    }


}

package experiments.roboticArm;

import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.ui.AmasMultiUIWindow;
import fr.irit.smac.amak.ui.VUIMulti;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class RobotLaunchExampleMultiUI extends Application{

	
	public static void main (String[] args) {
		
		
		Application.launch(args);
		
	
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

	    try{
            Configuration.multiUI=true;
            Configuration.commandLineMode =false;


            int jointsNb = 4;
            AmasMultiUIWindow window = new AmasMultiUIWindow("Robot Arm");
            WorldExampleMultiUI env = new WorldExampleMultiUI(window);
            VUIMulti vui = new VUIMulti("Robot");


            double distances[] = new double[jointsNb];
            for(int i = 0;i<jointsNb;i++){
                distances[i] = 100 - (i*20);
            }
            RobotArmManager robotArmManager = new RobotArmManager(jointsNb, distances);
            RobotController robotController = new RobotController(jointsNb);
            RobotWorlExampleMultiUI robot = new RobotWorlExampleMultiUI(window, vui, env, robotController, robotArmManager, jointsNb);

            //startTask(ants, 500, 10);
        }catch (Exception e){
            System.out.println(e);
        }

			
	}
	
	public void startTask(RobotWorlExampleMultiUI amas, long wait, int cycles)
    {
        // Create a Runnable
        Runnable task = new Runnable()
        {
            public void run()
            {
                runTask(amas, wait, cycles);
            }
        };
 
        // Run the task in a background thread
        Thread backgroundThread = new Thread(task);
        // Terminate the running thread if the application exits
        backgroundThread.setDaemon(true);
        // Start the thread
        backgroundThread.start();
    }
	
	public void runTask(RobotWorlExampleMultiUI amas, long wait, int cycles)
    {
        for(int i = 0; i < cycles; i++) 
        {
            try
            {
                // Get the Status
                final String status = "Processing " + i + " of " + cycles;
                 
                // Update the Label on the JavaFx Application Thread        
                Platform.runLater(new Runnable() 
                {
                    @Override
                    public void run() 
                    {
                    	amas.cycle();
                    	System.out.println(status);
                    }
                });
         
                Thread.sleep(wait);
            }
            catch (InterruptedException e) 
            {
                e.printStackTrace();
            }
        }
    }   

	
	@Override
	public void stop() throws Exception {
		super.stop();
		System.exit(0);
	}
}

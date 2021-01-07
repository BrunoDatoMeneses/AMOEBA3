package experiments.tests.gaussianPlot;

import fr.irit.smac.amak.Agent;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.ui.AmasMultiUIWindow;
import fr.irit.smac.amak.ui.drawables.DrawableCircle;
import fr.irit.smac.amak.ui.drawables.DrawableLine;
import javafx.application.Platform;
import utils.Pair;

public class RobotExampleMutliUI extends Agent<RobotWorlExampleMultiUI, WorldExampleMultiUI> {



	/**
	 * Constructor of the ant
	 * 
	 * @param amas
	 *            the amas the ant belongs to
	 * @param startX
	 *            Initial X coordinate
	 * @param startY
	 *            Initial Y coordinate
	 */
	public RobotExampleMutliUI(AmasMultiUIWindow window, RobotWorlExampleMultiUI amas, double startX, double startY) {
		super(window, amas, startX, startY);


	}
	@Override
	public void onInitialization() {





	}

	@Override
	protected void onRenderingInitialization() {

		for(int i=1;i<=20;i++){
			double  rangeLength = Math.pow((double)i/5,4);
			getAmas().getVuiErrorDispersion().createAndAddRectangle(-rangeLength/2,-(2*i+1)*2,rangeLength,1);
		}


	}

	/**
	 * Move in a random direction
	 */
	@Override
	protected void onDecideAndAct() {


		// TODO ici






	}



	@Override
	public void onUpdateRender() {

		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{

				double rangeCenter = 0;
				double rangeLength = 1;
				java.util.Random r = new java.util.Random();
				double ramdomGaussianPosition;
				for(int i=1;i<=20;i++){
					rangeLength = Math.pow((double)i/5,4);
					ramdomGaussianPosition = (r.nextGaussian() * Math.pow((rangeLength/(2*30)),1)) + rangeCenter;

					//ramdomGaussianPosition = Math.pow(r.nextGaussian() , Math.pow((rangeLength/10),2));
					getAmas().getVuiErrorDispersion().createAndAddCircle(ramdomGaussianPosition, -(2*i)*2,0.001);
				}




			}
		});



	}



	public double randomGaussian(double mean, double stdDev){
		java.util.Random rand = new java.util.Random(); //reuse this if you are generating many
		double u1 = 1.0-rand.nextDouble(); //uniform(0,1] random doubles
		double u2 = 1.0-rand.nextDouble();
		double randStdNormal = Math.sqrt (-2.0 * Math.log(u1)) *
				Math.sin(2.0 * Math.PI * u2); //random normal(0,1)

		return mean + stdDev * randStdNormal; //random normal(mean,stdDev^2)
	}

	public double randomGaussianNorm(){
		java.util.Random rand = new java.util.Random(); //reuse this if you are generating many
		double u1 = 1.0-rand.nextDouble(); //uniform(0,1] random doubles
		double u2 = 1.0-rand.nextDouble();

		return Math.sqrt (-2.0 * Math.log(u1)) * Math.sin(2.0 * Math.PI * u2); //random normal(0,1)
	}
}

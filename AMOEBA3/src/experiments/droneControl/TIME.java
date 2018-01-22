package experiments.droneControl;

public class TIME {
	
	private long previousTime;
	
	public TIME(){
		previousTime = System.currentTimeMillis();
	}
	
	public long getCurrentTime(){
		return System.currentTimeMillis();
	}
	
	public double getTimeDelta(){
		double deltaTime = System.currentTimeMillis() - previousTime;
		previousTime = System.currentTimeMillis();
		return deltaTime;
	}
	
}

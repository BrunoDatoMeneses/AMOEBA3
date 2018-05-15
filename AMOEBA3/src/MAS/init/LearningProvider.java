package MAS.init;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.FutureTask;

import MAS.kernel.AMOEBA;

// TODO: Auto-generated Javadoc
/**
 * The Class LearningProvider.
 */
public class LearningProvider implements Runnable {
	
	/** The amoeba. */
	private AMOEBA amoeba;
	
	/** The outputs list. */
	private List<ManagerOutput> outputsList = Collections.synchronizedList(new ArrayList<ManagerOutput>());
	
	/** The bufferization. */
	private boolean bufferization = false;
	
	/** The start thread. */
	private boolean startThread = false;
	
	/** The first learn. */
	private boolean firstLearn = false;
	
	/** The lock. */
	private Object lock = new Object();
	
	/** The request lock. */
	private boolean requestLock = false;
	
	/**
	 * Sets the amoeba.
	 *
	 * @param amoeba the new amoeba
	 */
	public void setAMOEBA(AMOEBA amoeba) {
		this.amoeba = amoeba;
	}
	
	/**
	 * Sets the bufferization.
	 *
	 * @param bufferization the new bufferization
	 */
	public void setBufferization(boolean bufferization) {
		this.bufferization = bufferization;
	}
	
	/**
	 * Learn request.
	 *
	 * @param actions the actions
	 */
	public void learnRequest(HashMap<String, Double> actions) {	
		if (!bufferization) {
			amoeba.learn(actions);
		} else {
			outputsList.add(new ManagerOutput(actions, "learn"));
			if (!startThread) {
				(new Thread(this)).start();
				startThread = true;
			}
		}	
	}
	
	/**
	 * Result request.
	 *
	 * @param actions the actions
	 * @return the double
	 */
	public double resultRequest(HashMap<String, Double> actions) {
		if (!bufferization) {
			return amoeba.request(actions);
		} else {
			while (!firstLearn) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
			}
			requestLock = true;
			double d;
			synchronized(lock) {
				d = amoeba.request(actions);
				requestLock = false;
				lock.notify();
			}	
			return d;
		}
		
	}
	
	/**
	 * Lazy request.
	 *
	 * @param actions the actions
	 * @return the future task
	 */
	public FutureTask<Double> lazyRequest(HashMap<String, Double> actions) {
		RequestCallable requestCallable = new RequestCallable(actions);
		requestCallable.setAMOEBA(amoeba);
		FutureTask<Double> futureValue = new FutureTask<Double>(requestCallable);
		ManagerOutput managerOutput = new ManagerOutput(actions, "request");
		managerOutput.setFutureTask(futureValue);
		outputsList.add(managerOutput);
		return futureValue;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("***** Call Starts *****");
		//int count = 1;
		
		synchronized (lock) {
			while( outputsList.size() > 0 ) {
				
				synchronized(outputsList) {
					//System.out.println("+++++Start Learn " + count + " +++++");
					//System.out.println("-----Size " + outputsList.size() + " -----");
					HashMap<String, Double> out = outputsList.get(0).getActions();
					String type = outputsList.get(0).getType();
					if (type.equals("request")) {
						Thread thread = new Thread(outputsList.get(0).getFutureTask());
						thread.start();
						while(thread.isAlive()) {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}	
						}
						/*double val = amoeba.request(out);
						requestResList.add(val);
						System.out.println("Output value : " + val);*/
					} else {
						
						amoeba.learn(out);
						firstLearn = true;
					}
					
					outputsList.remove(0);
					//outputsList.remove(0);
					//System.out.println("----------End Learn: " + count + "----------");
				}
				
				while (requestLock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				// Condition in case the list is empty while the execution is in progress
				if (outputsList.size() == 0) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}	
				}		
				//count++;	
			}
			
		}
		
		System.out.println("***** Call Ends *****");
		
	}

}

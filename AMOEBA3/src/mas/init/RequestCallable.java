package mas.init;

import java.util.HashMap;
import java.util.concurrent.Callable;

import mas.kernel.AMOEBA;

// TODO: Auto-generated Javadoc
/**
 * The Class RequestCallable.
 */
public class RequestCallable implements Callable<Double> {

	/** The amoeba. */
	private AMOEBA amoeba;
	
	/** The actions. */
	private HashMap<String, Double> actions;
	
	/**
	 * Instantiates a new request callable.
	 *
	 * @param actions the actions
	 */
	public RequestCallable(HashMap<String, Double> actions) {
		this.actions = actions;
	}
	
	/**
	 * Sets the amoeba.
	 *
	 * @param amoeba the new amoeba
	 */
	public void setAMOEBA(AMOEBA amoeba) {
		this.amoeba = amoeba;
	}
	
	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Double call() throws Exception {
		// TODO Auto-generated method stub
		return amoeba.request(actions);
	}

}

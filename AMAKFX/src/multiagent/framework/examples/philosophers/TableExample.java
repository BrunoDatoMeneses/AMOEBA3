package multiagent.framework.examples.philosophers;

import multiagent.framework.Environment;
import multiagent.framework.Scheduling;

public class TableExample extends Environment {
	public TableExample(Object...params) {
		super(Scheduling.HIDDEN, params);
	}

	private ForkExample[] forks;

	@Override
	public void onInitialization() {
		// Set 10 forks on the table
		forks = new ForkExample[10];
		for (int i = 0; i < forks.length; i++)
			forks[i] = new ForkExample();
	}

	public ForkExample[] getForks() {
		return forks;
	}
}

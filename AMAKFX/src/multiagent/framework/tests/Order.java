package multiagent.framework.tests;

import multiagent.framework.Amas;
import multiagent.framework.Configuration;
import multiagent.framework.Environment;
import multiagent.framework.Scheduling;
import multiagent.framework.tools.Log;

public class Order {
	public boolean lastExecutedWasEnvironment;
	public boolean firstFound = false;
	public static void main(String[] args) {
		new Order();
		
		
	}
	public Order() {
		Configuration.commandLineMode = true;
		new MyAMAS().start();
	}
	
	public class MyAMAS extends Amas<MyEnv> {

		public MyAMAS() {
			super(new MyEnv(), Scheduling.DEFAULT);
		}
		@Override
		public boolean stopCondition() {
			return cycle==100000000;
		}
		@Override
		protected void onSystemCycleEnd() {
			if (!firstFound) {
				Log.defaultLog.debug("test","First is MAS");
				firstFound = true;
			}
			
			if (!lastExecutedWasEnvironment) {
				Log.defaultLog.fatal("test", "last executed was not the environment");
				System.exit(-1);
			}
			lastExecutedWasEnvironment = false;
		}
	}
	public class MyEnv extends Environment {

		public MyEnv() {
			super(Scheduling.DEFAULT);
		}
		@Override
		public void onCycle() {
			if (!firstFound) {
				Log.defaultLog.debug("test","First is Environment");
				firstFound = true;
			}
			
			lastExecutedWasEnvironment = true;
		}
	}

}

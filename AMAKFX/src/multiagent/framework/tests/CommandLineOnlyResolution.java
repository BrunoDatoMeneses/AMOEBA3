package multiagent.framework.tests;

import multiagent.framework.Amas;
import multiagent.framework.Configuration;
import multiagent.framework.Environment;
import multiagent.framework.Scheduling;
import multiagent.framework.tools.Log;

public class CommandLineOnlyResolution {

	public static void main(String[] args) {
		new CommandLineOnlyResolution();
	}
	public CommandLineOnlyResolution() {
		Configuration.commandLineMode = true;
		new MyAMAS().start();
	}
	
	public class MyAMAS extends Amas<MyEnv> {

		public MyAMAS() {
			super(new MyEnv(), Scheduling.DEFAULT);
		}
		@Override
		public boolean stopCondition() {
			return cycle==100;
		}
		@Override
		protected void onSystemCycleEnd() {
			Log.defaultLog.debug("test", "yolo");
		}
	}
	public class MyEnv extends Environment {

		public MyEnv() {
			super(Scheduling.DEFAULT);
		}
	}

}

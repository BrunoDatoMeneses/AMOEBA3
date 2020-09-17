package multiagent.framework.examples.philosophers;

import multiagent.framework.Amas;
import multiagent.framework.Configuration;
import multiagent.framework.Scheduling;
import multiagent.framework.tools.RunLaterHelper;
import multiagent.framework.ui.AmakPlot;
import multiagent.framework.ui.MainWindow;
import multiagent.framework.ui.VUI;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class PhilosophersAMASExample extends Amas<TableExample> {
	private Label comp;
	private PhilosopherExample[] ps;
	
	public AmakPlot plot;

	public PhilosophersAMASExample(TableExample env) {
		super(env, Scheduling.DEFAULT);
	}
	
	@Override
	protected void onRenderingInitialization() {
		super.onRenderingInitialization();
		plot = new AmakPlot("Eaten pasten", AmakPlot.ChartType.BAR, "Philosophers", "Number of eaten pastas");
	}

	@Override
	protected void onInitialConfiguration() {
		Configuration.executionPolicy = ExecutionPolicy.TWO_PHASES;
		comp = new Label("Cycle");
		MainWindow.addToolbar(comp);

		VUI.get().createAndAddRectangle(20, 20, 20, 20).setColor(Color.RED).setFixed().setLayer(10).setShowInExplorer(false);
		VUI.get().createAndAddString(45, 25, "Hungry").setFixed().setLayer(10).setShowInExplorer(false);

		VUI.get().createAndAddRectangle(20, 45, 20, 20).setColor(Color.BLUE).setFixed().setLayer(10).setShowInExplorer(false);
		VUI.get().createAndAddString(45, 50, "Eating").setFixed().setLayer(10).setShowInExplorer(false);

		VUI.get().createAndAddRectangle(20, 70, 20, 20).setColor(Color.GREEN).setFixed().setLayer(10).setShowInExplorer(false);
		VUI.get().createAndAddString(45, 75, "Thinking").setFixed().setLayer(10).setShowInExplorer(false);
	}

	@Override
	protected void onInitialAgentsCreation() {
		ps = new PhilosopherExample[getEnvironment().getForks().length];
		// Create one agent per fork
		for (int i = 0; i < getEnvironment().getForks().length - 1; i++) {
			ps[i] = new PhilosopherExample(i, this, getEnvironment().getForks()[i], getEnvironment().getForks()[i + 1]);
		}

		// Let the last philosopher takes the first fork (round table)
		ps[getEnvironment().getForks().length - 1] = new PhilosopherExample(getEnvironment().getForks().length - 1,
				this, getEnvironment().getForks()[getEnvironment().getForks().length - 1],
				getEnvironment().getForks()[0]);

		// Add neighborhood
		for (int i = 1; i < ps.length; i++) {
			ps[i].addNeighbor(ps[i - 1]);
			ps[i - 1].addNeighbor(ps[i]);
		}
		ps[0].addNeighbor(ps[ps.length - 1]);
		ps[ps.length - 1].addNeighbor(ps[0]);
	}

	@Override
	protected void onSystemCycleBegin() {
		RunLaterHelper.runLater(() -> {
			comp.setText("Cycle " + getCycle());
		});
	}
}

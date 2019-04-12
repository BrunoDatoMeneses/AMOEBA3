package fr.irit.smac.amak.examples.philosophers;

import fr.irit.smac.amak.Amas;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.Scheduling;
import fr.irit.smac.amak.ui.MainWindow;
import fr.irit.smac.amak.ui.VUI;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.paint.Color;

public class PhilosophersAMASExample extends Amas<TableExample> {
	private Label comp;
	private PhilosopherExample[] ps;

	public PhilosophersAMASExample(TableExample env) {
		super(env, Scheduling.DEFAULT);
	}

	@Override
	protected void onInitialConfiguration() {
		Configuration.executionPolicy = ExecutionPolicy.TWO_PHASES;
		ToolBar toolbar = new ToolBar();
		comp = new Label("Cycle");
		comp.setPrefSize(200, 100);
		toolbar.getItems().add(comp);
		MainWindow.addToolbar(toolbar);
		VUI.get().createRectangle(55, 45, 110, 90).setColor(new Color(0.9d, 0.9d, 0.9d, 0.5d)).setFixed().setLayer(5);

		VUI.get().createRectangle(20, 20, 20, 20).setColor(Color.RED).setFixed().setLayer(10);
		VUI.get().createString(45, 25, "Hungry").setFixed().setLayer(10);

		VUI.get().createRectangle(20, 45, 20, 20).setColor(Color.BLUE).setFixed().setLayer(10);
		VUI.get().createString(45, 50, "Eating").setFixed().setLayer(10);

		VUI.get().createRectangle(20, 70, 20, 20).setColor(Color.GREEN).setFixed().setLayer(10);
		VUI.get().createString(45, 75, "Thinking").setFixed().setLayer(10);
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
		Platform.runLater(() -> {
			comp.setText("Cycle " + getCycle());
		});
	}
}

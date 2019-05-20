package gui;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import agents.percept.Percept;
import fr.irit.smac.amak.tools.RunLaterHelper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import kernel.AMOEBA;

/**
 * A graphical tool for selecting the dimensions to display
 * @author Hugo
 *
 */
public class DimensionSelector extends HBox {
	private ComboBox<Percept> dim1 = new ComboBox<>();
	private ComboBox<Percept> dim2 = new ComboBox<>();
	private AMOEBA amoeba;
	
	public DimensionSelector(AMOEBA amoeba) {
		this.amoeba = amoeba;
		this.setAlignment(Pos.CENTER);
		this.getChildren().addAll(dim1, dim2);
		this.update();
	}
	
	/**
	 * Update percepts list
	 * @param amoeba
	 */
	public void update() {
		ArrayList<Percept> perceptNames = amoeba.getPercepts();
		Semaphore done = new Semaphore(0);
		dim1.setOnAction(null);
		dim2.setOnAction(null);
		RunLaterHelper.runLater(() -> {
			dim1.getItems().clear();
			dim2.getItems().clear();
			dim1.setItems(FXCollections.observableList(perceptNames));
			dim2.setItems(FXCollections.observableList(perceptNames));
			if(perceptNames.size() >= 2) {
				dim1.setValue(perceptNames.get(0));
				dim2.setValue(perceptNames.get(1));
			} else if (perceptNames.size() == 1) {
				dim1.setValue(perceptNames.get(0));
				dim2.setValue(perceptNames.get(0));
			}
			done.release();
		});
		try {
			done.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		dim1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				onChange();
			}
		});
		dim2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				onChange();
			}
		});
	}
	
	/**
	 * Return the 1st selected dimension
	 * @return selected percept
	 */
	public Percept d1() {
		return dim1.getValue();
	}
	
	/**
	 * Return the 2nd selected dimension
	 * @return selected percept
	 */
	public Percept d2() {
		return dim2.getValue();
	}
	
	/**
	 * Called when selected the dimensions have changed
	 */
	private void onChange() {
		amoeba.updateAgentsVisualisation();
	}
}

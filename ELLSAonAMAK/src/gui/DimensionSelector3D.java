package gui;

import agents.percept.Percept;
import multiagent.framework.tools.RunLaterHelper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * A graphical tool for selecting the dimensions to display
 * @author Hugo
 *
 */
public class DimensionSelector3D extends HBox {
	private ComboBox<Percept> dim1 = new ComboBox<>();
	private ComboBox<Percept> dim2 = new ComboBox<>();
	private ComboBox<Percept> dim3 = new ComboBox<>();
	private EventHandler<ActionEvent> onChange;

	public DimensionSelector3D(List<Percept> percepts, EventHandler<ActionEvent> onChange) {
		this.setAlignment(Pos.CENTER);
		this.getChildren().addAll(dim1, dim2, dim3);
		this.onChange = onChange;
		this.update(percepts);
	}
	
	/**
	 * Update percepts list
	 * @param amoeba
	 */
	public void update(List<Percept> percepts) {
		Semaphore done = new Semaphore(0);
		dim1.setOnAction(null);
		dim2.setOnAction(null);
		dim3.setOnAction(null);
		RunLaterHelper.runLater(() -> {
			dim1.getItems().clear();
			dim2.getItems().clear();
			dim3.getItems().clear();
			dim1.setItems(FXCollections.observableList(percepts));
			dim2.setItems(FXCollections.observableList(percepts));
			dim3.setItems(FXCollections.observableList(percepts));
			if(percepts.size() >= 3){
				dim1.setValue(percepts.get(0));
				dim2.setValue(percepts.get(1));
				dim3.setValue(percepts.get(2));
			}
			else if(percepts.size() == 2) {
				dim1.setValue(percepts.get(0));
				dim2.setValue(percepts.get(1));
				dim3.setValue(percepts.get(1));
			} else if (percepts.size() == 1) {
				dim1.setValue(percepts.get(0));
				dim2.setValue(percepts.get(0));
				dim3.setValue(percepts.get(0));
			}
			done.release();
		});
		try {
			done.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		dim1.setOnAction(onChange);
		dim2.setOnAction(onChange);
		dim3.setOnAction(onChange);
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
	 * Return the 3rd selected dimension
	 * @return selected percept
	 */
	public Percept d3() {
		return dim3.getValue();
	}
	
	/**
	 * Set the handler called when selected dimension change.
	 * @param onChange
	 */
	public void setOnChange(EventHandler<ActionEvent> onChange) {
		this.onChange = onChange;
		dim1.setOnAction(onChange);
		dim2.setOnAction(onChange);
		dim3.setOnAction(onChange);
	}
}

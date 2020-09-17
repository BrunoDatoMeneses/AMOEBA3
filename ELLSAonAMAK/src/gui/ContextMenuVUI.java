package gui;

import java.util.HashMap;
import java.util.Optional;

import agents.percept.Percept;
import multiagent.framework.tools.Log;
import multiagent.framework.ui.VUI;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.VBox;
import kernel.ELLSA;

/**
 * The ContextMenu that is shown when right-clicking the {@link VUI} canvas
 * @author Hugo
 *
 */
public class ContextMenuVUI extends ContextMenu {
	/**
	 * If true will skip window asking for input in 2D problems
	 */
	public static boolean quick2DRequest = false;
	private double reqHereX;
	private double reqHereY;
	
	/**
	 * Create a {@link ContextMenu} suited for our needs, composed of 2 items : "Request Here" and "Learn here".<br/>
	 * Set itself as the vui canvas {@link ContextMenu}. 
	 * @param ellsa the amoeba where {@link ELLSA#request(HashMap)} and {@link ELLSA#learn(HashMap)} will be executed.
	 * @param vui the {@link VUI} hosting the {@link ContextMenuVUI}
	 */
	public ContextMenuVUI(ELLSA ellsa, VUI vui) {
		// "request here" menu item
		setupRequestHereMenuItem(ellsa, vui);
		
		// "learn here" menu item
		setupLearnHereMenuItem(ellsa, vui);
		
		// show context menu on context menu event from VUI's canvas
		vui.getCanvas().setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
			@Override
			public void handle(ContextMenuEvent event) {
				reqHereX = event.getX();
				reqHereY = event.getY();
				ContextMenuVUI.this.show(vui.getCanvas(), event.getScreenX(), event.getScreenY());
			}
		});	
	}

	private void setupRequestHereMenuItem(ELLSA ellsa, VUI vui) {
		MenuItem reqHere = new MenuItem("Request Here");
		reqHere.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(quick2DRequest && ellsa.getPercepts().size() == 2) {
					reqTwoDimension(ellsa, vui);
				} else {
					reqNDimension(ellsa, vui);
				}
			}

		});
		this.getItems().add(reqHere);
	}
	
	/**
	 * The "Request Here" action performed when the amoeba is 2D.<br/>
	 * Execute a {@link ELLSA#request(HashMap)} at the position of the click.
	 * @param ellsa
	 * @param vui
	 */
	private void reqTwoDimension(ELLSA ellsa, VUI vui) {
		double x = vui.screenToWorldX(reqHereX);
		double y = vui.screenToWorldY(reqHereY);
		HashMap<String, Double> req = new HashMap<String, Double>();
		req.put(ellsa.getDimensionSelector().d1().getName(), x);
		req.put(ellsa.getDimensionSelector().d2().getName(), y);
		req.put("oracle", 0.0);
		double res = ellsa.request(req);
		Log.defaultLog.inform("AMOEBA", "Request Here for x:"+x+" y:"+y+" -> "+res+".");
	}
	
	/**
	 * The "Request Here" action performed when the amoeba is not 2D.<br/>
	 * Show a {@link Dialog} prompting the user to inputs value for the {@link ELLSA#request(HashMap)}.
	 * @param ellsa
	 * @param vui
	 */
	private void reqNDimension(ELLSA ellsa, VUI vui) {
		double x = vui.screenToWorldX(reqHereX);
		double y = vui.screenToWorldY(reqHereY);
		
		Dialog<HashMap<String, Double>> dialog = new Dialog<>();
		dialog.setTitle("Inputs");
		dialog.setHeaderText("Fill inputs");
		
	    // Set the button types.
	    ButtonType okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
	    dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
		
		// inputs
		HashMap<String, TextField> textFields = new HashMap<>();
		VBox vbox = new VBox();
		for(Percept p : ellsa.getPercepts()) {
			TextField tf = new TextField();
			textFields.put(p.getName(), tf);
			tf.setPromptText(p.getName());
			if(p.getName().equals(ellsa.getDimensionSelector().d1().getName())) {
				tf.setText(x+"");
			}
			if(p.getName().equals(ellsa.getDimensionSelector().d2().getName())) {
				tf.setText(y+"");
			}
			vbox.getChildren().add(tf);
		}
		
		dialog.getDialogPane().setContent(vbox);
		dialog.setResultConverter(dialogButton -> {
	        if (dialogButton == okButtonType) {
	        	HashMap<String, Double> req = new HashMap<String, Double>();
	        	for(String k : textFields.keySet()) {
	        		req.put(k, Double.valueOf(textFields.get(k).getText()));
	        	}
	        	req.put("oracle", 0.0);
	            return req;
	        }
	        return null;
	    });
		
		Optional<HashMap<String, Double>> result = dialog.showAndWait();
		result.ifPresent(req -> {
			double res = ellsa.request(req);
			Log.defaultLog.inform("AMOEBA", "Request Here for "+req+"\n-> "+res+".");
		});
	}
	
	private void setupLearnHereMenuItem(ELLSA ellsa, VUI vui) {
		MenuItem learnHere = new MenuItem("Learn Here");
		learnHere.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(quick2DRequest && ellsa.getPercepts().size() == 2) {
					learnTwoDimension(ellsa, vui);
				} else {
					learnNDimebsion(ellsa, vui);
				}
			}

		});
		this.getItems().add(learnHere);
	}
	
	/**
	 * The "Learn Here" action performed when the amoeba is 2D.<br/>
	 * Execute a {@link ELLSA#learn(HashMap)} at the position of the click.
	 * @param ellsa
	 * @param vui
	 */
	private void learnTwoDimension(ELLSA ellsa, VUI vui) {
		double x = vui.screenToWorldX(reqHereX);
		double y = vui.screenToWorldY(reqHereY);
		HashMap<String, Double> req = new HashMap<String, Double>();
		req.put(ellsa.getDimensionSelector().d1().getName(), x);
		req.put(ellsa.getDimensionSelector().d2().getName(), y);
		req.put("oracle", ellsa.studiedSystem.requestOracle(req));
		ellsa.learn(req);
	}
	
	/**
	 * The "Learn Here" action performed when the amoeba is not 2D.<br/>
	 * Show a {@link Dialog} prompting the user to inputs value for the {@link ELLSA#learn(HashMap)}.
	 * @param ellsa
	 * @param vui
	 */
	private void learnNDimebsion(ELLSA ellsa, VUI vui) {
		double x = vui.screenToWorldX(reqHereX);
		double y = vui.screenToWorldY(reqHereY);
		
		Dialog<HashMap<String, Double>> dialog = new Dialog<>();
		dialog.setTitle("Inputs");
		dialog.setHeaderText("Fill inputs");
		
	    // Set the button types.
	    ButtonType okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
	    dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
		
		// inputs
		HashMap<String, TextField> textFields = new HashMap<>();
		VBox vbox = new VBox();
		for(Percept p : ellsa.getPercepts()) {
			TextField tf = new TextField();
			textFields.put(p.getName(), tf);
			tf.setPromptText(p.getName());
			if(p.getName().equals(ellsa.getDimensionSelector().d1().getName())) {
				tf.setText(x+"");
			}
			if(p.getName().equals(ellsa.getDimensionSelector().d2().getName())) {
				tf.setText(y+"");
			}
			vbox.getChildren().add(tf);
		}
		
		//oracle
		TextField oracle = new TextField();
		textFields.put("oracle", oracle);
		oracle.setPromptText("oracle");
		Button autoOracle = new Button("Autofill oracle");
		autoOracle.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				HashMap<String, Double> req = new HashMap<String, Double>();
				for(String k : textFields.keySet()) {
					if(!"oracle".equals(k)) {
						req.put(k, Double.valueOf(textFields.get(k).getText()));
					}
	        	}
				oracle.setText(ellsa.studiedSystem.requestOracle(req)+"");
			}
		});
		vbox.getChildren().addAll(oracle, autoOracle);
		
		dialog.getDialogPane().setContent(vbox);
		dialog.setResultConverter(dialogButton -> {
	        if (dialogButton == okButtonType) {
	        	HashMap<String, Double> req = new HashMap<String, Double>();
	        	for(String k : textFields.keySet()) {
	        		req.put(k, Double.valueOf(textFields.get(k).getText()));
	        	}
	            return req;
	        }
	        return null;
	    });
		
		Optional<HashMap<String, Double>> result = dialog.showAndWait();
		result.ifPresent(req -> {
			ellsa.learn(req);
			Log.defaultLog.inform("AMOEBA", "Learn Here for "+req+" done.");
		});
	}
}

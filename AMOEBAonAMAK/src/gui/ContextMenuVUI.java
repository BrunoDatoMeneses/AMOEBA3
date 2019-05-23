package gui;

import java.util.HashMap;

import fr.irit.smac.amak.tools.Log;
import fr.irit.smac.amak.ui.VUI;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import kernel.AMOEBA;

/**
 * The ContextMenu that is shown when right-clicking the VUI canvas
 * @author Hugo
 *
 */
public class ContextMenuVUI extends ContextMenu {
	
	private double reqHereX;
	private double reqHereY;
	
	public ContextMenuVUI(AMOEBA amoeba, VUI vui) {
		// "request here" menu item
		MenuItem reqHere = new MenuItem("Request Here");
		reqHere.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				double x = vui.screenToWorldX(reqHereX);
				double y = vui.screenToWorldY(reqHereY);
				HashMap<String, Double> req = new HashMap<String, Double>();
				req.put(amoeba.getDimensionSelector().d1().getName(), x);
				req.put(amoeba.getDimensionSelector().d2().getName(), y);
				req.put("oracle", 0.0);
				double res = amoeba.request(req);
				Log.inform("AMOEBA", "Request Here for x:"+x+" y:"+y+" -> "+res+".");
			}
		});
		this.getItems().add(reqHere);
		
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
}

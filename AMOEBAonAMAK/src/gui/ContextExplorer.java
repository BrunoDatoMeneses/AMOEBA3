package gui;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import agents.context.Context;
import fr.irit.smac.amak.ui.MainWindow;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import kernel.AMOEBA;

public class ContextExplorer extends ScrollPane {

	private AMOEBA amoeba;
	private List<Context> contextList;

	private VBox vbox;
	private TitledPane contextsPane;
	private VBox cpVBox;
	private TextField search;

	public ContextExplorer(AMOEBA amoeba) {
		this.amoeba = amoeba;

		this.setMaxWidth(Double.MAX_VALUE);
		this.setMaxHeight(Double.MAX_VALUE);

		vbox = new VBox();
		vbox.setFillWidth(true);
		this.setContent(vbox);

		// refresh and close button
		HBox hboxButtons = new HBox();
		Button refresh = new Button("Refresh");
		refresh.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				update();
			}
		});
		Button close = new Button("Close");
		close.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				MainWindow.setLeftPanel(null);
			}
		});
		hboxButtons.getChildren().addAll(refresh, close);

		// search bar
		search = new TextField();
		search.setPromptText("regular expression");
		// update list on change
		search.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				search.setStyle(null);
				try {
					update();
				} catch (PatternSyntaxException ex) {
					search.setStyle("-fx-border-color: red;");
				}
			}
		});

		cpVBox = new VBox();
		contextsPane = new TitledPane("Contexts", cpVBox);

		vbox.getChildren().addAll(hboxButtons, search, contextsPane);
		update();
	}

	/**
	 * Update the list of context
	 */
	public void update() {
		contextList = amoeba.getContexts();
		// very crude color sort, we only look at red.
		contextList.sort(new Comparator<Context>() {
			@Override
			public int compare(Context o1, Context o2) {
				Color c1 = o1.getVisualizations().getDrawable().getColor();
				Color c2 = o2.getVisualizations().getDrawable().getColor();
				return (int) ((c1.getRed()*255)-(c2.getRed()*255));
			}
		});
		cpVBox.getChildren().clear();
		Pattern p = Pattern.compile(search.getText());
		for(Context c : contextList) {
			if(p.matcher(c.toStringFull()).find()) {
				cpVBox.getChildren().add(c.getVisualizations().getMini().getNode());
			}
		}
	}
}

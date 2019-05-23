package gui.context;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import agents.context.Context;
import fr.irit.smac.amak.ui.MainWindow;
import gui.AmoebaWindow;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import kernel.AMOEBA;

/**
 * A piece of GUI allowing to see and look for contexts.
 * @author Hugo
 *
 */
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
		
		// Add to main window
		AmoebaWindow.setLeftPanel(this);
		MenuItem miContextExplorer = new MenuItem("Context Explorer");
		miContextExplorer.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				AmoebaWindow.setLeftPanel(ContextExplorer.this);
			}
		});
		AmoebaWindow.addToMenu("Window", miContextExplorer);
	}

	/**
	 * Update the list of context
	 */
	public void update() {
		contextList = amoeba.getContexts();
		// crude color sort
		contextList.sort(new Comparator<Context>() {
			@Override
			public int compare(Context o1, Context o2) {
				Color c1 = AmoebaWindow.instance().getContextVisualizations(o1).getDrawable().getColor();
				Color c2 = AmoebaWindow.instance().getContextVisualizations(o2).getDrawable().getColor();
				double score1 = c1.getRed()*100 + c1.getGreen()*10 + c1.getBlue();
				double score2 = c2.getRed()*100 + c2.getGreen()*10 + c2.getBlue();
				return (int) ((score1 - score2)*10);
			}
		});
		cpVBox.getChildren().clear();
		Pattern p = Pattern.compile(search.getText());
		for(Context c : contextList) {
			if(p.matcher(c.toStringFull()).find()) {
				AmoebaWindow.instance().getContextVisualizations(c).getMini().update();
				cpVBox.getChildren().add(AmoebaWindow.instance().getContextVisualizations(c).getMini().getNode());
			}
		}
	}
}

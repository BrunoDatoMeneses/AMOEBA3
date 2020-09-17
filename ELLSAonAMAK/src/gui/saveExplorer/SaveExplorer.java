package gui.saveExplorer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

import agents.percept.Percept;
import multiagent.framework.tools.SerializeBase64;
import multiagent.framework.ui.VUI;
import gui.DimensionSelector;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import kernel.ELLSA;
import kernel.StudiedSystem;
import kernel.backup.SaveHelperImpl;

/**
 * Graphical element to browse and load (auto)saves for a specific amoeba. 
 * @see SaveHelperImpl
 * @see ELLSA
 * @author Hugo
 *
 */
public class SaveExplorer extends VBox {
	
	private ELLSA ellsa;
	
	@FXML private ComboBox<String> comboBoxA;
	@FXML private ComboBox<String> comboBoxM;
	
	/**
	 * create a SaveExplorer for an AMOEBA.
	 * The amoeba MUST have a working {@link ELLSA#saver}.
	 * @param ellsa
	 * @see SaveHelperImpl
	 */
	public SaveExplorer(ELLSA ellsa) {
		this.ellsa = ellsa;
		try {
			//load the fxml for THIS SaveExplorer
			URL url = getClass().getResource("SaveExplorer.fxml");
			VBox root = FXMLLoader.load(url, null, null, new Callback<Class<?>, Object>() {
				@Override
				public Object call(Class<?> param) {
					return SaveExplorer.this;
				}
			});
			this.getChildren().add(root);
		} catch (IOException e) {
			e.printStackTrace();
		}

		quickDisplayCombobox(comboBoxA);

		quickDisplayCombobox(comboBoxM);
		
	}
	
	// Handler, A : Auto saves, M : Manual saves. --------
	@FXML protected void handleRefresh(ActionEvent event) {
		update();
	}
	
	@FXML protected void handleLaunchA(ActionEvent event) {
		try {
			exec(SaveExplorer.class, comboBoxA.getValue(), SerializeBase64.serialize(ellsa.studiedSystem));
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} 
	}
	
	@FXML protected void handleLaunchM(ActionEvent event) {
		try {
			exec(SaveExplorer.class, comboBoxM.getValue(), SerializeBase64.serialize(ellsa.studiedSystem));
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} 
	}
	
	@FXML protected void handlePrevA(ActionEvent event) {
		 ListIterator<String> iter = comboBoxA.getItems().listIterator();
		// get to combo box position
		while(iter.hasNext() && (!iter.next().equals(comboBoxA.getValue())));
		
		if(iter.hasPrevious()) iter.previous();
		if(iter.hasPrevious()) comboBoxA.setValue(iter.previous());
	}
	
	@FXML protected void handleNextA(ActionEvent event) {
		 ListIterator<String> iter = comboBoxA.getItems().listIterator();
		// get to combo box position
		while(iter.hasNext() && (!iter.next().equals(comboBoxA.getValue())));
		
		if(iter.hasNext()) comboBoxA.setValue(iter.next());
	}
	
	@FXML protected void handlePrevM(ActionEvent event) {
		 ListIterator<String> iter = comboBoxM.getItems().listIterator();
		// get to combo box position
		while(iter.hasNext() && (!iter.next().equals(comboBoxM.getValue())));
		
		if(iter.hasPrevious()) iter.previous();
		if(iter.hasPrevious()) comboBoxM.setValue(iter.previous());
	}
	
	@FXML protected void handleNextM(ActionEvent event) {
		 ListIterator<String> iter = comboBoxM.getItems().listIterator();
		// get to combo box position
		while(iter.hasNext() && (!iter.next().equals(comboBoxM.getValue())));
		
		if(iter.hasNext()) comboBoxM.setValue(iter.next());
	}
	
	@FXML protected void handleLoadA(ActionEvent event) {
		ellsa.saver.load(comboBoxA.getValue());
	}
	
	@FXML protected void handleLoadM(ActionEvent event) {
		ellsa.saver.load(comboBoxM.getValue());
	}
	
	@FXML protected void handlePreviewA(ActionEvent event) {
		quickDisplay(Paths.get(comboBoxA.getValue()));
	}
	
	@FXML protected void handlePreviewM(ActionEvent event) {
		quickDisplay(Paths.get(comboBoxM.getValue()));
	}
	// ---------------------------------------------------
	
	/**
	 * Update the list of available saves
	 */
	public void update() {
		comboBoxA.getItems().clear();
		List<Path> la = ellsa.saver.listAutoSaves();
		la.sort(new Comparator<Path>() {
			@Override
			public int compare(Path o1, Path o2) {
				Scanner s1 = new Scanner(o1.getFileName().toString().substring(0, o1.getFileName().toString().lastIndexOf('.')));
				Scanner s2 = new Scanner(o2.getFileName().toString().substring(0, o2.getFileName().toString().lastIndexOf('.')));
				int res = 0;
				if(s1.hasNextInt()) {
					if(s2.hasNextInt()) {
						res = s1.nextInt() - s2.nextInt();
					} else {
						res = 1;
					}
				} else if (s2.hasNextInt()) {
					res = -1;
				} else {
					res = o1.compareTo(o2);
				}
				s1.close();
				s2.close();
				return res;
			}
		});
		for(Path p : la) {
			comboBoxA.getItems().add(p.toString());
		}
		
		comboBoxM.getItems().clear();
		for(Path p : ellsa.saver.listManualSaves()) {
			comboBoxM.getItems().add(p.toString());
		}
	}
	
	/**
	 * Configure a ComboBox to display a preview when its value is changed
	 * @param cb
	 */
	private void quickDisplayCombobox(ComboBox<String> cb) {
		cb.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if(newValue != null)
					quickDisplay(Paths.get(newValue));
			}
		});
	}
	
	/**
	 * Create/update the preview in the SaveExplorer based on a save file.
	 * @param path path to the save file to preview
	 */
	private void quickDisplay(Path path) {
		VUI vui = VUI.get("Save Explorer");
		if(!this.getChildren().contains(vui.getPanel())) {
			vui.setDefaultView(200, 0, 0);
			this.getChildren().add(vui.getPanel());
			VBox.setVgrow(vui.getPanel(), Priority.SOMETIMES);
		}
		if(path != null) {
			
			//get or add dimension selector
			DimensionSelector ds;
			if(vui.toolbar.getItems().size() == 4) {
				ds = (DimensionSelector) vui.toolbar.getItems().get(3);
			} else {
				ds = DrawFromXml.createDimensionSelector(path);
				vui.toolbar.getItems().add(3, ds);
			}
			
			DrawFromXml.draw(vui, path, ds.d1().getName(), ds.d2().getName());
			ds.setOnChange(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					DrawFromXml.draw(vui, path, ds.d1().getName(), ds.d2().getName());
				}
			});
		}
		vui.updateCanvas();
	}
	
	// thx https://stackoverflow.com/questions/636367/executing-a-java-application-in-a-separate-process
	/**
	 * Create a new process
	 * @param klass the starting class
	 * @param arg1 the path to the config file
	 * @param arg2 the serialized studied system
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static void exec(Class klass, String arg1, String arg2) throws IOException, InterruptedException {
		String javaHome = System.getProperty("java.home");
		String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
		String classpath = System.getProperty("java.class.path");
		String className = klass.getName();

		ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className, arg1, arg2);

		Process process = builder.inheritIO().start();
		//process.waitFor();
	}
	
	/**
	 * Launch a new AMOEBA from a save and a serialized studied system
	 * @param args
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		System.out.println("New AMOEBA launched.");
		ELLSA ellsa = new ELLSA(null,null,args[0], (StudiedSystem)SerializeBase64.deserialize(args[1]));
		//amoeba.allowGraphicalScheduler(false);
		for(Percept p : ellsa.getPercepts()) {
			p.setValue(ellsa.getPerceptions(p.getName()));
		}
		ellsa.updateAgentsVisualisation();
	}
}

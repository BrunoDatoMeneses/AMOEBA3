package gui.saveExplorer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

import agents.percept.Percept;
import fr.irit.smac.amak.tools.SerializeBase64;
import fr.irit.smac.amak.ui.VUI;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import kernel.AMOEBA;
import kernel.StudiedSystem;

/**
 * Graphical element to browse and load (auto)save for a specific amoeba. 
 * @author Hugo
 *
 */
public class SaveExplorer extends VBox {
	
	private AMOEBA amoeba;
	
	@FXML private ComboBox<String> comboBoxA;
	@FXML private ComboBox<String> comboBoxM;
	
	public SaveExplorer(AMOEBA amoeba) {
		this.amoeba = amoeba;
		try {
			VBox root = FXMLLoader.load(getClass().getResource("SaveExplorer.fxml"), null, null, new Callback<Class<?>, Object>() {
				@Override
				public Object call(Class<?> param) {
					return SaveExplorer.this;
				}
			});
			//System.out.println(root.getChildren());
			this.getChildren().add(root);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		quickDisplayCombobox(comboBoxA);

		quickDisplayCombobox(comboBoxM);
		
	}
	
	@FXML protected void handleRefresh(ActionEvent event) {
		update();
	}
	
	@FXML protected void handleLaunchA(ActionEvent event) {
		try {
			exec(SaveExplorer.class, comboBoxA.getValue(), SerializeBase64.serialize(amoeba.studiedSystem));
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} 
	}
	
	@FXML protected void handleLaunchM(ActionEvent event) {
		try {
			exec(SaveExplorer.class, comboBoxM.getValue(), SerializeBase64.serialize(amoeba.studiedSystem));
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
	
	@FXML protected void handleLoadA(ActionEvent event) {
		amoeba.saver.load(comboBoxA.getValue());
	}
	
	@FXML protected void handleLoadM(ActionEvent event) {
		amoeba.saver.load(comboBoxM.getValue());
	}
	
	@FXML protected void handlePreviewA(ActionEvent event) {
		quickDisplay(Paths.get(comboBoxA.getValue()));
	}
	
	@FXML protected void handlePreviewM(ActionEvent event) {
		quickDisplay(Paths.get(comboBoxM.getValue()));
	}
	
	public void update() {
		comboBoxA.getItems().clear();
		List<Path> la = amoeba.saver.listAutoSaves();
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
		for(Path p : amoeba.saver.listManualSaves()) {
			comboBoxM.getItems().add(p.toString());
		}
	}
	
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
	 * Create the preview of a save file
	 * @param path
	 */
	public void quickDisplay(Path path) {
		VUI vui = VUI.get("Save Explorer");
		if(!this.getChildren().contains(vui.getPanel())) {
			this.getChildren().add(vui.getPanel());
			VBox.setVgrow(vui.getPanel(), Priority.SOMETIMES);
		}
		vui.clear();
		if(path != null)
			DrawFromXml.draw(vui, path);
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
		AMOEBA amoeba = new AMOEBA(args[0], (StudiedSystem)SerializeBase64.deserialize(args[1]));
		amoeba.saver.deleteFolderOnClose = false;
		//amoeba.allowGraphicalScheduler(false);
		for(Percept p : amoeba.getPercepts()) {
			p.setValue(amoeba.getPerceptionsOrAction(p.getName()));
		}
		amoeba.updateAgentsVisualisation();
	}
}

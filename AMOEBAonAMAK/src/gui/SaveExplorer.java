package gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import agents.percept.Percept;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import kernel.AMOEBA;

/**
 * Graphical element to browse and load (auto)save for a specific amoeba. 
 * @author Hugo
 *
 */
public class SaveExplorer extends VBox {
	
	private AMOEBA amoeba;
	
	private ComboBox<String> comboBoxA;
	private ComboBox<String> comboBoxM;
	
	public SaveExplorer(AMOEBA amoeba) {
		this.amoeba = amoeba;
		
		Button refresh = new Button("Refresh");
		refresh.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				update();
			}
		});
		this.getChildren().add(refresh);
		
		HBox hbox = new HBox();
		this.getChildren().add(hbox);
		
		VBox vboxAuto = new VBox();
		vboxAuto.getChildren().add(new Label("Autosaves"));
		comboBoxA = new ComboBox<String>();
		vboxAuto.getChildren().add(comboBoxA);
		Button launchA = new Button("Launch");
		launchA.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					exec(SaveExplorer.class, comboBoxA.getValue());
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		});
		vboxAuto.getChildren().add(launchA);
		
		VBox vboxManual = new VBox();
		vboxManual.getChildren().add(new Label("Manual saves"));
		comboBoxM = new ComboBox<String>();
		vboxManual.getChildren().add(comboBoxM);
		Button launchM = new Button("Launch");
		launchM.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					exec(SaveExplorer.class, comboBoxM.getValue());
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		});
		vboxManual.getChildren().add(launchM);
		
		hbox.getChildren().addAll(vboxAuto, vboxManual);
	}
	
	public void update() {
		comboBoxA.getItems().clear();
		for(Path p : amoeba.saver.listAutoSaves()) {
			comboBoxA.getItems().add(p.toString());
		}
		comboBoxM.getItems().clear();
		for(Path p : amoeba.saver.listManualSaves()) {
			comboBoxM.getItems().add(p.toString());
		}
	}
	
	// thx https://stackoverflow.com/questions/636367/executing-a-java-application-in-a-separate-process
	private static void exec(Class klass, String arg) throws IOException, InterruptedException {
		String javaHome = System.getProperty("java.home");
		String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
		String classpath = System.getProperty("java.class.path");
		String className = klass.getName();

		ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className, arg);

		Process process = builder.inheritIO().start();
		//process.waitFor();
	}
	
	public static void main(String[] args) {
		AMOEBA amoeba = new AMOEBA(args[0], null);
		amoeba.saver.deleteFolderOnClose = false;
		amoeba.allowGraphicalScheduler(false);
		for(Percept p : amoeba.getPercepts()) {
			p.setValue(amoeba.getPerceptionsOrAction(p.getName()));
		}
		amoeba.updateAgentsVisualisation();
	}
}

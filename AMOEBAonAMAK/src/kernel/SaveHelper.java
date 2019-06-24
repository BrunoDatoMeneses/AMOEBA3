package kernel;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import fr.irit.smac.amak.ui.MainWindow;
import gui.AmoebaWindow;
import gui.saveExplorer.SaveExplorer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import utils.DeleteDirectory;

/**
 * An helper class that handle save, autosave, and load needs of an AMOEBA.
 * @author Hugo
 *
 */
public class SaveHelper {
	/**
	 * Path to the saves' root directory. Default is 'saves'.
	 */
	public static final String savesRoot = "saves";
	public static final String autosaveDirName = "autosave";
	public static final String manualsaveDirName = "manual";

	/**
	 * The backup system used by the SaveHelper.
	 */
	public IBackupSystem backupSystem;
	
	/**
	 * If false {@link SaveHelper#autosave()} will do nothing.
	 */
	public boolean autoSave = true;
	
	/**
	 * Will the SaveHelper delete {@link SaveHelper#dir} when closing the MainWindow ?
	 */
	public boolean deleteFolderOnClose = true;
	
	/**
	 * Path to the autosave directory.
	 */
	private Path dirAuto;
	
	/**
	 * Path to the manual save directory.
	 */
	private Path dirManual;
	
	/**
	 * Path to the save directory.
	 */
	public Path dir;

	private AMOEBA amoeba;

	/**
	 * Create a SaveHelper for an amoeba.<br/>
	 * Saves are stored in {@link SaveHelper#savesRoot}, under a directory named after the amoeba and creation time of the SaveHelper.<br/>
	 * Autosave for this SaveHelper can be deactivated with {@link SaveHelper#autoSave}.<br/>
	 * By default, the save folder for this amoeba is deleted when the application is closed, this can be changed with {@link SaveHelper#deleteFolderOnClose}.
	 * @param amoeba
	 */
	public SaveHelper(AMOEBA amoeba) {
		this.amoeba = amoeba;
		backupSystem = new BackupSystem(amoeba);
		String dirName = amoeba.toString() + "_" + System.currentTimeMillis();
		dir = Paths.get(savesRoot, dirName);
		if (autoSave) {
			dirAuto = Paths.get(dir.toString(), autosaveDirName);
			try {
				Files.createDirectories(dirAuto);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Cannot create auto save directory. Auto saving is disabled.");
				dirAuto = null;
				autoSave = false;
			}
		}
		dirManual = Paths.get(dir.toString(), manualsaveDirName);
		try {
			Files.createDirectories(dirManual);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Cannot create manual save directory.");
			dirManual = null;
		}

		// add graphical element if relevant
		if (AmoebaWindow.isInstance()) {
			SaveExplorer se = new SaveExplorer(amoeba);
			AmoebaWindow.addTabbedPanel("Save Explorer", se);
			AmoebaWindow.addOnCloseAction(()-> {
				if(deleteFolderOnClose) {
					try {
						DeleteDirectory.deleteDirectoryRecursion(dir);
					} catch (IOException e) {
						e.printStackTrace();
						System.err.println("Failed to delete saves files on close.");
					}
				}
			});
			setupGraphicalTool();
		}
	}

	/**
	 * Load a save pointed by path.
	 * @param path path to the save.
	 */
	public void load(String path) {
		File f = new File(path);
		load(f);
	}
	
	/**
	 * Load a save from file.
	 * @param path path to the save.
	 */
	public void load(File file) {
		backupSystem.load(file);
	}

	/**
	 * Create a save at path.
	 * @param path path of the new save
	 */
	public void save(String path) {
		File f = new File(path);
		save(f);
	}
	
	/**
	 * Create a save in file.
	 * @param path path of the new save
	 */
	public void save(File file) {
		backupSystem.setLoadPresetContext(true);
		backupSystem.save(file);
	}

	/**
	 * Add a new save in {@link SaveHelper#dirManual}.
	 * @param name
	 */
	public void newManualSave(String name) {
		String c = (name == null || "".equals(name)) ? "" : ("_" + name);
		c.replace('/', '-');
		c.replace('\\', '-');
		Path p = Paths.get(dirManual.toString(), amoeba.getCycle() + c + "." + backupSystem.getExtension());
		backupSystem.setLoadPresetContext(true);
		backupSystem.save(p.toFile());
	}

	/**
	 * Add a new save in {@link SaveHelper#dirAuto}.
	 */
	public void autosave() {
		if (autoSave) {
			Path p = Paths.get(dirAuto.toString(), amoeba.getCycle() + "." + backupSystem.getExtension());
			backupSystem.setLoadPresetContext(true);
			backupSystem.save(p.toFile());
		}
	}

	/**
	 * List saves in {@link SaveHelper#dirAuto}.
	 */
	public List<Path> listAutoSaves() {
		List<Path> l = new ArrayList<>();
		try (DirectoryStream<Path> d = Files.newDirectoryStream(dirAuto)){
			d.iterator().forEachRemaining(l::add);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Cannot read saves list. Empty list returned.");
		}
		return l;
	}

	/**
	 * List saves in {@link SaveHelper#dirManual}.
	 */
	public List<Path> listManualSaves() {
		List<Path> l = new ArrayList<>();
		try (DirectoryStream<Path> d = Files.newDirectoryStream(dirManual)){
			d.iterator().forEachRemaining(l::add);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Cannot read saves list. Empty list returned.");
		}
		return l;
	}

	/**
	 * Add save/load options in the main window.
	 */
	private void setupGraphicalTool() {
		MainWindow mw = AmoebaWindow.instance();
		// TODO remove if they exist items Save and Load in menu Option.
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML", "*.xml"),
				new FileChooser.ExtensionFilter("All", "*.*"));

		// Creation of the load menu item
		EventHandler<ActionEvent> eventLoad = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				amoeba.getScheduler().stop();
				File file = fileChooser.showOpenDialog(mw.stage);
				if (file != null)
					backupSystem.load(file);
			}
		};
		MainWindow.addOptionsItem("Load", eventLoad);

		// Creation of the save menu item
		EventHandler<ActionEvent> eventSave = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				amoeba.getScheduler().stop();
				File file = fileChooser.showSaveDialog(mw.stage);
				if (file != null)
					backupSystem.save(file);
			}
		};
		MainWindow.addOptionsItem("Save", eventSave);
	}
}

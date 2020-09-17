package kernel.backup;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import multiagent.framework.Configuration;
import multiagent.framework.ui.MainWindow;
import gui.EllsaMultiUIWindow;
import gui.EllsaWindow;
import gui.saveExplorer.SaveExplorer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import kernel.ELLSA;
import utils.DeleteDirectory;

/**
 * The standard implementation of {@link ISaveHelper}
 * @see SaveHelperDummy
 * @author Hugo
 *
 */
public class SaveHelperImpl implements ISaveHelper{
	/**
	 * Path to the saves' root directory. Default is 'saves'.
	 */
	public static final String savesRoot = "saves";
	public static final String autosaveDirName = "autosave";
	public static final String manualsaveDirName = "manual";

	
	public EllsaMultiUIWindow amoebaMultiUIWindow;
	
	/**
	 * The backup system used by the SaveHelper.
	 */
	public IBackupSystem backupSystem;
	
	/**
	 * If false {@link SaveHelperImpl#autosave()} will do nothing.<br/>
	 * Default at not( {@link Configuration#commandLineMode} ).
	 */
	public boolean autoSave = true;
	
	/**
	 * Will the SaveHelper delete {@link SaveHelperImpl#dir} when closing the MainWindow ?
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

	private ELLSA ellsa;

	/**
	 * Create a SaveHelper for an amoeba.<br/>
	 * Saves are stored in {@link SaveHelperImpl#savesRoot}, under a directory named after the amoeba and creation time of the SaveHelper.<br/>
	 * Autosave for this SaveHelper can be deactivated with {@link SaveHelperImpl#autoSave}.<br/>
	 * By default, the save folder for this amoeba is deleted when the application is closed, this can be changed with {@link SaveHelperImpl#deleteFolderOnClose}.
	 * @param ellsa
	 */
	public SaveHelperImpl(ELLSA ellsa) {
		autoSave = !Configuration.commandLineMode;
		this.ellsa = ellsa;
		backupSystem = new BackupSystem(ellsa);
		String dirName = ellsa.toString() + "_" + System.currentTimeMillis();
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
		if (EllsaWindow.isInstance()) {
			SaveExplorer se = new SaveExplorer(ellsa);
			EllsaWindow.addTabbedPanel("Save Explorer", se);
			EllsaWindow.addOnCloseAction(()-> {
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
	
	public SaveHelperImpl(ELLSA ellsa, EllsaMultiUIWindow window) {
		amoebaMultiUIWindow = window;
		autoSave = !Configuration.commandLineMode;
		this.ellsa = ellsa;
		backupSystem = new BackupSystem(ellsa);
		String dirName = ellsa.toString() + "_" + System.currentTimeMillis();
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
		SaveExplorer se = new SaveExplorer(ellsa);
		window.addTabbedPanel("Save Explorer", se);
		window.addOnCloseAction(()-> {
			if(deleteFolderOnClose) {
				try {
					DeleteDirectory.deleteDirectoryRecursion(dir);
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("Failed to delete saves files on close.");
				}
			}
		});
		setupGraphicalTool(window);
	}

	@Override
	public void load(String path) {
		File f = new File(path);
		load(f);
	}
	
	@Override
	public void load(File file) {
		backupSystem.load(file);
	}

	@Override
	public void save(String path) {
		File f = new File(path);
		save(f);
	}
	
	@Override
	public void save(File file) {
		backupSystem.setLoadPresetContext(true);
		backupSystem.save(file);
	}

	@Override
	public void newManualSave(String name) {
		String c = (name == null || "".equals(name)) ? "" : ("_" + name);
		c.replace('/', '-');
		c.replace('\\', '-');
		Path p = Paths.get(dirManual.toString(), ellsa.getCycle() + c + "." + backupSystem.getExtension());
		backupSystem.setLoadPresetContext(true);
		backupSystem.save(p.toFile());
	}

	@Override
	public void newManualSave(String name, String path) {
		String c = (name == null || "".equals(name)) ? "" : ("_" + name);
		c.replace('/', '-');
		c.replace('\\', '-');
		Path p = Paths.get(path, ellsa.getCycle() + c + "." + backupSystem.getExtension());
		backupSystem.setLoadPresetContext(true);
		backupSystem.save(p.toFile());
	}

	@Override
	public void autosave() {
		if (autoSave) {
			Path p = Paths.get(dirAuto.toString(), ellsa.getCycle() + "." + backupSystem.getExtension());
			backupSystem.setLoadPresetContext(true);
			backupSystem.save(p.toFile());
		}
	}

	@Override
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

	@Override
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
		EllsaMultiUIWindow mw = amoebaMultiUIWindow;
		// TODO remove if they exist items Save and Load in menu Option.
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML", "*.xml"),
				new FileChooser.ExtensionFilter("All", "*.*"));

		// Creation of the load menu item
		EventHandler<ActionEvent> eventLoad = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ellsa.getScheduler().stop();
				File file = fileChooser.showOpenDialog(mw);
				if (file != null)
					backupSystem.load(file);
			}
		};
		MainWindow.addOptionsItem("Load", eventLoad);

		// Creation of the save menu item
		EventHandler<ActionEvent> eventSave = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ellsa.getScheduler().stop();
				File file = fileChooser.showSaveDialog(mw);
				if (file != null)
					backupSystem.save(file);
			}
		};
		MainWindow.addOptionsItem("Save", eventSave);
	}
	
	private void setupGraphicalTool(EllsaMultiUIWindow window) {
		EllsaMultiUIWindow mw = amoebaMultiUIWindow;
		// TODO remove if they exist items Save and Load in menu Option.
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML", "*.xml"),
				new FileChooser.ExtensionFilter("All", "*.*"));

		// Creation of the load menu item
		EventHandler<ActionEvent> eventLoad = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ellsa.getScheduler().stop();
				File file = fileChooser.showOpenDialog(mw);
				if (file != null)
					backupSystem.load(file);
			}
		};
		window.addOptionsItem("Load", eventLoad);

		// Creation of the save menu item
		EventHandler<ActionEvent> eventSave = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ellsa.getScheduler().stop();
				File file = fileChooser.showSaveDialog(mw);
				if (file != null)
					backupSystem.save(file);
			}
		};
		window.addOptionsItem("Save", eventSave);
	}

	@Override
	public void setAutoSave(boolean value) {
		autoSave = value;
	}

	@Override
	public boolean getAutoSave() {
		return autoSave;
	}
}

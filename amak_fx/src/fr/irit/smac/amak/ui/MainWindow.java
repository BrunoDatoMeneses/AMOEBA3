package fr.irit.smac.amak.ui;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import fr.irit.smac.amak.Information;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * This window is the main one of an AMAS developed using AMAK. It contains a
 * toolbar panel and various spaces for panels
 * 
 * @author of the original version (the Swing one) Alexandre Perles, Marcillaud Guilhem
 *
 */
public class MainWindow extends Application {

	/**
	 * Unique ID meant to handle serialization correctly
	 */
	private static final long serialVersionUID = 2607956693857748227L;
	/**
	 * The window itself
	 */
	private Stage stage;
	/**
	 * The panel which contains the toolbar
	 */
	private FlowPane toolbarPanel;
	/**
	 * The main panel is split in two panels. This allows to dynamically resize
	 * these two panels.
	 */
	private SplitPane splitPane;
	/**
	 * The menu bar of the window
	 */
	private MenuBar menuBar;
	/**
	 * The option menu
	 */
	private Menu optionsMenu;
	/**
	 * The panel in which panels with tab can be added
	 */
	private TabPane tabbedPanel;
	/**
	 * For an AMAK process it can only be one instance of MainWindow
	 */
	private static MainWindow instance;
	/**
	 * Lock present to avoid the creation of a MainWindow while another is creating
	 */
	private static ReentrantLock instanceLock = new ReentrantLock();
	/**
	 * Use to synchronize threads
	 */
	private static boolean startEnded = false;
	/**
	 * Locks to be sure the window has been created before other graphical elements
	 */
	private static ReentrantLock startLock = new ReentrantLock();
	private static Condition waiting = startLock.newCondition();
	
	/**
	 * Create the frame.
	 */
	public MainWindow() {
		instanceLock.lock();
		if (instance == null) {
			System.out.println("Before launching");
			Thread ui = new Thread(new Runnable() {
			    @Override
			    public void run() {
					Application.launch(MainWindow.class);
			    }
			});
			ui.start();
			System.out.println("After launching");
		}
		instanceLock.unlock();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		startLock.lock();
		System.out.println("BEGIN START");
		// Creation of scene and root group
		primaryStage.setTitle("AMAK");
		Group root = new Group();
		Scene scene = new Scene(root, 450, 300);
		stage = primaryStage;
		
		System.out.println("Border organization of the scene");
		// Border organization of the scene
		BorderPane organizationPane = new BorderPane();
		root.getChildren().add(organizationPane);
		
		System.out.println("Creation of the toolbar (Bottom)");
		// Creation of the toolbar (Bottom)
		toolbarPanel = new FlowPane();
		toolbarPanel.setHgap(5);
		toolbarPanel.setVgap(5);
		organizationPane.setBottom(toolbarPanel);
		
		System.out.println("Creation of the split pane (Center)");
		// Creation of the split pane (Center)
		splitPane = new SplitPane();
		splitPane.setOrientation(Orientation.HORIZONTAL);
		organizationPane.setCenter(splitPane);
		
		System.out.println("Creation of the left part of the split pane (Center Left)");
		// Creation of the left part of the split pane (Center Left)
		AnchorPane left = new AnchorPane();
		splitPane.getItems().add(left);
		
		System.out.println("Creation of the right part of the split pane (Center Right)");
		// Creation of the right part of the split pane (Center Right)
		tabbedPanel = new TabPane();
		splitPane.getItems().add(tabbedPanel);
		
		System.out.println("Creation of the menu bar (Top)");
		// Creation of the menu bar (Top)
		menuBar = new MenuBar();
		optionsMenu = new Menu("Options");
		menuBar.getMenus().add(optionsMenu);
		organizationPane.setTop(menuBar);
		
		System.out.println("Creation of the close menu item");
		// Creation of the close menu item
		MenuItem menuItem = new MenuItem("Close");
		menuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.exit(0);
			}
		});
		optionsMenu.getItems().add(menuItem);
		
		menuBar.getMenus().add(new Menu("AMAK v" + Information.VERSION));

		instance = this;
		
		startEnded = true;
		waiting.signal();
		startLock.unlock();
		
		System.out.println("Association between the root Group and the main VUI");
		// Association between the root Group and the main VUI
		VUI.get().setCanvas(scene, root);
		
		stage.setScene(scene);
		stage.show();
		System.out.println("END START");
	}

	/**
	 * Add a close action to the listener
	 * 
	 * @param onClose
	 *            The action to be executed when the window is closed
	 */
	public static void addOnCloseAction(EventHandler<WindowEvent> onClose) {
		instance();
		if (!startEnded) {
			startLock.lock();
			try {
				while (!startEnded)
					waiting.await();
				waiting.signal();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				startLock.unlock();
			}
		}
		instance().stage.setOnCloseRequest(onClose);
	}
	
	/**
	 * Change the icon of the window
	 * 
	 * @param filename
	 *            The filename of the icon
	 */
	public static void setWindowIcon(String filename) {
		instance();
		if (!startEnded) {
			startLock.lock();
			try {
				while (!startEnded)
					waiting.await();
				waiting.signal();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				startLock.unlock();
			}
		}
		instance().stage.getIcons().add(new Image(filename));
	}
	
	/**
	 * Change the title of the main window
	 * 
	 * @param title
	 *            The new title
	 */
	public static void setWindowTitle(String title) {
		instance();
		if (!startEnded) {
			startLock.lock();
			try {
				while (!startEnded)
					waiting.await();
				waiting.signal();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				startLock.unlock();
			}
		}
		instance().stage.setTitle(title);
	}
	
	/**
	 * Add a button in the menu options
	 * 
	 * @param title
	 *            The title of the button
	 * @param event
	 *            The action to be executed
	 */
	public static void addMenuItem(String title, EventHandler<ActionEvent> event) {
		instance();
		MenuItem menuItem = new MenuItem(title);
		menuItem.setOnAction(event);
		if (!startEnded) {
			startLock.lock();
			try {
				while (!startEnded)
					waiting.await();
				waiting.signal();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				startLock.unlock();
			}
		}
		instance().optionsMenu.getItems().add(menuItem);
	}
	
	/**
	 * Add a toolBar
	 * 
	 * @param toolbar
	 *            The ToolBar.
	 */
	public static void addToolbar(ToolBar toolbar) {
		instance();
		System.out.println("BEGIN addToolbar");
		if (!startEnded) {
			startLock.lock();
			try {
				while (!startEnded)
					waiting.await();
				waiting.signal();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				startLock.unlock();
			}
		}
		instance().toolbarPanel.getChildren().add(toolbar);
		instance().stage.sizeToScene();
		System.out.println("END addToolbar");
	}
	
	/**
	 * Set a panel to the left
	 * 
	 * @param panel
	 *            The panel
	 */
	public static void setLeftPanel(Pane panel) {
		instance();
		if (!startEnded) {
			startLock.lock();
			try {
				while (!startEnded)
					waiting.await();
				waiting.signal();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				startLock.unlock();
			}
		}
		instance().splitPane.getItems().set(0, panel);
		instance().stage.sizeToScene();
	}
	
	/**
	 * Set a panel to the right
	 * 
	 * @param panel
	 *            The panel
	 */
	public static void setRightPanel(Pane panel) {
		instance();
		if (!startEnded) {
			startLock.lock();
			try {
				while (!startEnded)
					waiting.await();
				waiting.signal();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				startLock.unlock();
			}
		}
		instance().splitPane.getItems().set(1, panel);
		instance().stage.sizeToScene();
	}
	
	/**
	 * Return the unique instance of MainWindow, may create it.
	 * 
	 * @return instance
	 */
	public static MainWindow instance() {
		instanceLock.lock();
		if (instance == null) {
			instance = new MainWindow();
		}
		instanceLock.unlock();
		return instance;
	}
	
	/**
	 * Add a panel with a tab
	 * 
	 * @param title
	 *            The title of the tab
	 * @param panel
	 *            The panel to add
	 */
	public static void addTabbedPanel(String title, Pane panel) {
		instance();
		if (!startEnded) {
			startLock.lock();
			try {
				while (!startEnded)
					waiting.await();
				waiting.signal();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				startLock.unlock();
			}
		}
		instance().tabbedPanel.getTabs().add(new Tab(title, panel));
		instance().stage.sizeToScene();
	}
}
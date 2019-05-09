package fr.irit.smac.amak.ui;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.InstanceAlreadyExistsException;

import fr.irit.smac.amak.Information;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
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
 * @author of the original version (the Swing one) Alexandre Perles, Marcillaud
 *         Guilhem
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
	public Stage stage;
	/**
	 * The panel which contains the toolbar
	 */
	public FlowPane toolbarPanel;
	/**
	 * The main panel is split in two panels. This allows to dynamically resize
	 * these two panels.
	 */
	public SplitPane splitPane;
	/**
	 * The menu bar of the window
	 */
	public MenuBar menuBar;
	/**
	 * The option menu
	 */
	public Menu optionsMenu;
	/**
	 * The panel in which panels with tab can be added
	 */
	public TabPane tabbedPanel;
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
	 * Boolean used in instance() function to forbid multiple calls of
	 * Application.launch()
	 */
	private static boolean isCreated = false;

	/**
	 * Create the frame.
	 * 
	 * @throws InstanceAlreadyExistsException
	 *             if the MainWindow has already been instantiated. This constructor
	 *             should be used by the Application of JavaFX only.
	 */
	public MainWindow() throws InstanceAlreadyExistsException {
		super();
		if (instance == null)
			instance = this;
		else
			throw new InstanceAlreadyExistsException();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Platform.runLater(() -> {
			startLock.lock();
			// Creation of scene and root group
			primaryStage.setTitle("AMAK");
			AnchorPane root = new AnchorPane();
			Scene scene = new Scene(root, 450, 300);
			stage = primaryStage;
			stage.setScene(scene);
			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					System.exit(0);
				}
			});

			// Border organization of the scene
			BorderPane organizationPane = new BorderPane();
			root.getChildren().add(organizationPane);
			organizationPane.prefHeightProperty().bind(stage.heightProperty());
			organizationPane.prefWidthProperty().bind(stage.widthProperty());

			// Creation of the toolbar (Bottom)
			toolbarPanel = new FlowPane();
			organizationPane.setBottom(toolbarPanel);

			// Creation of the split pane (Center)
			splitPane = new SplitPane();
			splitPane.setOrientation(Orientation.HORIZONTAL);
			organizationPane.setCenter(splitPane);

			// Creation of the left part of the split pane (Center Left)
			AnchorPane left = new AnchorPane();
			splitPane.getItems().add(left);

			// Creation of the right part of the split pane (Center Right)
			tabbedPanel = new TabPane();
			splitPane.getItems().add(tabbedPanel);

			// Creation of the menu bar (Top)
			menuBar = new MenuBar();
			optionsMenu = new Menu("Options");
			organizationPane.setTop(menuBar);

			// Creation of the close menu item
			MenuItem menuItem = new MenuItem("Close");
			menuItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					System.exit(0);
				}
			});
			optionsMenu.getItems().add(menuItem);

			menuBar.getMenus().add(optionsMenu);
			menuBar.getMenus().add(new Menu("AMAK v" + Information.VERSION));

			startEnded = true;
			waiting.signal();
			startLock.unlock();

			stage.show();
		});
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
		instance.stage.setOnCloseRequest(onClose);
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
		Platform.runLater(() -> instance.stage.getIcons().add(new Image(filename)));
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
		Platform.runLater(() -> instance.stage.setTitle(title));
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
		Platform.runLater(() -> instance.optionsMenu.getItems().add(menuItem));
	}

	/**
	 * Add a toolBar
	 * 
	 * @param toolbar
	 *            The ToolBar.
	 */
	public static void addToolbar(ToolBar toolbar) {
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
		Platform.runLater(() -> instance.toolbarPanel.getChildren().add(toolbar));
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
		Platform.runLater(() -> instance.splitPane.getItems().set(1, panel));
	}

	/**
	 * Return the unique instance of MainWindow, may create it.
	 * 
	 * @return instance
	 */
	public static MainWindow instance() {
		instanceLock.lock();
		if (!isCreated) {
			isCreated = true;
			Thread ui = new Thread(new Runnable() {
				@Override
				public void run() {
					Application.launch(MainWindow.class);
				}
			});
			ui.start();
		}
		instanceLock.unlock();
		return instance;
	}
	
	/**
	 * Indicate if an instance of MainWindow exist.
	 * 
	 * @return true if an instance of MainWindow exist.
	 */
	public static boolean isInstance() {
		return (instance != null);
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
		Platform.runLater(() -> instance.tabbedPanel.getTabs().add(new Tab(title, panel)));
	}
}
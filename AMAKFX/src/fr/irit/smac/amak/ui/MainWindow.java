package fr.irit.smac.amak.ui;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.InstanceAlreadyExistsException;

import fr.irit.smac.amak.Information;
import fr.irit.smac.amak.tools.RunLaterHelper;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
	 * The window itself
	 */
	public Stage stage;
	/**
	 * The panel which contains the toolbar
	 */
	public ToolBar toolbarPanel;

	/**
	 * The main pane of AMAK
	 */
	public BorderPane organizationPane;
	
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
	 * A semaphore that will bloc instance() if no instance exist yet
	 */
	private static Semaphore instanceCreated = new Semaphore(0);

	/**
	 * Create the frame.
	 * 
	 * @throws InstanceAlreadyExistsException
	 *             if the MainWindow has already been instantiated. This constructor
	 *             should be used by the Application of JavaFX only.
	 */
	public MainWindow() throws InstanceAlreadyExistsException {
		super();
		if (instance == null) {
			instance = this;
			instanceCreated.release();
		} else {
			throw new InstanceAlreadyExistsException();
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		startLock.lock();
		VBox root = new VBox();
		
		// Creation of the menu bar (Top)
		menuBar = new MenuBar();
		optionsMenu = new Menu("Options");
		root.getChildren().add(menuBar);
		
		// Border organization
		organizationPane = new BorderPane();
		organizationPane.setMinSize(200, 200); //that way we avoid 0 size, which can cause problems
		root.getChildren().add(organizationPane);
		VBox.setVgrow(organizationPane, Priority.ALWAYS);
		
		// Creation of scene
		primaryStage.setTitle("AMAK");
		Scene scene = new Scene(root, 450, 300);
		stage = primaryStage;
		stage.setScene(scene);
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				System.exit(0);
			}
		});

		// Creation of the toolbar (Bottom)
		toolbarPanel = new ToolBar();
		organizationPane.setBottom(toolbarPanel);

		// Creation of the right part of the split pane (Center Right)
		tabbedPanel = new TabPane();
		organizationPane.setCenter(tabbedPanel);

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
		menuBar.getMenus().add(new Menu("AMAKFX v" + Information.VERSION));

		startEnded = true;
		waiting.signal();
		startLock.unlock();

		stage.show();
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
		RunLaterHelper.runLater(() -> instance.stage.getIcons().add(new Image(filename)));
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
		RunLaterHelper.runLater(() -> instance.stage.setTitle(title));
	}

	/**
	 * Add a menu on the menu bar
	 * 
	 * @param menu
	 */
	public static void addMenu(Menu menu) {
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
		RunLaterHelper.runLater(() -> instance.menuBar.getMenus().add(menu));
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
		RunLaterHelper.runLater(() -> instance.optionsMenu.getItems().add(menuItem));
	}

	/**
	 * Add a tool in the toolbar.
	 * 
	 * @param tool
	 */
	public static void addToolbar(Node tool) {
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
		RunLaterHelper.runLater(() -> instance.toolbarPanel.getItems().add(tool));
	}

	/**
	 * Set a panel to the left
	 * 
	 * @param panel
	 *            The panel
	 */
	public static void setLeftPanel(Node panel) {
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
		RunLaterHelper.runLater(() -> instance.organizationPane.setLeft(panel));
	}

	/**
	 * Set a panel to the right
	 * 
	 * @param panel
	 *            The panel
	 */
	public static void setRightPanel(Node panel) {
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
		RunLaterHelper.runLater(() -> instance.organizationPane.setRight(panel));
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
		try {
			instanceCreated.acquire();
			instanceCreated.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
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
	public static void addTabbedPanel(String title, Node panel) {
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
		Tab t = new Tab(title, panel);
		RunLaterHelper.runLater(() -> instance.tabbedPanel.getTabs().add(t));
	}
}
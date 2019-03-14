package fr.irit.smac.amak.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import fr.irit.smac.amak.ui.drawables.Drawable;
import fr.irit.smac.amak.ui.drawables.DrawableImage;
import fr.irit.smac.amak.ui.drawables.DrawablePoint;
import fr.irit.smac.amak.ui.drawables.DrawableRectangle;
import fr.irit.smac.amak.ui.drawables.DrawableString;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;

/**
 * 
 * Vectorial UI: This class allows to create dynamic rendering with zoom and
 * move capacities
 * 
 * @author of original version (the Swing one) perles
 *
 */
public class VUI {
	/**
	 * List of objects currently being drawn by the VUI
	 */
	private List<Drawable> drawables = new ArrayList<>();
	/**
	 * Lock to avoid concurrent modification on the list {@link #drawables}
	 */
	private ReentrantLock drawablesLock = new ReentrantLock();

	/**
	 * A static map to facilitate access to different instances of VUI
	 */
	private static Map<String, VUI> instances = new HashMap<>();

	/**
	 * The horizontal offset of the drawing zone. Used to allow the user to move the
	 * view.
	 */
	private double worldOffsetX;

	/**
	 * The vertical offset of the drawing zone. Used to allow the user to move the
	 * view.
	 */
	private double worldOffsetY;

	/**
	 * The last horizontal position of the mouse when dragging
	 */
	protected Double lastDragX;

	/**
	 * The last vertical position of the mouse when dragging
	 */
	protected Double lastDragY;

	/**
	 * The main panel of the VUI
	 */
	private BorderPane panel;

	/**
	 * The canvas on which all is drawn
	 */
	private Group canvas;

	/**
	 * Label aiming at showing information about the VUI (zoom and offset)
	 */
	private Label statusLabel;

	/**
	 * The default value of the {@link #zoom}
	 */
	private double defaultZoom = 100;
	/**
	 * The default horizontal position of the view
	 */
	private double defaultWorldCenterX = 0;
	/**
	 * The default vertical position of the view
	 */
	private double defaultWorldCenterY = 0;
	/**
	 * The value of the zoom. 100 means 1/1 scale
	 */
	protected double zoom = defaultZoom;

	/**
	 * The horizontal position of the view
	 */
	private double worldCenterX = defaultWorldCenterX;

	/**
	 * The vertical position of the view
	 */
	private double worldCenterY = defaultWorldCenterY;
	
	/**
	 * The associated scene
	 */
	private Scene scene;
	
	/**
	 * Get the default VUI
	 * 
	 * @return the default VUI
	 */
	public static VUI get() {
		return get("Default");
	}

	/**
	 * Create or get a VUI
	 * 
	 * @param id
	 *            The unique id of the VUI
	 * @return The VUI with id "id"
	 */
	public static VUI get(String id) {
		System.out.println("get id");
		if (!instances.containsKey(id)) {
			System.out.println("not contains");
			VUI value = new VUI(id);
			System.out.println("Created of VUI");
			instances.put(id, value);
			System.out.println("Add id to instances");
			return value;
		}
		System.out.println("No need to create");
		return instances.get(id);
	}

	/**
	 * Constructor of the VUI. This one is private as it can only be created through
	 * static method.
	 * 
	 * @param title
	 *            The title used for the vui
	 */
	private VUI(String title) {
		System.out.println("CONSTR VUI");
		panel = new BorderPane();
		System.out.println("New BorderPane created");
		
		HBox statusPanel = new HBox();
		System.out.println("New HBox created");
		statusLabel = new Label("status");
		System.out.println("New Label created");
		statusLabel.setTextAlignment(TextAlignment.LEFT);
		System.out.println("After set alignment");
		statusPanel.getChildren().add(statusLabel);
		System.out.println("statusLabel added statusPanel");
		Button resetButton = new Button("Reset");
		System.out.println("New Button created");
		resetButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				zoom = defaultZoom;
				worldCenterX = defaultWorldCenterX;
				worldCenterY = defaultWorldCenterY;
				updateCanvas();
			}
		});
		System.out.println("New action associated to the button");
		statusPanel.getChildren().add(resetButton);
		System.out.println("resetButton added to statusPanel");
		panel.setBottom(statusPanel);
		System.out.println("statusPanel added to panel");
		MainWindow.addTabbedPanel("VUI #" + title, panel);
		System.out.println("tabbedPanel added to MainWindow");
	}

	/**
	 * Convert a distance in the world to its equivalent on the screen
	 * 
	 * @param d
	 *            the in world distance
	 * @return the on screen distance
	 */
	public double worldToScreenDistance(double d) {
		return d * getZoomFactor();
	}

	/**
	 * Convert a distance on the screen to its equivalent in the world
	 * 
	 * @param d
	 *            the on screen distance
	 * @return the in world distance
	 */
	public double screenToWorldDistance(double d) {
		return d / getZoomFactor();
	}

	/**
	 * Convert a X in the world to its equivalent on the screen
	 * 
	 * @param x
	 *            the X in world
	 *
	 * @return the X on screen distance
	 */
	public double worldToScreenX(double x) {
		return (x + getWorldOffsetX()) * getZoomFactor();
	}

	/**
	 * A value that must be multiplied to scale objects
	 * 
	 * @return the zoom factor
	 */
	public double getZoomFactor() {
		return zoom / 100;
	}

	/**
	 * Convert a Y in the world to its equivalent on the screen
	 * 
	 * @param y
	 *            the Y in world
	 *
	 * @return the Y on screen distance
	 */
	public double worldToScreenY(double y) {
		return (y + getWorldOffsetY()) * getZoomFactor();
	}

	/**
	 * Convert a X on the screen to its equivalent in the world
	 * 
	 * @param x
	 *            the X on screen
	 *
	 * @return the X in the world distance
	 */
	public double screenToWorldX(double x) {
		return x / getZoomFactor() - getWorldOffsetX();
	}

	/**
	 * Convert a Y on the screen to its equivalent in the world
	 * 
	 * @param y
	 *            the Y on screen
	 *
	 * @return the Y in the world distance
	 */
	public double screenToWorldY(double y) {
		return y / getZoomFactor() - getWorldOffsetY();
	}

	/**
	 * Add an object to the VUI and repaint it
	 * 
	 * @param d
	 *            the new object
	 */
	public void add(Drawable d) {
		d.setPanel(this);
		drawablesLock.lock();
		drawables.add(d);
		drawablesLock.unlock();
		updateCanvas();
	}

	/**
	 * Refresh the canvas
	 */
	public void updateCanvas() {
		statusLabel.setText(String.format("Zoom: %.2f Center: (%.2f,%.2f)", zoom, worldCenterX, worldCenterY));
	}

	/**
	 * Get the width of the canvas
	 * 
	 * @return the canvas width
	 */
	public double getCanvasWidth() {
		return scene.getWidth();
	}

	/**
	 * Get the height of the canvas
	 * 
	 * @return the canvas height
	 */
	public double getCanvasHeight() {
		return scene.getHeight();
	}

	/**
	 * Get the value that must be added to the X coordinate of in world object
	 * 
	 * @return the X offset
	 */
	public double getWorldOffsetX() {
		return worldOffsetX;
	}

	/**
	 * Set the value that must be added to the X coordinate of in world object
	 * 
	 * @param offsetX
	 *            the X offset
	 */
	public void setWorldOffsetX(double offsetX) {
		this.worldOffsetX = offsetX;
	}

	/**
	 * Get the value that must be added to the Y coordinate of in world object
	 * 
	 * @return the Y offset
	 */
	public double getWorldOffsetY() {
		return worldOffsetY;
	}

	/**
	 * Set the value that must be added to the Y coordinate of in world object
	 * 
	 * @param offsetY
	 *            the Y offset
	 */
	public void setWorldOffsetY(double offsetY) {
		this.worldOffsetY = offsetY;
	}

	/**
	 * Create a point and start rendering it
	 * 
	 * @param dx
	 *            the x coordinate
	 * @param dy
	 *            the y coordinate
	 * @return the point object
	 */
	public DrawablePoint createPoint(double dx, double dy) {
		DrawablePoint drawablePoint = new DrawablePoint(this, dx, dy);
		add(drawablePoint);
		return drawablePoint;
	}

	/**
	 * Create a rectangle and start rendering it
	 * 
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 * @param w
	 *            the width
	 * @param h
	 *            the height
	 * @return the rectangle object
	 */
	public DrawableRectangle createRectangle(double x, double y, double w, double h) {
		DrawableRectangle d = new DrawableRectangle(this, x, y, w, h);
		add(d);
		return d;
	}

	/**
	 * Set the default configuration of the view
	 * 
	 * @param zoom
	 *            the initial zoom value
	 * @param worldCenterX
	 *            the initial X center value
	 * @param worldCenterY
	 *            the initial Y center value
	 */
	public void setDefaultView(double zoom, double worldCenterX, double worldCenterY) {
		this.zoom = zoom;
		this.worldCenterX = worldCenterX;
		this.worldCenterY = worldCenterY;
		this.defaultZoom = zoom;
		this.defaultWorldCenterX = worldCenterX;
		this.defaultWorldCenterY = worldCenterY;
	}

	/**
	 * Create an image and start rendering it
	 * 
	 * @param dx
	 *            the x coordinate
	 * @param dy
	 *            the y coordinate
	 * @param filename
	 *            the filename of the image
	 * @return the created image
	 */
	public DrawableImage createImage(double dx, double dy, String filename) {
		DrawableImage image = new DrawableImage(this, dx, dy, filename);
		add(image);
		return image;
	}

	/**
	 * Create a string and start rendering it
	 * 
	 * @param dx
	 *            the x coordinate
	 * @param dy
	 *            the y coordinate
	 * @param text
	 *            the text to display
	 * @return the created string
	 */
	public DrawableString createString(int dx, int dy, String text) {
		DrawableString ds = new DrawableString(this, dx, dy, text);
		add(ds);
		return ds;
	}

	/**
	 * Set the canvas of the VUI
	 * 
	 * @param scene
	 *            The associated scene
	 * @param canvas
	 *            The associated canvas
	 */
	public void setCanvas(Scene scene, Group root) {
		this.scene = scene;
		canvas = root;
		
		canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				lastDragX = event.getX();
				lastDragY = event.getY();
			}
		});
		canvas.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				lastDragX = null;
				lastDragY = null;
			}
		});
		canvas.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				try {
				worldCenterX += screenToWorldDistance(event.getX() - lastDragX);
				worldCenterY += screenToWorldDistance(event.getY() - lastDragY);
				lastDragX = event.getX();
				lastDragY = event.getY();
				updateCanvas();
				} catch (Exception ez) {
					// Catch exception occurring when mouse is out of the canvas
				}
			}
		});
		
		canvas.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				double wdx = screenToWorldDistance(scene.getWidth() / 2 - event.getX());
				double wdy = screenToWorldDistance(scene.getHeight() / 2 - event.getY());
				
				zoom -= event.getTouchCount() * 10;
				if (zoom < 10)
					zoom = 10;
				
				double wdx2 = screenToWorldDistance(scene.getWidth() / 2 - event.getX());
				double wdy2 = screenToWorldDistance(scene.getHeight() / 2 - event.getY());
				worldCenterX -= wdx2 - wdx;
				worldCenterY -= wdy2 - wdy;
				updateCanvas();
			}
		});
		
		canvas.prefWidth(800);
		canvas.prefHeight(600);
		panel.setCenter(canvas);
	}
	
	/**
	 * Use to get the associated group
	 */
	public Group getGroup() {
		return canvas;
	}
}

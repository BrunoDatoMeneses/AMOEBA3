package fr.irit.smac.amak.ui.drawables;

import java.util.HashMap;
import java.util.Map;

import fr.irit.smac.amak.tools.Log;
import fr.irit.smac.amak.ui.VUI;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DrawableImage extends Drawable {

	private String filename;
	private ImageView image;
	private static Map<String, Image> loadedImages = new HashMap<>();

	public DrawableImage(VUI vui, double dx, double dy, String filename) {
		super(vui, dx, dy, 0, 0);
		image = new ImageView(new Image(filename));
		this.setFilename(filename);
		Platform.runLater(() -> {
			// Update UI here.
			vui.getCanvas().getChildren().add(image);
		});
	}

	private Image loadByFilename(String filename) throws NullPointerException, IllegalArgumentException {
		if (!loadedImages.containsKey(filename)) {
			loadedImages.put(filename, new Image(filename));
		}
		return loadedImages.get(filename);
	}

	public void setFilename(String filename) {
		this.filename = filename;
		try {
			image.setImage(loadByFilename(this.filename));
		} catch (NullPointerException | IllegalArgumentException e) {
			Log.error("AMAK", "Can't find/load the file %s", this.filename);
			try {
				image.setImage(loadByFilename("file:Resources/unavailable.png"));
			} catch (NullPointerException | IllegalArgumentException e1) {
				Log.fatal("AMAK", "Can't load resources belonging to AMAK. Bad things may happen.");
			}
		}
		setWidth(this.image.getFitWidth());
		setHeight(this.image.getFitHeight());
	}

	@Override
	public void _onDraw() {
		image.setX(left() - getWidth() / 2);
		image.setY(top() - getHeight() / 2);
		image.setFitWidth(getRenderedWidth());
		image.setFitHeight(getRenderedHeight());
	}
}
package fr.irit.smac.amak.ui.drawables;

import java.util.HashMap;
import java.util.Map;


import fr.irit.smac.amak.tools.Log;
import fr.irit.smac.amak.ui.VUI;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Affine;

public class DrawableImage extends Drawable {

	private String filename;
	private ImageView image;
	private static Map<String, Image> loadedImages = new HashMap<>();

	public DrawableImage(VUI vui, double dx, double dy, String filename) {
		super(vui, dx, dy, 0, 0);
		this.setFilename(filename);
		image = new ImageView(new Image(filename));
		vui.getCanvas().getChildren().add(image);
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
				image.setImage(loadByFilename("Resources/unavailable.png"));
			} catch (NullPointerException | IllegalArgumentException e1) {
				Log.fatal("AMAK", "Can't load resources belonging to AMAK. Bad things may happen.");
			}
		}
		setWidth(this.image.getFitWidth());
		setHeight(this.image.getFitHeight());
	}

	@Override
	public void _onDraw() {
		Affine identity = new Affine();
		Affine trans = new Affine();
		trans.setToTransform(identity);
		trans.setTx(left());
		trans.setTy(top());
		trans.appendRotation(getAngle(), getRenderedWidth() / 2, getRenderedHeight() / 2);
		image.getTransforms().addAll(trans);
		if (!isFixed()) {
			image.setFitWidth(vui.getZoomFactor()*getWidth()/this.image.getFitWidth());
			image.setFitHeight(vui.getZoomFactor()*getHeight()/this.image.getFitHeight());
		}
	}
}

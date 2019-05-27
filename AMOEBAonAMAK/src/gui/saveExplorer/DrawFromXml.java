package gui.saveExplorer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import fr.irit.smac.amak.ui.VUI;
import fr.irit.smac.amak.ui.drawables.DrawableRectangle;
import javafx.scene.paint.Color;

public class DrawFromXml {
	private static final String PERCEPT_NODE = "Sensor";
	
	public static void draw(VUI vui, Path path) {
		SAXBuilder sxb = new SAXBuilder();
		Document doc;
		try {
			doc = sxb.build(path.toFile());

			Element rootElement = doc.getRootElement();
			loadPresetContexts(rootElement, vui);
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void loadPresetContexts(Element systemElement, VUI vui) {
		Element presetContextsElement = systemElement.getChild("PresetContexts");
		
		Element lastPerceptionsAndActionState = presetContextsElement.getChild("LastPerceptionsAndActionState");
		List<String> keys = null;
		if(lastPerceptionsAndActionState != null) {
			HashMap<String, Double> perceptionAndAction = new HashMap<>();
			for(Attribute att : lastPerceptionsAndActionState.getAttributes()) {
				perceptionAndAction.put(att.getName(), Double.valueOf(att.getValue()));
			}
			keys = new ArrayList<>(perceptionAndAction.keySet());
			keys.remove("oracle");
			keys.sort(Comparator.comparing(String::toString));
			vui.createAndAddPoint(perceptionAndAction.get(keys.get(0)), perceptionAndAction.get(keys.get(1)));
		}
		
		List<Element> contextsElement = presetContextsElement.getChildren("Context");
		

		for (Element contextElement : contextsElement) {
			loadContext(contextElement, vui, keys.get(0), keys.get(1));
		}
		vui.updateCanvas();
	}
	
	private static void loadContext(Element contextElement, VUI vui, String d1, String d2) {
		// -- Load Ranges
		Element rangesElement = contextElement.getChild("Ranges");
		Map<String, Double> starts = new HashMap<>();
		Map<String, Double> ends = new HashMap<>();
		loadRanges(rangesElement, starts, ends);
		double l1 = ends.get(d1)-starts.get(d1);
		double l2 = ends.get(d2)-starts.get(d2);
		double x = starts.get(d1);
		double y = starts.get(d2);
		DrawableRectangle rectangle = new DrawableRectangle(x, y, l1, l2);
		rectangle.setColor(Color.SALMON);
		rectangle.getNode().setOpacity(0.7);
		rectangle.getNode().setStyle("-fx-stroke: black; -fx-stroke-width: 1;");
		vui.add(rectangle);		
	}
	
	private static void loadRanges(Element elemRanges, Map<String, Double> starts, Map<String, Double> ends) {
		for (Element rangeElement : elemRanges.getChildren()) {
			String perceptName = rangeElement.getAttributeValue(PERCEPT_NODE);
			double start = Double.valueOf(rangeElement.getAttributeValue("Start"));
			double end = Double.valueOf(rangeElement.getAttributeValue("End"));

			starts.put(perceptName, start);
			ends.put(perceptName, end);
		}
	}
}

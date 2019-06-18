package gui.saveExplorer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import agents.percept.Percept;
import fr.irit.smac.amak.ui.VUI;
import fr.irit.smac.amak.ui.drawables.Drawable;
import fr.irit.smac.amak.ui.drawables.DrawableRectangle;
import gui.DimensionSelector;
import gui.utils.ContextColor;
import javafx.scene.paint.Color;

/**
 * Class providing methods to draw a preview of an AMOEBA onto a VUI
 * @author Hugo
 *
 */
public class DrawFromXml {
	private static final String PERCEPT_NODE = "Sensor";
	
	/**
	 * Create a {@link DimensionSelector} based on a save file
	 * @param path path to the save file
	 * @return
	 */
	public static DimensionSelector createDimensionSelector(Path path) {
		SAXBuilder sxb = new SAXBuilder();
		Document doc;
		Map<String, Percept> perceptsByName= new HashMap<>();
		try {
			doc = sxb.build(path.toFile());

			Element rootElement = doc.getRootElement();
			loadStartingAgents(rootElement, perceptsByName);
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
		return new DimensionSelector(new ArrayList<Percept>(perceptsByName.values()), null);
	}
	
	private static void loadStartingAgents(Element systemElement, Map<String, Percept> perceptsByName) {
		Element startingAgentsElement = systemElement.getChild("StartingAgents");
		List<Element> agentsElement = startingAgentsElement.getChildren();

		for (Element startingAgentElement : agentsElement) {
			switch (startingAgentElement.getName()) {
			case PERCEPT_NODE:
				loadSensor(startingAgentElement, perceptsByName);
				break;
			default:
			}
		}
	}
	
	private static void loadSensor(Element sensorElement, Map<String, Percept> perceptsByName) {
		Percept percept = new Percept(null);
		percept.setName(sensorElement.getAttributeValue("Name"));
		boolean isEnum = Boolean.valueOf(sensorElement.getAttributeValue("Enum"));
		percept.setEnum(isEnum);

		perceptsByName.put(percept.getName(), percept);
	}
	
	/**
	 * Draw the preview of an AMOEBA from a save file onto a VUI.<br/>
	 * You might want to use a {@link DimensionSelector} for the dim1 and dim2 parameters.
	 * @param vui {@link VUI} on which the preview will be drawn
	 * @param path path to the save file
	 * @param dim1 percept name for the 1st dimension (usually x)
	 * @param dim2 percept name for the 2nd dimension (usually y)
	 * @see DrawFromXml#createDimensionSelector(Path)
	 */
	public static void draw(VUI vui, Path path, String dim1, String dim2) {
		SAXBuilder sxb = new SAXBuilder();
		Document doc;
		try {
			doc = sxb.build(path.toFile());

			Element rootElement = doc.getRootElement();
			loadPresetContexts(rootElement, vui, dim1, dim2);
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void loadPresetContexts(Element systemElement, VUI vui, String dim1, String dim2) {
		Element presetContextsElement = systemElement.getChild("PresetContexts");
		
		Element lastPerceptionsAndActionState = presetContextsElement.getChild("LastPerceptionsAndActionState");
		HashMap<String, Double> perceptionAndAction = new HashMap<>();
		for(Attribute att : lastPerceptionsAndActionState.getAttributes()) {
			perceptionAndAction.put(att.getName(), Double.valueOf(att.getValue()));
		}
		
		List<Element> contextsElement = presetContextsElement.getChildren("Context");
		
		Set<String> old = vui.getDrawables().parallelStream().map(Drawable::getName).collect(Collectors.toSet());
		Set<String> present = new HashSet<>();
		// add or update drawable present in the xml file
		for (Element contextElement : contextsElement) {
			String name = "Context : "+contextElement.getAttributeValue("Name");
			present.add(name);
			loadContext(contextElement, name, vui, dim1, dim2);
		}
		old.removeAll(present); // set of drawable no longer present
		vui.getDrawables().stream().filter(d -> old.contains(d.getName())).
			collect(Collectors.toSet()). // make a copy to avoid modification of the stream
			forEach(Drawable::delete); // delete old drawable
		
		Drawable point = vui.createAndAddPoint(0, 0).setShowInExplorer(false);
		point.move(perceptionAndAction.get(dim1), perceptionAndAction.get(dim2));
		
		
		vui.updateCanvas();
	}
	
	private static void loadContext(Element contextElement, String name, VUI vui, String d1, String d2) {
		// Load Ranges
		Element rangesElement = contextElement.getChild("Ranges");
		Map<String, Double> starts = new HashMap<>();
		Map<String, Double> ends = new HashMap<>();
		loadRanges(rangesElement, starts, ends);
		double l1 = ends.get(d1)-starts.get(d1);
		double l2 = ends.get(d2)-starts.get(d2);
		double x = starts.get(d1);
		double y = starts.get(d2);
		
		// Draw Ranges as 2D rectangle
		Optional<Drawable> optRect = vui.getDrawables().stream().filter(d -> name.equals(d.getName())).findAny();
		DrawableRectangle rectangle;
		if(optRect.isPresent()) {
			rectangle = (DrawableRectangle) optRect.get();
			rectangle.setWidth(l1);
			rectangle.setHeight(l2);
			rectangle.move(x, y);
		} else {
			rectangle = vui.createAndAddRectangle(x, y, l1, l2);
		}
		Element localModelElement = contextElement.getChild("LocalModel");
		List<Double> coefs = new ArrayList<>();
		loadLocalModel(localModelElement, rectangle, coefs);
		
		// Load and set contexts infos
		rectangle.setName("Context : "+contextElement.getAttributeValue("Name"));
		String coefsString = "";
		for(Double c : coefs) {
			coefsString+=" "+c+" ";
		}
		String attributesString = "";
		for(Attribute att : contextElement.getAttributes()) {
			attributesString += att.getName()+" : "+att.getValue()+"\n";
		}
		rectangle.setInfo(
			name+"\n"+
			"Model : "+coefsString+"\n"+
			attributesString
		);	
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
	
	private static void loadLocalModel(Element localModelElement, DrawableRectangle rectangle, List<Double> coefs) {
		for(Element e : localModelElement.getChild("Coefs").getChildren()) {
			coefs.add(Double.valueOf(e.getAttributeValue("v")));
		}
		Double[] c = ContextColor.colorFromCoefs(coefs);
		rectangle.setColor(new Color(c[0], c[1], c[2], 90d / 255d));
	}
}

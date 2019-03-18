package kernel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.IllegalDataException;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import agents.context.Context;
import agents.context.Range;
import agents.head.Head;
import agents.percept.Percept;
import experiments.F_XY_System;
import fr.irit.smac.amak.Configuration;

/**
 * @author Labbeti
 */
public class SaveState implements ISaveState {
	// Static final values
	private static final String HEAD_NODE = "Controller";
	private static final String PERCEPT_NODE = "Sensor";
	private static final String CONTEXT_NODE = "Context";

	// Members
	private AMOEBA amoeba;
	private Map<String, Percept> perceptsByName;

	// -------------------- Public Methods --------------------

	public SaveState(AMOEBA amoeba) {
		this.amoeba = amoeba;
		this.perceptsByName = null;
	}

	public void load(File file) {
		amoeba.clear();

		SAXBuilder sxb = new SAXBuilder();
		Document doc;
		try {
			doc = sxb.build(file);

			loadConfiguration(doc);
			loadStartingAgents(doc);
			if (amoeba.isLoadPresetContext()) {
				loadPresetContexts(doc);
			}
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
	}

	public void save(File file) {
		Element elemSystem = new Element("System");
		Document doc = new Document(elemSystem);

		saveConfiguration(doc);
		saveStartingAgents(doc);
		savePresetContexts(doc);

		XMLOutputter xml = new XMLOutputter();
		xml.setFormat(Format.getPrettyFormat());

		try {
			xml.output(doc, new FileWriter(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// -------------------- Private methods --------------------

	private void loadConfiguration(Document doc) {
		Element elemSystem = doc.getRootElement();
		Element elemConfiguration = elemSystem.getChild("Configuration");
		Element elemLearning = elemConfiguration.getChild("Learning");

		try {
			// boolean allowed = elemLearning.getAttribute("allowed").getBooleanValue(); //
			// TODO ?
			boolean creationOfNewContext = elemLearning.getAttribute("creationOfNewContext").getBooleanValue();
			boolean loadPresetContext = elemLearning.getAttribute("loadPresetContext").getBooleanValue();

			amoeba.setCreationOfNewContext(creationOfNewContext);
			amoeba.setLoadPresetContext(loadPresetContext);
		} catch (DataConversionException e) {
			e.printStackTrace();
		}
	}

	private void loadStartingAgents(Document doc) {
		Element elemSystem = doc.getRootElement();
		Element elemStartingAgents = elemSystem.getChild("StartingAgents");
		List<Element> elemAgents = elemStartingAgents.getChildren();
		this.perceptsByName = new HashMap<>();

		for (Element elemAgent : elemAgents) {
			if (elemAgent.getName().equals(HEAD_NODE)) {
				Head head = new Head(amoeba);
				head.setName(elemAgent.getAttributeValue("Name"));
				head.setNoCreation(!amoeba.isCreationOfNewContext());
				amoeba.setHead(head);
			} else if (elemAgent.getName().equals(PERCEPT_NODE)) {
				Percept percept = new Percept(amoeba);
				percept.setName(elemAgent.getAttributeValue("Name"));
				this.perceptsByName.put(percept.getName(), percept);
			}
		}
	}

	private void loadPresetContexts(Document doc) {
		Element elemSystem = doc.getRootElement();
		Element elemPresetContexts = elemSystem.getChild("PresetContexts");
		List<Element> elemContexts = elemPresetContexts.getChildren();

		for (Element elemContext : elemContexts) {
			String contextName = elemContext.getAttributeValue("Name");
			Head head = amoeba.getHeads().get(0);

			Element elemRanges = elemContext.getChild("Ranges");
			Map<Percept, Double> starts = new HashMap<>();
			Map<Percept, Double> ends = new HashMap<>();
			
			for (Element elemRange : elemRanges.getChildren()) {
				String perceptName = elemRange.getAttributeValue(PERCEPT_NODE);
				double start = Double.valueOf(elemRange.getAttributeValue("Start"));
				double end = Double.valueOf(elemRange.getAttributeValue("End"));

				if (!perceptsByName.containsKey(perceptName)) {
					System.out.println(perceptsByName);
					throw new IllegalDataException("Found unknown percept name " + perceptName + " in file\n");
				}
				Percept percept = perceptsByName.get(perceptName);
				
				starts.put(percept, start);
				ends.put(percept, end);
			}
			
			new Context(amoeba, head, contextName, starts, ends);
		}
	}

	private void saveConfiguration(Document doc) {
		Element elemConfiguration = new Element("Configuration");
		Element elemLearning = new Element("Learning");
		List<Attribute> attributes = new ArrayList<>();

		// attributes.add(new Attribute("allowed", String.valueOf(true))); // TODO ?
		attributes.add(new Attribute("creationOfNewContext", String.valueOf(amoeba.isCreationOfNewContext())));
		attributes.add(new Attribute("loadPresetContext", String.valueOf(amoeba.isLoadPresetContext())));
		elemLearning.setAttributes(attributes);
		elemConfiguration.addContent(elemLearning);
		doc.getRootElement().addContent(elemConfiguration);
	}

	private void saveStartingAgents(Document doc) {
		Element elemStartingAgents = new Element("StartingAgents");
		List<Head> heads = amoeba.getHeads();
		List<Percept> percepts = amoeba.getPercepts();

		for (Head head : heads) {
			Element elemController = new Element(HEAD_NODE);
			List<Attribute> attributes = new ArrayList<>();

			attributes.add(new Attribute("Name", head.getName()));
			elemController.setAttributes(attributes);
			elemStartingAgents.addContent(elemController);
		}

		for (Percept percept : percepts) {
			Element elemSensor = new Element(PERCEPT_NODE);
			List<Attribute> attributes = new ArrayList<>();

			attributes.add(new Attribute("Name", percept.getName()));
			elemSensor.setAttributes(attributes);
			elemStartingAgents.addContent(elemSensor);
		}

		doc.getRootElement().addContent(elemStartingAgents);
	}

	private void savePresetContexts(Document doc) {
		Element elemPresetContexts = new Element("PresetContexts");
		List<Context> contexts = amoeba.getContexts();
		
		elemPresetContexts.setAttribute(new Attribute("nb", String.valueOf(contexts.size())));

		for (Context context : contexts) {
			Element elemContext = new Element(CONTEXT_NODE);
			HashMap<Percept, Range> ranges = context.getRanges();
			Element eRanges = new Element("Ranges");

			for (Entry<Percept, Range> entry : ranges.entrySet()) {
				Percept percept = entry.getKey();
				Range range = entry.getValue();

				List<Attribute> attributes = new ArrayList<>();
				attributes.add(new Attribute(PERCEPT_NODE, percept.getName()));
				attributes.add(new Attribute("Start", String.valueOf(range.getStart())));
				attributes.add(new Attribute("End", String.valueOf(range.getEnd())));

				Element eRange = new Element("Range").setAttributes(attributes);
				eRanges.addContent(eRange);
			}
			elemContext.addContent(eRanges);

			List<Attribute> eAgentAttributes = new ArrayList<>();
			eAgentAttributes.add(new Attribute("Name", String.valueOf(context.getName())));
			elemContext.setAttributes(eAgentAttributes);
			elemPresetContexts.addContent(elemContext);
		}

		doc.getRootElement().addContent(elemPresetContexts);
	}

	// -------------------- DEBUG methods --------------------
	
	public static void main(String[] args) {
		File file = new File("./resources/twoDimensionsLauncher.xml");
		
		World world = new World();
		StudiedSystem studiedSystem = new F_XY_System(50.0);
		Configuration.commandLineMode = false;
		AMOEBA amoeba = new AMOEBA(world, file, studiedSystem);
		SaveState saveState = new SaveState(amoeba);
		File srcFile = new File("./resources/save1.xml");
		
		// DEBUG
		System.out.println("DEBUG: before load: contexts = " + amoeba.getContexts().size() + "/" + amoeba.getAgents().size());
		saveState.load(srcFile);

		amoeba.setDataForErrorMargin(1000, 5, 0.4, 0.1, 40, 80);
		amoeba.setDataForInexactMargin(500, 2.5, 0.2, 0.05, 40, 80);
		amoeba.setNoRenderUpdate(false);
		amoeba.allowGraphicalScheduler(true);
		
		System.out.println("DEBUG: Begin learning.");
		// Example for using the learn method
		int nbCycle = 1;
		for (int i = 0; i < nbCycle; ++i) {
			studiedSystem.playOneStep();
			amoeba.learn(studiedSystem.getOutput());
		}

		System.out.println("DEBUG: after  load: contexts = " + amoeba.getContexts().size() + "/" + amoeba.getAgents().size());

		// TEST "Labbeti" for XML SAVE
		File trgFile = new File("./resources/save2.xml");
		saveState.save(trgFile);
	}
}

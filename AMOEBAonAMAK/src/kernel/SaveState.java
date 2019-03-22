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
import org.jdom2.Comment;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.IllegalDataException;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import agents.context.Context;
import agents.context.Experiment;
import agents.context.Range;
import agents.context.localModel.LocalModel;
import agents.context.localModel.LocalModelAverage;
import agents.context.localModel.LocalModelFirstExp;
import agents.context.localModel.LocalModelMillerRegression;
import agents.context.localModel.TypeLocalModel;
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

	// -------------------- Constructor --------------------

	public SaveState(AMOEBA amoeba) {
		this.amoeba = amoeba;
		this.perceptsByName = new HashMap<>();
	}

	// -------------------- Public Methods --------------------
	
	/**
	 * Load from an XML file a model for AMOEBA.
	 * 
	 * @param file The file you want to read. It is supposed to be a text file with
	 *             XML format.
	 */
	@Override
	public void loadXML(File file) {
		amoeba.clearAgents();
		perceptsByName.clear();

		SAXBuilder sxb = new SAXBuilder();
		Document doc;
		try {
			doc = sxb.build(file);

			Element rootElement = doc.getRootElement();
			loadConfiguration(rootElement);
			loadStartingAgents(rootElement);
			if (amoeba.isLoadPresetContext()) {
				loadPresetContexts(rootElement);
			}
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save into a file the current model of AMOEBA (agents and some variables in
	 * AMOEBA class).
	 * 
	 * @note Remember AMAK add agents only at the end of a cycle, trying to save
	 *       just after load will result in a XML file without any agents.
	 * @param file The file where you want to insert the model.
	 */
	@Override
	public void saveXML(File file) {
		Element elemSystem = new Element("System");
		Document doc = new Document(elemSystem);

		Element rootElement = doc.getRootElement();
		saveConfiguration(rootElement);
		saveStartingAgents(rootElement);
		savePresetContexts(rootElement);

		XMLOutputter xml = new XMLOutputter();
		xml.setFormat(Format.getPrettyFormat());

		try {
			xml.output(doc, new FileWriter(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// -------------------- Private methods --------------------

	private void loadConfiguration(Element systemElement) {
		Element configurationElement = systemElement.getChild("Configuration");
		Element learningElement = configurationElement.getChild("Learning");

		try {
			boolean creationOfNewContext = learningElement.getAttribute("creationOfNewContext").getBooleanValue();
			boolean loadPresetContext = learningElement.getAttribute("loadPresetContext").getBooleanValue();

			amoeba.setCreationOfNewContext(creationOfNewContext);
			amoeba.setLoadPresetContext(loadPresetContext);
		} catch (DataConversionException e) {
			e.printStackTrace();
		}
	}

	private void loadStartingAgents(Element systemElement) {
		Element startingAgentsElement = systemElement.getChild("StartingAgents");
		List<Element> agentsElement = startingAgentsElement.getChildren();

		int nbHeadsAdded = 0;
		int nbPerceptsAdded = 0;
		for (Element startingAgentElement : agentsElement) {
			if (startingAgentElement.getName().equals(HEAD_NODE)) {
				Head head = new Head(amoeba);
				head.setName(startingAgentElement.getAttributeValue("Name"));
				head.setNoCreation(!amoeba.isCreationOfNewContext());
				
				// TODO : maybe set default values if you cant find errorMargin and inexactMargin ?
				Element errorMarginElement = startingAgentElement.getChild("ErrorMargin");
				double errorAllowed = Double.valueOf(errorMarginElement.getAttributeValue("ErrorAllowed"));
				double augmentationFactorError = Double.valueOf(errorMarginElement.getAttributeValue("AugmentationFactorError"));
				double diminutionFactorError = Double.valueOf(errorMarginElement.getAttributeValue("DiminutionFactorError"));
				double minErrorAllowed = Double.valueOf(errorMarginElement.getAttributeValue("MinErrorAllowed"));
				int nConflictBeforeAugmentation = Integer.valueOf(errorMarginElement.getAttributeValue("NConflictBeforeAugmentation"));
				int nSuccessBeforeDiminution = Integer.valueOf(errorMarginElement.getAttributeValue("NConflictBeforeAugmentation"));
				head.setDataForErrorMargin(errorAllowed, augmentationFactorError, diminutionFactorError, minErrorAllowed, nConflictBeforeAugmentation, nSuccessBeforeDiminution);
				
				Element inexactMarginElement = startingAgentElement.getChild("InexactMargin");
				double inexactAllowed = Double.valueOf(inexactMarginElement.getAttributeValue("InexactAllowed"));
				double augmentationInexactError = Double.valueOf(inexactMarginElement.getAttributeValue("AugmentationInexactError"));
				double diminutionInexactError = Double.valueOf(inexactMarginElement.getAttributeValue("DiminutionInexactError"));
				double minInexactAllowed = Double.valueOf(inexactMarginElement.getAttributeValue("MinInexactAllowed"));
				int nConflictBeforeInexactAugmentation = Integer.valueOf(inexactMarginElement.getAttributeValue("NConflictBeforeInexactAugmentation"));
				int nSuccessBeforeInexactDiminution = Integer.valueOf(inexactMarginElement.getAttributeValue("NSuccessBeforeInexactDiminution"));
				head.setDataForInexactMargin(inexactAllowed, augmentationInexactError, diminutionInexactError, minInexactAllowed, nConflictBeforeInexactAugmentation, nSuccessBeforeInexactDiminution);
				
				amoeba.setHead(head);
				nbHeadsAdded++;
			} else if (startingAgentElement.getName().equals(PERCEPT_NODE)) {
				Percept percept = new Percept(amoeba);
				percept.setName(startingAgentElement.getAttributeValue("Name"));
				boolean isEnum = Boolean.valueOf(startingAgentElement.getAttributeValue("Enum"));
				percept.setEnum(isEnum);
				
				perceptsByName.put(percept.getName(), percept);
				nbPerceptsAdded++;
			}
		}

		if (nbHeadsAdded != 1) {
			throw new IllegalDataException("Cannot load an AMOEBA without only 1 Head agent.");
		}
		if (nbPerceptsAdded == 0) {
			throw new IllegalDataException("Cannot load an AMOEBA without at least 1 Percept agent.");
		}
	}

	private void loadPresetContexts(Element systemElement) {
		Element presetContextsElement = systemElement.getChild("PresetContexts");
		List<Element> contextsElement = presetContextsElement.getChildren();

		for (Element contextElement : contextsElement) {
			loadContext(contextElement);
		}
	}

	private void loadContext(Element contextElement) {
		// -- Load Ranges
		Element rangesElement = contextElement.getChild("Ranges");
		Map<Percept, Double> starts = new HashMap<>();
		Map<Percept, Double> ends = new HashMap<>();
		loadRanges(rangesElement, starts, ends);

		// -- Load Experiments
		Element experimentsElement = contextElement.getChild("Experiments");
		ArrayList<Experiment> experiments = new ArrayList<>();
		loadExperiments(experimentsElement, experiments);

		// -- Load attributes
		String contextName = contextElement.getAttributeValue("Name");
		String localModelName = contextElement.getAttributeValue("LocalModel");
		TypeLocalModel type = TypeLocalModel.valueOf(localModelName);
		LocalModel localModel;
		switch (type) {
		case AVERAGE:
			localModel = new LocalModelAverage();
			break;
		case FIRST_EXPERIMENT:
			localModel = new LocalModelFirstExp();
			break;
		case MILLER_REGRESSION:
			localModel = new LocalModelMillerRegression(starts.size());
			break;
		default:
			throw new IllegalArgumentException("Found unknown model " + localModelName + " in XML file. ");
		}
		double confidence = Double.valueOf(contextElement.getAttributeValue("Confidence"));
		
		// -- Create context
		Head head = amoeba.getHeads().get(0);
		new Context(amoeba, head, contextName, starts, ends, experiments, localModel, confidence);
	}

	private void loadRanges(Element elemRanges, Map<Percept, Double> starts, Map<Percept, Double> ends) {
		for (Element rangeElement : elemRanges.getChildren()) {
			String perceptName = rangeElement.getAttributeValue(PERCEPT_NODE);
			double start = Double.valueOf(rangeElement.getAttributeValue("Start"));
			double end = Double.valueOf(rangeElement.getAttributeValue("End"));

			if (!perceptsByName.containsKey(perceptName)) {
				System.out.println(perceptsByName);
				throw new IllegalDataException("Found unknown percept name " + perceptName + " in file\n");
			}

			Percept percept = perceptsByName.get(perceptName);
			starts.put(percept, start);
			ends.put(percept, end);
		}
	}

	private void loadExperiments(Element experimentsElement, List<Experiment> experiments) {
		for (Element experimentElement : experimentsElement.getChildren()) {
			Element valuesElement = experimentElement.getChild("Values");
			Experiment experiment = new Experiment();

			double proposition = Double.valueOf(experimentElement.getAttributeValue("Proposition"));
			experiment.setProposition(proposition);

			for (Element valueElement : valuesElement.getChildren()) {
				String perceptName = valueElement.getAttributeValue(PERCEPT_NODE);
				double value = Double.valueOf(valueElement.getAttributeValue("Value"));

				if (!perceptsByName.containsKey(perceptName)) {
					System.out.println(perceptsByName);
					throw new IllegalDataException("Found unknown percept name " + perceptName + " in file\n");
				}

				Percept percept = perceptsByName.get(perceptName);
				experiment.addDimension(percept, value);
			}
			experiments.add(experiment);
		}
	}

	private void saveConfiguration(Element rootElement) {
		Element configurationElement = new Element("Configuration");
		Element learningElement = new Element("Learning");
		List<Attribute> learningAttributes = new ArrayList<>();

		learningAttributes.add(new Attribute("creationOfNewContext", String.valueOf(amoeba.isCreationOfNewContext())));
		learningAttributes.add(new Attribute("loadPresetContext", String.valueOf(amoeba.isLoadPresetContext())));
		learningElement.setAttributes(learningAttributes);

		configurationElement.addContent(learningElement);		
		rootElement.addContent(configurationElement);
	}

	private void saveStartingAgents(Element rootElement) {
		Element startingAgentsElement = new Element("StartingAgents");
		List<Head> heads = amoeba.getHeads();
		List<Percept> percepts = amoeba.getPercepts();

		for (Head head : heads) {
			Element controllerElement = new Element(HEAD_NODE);
			List<Attribute> attributes = new ArrayList<>();

			attributes.add(new Attribute("Name", head.getName()));
			controllerElement.setAttributes(attributes);

			Element errorMarginElement = new Element("ErrorMargin");
			List<Attribute> errorAttributes = new ArrayList<>();
			errorAttributes.add(new Attribute("ErrorAllowed", String.valueOf(head.getErrorAllowed())));
			errorAttributes.add(new Attribute("AugmentationFactorError", String.valueOf(head.getErrorAllowed())));
			errorAttributes.add(new Attribute("DiminutionFactorError", String.valueOf(head.getDiminutionFactorError())));
			errorAttributes.add(new Attribute("MinErrorAllowed", String.valueOf(head.getMinErrorAllowed())));
			errorAttributes.add(new Attribute("NConflictBeforeAugmentation", String.valueOf(head.getNConflictBeforeAugmentation())));
			errorAttributes.add(new Attribute("NSuccessBeforeDiminution", String.valueOf(head.getNSuccessBeforeDiminution())));
			errorMarginElement.setAttributes(errorAttributes);
			controllerElement.addContent(errorMarginElement);
			
			Element inexactMarginElement = new Element("InexactMargin");
			List<Attribute> inexactAttributes = new ArrayList<>();
			inexactAttributes.add(new Attribute("InexactAllowed", String.valueOf(head.getInexactAllowed())));
			inexactAttributes.add(new Attribute("AugmentationInexactError", String.valueOf(head.getAugmentationInexactError())));
			inexactAttributes.add(new Attribute("DiminutionInexactError", String.valueOf(head.getDiminutionInexactError())));
			inexactAttributes.add(new Attribute("MinInexactAllowed", String.valueOf(head.getMinInexactAllowed())));
			inexactAttributes.add(new Attribute("NConflictBeforeInexactAugmentation", String.valueOf(head.getNConflictBeforeInexactAugmentation())));
			inexactAttributes.add(new Attribute("NSuccessBeforeInexactDiminution", String.valueOf(head.getNSuccessBeforeInexactDiminution())));
			inexactMarginElement.setAttributes(inexactAttributes);
			controllerElement.addContent(inexactMarginElement);

			startingAgentsElement.addContent(controllerElement);
		}

		for (Percept percept : percepts) {
			Element sensorElement = new Element(PERCEPT_NODE);
			List<Attribute> attributes = new ArrayList<>();

			attributes.add(new Attribute("Name", percept.getName()));
			attributes.add(new Attribute("Enum", String.valueOf(percept.isEnum())));
			sensorElement.setAttributes(attributes);
			startingAgentsElement.addContent(sensorElement);
		}

		rootElement.addContent(startingAgentsElement);
	}

	private void savePresetContexts(Element rootElement) {
		List<Context> contexts = amoeba.getContexts();
		Element presetContextsElement = new Element("PresetContexts");
		presetContextsElement.addContent(new Comment(" Nb contexts = " + String.valueOf(contexts.size()) + " "));

		for (Context context : contexts) {
			if (!context.isDying()) {
				saveContext(context, presetContextsElement);
			}
		}

		rootElement.addContent(presetContextsElement);
	}

	private void saveContext(Context context, Element presetContextsElement) {
		Element contextElement = new Element(CONTEXT_NODE);

		// -- Saving Ranges
		HashMap<Percept, Range> ranges = context.getRanges();
		saveRanges(ranges, contextElement);

		// -- Saving Experiments
		ArrayList<Experiment> experiments = context.getExperiments();
		saveExperiments(experiments, contextElement);

		// -- Add attributes
		List<Attribute> agentAttributes = new ArrayList<>();
		agentAttributes.add(new Attribute("Name", String.valueOf(context.getName())));
		agentAttributes.add(new Attribute("LocalModel", context.getFunction().getType().name()));
		agentAttributes.add(new Attribute("Confidence", String.valueOf(context.getConfidence())));
		
		contextElement.setAttributes(agentAttributes);
		presetContextsElement.addContent(contextElement);
	}

	private void saveRanges(Map<Percept, Range> ranges, Element contextElement) {
		Element rangesElement = new Element("Ranges");
		for (Entry<Percept, Range> entry : ranges.entrySet()) {
			Percept percept = entry.getKey();
			Range range = entry.getValue();

			List<Attribute> attributes = new ArrayList<>();
			attributes.add(new Attribute(PERCEPT_NODE, percept.getName()));
			attributes.add(new Attribute("Start", String.valueOf(range.getStart())));
			attributes.add(new Attribute("End", String.valueOf(range.getEnd())));

			Element rangeElement = new Element("Range").setAttributes(attributes);
			rangesElement.addContent(rangeElement);
		}
		contextElement.addContent(rangesElement);
	}

	private void saveExperiments(List<Experiment> experiments, Element contextElement) {
		Element experimentsElement = new Element("Experiments");
		for (Experiment experiment : experiments) {
			Map<Percept, Double> values = experiment.getValues();
			Element valuesElement = new Element("Values");

			for (Entry<Percept, Double> entry : values.entrySet()) {
				Percept percept = entry.getKey();
				Double value = entry.getValue();

				List<Attribute> attributes = new ArrayList<>();
				attributes.add(new Attribute(PERCEPT_NODE, percept.getName()));
				attributes.add(new Attribute("Value", String.valueOf(value)));

				Element eValue = new Element("Value");
				eValue.setAttributes(attributes);
				valuesElement.addContent(eValue);
			}

			Element experimentElement = new Element("Experiment");
			experimentElement.addContent(valuesElement);
			experimentElement.setAttribute(new Attribute("Proposition", String.valueOf(experiment.getProposition())));

			experimentsElement.addContent(experimentElement);
		}
		contextElement.addContent(experimentsElement);
	}

	// -------------------- DEBUG methods --------------------

	// TODO (Labbeti) : erase this function or clean
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		File file = new File("./resources/twoDimensionsLauncher.xml");
		File srcFile = new File("./resources/save1.xml");
		File trgFile = new File("./resources/save2.xml");

		Configuration.commandLineMode = false;
		World world = new World();
		StudiedSystem studiedSystem = new F_XY_System(50.0);
		AMOEBA amoeba = new AMOEBA(world, studiedSystem);
		SaveState saveState = new SaveState(amoeba);

		System.out.println(
				"DEBUG: before load: contexts/agents = " + amoeba.getContexts().size() + "/" + amoeba.getAgents().size());
		saveState.loadXML(file);
		
		// TODO : rem this:
		//amoeba.setDataForErrorMargin(1000, 5, 0.4, 0.1, 40, 80);
		//amoeba.setDataForInexactMargin(500, 2.5, 0.2, 0.05, 40, 80);
		amoeba.setRenderUpdate(false); // TODO to save ?
		amoeba.allowGraphicalScheduler(false); // TODO to save ?

		int nbCycles = 1;
		System.out.println("DEBUG: Begin learning with " + nbCycles + " cycles.");
		// Example for using the learn method
		for (int i = 0; i < nbCycles; ++i) {
			studiedSystem.playOneStep();
			amoeba.learn(studiedSystem.getOutput());
		}

		// DEBUG
		System.out.println(
				"DEBUG: after  load: contexts/agents = " + amoeba.getContexts().size() + "/" + amoeba.getAgents().size());

		amoeba.setRenderUpdate(true);
		amoeba.allowGraphicalScheduler(true);

		saveState.saveXML(trgFile);
	}
}

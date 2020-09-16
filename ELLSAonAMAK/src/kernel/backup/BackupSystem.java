package kernel.backup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import kernel.ELLSA;
import kernel.EllsaData;
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
import agents.context.localModel.TypeLocalModel;
import agents.head.Head;
import agents.percept.Percept;
import utils.XMLSerialization;

/**
 * @author Labbeti
 */
public class BackupSystem implements IBackupSystem {
	// Static final values
	private static final String HEAD_NODE = "Controller";
	private static final String PERCEPT_NODE = "Sensor";
	private static final String CONTEXT_NODE = "Context";

	// Members
	private ELLSA ellsa;
	private Map<String, Percept> perceptsByName = new HashMap<>();
	private boolean loadPresetContext = true;
	private boolean amoebaDataLoaded = false;

	// -------------------- Constructor --------------------

	public BackupSystem(ELLSA ellsa) {
		this.ellsa = ellsa;
	}

	// -------------------- Public Methods --------------------

	@Override
	public void load(File file) {
		ellsa.clearAgents();
		perceptsByName.clear();

		SAXBuilder sxb = new SAXBuilder();
		Document doc;
		try {
			doc = sxb.build(file);

			Element rootElement = doc.getRootElement();
			loadConfiguration(rootElement);
			loadStartingAgents(rootElement);
			if (isLoadPresetContext()) {
				loadPresetContexts(rootElement);
			}
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
		ellsa.onLoadEnded();
	}

	@Override
	public void save(File file) {
		Element elemSystem = new Element("System");
		Document doc = new Document(elemSystem);

		Element rootElement = doc.getRootElement();
		saveConfiguration(rootElement);
		saveStartingAgents(rootElement);
		savePresetContexts(rootElement);

		XMLOutputter xml = new XMLOutputter();
		xml.setFormat(Format.getPrettyFormat());

		try (FileWriter fw = new FileWriter(file)) {
			xml.output(doc, fw);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setLoadPresetContext(boolean loadPresetContext) {
		this.loadPresetContext = loadPresetContext;
	}

	public boolean isLoadPresetContext() {
		return loadPresetContext;
	}

	@Override
	public String getExtension() {
		return "xml";
	}

	// -------------------- Private methods --------------------

	private void loadConfiguration(Element systemElement) {
		Element configurationElement = systemElement.getChild("Configuration");
		Element learningElement = configurationElement.getChild("Learning");

		try {
			boolean creationOfNewContext = learningElement.getAttribute("creationOfNewContext").getBooleanValue();
			boolean loadPresetContext = learningElement.getAttribute("loadPresetContext").getBooleanValue();

			ellsa.setCreationOfNewContext(creationOfNewContext);
			setLoadPresetContext(loadPresetContext);
		} catch (DataConversionException e) {
			e.printStackTrace();
		}
		
		// amoeba data
		Element dataElement = configurationElement.getChild("Data");
		if(dataElement != null) { // the data field is optionnal. We don't ask human to write it !
			XMLSerialization<EllsaData> decode = new XMLSerialization<>();
			ellsa.data = decode.fromString(dataElement.getText());
			amoebaDataLoaded = true;
		}
	}

	private void loadStartingAgents(Element systemElement) {
		Element startingAgentsElement = systemElement.getChild("StartingAgents");
		List<Element> agentsElement = new ArrayList<>(startingAgentsElement.getChildren());

		int nbHeadsAdded = 0;
		int nbPerceptsAdded = 0;
		
		// load head first
		agentsElement.sort(new Comparator<Element>() {
			@Override
			public int compare(Element o1, Element o2) {
				if(o1.getName().equals(HEAD_NODE))
					return -1;
				else if(o1.getName().equals(PERCEPT_NODE))
					return 1;
				else
					return 0;
			}
		});
		
		for (Element startingAgentElement : agentsElement) {
			switch (startingAgentElement.getName()) {
			case HEAD_NODE:
				loadController(startingAgentElement);
				nbHeadsAdded++;
				break;
			case PERCEPT_NODE:
				loadSensor(startingAgentElement);
				nbPerceptsAdded++;
				break;
			default:
				throw new IllegalDataException(
						"Unknown agent " + startingAgentElement.getName() + " in section \"StartingAgents\"");
			}
		}

		if (nbHeadsAdded != 1) {
			throw new IllegalDataException("Cannot load an AMOEBA without only 1 Head agent.");
		}
		if (nbPerceptsAdded == 0) {
			throw new IllegalDataException("Cannot load an AMOEBA without at least 1 Percept agent.");
		}
		
		ellsa.addPendingAgents();
	}

	private void loadPresetContexts(Element systemElement) {
		Element presetContextsElement = systemElement.getChild("PresetContexts");

		Element lastPerceptionsAndActionState = presetContextsElement.getChild("LastPerceptionsAndActionState");
		if (lastPerceptionsAndActionState != null) {
			HashMap<String, Double> perceptionAndAction = new HashMap<>();
			for (Attribute att : lastPerceptionsAndActionState.getAttributes()) {
				perceptionAndAction.put(att.getName(), Double.valueOf(att.getValue()));
			}
			ellsa.setPerceptionsAndActionState(perceptionAndAction);
		}

		List<Element> contextsElement = presetContextsElement.getChildren("Context");

		for (Element contextElement : contextsElement) {
			loadContext(contextElement);
		}
	}

	private void loadController(Element controllerElement) {
		Head head = new Head(ellsa);
		head.setName(controllerElement.getAttributeValue("Name"));
		head.setNoCreation(!ellsa.isCreationOfNewContext());
		
		if(!amoebaDataLoaded) {
			// If no AmoebaData was loaded, then we need to initialize the head a bit more :
			Element errorMarginElement = controllerElement.getChild("ErrorMargin");
			double errorAllowed = Double.valueOf(errorMarginElement.getAttributeValue("ErrorAllowed"));
			double augmentationFactorError = Double
					.valueOf(errorMarginElement.getAttributeValue("AugmentationFactorError"));
			double diminutionFactorError = Double.valueOf(errorMarginElement.getAttributeValue("DiminutionFactorError"));
			double minErrorAllowed = Double.valueOf(errorMarginElement.getAttributeValue("MinErrorAllowed"));
			int nConflictBeforeAugmentation = Integer
					.valueOf(errorMarginElement.getAttributeValue("NConflictBeforeAugmentation"));
			int nSuccessBeforeDiminution = Integer
					.valueOf(errorMarginElement.getAttributeValue("NConflictBeforeAugmentation"));
			head.setDataForErrorMargin(errorAllowed, augmentationFactorError, diminutionFactorError, minErrorAllowed,
					nConflictBeforeAugmentation, nSuccessBeforeDiminution);
		}
		
		ellsa.setHead(head);
	}

	private void loadSensor(Element sensorElement) {
		Percept percept = new Percept(ellsa);
		percept.setName(sensorElement.getAttributeValue("Name"));
		boolean isEnum = Boolean.valueOf(sensorElement.getAttributeValue("Enum"));
		percept.setEnum(isEnum);
		String max = sensorElement.getAttributeValue("Max");
		if(max != null) percept.setMax(Double.valueOf(max));
		String min = sensorElement.getAttributeValue("Min");
		if(min != null) percept.setMin(Double.valueOf(min));

		perceptsByName.put(percept.getName(), percept);
	}

	private void loadContext(Element contextElement) {
		Context context = new Context(ellsa);
		
		// -- Load Ranges
		Element rangesElement = contextElement.getChild("Ranges");
		loadRanges(context, rangesElement);

		// -- Load Local Model
		loadLocalModel(context, contextElement);
		
		// -- Load attributes
		context.setName(contextElement.getAttributeValue("Name"));
		context.setConfidence(Double.valueOf(contextElement.getAttributeValue("Confidence")));
		for(Percept p : ellsa.getPercepts()) {
			p.updateContextProjectionEnd(context);
			p.updateContextProjectionStart(context);
		}

	}
	
	private void loadLocalModel(Context context, Element parentElement) {
		Element localModelElement = parentElement.getChild("LocalModel");
		
		// -- Load Experiments
		Element experimentsElement = localModelElement.getChild("Experiments");
		ArrayList<Experiment> experiments = new ArrayList<>();
		loadExperiments(experiments ,context, experimentsElement);
		
		// -- Load Model
		String localModelName = localModelElement.getAttributeValue("Type");
		TypeLocalModel type = TypeLocalModel.valueOf(localModelName);
		LocalModel localModel = ellsa.buildLocalModel(context, type);
		List<Double> coefs = new ArrayList<>();
		for(Element e : localModelElement.getChild("Coefs").getChildren()) {
			coefs.add(Double.valueOf(e.getAttributeValue("v")));
		}
		Double[] coefArray = coefs.toArray(new Double[coefs.size()]);
		localModel.setFirstExperiments(experiments);
		localModel.setCoef(coefArray);
		context.setLocalModel(localModel);
	}

	private void loadRanges(Context context, Element elemRanges) {
		HashMap<Percept, Range> ranges = new HashMap<>();
		for (Element rangeElement : elemRanges.getChildren()) {
			String perceptName = rangeElement.getAttributeValue(PERCEPT_NODE);
			double start = Double.valueOf(rangeElement.getAttributeValue("Start"));
			double end = Double.valueOf(rangeElement.getAttributeValue("End"));

			if (!perceptsByName.containsKey(perceptName)) {
				System.out.println(perceptsByName);
				throw new IllegalDataException("Found unknown percept name " + perceptName + " in file\n");
			}

			Percept percept = perceptsByName.get(perceptName);
			Range range = new Range(context, start, end, 0, true, true, percept);
			ranges.put(percept, range);
		}
		context.setRanges(ranges);
	}

	private void loadExperiments(List<Experiment> experiments, Context context, Element experimentsElement) {
		for (Element experimentElement : experimentsElement.getChildren()) {
			Element valuesElement = experimentElement.getChild("Values");
			Experiment experiment = new Experiment(context);

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

		learningAttributes.add(new Attribute("creationOfNewContext", String.valueOf(ellsa.isCreationOfNewContext())));
		learningAttributes.add(new Attribute("loadPresetContext", String.valueOf(isLoadPresetContext())));
		learningElement.setAttributes(learningAttributes);
		configurationElement.addContent(learningElement);
		
		// amoeba data
		Element dataElement = new Element("Data");
		XMLSerialization<EllsaData> encode = new XMLSerialization<>();
		dataElement.setText(encode.toString(ellsa.data));
		configurationElement.addContent(dataElement);
		
		rootElement.addContent(configurationElement);
	}

	private void saveStartingAgents(Element rootElement) {
		Element startingAgentsElement = new Element("StartingAgents");
		List<Head> heads = ellsa.getHeads();
		List<Percept> percepts = ellsa.getPercepts();

		for (Head head : heads) {
			saveController(head, startingAgentsElement);
		}

		for (Percept percept : percepts) {
			saveSensor(percept, startingAgentsElement);
		}

		rootElement.addContent(startingAgentsElement);
	}

	private void savePresetContexts(Element rootElement) {
		List<Context> contexts = ellsa.getContexts();
		Element presetContextsElement = new Element("PresetContexts");

		Element lastPerceptionsAndActionState = new Element("LastPerceptionsAndActionState");
		HashMap<String, Double> perceptionAndAction = ellsa.getPerceptionsAndActionState();
		List<Attribute> attributes = new ArrayList<>();
		for (String key : perceptionAndAction.keySet()) {
			attributes.add(new Attribute(key, "" + perceptionAndAction.get(key)));
		}
		lastPerceptionsAndActionState.setAttributes(attributes);
		presetContextsElement.addContent(lastPerceptionsAndActionState);

		presetContextsElement.addContent(new Comment(" Nb contexts = " + String.valueOf(contexts.size()) + " "));

		for (Context context : contexts) {
			if (!context.isDying()) {
				saveContext(context, presetContextsElement);
			}
		}

		rootElement.addContent(presetContextsElement);
	}

	private void saveController(Head head, Element startingAgentsElement) {

		Element controllerElement = new Element(HEAD_NODE);
		List<Attribute> attributes = new ArrayList<>();

		attributes.add(new Attribute("Name", head.getName()));
		controllerElement.setAttributes(attributes);

		Element errorMarginElement = new Element("ErrorMargin");
		List<Attribute> errorAttributes = new ArrayList<>();
		errorAttributes
				.add(new Attribute("ErrorAllowed", String.valueOf(head.getAmas().data.predictionPerformance.performanceIndicator)));
		errorAttributes.add(new Attribute("AugmentationFactorError",
				String.valueOf(head.getAmas().data.predictionPerformance.augmentationFactor)));
		errorAttributes.add(
				new Attribute("DiminutionFactorError", String.valueOf(head.getAmas().data.predictionPerformance.diminutionFactor)));
		errorAttributes.add(
				new Attribute("MinErrorAllowed", String.valueOf(head.getAmas().data.predictionPerformance.minPerformanceIndicator)));
		errorAttributes.add(new Attribute("NConflictBeforeAugmentation",
				String.valueOf(head.getAmas().data.predictionPerformance.conflictsBeforeAugmentation)));
		errorAttributes.add(new Attribute("NSuccessBeforeDiminution",
				String.valueOf(head.getAmas().data.predictionPerformance.successesBeforeDiminution)));
		errorMarginElement.setAttributes(errorAttributes);
		controllerElement.addContent(errorMarginElement);

		startingAgentsElement.addContent(controllerElement);

	}

	private void saveSensor(Percept percept, Element startingAgentsElement) {
		Element sensorElement = new Element(PERCEPT_NODE);
		List<Attribute> attributes = new ArrayList<>();

		attributes.add(new Attribute("Name", percept.getName()));
		attributes.add(new Attribute("Enum", String.valueOf(percept.isEnum())));
		attributes.add(new Attribute("Max", percept.getMax()+""));
		attributes.add(new Attribute("Min", percept.getMin()+""));
		sensorElement.setAttributes(attributes);
		startingAgentsElement.addContent(sensorElement);
	}

	private void saveContext(Context context, Element presetContextsElement) {
		Element contextElement = new Element(CONTEXT_NODE);

		// -- Saving Ranges
		HashMap<Percept, Range> ranges = context.getRanges();
		saveRanges(ranges, contextElement);

		// -- Saving Local Model
		saveLocalModel(context, contextElement);

		// -- Add attributes
		List<Attribute> agentAttributes = new ArrayList<>();
		agentAttributes.add(new Attribute("Name", String.valueOf(context.getName())));
		agentAttributes.add(new Attribute("Confidence", String.valueOf(context.getConfidence())));
		agentAttributes.add(new Attribute("ActionsProposal", context.getActionProposal() + ""));
		if(context.getAmas().getValidContexts() != null)
			agentAttributes.add(new Attribute("Activated", (context.getAmas().getValidContexts().contains(context)) + ""));

		contextElement.setAttributes(agentAttributes);
		presetContextsElement.addContent(contextElement);
	}

	private void saveLocalModel(Context context, Element contextElement) {
		Element localModelElement = new Element("LocalModel");
		localModelElement.setAttribute("Type", context.getFunction().getType().name());

		// save coef
		Element coefs = new Element("Coefs");
		List<Element> coefsElements = new ArrayList<>();
		for (double c : context.getFunction().getCoef()) {
			coefsElements.add(new Element("Value").setAttribute("v", c + ""));
		}
		coefs.addContent(coefsElements);
		localModelElement.addContent(coefs);
		
		// -- Saving Experiments
		ArrayList<Experiment> experiments = context.getLocalModel().getFirstExperiments();
		saveExperiments(experiments, localModelElement);
		
		contextElement.addContent(localModelElement);
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

	private void saveExperiments(List<Experiment> experiments, Element parentElement) {
		Element experimentsElement = new Element("Experiments");
		for (Experiment experiment : experiments) {
			Map<Percept, Double> values = experiment.getValuesAsHashMap();
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
			experimentElement
					.setAttribute(new Attribute("Proposition", String.valueOf(experiment.getProposition())));

			experimentsElement.addContent(experimentElement);
		}
		parentElement.addContent(experimentsElement);
	}
}
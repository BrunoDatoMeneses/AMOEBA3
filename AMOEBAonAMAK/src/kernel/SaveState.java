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
	public void load(File file) {
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
	public void save(File file) {
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

	private void loadConfiguration(Element elemSystem) {
		Element elemConfiguration = elemSystem.getChild("Configuration");
		Element elemLearning = elemConfiguration.getChild("Learning");

		try {
			boolean creationOfNewContext = elemLearning.getAttribute("creationOfNewContext").getBooleanValue();
			boolean loadPresetContext = elemLearning.getAttribute("loadPresetContext").getBooleanValue();

			amoeba.setCreationOfNewContext(creationOfNewContext);
			amoeba.setLoadPresetContext(loadPresetContext);
		} catch (DataConversionException e) {
			e.printStackTrace();
		}
	}

	private void loadStartingAgents(Element elemSystem) {
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

	private void loadPresetContexts(Element elemSystem) {
		Element elemPresetContexts = elemSystem.getChild("PresetContexts");
		List<Element> elemContexts = elemPresetContexts.getChildren();

		for (Element elemContext : elemContexts) {
			String contextName = elemContext.getAttributeValue("Name");
			Head head = amoeba.getHeads().get(0);

			// -- Load Ranges
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

			// -- Load Experiments
			Element elemExperiments = elemContext.getChild("Experiments");
			ArrayList<Experiment> experiments = new ArrayList<>();

			for (Element elemExperiment : elemExperiments.getChildren()) {
				Element elemValues = elemExperiment.getChild("Values");
				Experiment experiment = new Experiment();
				
				String elemProposition = elemExperiment.getAttributeValue("Proposition");
				double proposition = Double.valueOf(elemProposition);
				experiment.setProposition(proposition);

				for (Element elemValue : elemValues.getChildren()) {
					String perceptName = elemValue.getAttributeValue(PERCEPT_NODE);
					double value = Double.valueOf(elemValue.getAttributeValue("Value"));

					if (!perceptsByName.containsKey(perceptName)) {
						System.out.println(perceptsByName);
						throw new IllegalDataException("Found unknown percept name " + perceptName + " in file\n");
					}
					Percept percept = perceptsByName.get(perceptName);
					experiment.addDimension(percept, value);
				}
				experiments.add(experiment);
			}

			// -- Load Model
			Element elemModel = elemContext.getChild("Model");
			String modelName = elemModel.getAttributeValue("type");
			TypeLocalModel type = TypeLocalModel.valueOf(modelName);
			LocalModel model;
			switch (type) {
			case AVERAGE:
				model = new LocalModelAverage();
				break;
			case FIRST_EXPERIMENT:
				model = new LocalModelFirstExp();
				break;
			case MILLER_REGRESSION:
				model = new LocalModelMillerRegression(starts.size());
				break;
			default:
				throw new IllegalArgumentException("Found unknown model " + modelName + " in XML file. ");
			}

			Context context = new Context(amoeba, head, contextName, starts, ends, experiments, model);
			context.getFunction().updateModel(context);
		}
	}

	private void saveConfiguration(Element rootElement) {
		Element elemConfiguration = new Element("Configuration");
		Element elemLearning = new Element("Learning");
		List<Attribute> attributes = new ArrayList<>();

		attributes.add(new Attribute("creationOfNewContext", String.valueOf(amoeba.isCreationOfNewContext())));
		attributes.add(new Attribute("loadPresetContext", String.valueOf(amoeba.isLoadPresetContext())));
		elemLearning.setAttributes(attributes);
		elemConfiguration.addContent(elemLearning);
		rootElement.addContent(elemConfiguration);
	}

	private void saveStartingAgents(Element rootElement) {
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

		rootElement.addContent(elemStartingAgents);
	}

	private void savePresetContexts(Element rootElement) {
		Element elemPresetContexts = new Element("PresetContexts");
		List<Context> contexts = amoeba.getContexts();

		for (Context context : contexts) {
			Element elemContext = new Element(CONTEXT_NODE);
			HashMap<Percept, Range> ranges = context.getRanges();
			Element eRanges = new Element("Ranges");

			// -- Saving Ranges
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

			// -- Saving Experiments
			ArrayList<Experiment> experiments = context.getExperiments();
			Element eExperiments = new Element("Experiments");
			for (Experiment experiment : experiments) {
				Map<Percept, Double> values = experiment.getValues();
				Element eValues = new Element("Values");

				for (Entry<Percept, Double> entry : values.entrySet()) {
					Percept percept = entry.getKey();
					Double value = entry.getValue();

					List<Attribute> attributes = new ArrayList<>();
					attributes.add(new Attribute(PERCEPT_NODE, percept.getName()));
					attributes.add(new Attribute("Value", String.valueOf(value)));

					Element eValue = new Element("Value");
					eValue.setAttributes(attributes);
					eValues.addContent(eValue);
				}

				Element eExperiment = new Element("Experiment");
				eExperiment.addContent(eValues);
				eExperiment.setAttribute(new Attribute("Proposition", String.valueOf(experiment.getProposition())));

				eExperiments.addContent(eExperiment);
			}
			elemContext.addContent(eExperiments);

			// -- Saving LocalModel
			LocalModel model = context.getFunction();
			Element elemModel = new Element("Model");
			elemModel.setAttribute(new Attribute("type", model.getType().name()));
			elemContext.addContent(elemModel);

			List<Attribute> eAgentAttributes = new ArrayList<>();
			eAgentAttributes.add(new Attribute("Name", String.valueOf(context.getName())));
			elemContext.setAttributes(eAgentAttributes);
			elemPresetContexts.addContent(elemContext);
		} // end for

		rootElement.addContent(elemPresetContexts);
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
				"DEBUG: before load: contexts = " + amoeba.getContexts().size() + "/" + amoeba.getAgents().size());
		saveState.load(srcFile);

		amoeba.setDataForErrorMargin(1000, 5, 0.4, 0.1, 40, 80);
		amoeba.setDataForInexactMargin(500, 2.5, 0.2, 0.05, 40, 80);
		amoeba.setNoRenderUpdate(true);
		amoeba.allowGraphicalScheduler(false);

		System.out.println("DEBUG: Begin learning.");
		// Example for using the learn method
		int nbCycles = 1;
		for (int i = 0; i < nbCycles; ++i) {
			studiedSystem.playOneStep();
			amoeba.learn(studiedSystem.getOutput());
		}

		// DEBUG
		System.out.println(
				"DEBUG: after  load: contexts = " + amoeba.getContexts().size() + "/" + amoeba.getAgents().size());

		amoeba.setNoRenderUpdate(false);
		amoeba.allowGraphicalScheduler(true);

		saveState.save(trgFile);
	}
}

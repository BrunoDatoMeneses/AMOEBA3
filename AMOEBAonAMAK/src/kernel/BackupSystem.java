package kernel;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import agents.AmoebaAgent;
import agents.context.Context;
import agents.head.Head;
import agents.percept.Percept;
import fr.irit.smac.amak.Agent;
import fr.irit.smac.amak.Amas;

public class BackupSystem implements IBackupSystem {
	static final String HEAD_ELEM_NAME = "Controller";
	static final String PERCEPT_ELEM_NAME = "Sensor";
	static final String CONTEXT_ELEM_NAME = "Context";
	
	private AMOEBA amoeba;

	public BackupSystem(AMOEBA amoeba) {
		this.amoeba = amoeba;
	}

	public void load(String filepath) {
		SAXBuilder sxb = new SAXBuilder();
		Document doc;
		try {
			doc = sxb.build(filepath);
			
			loadConfiguration(doc);
			loadStartingAgents(doc);
			if (amoeba.isLoadPresetContext()) {
				loadPresetContexts(doc);
			}
			
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
	}

	public void save(String filepath) {
		Element elemSystem = new Element("System");
		Document doc = new Document(elemSystem);

		saveConfiguration(doc);
		saveStartingAgents(doc);
		savePresetContexts(doc);

		XMLOutputter xml = new XMLOutputter();
		xml.setFormat(Format.getPrettyFormat());

		try {
			xml.output(doc, new FileWriter(filepath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadConfiguration(Document doc) {
		Element elemSystem = doc.getRootElement();
		Element elemConfiguration = elemSystem.getChild("Configuration");
		Element elemLearning = elemConfiguration.getChild("Learning");
		
		try {
			boolean allowed = elemLearning.getAttribute("allowed").getBooleanValue();
			boolean creationOfNewContext = elemLearning.getAttribute("creationOfNewContext").getBooleanValue();
			boolean loadPresetContext = elemLearning.getAttribute("loadPresetContext").getBooleanValue();
			
			amoeba.setCreationOfNewContext(creationOfNewContext);
			amoeba.setLoadPresetContext(loadPresetContext));
		} catch (DataConversionException e) {
			e.printStackTrace();
		}
	}
	
	private void loadStartingAgents(Document doc) {
		Element elemSystem = doc.getRootElement();
		Element elemStartingAgents = elemSystem.getChild("StartingAgents");
		List<Element> elemAgents = elemStartingAgents.getChildren();
		// TODO
		for (Element elemAgent : elemAgents) {
			if (elemAgent.getName().equals(HEAD_ELEM_NAME)) {
				Head head = new Head(amoeba);
				head.setName(elemAgent.getAttributeValue("name"));
			}
			else if (elemAgent.getName().equals(PERCEPT_ELEM_NAME)) {
				Percept percept = new Percept(amoeba);
				percept.setName(elemAgent.getAttributeValue("name"));
			}
		}
	}
	private void loadPresetContexts(Document doc) {		
		Element elemSystem = doc.getRootElement();
		Element elemPresetContexts = elemSystem.getChild("PresetContexts");
		List<Element> elemContexts = elemPresetContexts.getChildren();
		for (Element elemContext : elemContexts) {
			// TODO
		}
	}
	
	private void saveConfiguration(Document doc) {
		Element elemConfiguration = new Element("Configuration");
		Element elemLearning = new Element("Learning");
		List<Attribute> attributes = new ArrayList<>();

		// TODO :
		// attributes.add(new Attribute("allowed", String.valueOf(true)));
		attributes.add(new Attribute("creationOfNewContext", String
				.valueOf(amoeba.isCreationOfNewContext())));
		attributes.add(new Attribute("loadPresetContext", String.valueOf(amoeba
				.isLoadPresetContext())));
		elemLearning.setAttributes(attributes);
		elemConfiguration.addContent(elemLearning);
		doc.getRootElement().addContent(elemConfiguration);
	}

	private void saveStartingAgents(Document doc) {
		Element elemStartingAgents = new Element("StartingAgents");
		List<Agent<? extends Amas<World>, World>> agents = amoeba.getAgents();

		for (Agent<? extends Amas<World>, World> agent : agents) {
			if (agent instanceof Head) {
				Head head = (Head) agent;
				Element elemController = new Element(HEAD_ELEM_NAME);
				List<Attribute> attributes = new ArrayList<>();

				attributes.add(new Attribute("name", head.getName()));
				elemController.setAttributes(attributes);
				elemStartingAgents.addContent(elemController);
			} else if (agent instanceof Percept) {
				Percept percept = (Percept) agent;
				Element elemSensor = new Element("Sensor");
				List<Attribute> attributes = new ArrayList<>();

				attributes.add(new Attribute("name", percept.getName()));
				elemSensor.setAttributes(attributes);
				elemStartingAgents.addContent(elemSensor);
			}
		}

		doc.getRootElement().addContent(elemStartingAgents);
	}
	

	private void savePresetContexts(Document doc) {
		List<Agent<? extends Amas<World>, World>> agents = amoeba.getAgents();
		Element eAgents = new Element("PresetContexts");

		for (Agent<? extends Amas<World>, World> agent : agents) {
			AmoebaAgent amoebaAgent = (AmoebaAgent) agent;
			Element eAgent = new Element("Context");

			if (amoebaAgent instanceof Context) {
				Context context = (Context) amoebaAgent;
				HashMap<Percept, agents.context.Range> ranges = context
						.getRanges();
				Element eRanges = new Element("Ranges");

				for (Entry<Percept, agents.context.Range> entry : ranges
						.entrySet()) {
					Percept percept = entry.getKey();
					agents.context.Range range = entry.getValue();

					List<Attribute> attributes = new ArrayList<>();
					attributes.add(new Attribute("perceptid", String
							.valueOf(percept.getId())));
					attributes.add(new Attribute("start", String.valueOf(range
							.getStart())));
					attributes.add(new Attribute("end", String.valueOf(range
							.getEnd())));
					attributes.add(new Attribute("name", percept.getName()));

					Element eRange = new Element("Range")
							.setAttributes(attributes);
					eRanges.addContent(eRange);
				}
				eAgent.addContent(eRanges);
			}

			List<Attribute> eAgentAttributes = new ArrayList<>();
			eAgentAttributes.add(new Attribute("id", String.valueOf(amoebaAgent
					.getId())));
			eAgent.setAttributes(eAgentAttributes);
			eAgents.addContent(eAgent);
		}
	}
}

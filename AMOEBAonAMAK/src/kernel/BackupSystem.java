package kernel;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import agents.AmoebaAgent;
import agents.context.Context;
import agents.head.Head;
import agents.percept.Percept;
import fr.irit.smac.amak.Agent;
import fr.irit.smac.amak.Amas;

public class BackupSystem implements IBackupSystem {
	private Amas<World> amas;

	public BackupSystem(Amas<World> amas) {
		this.amas = amas;
	}

	public void load(String filepath) {
		// TODO
		throw new UnsupportedOperationException();
	}

	public void save(String filepath) {
		Element elemSystem = new Element("system");
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

	private void saveConfiguration(Document doc) {
		Element elemConfiguration = new Element("configuration");
		// TODO
		doc.getRootElement().addContent(elemConfiguration);
	}

	private void saveStartingAgents(Document doc) {
		Element elemStartingAgents = new Element("startingAgents");
		// TODO
		doc.getRootElement().addContent(elemStartingAgents);
	}

	private void savePresetContexts(Document doc) {
		List<Agent<? extends Amas<World>, World>> agents = amas.getAgents();
		Element eAgents = new Element("system");

		for (Agent<? extends Amas<World>, World> agent : agents) {
			AmoebaAgent amoebaAgent = (AmoebaAgent) agent;
			Element eAgent = new Element("context");

			if (amoebaAgent instanceof Context) {
				Context context = (Context) amoebaAgent;
				HashMap<Percept, agents.context.Range> ranges = context
						.getRanges();
				Element eRanges = new Element("ranges");

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

					Element eRange = new Element("range")
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

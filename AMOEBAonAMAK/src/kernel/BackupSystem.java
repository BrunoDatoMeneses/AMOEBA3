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

	public void save(String filepath) {
		List<Agent<? extends Amas<World>, World>> agents = amas.getAgents();
		saveAgents(agents, filepath);
	}

	public void load(String filepath) {
		// TODO
		throw new UnsupportedOperationException();
	}

	private void saveAgents(List<Agent<? extends Amas<World>, World>> agents, String filepath) {
		Element eAgents = new Element("agents");
		Document doc = new Document(eAgents);

		for (Agent<? extends Amas<World>, World> agent : agents) {
			AmoebaAgent amoebaAgent = (AmoebaAgent) agent;

			Element eAgent = new Element("agent");
			String agentType = "unknown";

			if (amoebaAgent instanceof Context) {
				Context context = (Context) amoebaAgent;
				HashMap<Percept, agents.context.Range> ranges = context.getRanges();
				Element eRanges = new Element("ranges");

				for (Entry<Percept, agents.context.Range> entry : ranges.entrySet()) {
					Percept percept = entry.getKey();
					agents.context.Range range = entry.getValue();

					List<Attribute> attributes = new ArrayList<>();
					attributes.add(new Attribute("perceptid", String.valueOf(percept.getId())));
					attributes.add(new Attribute("start", String.valueOf(range.getStart())));
					attributes.add(new Attribute("end", String.valueOf(range.getEnd())));

					Element eRange = new Element("range").setAttributes(attributes);
					eRanges.addContent(eRange);
				}
				eAgent.addContent(eRanges);
				agentType = "context";
			} else if (amoebaAgent instanceof Percept) {
				agentType = "percept";
			} else if (amoebaAgent instanceof Head) {
				agentType = "head";
			}

			List<Attribute> eAgentAttributes = new ArrayList<>();
			eAgentAttributes.add(new Attribute("id", String.valueOf(amoebaAgent.getId())));
			eAgentAttributes.add(new Attribute("type", agentType));
			eAgent.setAttributes(eAgentAttributes);
			eAgents.addContent(eAgent);
		}

		XMLOutputter xml = new XMLOutputter();
		xml.setFormat(Format.getPrettyFormat());

		try {
			xml.output(doc, new FileWriter(filepath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

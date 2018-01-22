package blackbox;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import kernel.Scheduler;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import agents.Agent;

// TODO: Auto-generated Javadoc
/**
 * The Class BlackBox.
 */
public class BlackBox implements Serializable {

	/** The scheduler. */
	private Scheduler scheduler;
	
	/** The black box agents. */
	private HashMap<String, BlackBoxAgent> blackBoxAgents = new HashMap<String, BlackBoxAgent>();
	
	/** The n probes. */
	private int nProbes = -1;  //Only used with the Unity simulation

	/**
	 * Instantiates a new black box.
	 *
	 * @param scheduler the scheduler
	 * @param systemFile the system file
	 */
	public BlackBox(Scheduler scheduler, File systemFile) {
		System.out.println("---Initialize the blackbox---");
		this.scheduler = scheduler;
		buildBlackBoxFromFile(systemFile);
		System.out.println("---End initialize the blackbox---");
	}
	
	/**
	 * Instantiates a new black box.
	 */
	public BlackBox() {
		System.out.println("---Initialize a void blackbox---");

		System.out.println("---End initialize a void blackbox---");
	}

	/**
	 * Builds the black box from file.
	 *
	 * @param systemFile the system file
	 */
	public void buildBlackBoxFromFile(File systemFile) {
		System.out.println("Build from file!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		SAXBuilder sxb = new SAXBuilder();
		Document document;
		try {
			document = sxb.build(systemFile);
			Element racine = document.getRootElement();
			// System.out.println(racine.getName());
			
			//		<Output Name="outC" DefaultValue="0"></Output>			

			if (nProbes > 0) {
				for (int i = 0 ; i < nProbes ; i++) {
					// System.out.println("Create new output in virtual XML");
						Element elem = new Element("Output");
						Attribute attrName = new Attribute("Name", "out_"+i);
						Attribute attrDefault = new Attribute("DefaultValue", "0");
						Attribute attrPort = new Attribute("Port", ""+(15300+i+1));
						elem.setAttribute(attrName);
						elem.setAttribute(attrDefault);
						elem.setAttribute(attrPort);
						racine.getChild("Outputs").addContent(elem);

				}
			}

			// Initialize the Input agents
			System.out.println(racine.getChild("Inputs"));
			System.out.println(racine.getChild("Inputs").getChildren(
					"Input"));
			for (Element element : racine.getChild("Inputs").getChildren(
					"Input")) {
				Input a = new Input();
				a.setName(element.getAttributeValue("Name"));
				a.setValue(Double.parseDouble(element
						.getAttributeValue("DefaultValue")));
				// System.out.println("v" + a.getValue());
				registerBlackBoxAgent(a);
			}

			// Initialize the Functions agents
			for (Element element : racine.getChild("Functions").getChildren(
					"Function")) {
				BBFunction a = new BBFunction();
				a.setName(element.getAttributeValue("Name"));
				a.setFunc(MathFunction.valueOf(element
						.getAttributeValue("Func")));
				registerBlackBoxAgent(a);
			}

			// Initialize the Output agents
			for (Element element : racine.getChild("Outputs").getChildren(
					"Output")) {
				// System.out.println("Initialize output");
				Output a = new Output();
				a.setName(element.getAttributeValue("Name"));
				if (element.getAttributeValue("Port") != null) a.setPort(Integer.parseInt(element.getAttributeValue("Port")));
				a.setValue(Double.parseDouble(element
						.getAttributeValue("DefaultValue")));

				a.setInput((Input) blackBoxAgents.get(element.getAttributeValue("Input")));
				registerBlackBoxAgent(a);
			}

			createLinks(racine);

		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Creates the links.
	 *
	 * @param root the root
	 */
	private void createLinks(Element root) {

		// Initialize the Input agents
		for (Element element : root.getChild("Inputs").getChildren("Input")) {
			for (Element target : element.getChildren("Target")) {
				blackBoxAgents
						.get(element.getAttribute("Name").getValue())
						.getTargets()
						.add(blackBoxAgents.get(target.getAttribute("Name")
								.getValue()));
			}
		}

		// Initialize the Output agents
		for (Element element : root.getChild("Outputs").getChildren("Output")) {
			for (Element target : element.getChildren("Target")) {
				blackBoxAgents
						.get(element.getAttribute("Name").getValue())
						.getTargets()
						.add(blackBoxAgents.get(target.getAttribute("Name")
								.getValue()));
			}
			
		}

		// Initialize the Function agents
		for (Element element : root.getChild("Functions").getChildren(
				"Function")) {
			for (Element target : element.getChildren("Target")) {
				blackBoxAgents
						.get(element.getAttribute("Name").getValue())
						.getTargets()
						.add(blackBoxAgents.get(target.getAttribute("Name")
								.getValue()));
			}
			((BBFunction) (blackBoxAgents.get(element.getAttribute("Name")
					.getValue()))).setAgentA(blackBoxAgents.get(element
					.getChild("InputA").getAttributeValue("Name")));
			((BBFunction) (blackBoxAgents.get(element.getAttribute("Name")
					.getValue()))).setAgentB(blackBoxAgents.get(element
					.getChild("InputB").getAttributeValue("Name")));
		}

	}

	/**
	 * Gets the agent by name.
	 *
	 * @param name the name
	 * @return the agent by name
	 */
	private Agent getAgentByName(String name) {
		return blackBoxAgents.get(name);
	}

	/**
	 * Register a new agent in the black box and in the scheduler.
	 *
	 * @param a the a
	 */
	public void registerBlackBoxAgent(BlackBoxAgent a) {
		if (scheduler != null) {
			scheduler.registerAgent(a);
		}
		blackBoxAgents.put(a.getName(), a);
	}

	/**
	 * Gets the scheduler.
	 *
	 * @return the scheduler
	 */
	public Scheduler getScheduler() {
		return scheduler;
	}

	/**
	 * Sets the scheduler.
	 *
	 * @param scheduler the new scheduler
	 */
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	/**
	 * Gets the black box agents.
	 *
	 * @return the black box agents
	 */
	public HashMap<String, BlackBoxAgent> getBlackBoxAgents() {
		return blackBoxAgents;
	}
	


	
	/**
	 * Gets the BB aof classes.
	 *
	 * @param classes the classes
	 * @return the BB aof classes
	 */
	public ArrayList<BlackBoxAgent> getBBAofClasses(Class<?> classes[]) {
		ArrayList<BlackBoxAgent> list = new ArrayList<BlackBoxAgent>();
		for (String k : blackBoxAgents.keySet()) {
			for (Class<?> cl : classes) {
				if (cl.isInstance(blackBoxAgents.get(k))) {
					list.add(blackBoxAgents.get(k));
				}
			}
		}
		long seed = System.nanoTime();
		Collections.shuffle(list, new Random(seed));
		
		return list;
	}
	
	/**
	 * Sets the black box agents.
	 *
	 * @param blackBoxAgents the black box agents
	 */
	public void setBlackBoxAgents(HashMap<String, BlackBoxAgent> blackBoxAgents) {
		this.blackBoxAgents = blackBoxAgents;
	}

	/**
	 * Gets the n probes.
	 *
	 * @return the n probes
	 */
	public int getnProbes() {
		return nProbes;
	}

	/**
	 * Sets the n probes.
	 *
	 * @param nProbes the new n probes
	 */
	public void setnProbes(int nProbes) {
		this.nProbes = nProbes;
	}


}

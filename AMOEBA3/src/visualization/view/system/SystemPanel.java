package visualization.view.system;

import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import mas.kernel.World;
import mas.agents.percept.Percept;
import mas.agents.SystemAgent;
import mas.agents.context.Context;
import mas.agents.head.Head;

// TODO: Auto-generated Javadoc
/**
 * The Class SystemPanel.
 */
public class SystemPanel extends JPanel{

	/** The table model. */
	DefaultTableModel tableModel;
	
	/** The graph system panel. */
	GrapheSystemPanel graphSystemPanel;
	
	/** The world. */
	private World world;

	
	/**
	 * Instantiates a new system panel.
	 *
	 * @param world the world
	 */
	public SystemPanel(World world) {
		this.world = world;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		JSplitPane splitPane_1 = new JSplitPane();
		add(splitPane_1);
		
		graphSystemPanel = new GrapheSystemPanel();
		splitPane_1.setLeftComponent(graphSystemPanel);
		
		Object[] col = {"TYPE","NAME","ID","1","2","3"};
		tableModel = new DefaultTableModel(col, 0);
		
		graphSystemPanel.setWorld(world);
		update();
		
		JTable table = new JTable(tableModel);
		splitPane_1.setRightComponent(new JScrollPane(table,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		table.setDefaultRenderer(Object.class, new JTableRenderer_Agents());

		
	}
	
	
	/**
	 * Update.
	 */
	public void update(){
		tableModel.setRowCount(0);
		
		//Object[] entete = {"TYPE","NAME","ID","XXX"};
		//tableModel.addRow(entete);
		
		int nVariable = 0;
		int nCriterion = 0;
		int nController = 0;
		int nContext = 0;
		
		HashMap<String, SystemAgent> hashAgents = world.getAgents();
		Object[] data = new Object[6];
	//	System.out.println(hashAgents.keySet());
		for (String s : hashAgents.keySet()) {
			SystemAgent a = hashAgents.get(s);
			data[0] = a.getClass().getSimpleName();
			data[1] = a.getName();
			data[2] = a.getID();
			
			if (a instanceof Head) {
				data[3] = a.getMessagesBin();
				//data[4] = ((NodexteController) a).getBestContext();
				nController++;
				tableModel.insertRow(nCriterion+nVariable,data);

			}
			if (a instanceof Percept) {
				data[3] = a.getMessagesBin();
				data[4] = ((Percept) a).getValue();
				nVariable++;
				tableModel.insertRow(nCriterion,data);
			} else if (a instanceof Context) {
				//data[3] = "Select : " + ((Nodexte)a).getNSelection();
				nContext++;
				tableModel.addRow(data);				
			}

		}
		
		graphSystemPanel.update();
		
	}


	/**
	 * Gets the table model.
	 *
	 * @return the table model
	 */
	public DefaultTableModel getTableModel() {
		return tableModel;
	}


	/**
	 * Sets the table model.
	 *
	 * @param tableModel the new table model
	 */
	public void setTableModel(DefaultTableModel tableModel) {
		this.tableModel = tableModel;
	}


	/**
	 * Gets the world.
	 *
	 * @return the world
	 */
	public World getWorld() {
		return world;
	}


	/**
	 * Sets the world.
	 *
	 * @param world the new world
	 */
	public void setWorld(World world) {
		this.world = world;
	}
	
	
}

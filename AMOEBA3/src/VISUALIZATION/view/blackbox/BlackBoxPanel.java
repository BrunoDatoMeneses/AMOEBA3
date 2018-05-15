package VISUALIZATION.view.blackbox;

import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import MAS.kernel.World;
import MAS.blackbox.BlackBox;
import MAS.blackbox.BlackBoxAgent;

// TODO: Auto-generated Javadoc
/**
 * The Class BlackBoxPanel.
 */
public class BlackBoxPanel extends JPanel{

	/** The table model. */
	DefaultTableModel tableModel;
	
	/** The world. */
	private World world;
	
	/** The graph panel. */
	GrapheBlackBoxPanel graphPanel;
	
	/**
	 * Instantiates a new black box panel.
	 *
	 * @param world the world
	 */
	public BlackBoxPanel(World world) {
		this.world = world;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		JSplitPane splitPane_1 = new JSplitPane();
		add(splitPane_1);
		
		graphPanel = new GrapheBlackBoxPanel();
		splitPane_1.setLeftComponent(graphPanel);
		
		String col[] = {"Type","Name","ID","Value"};
		tableModel = new DefaultTableModel(col, 0);
		
		update();
		
		JTable table = new JTable(tableModel);
		splitPane_1.setRightComponent(table);	
	}
	
	
	/**
	 * Update.
	 */
	public void update(){
		tableModel.setRowCount(0);
		
		Object[] entete = {"TYPE","NAME","ID","VALUE"};
		tableModel.addRow(entete);
		
		BlackBox bb = world.getBlackbox();
		HashMap<String, BlackBoxAgent> hashAgents = bb.getBlackBoxAgents();
		for (String s : hashAgents.keySet()) {
			BlackBoxAgent a = hashAgents.get(s);
			Object[] data = {
					a.getClass().getSimpleName(),
					a.getName(),
					a.getID(),
					a.getValue()
					};
			tableModel.addRow(data);
		}
		
		graphPanel.update();
		
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
	
	/**
	 * Sets the black box.
	 *
	 * @param blackBox the new black box
	 */
	public void setBlackBox(BlackBox blackBox) {
		graphPanel.setBlackBox(blackBox);
	}
}

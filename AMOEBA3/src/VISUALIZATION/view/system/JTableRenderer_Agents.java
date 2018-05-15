package VISUALIZATION.view.system;

import java.awt.Color;
import java.awt.Component;
import java.util.Stack;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import MAS.kernel.Config;
import MAS.agents.Percept;
import MAS.agents.context.Context;
import MAS.agents.messages.Message;


// TODO: Auto-generated Javadoc
/**
 * The Class JTableRenderer_Agents.
 */
public class JTableRenderer_Agents extends DefaultTableCellRenderer {

	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);
	
		this.setIcon(null);
	if (value != null) {
		if (column == 0 && value instanceof String) {
			String string = (String) value;
			

			if (string.startsWith("Percept")) {
			   	this.setBackground(new Color(176,242,182));
		    }
			else if (string.startsWith("Criterion")) {
			   	this.setBackground(new Color(255,244,141));
			}
			else if (string.startsWith("Nodexte")) {
			   	this.setBackground(new Color(240,255,255));
			}
			else if (string.startsWith("NodexteController")) {
			   	this.setBackground(new Color(245,245,221));
			}

		}
		else if (value instanceof Stack<?> && !((Stack<?>) value).isEmpty()){
			if (((Stack<?>) value).get(0) instanceof Message && ((Stack<?>) value).size() < 10){
				Stack<Message> stack = ((Stack<Message>) value);
				Stack<Message> newStack = new Stack<Message>();
				this.setText("");
				Icon[] icons = new Icon[stack.size()];
				while (!stack.isEmpty()) {
					Message m = stack.pop();
					Icon soloIcon = null;
					if (m.getSender() instanceof Percept) {
						soloIcon = Config.getIcon("status.png");
					} else if  (m.getSender() instanceof Context) {
						soloIcon = Config.getIcon("status-away.png");
					} else /*if  (m.getSender() instanceof Controller)*/ {
						soloIcon = Config.getIcon("status-offline.png");
					}
					
					icons[stack.size()] = soloIcon;
					newStack.push(m);
				}
				while (!newStack.isEmpty()) {
					stack.push(newStack.pop());
				}
	//			((Stack<Message>) value) = newStack;

				CompoundIcon icon = new CompoundIcon(icons);
				this.setIcon(icon);
			}
		}
		/*else {
			
			
			
			if (column == 1 && ((String)table.getValueAt(row, column-1)).startsWith("(Attribute)")) {
				this.setToolTipText(row + " " + column + " " + table.getValueAt(row, column-1));
				this.setBackground(new Color(211, 192, 255));
			}
			else {
				this.setBackground(Color.WHITE);
			}
			
		}

	}*/ else if (! ((value instanceof String)||(value instanceof Double)||(value instanceof Integer))){
		this.setText("");
	} else {
	//	this.setText(value.toString());
	}
		
		
	}
	return this;
	}
}
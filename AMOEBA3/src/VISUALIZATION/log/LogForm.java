package VISUALIZATION.log;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import MAS.kernel.Config;

// TODO: Auto-generated Javadoc
/**
 * The Class LogForm.
 */
public class LogForm {
	
	/** The console box. */
	private JCheckBox consoleBox;
	
	/** The file box. */
	private JCheckBox fileBox;
	
	/** The field criticity. */
	private JTextField fieldCriticity;
	
	/** The tag list. */
	private JComboBox[] tagList;
	
	/** The log frame. */
	private JFrame logFrame;
	
	/** The panel. */
	private JPanel panel;
	
	/** The label criticity. */
	private JLabel labelCriticity;
	
	/** The label console box. */
	private JLabel labelConsoleBox;
	
	/** The label file box. */
	private JLabel labelFileBox;
	
	/** The label tag. */
	private JLabel labelTag;
	
	/** The tag pos. */
	private int tagPos;
	
	/** The log file. */
	private LogFile logFile;
	
	/** The previous state. */
	private boolean previousState;

	/**
	 * Creates the log form.
	 */
	public void createLogForm() {
		
		previousState = logFile.getPreviousState();
		logFrame = new JFrame();
		logFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		panel = new JPanel();
		panel.setLayout(null);
		
		// Criticity
		labelCriticity = new JLabel("Criticity");
		labelCriticity.setBounds(10, 10, 80, 25);
		fieldCriticity = new JTextField(20);
		fieldCriticity.setBounds(100, 10, 120, 25);
		
		// CheckBox - Console
		consoleBox = new JCheckBox();
		labelConsoleBox = new JLabel("Console");
		consoleBox.setSelected(true);
		consoleBox.setBounds(10, 40, 25, 25);
		labelConsoleBox.setBounds(40, 40, 75, 25);
		
		// CheckBox - File
		fileBox = new JCheckBox();
		labelFileBox = new JLabel("File");
		fileBox.setBounds(120, 40, 25, 25);
		labelFileBox.setBounds(150, 40, 75, 25);
		
		if (previousState) {
			fieldCriticity.setText(Integer.toString(logFile.getCrititity()));
			consoleBox.setSelected(false);
			fileBox.setSelected(false);
			if (logFile.isConsole()) {
				consoleBox.setSelected(true);
			} 
			if (logFile.isFile()) {
				fileBox.setSelected(true);
			} 
		}
		
		// Agent tags
		labelTag = new JLabel("Agent tag(s)");
		labelTag.setBounds(10, 70, 80, 25);
		
		// Button OK
		JButton btnOK = new JButton("OK");
		btnOK.setBounds(90, 100, 70, 25);
		btnOK.addActionListener(e -> { getValueForLog(); }); 
		
		tagPos = 0;
		JButton[] btnDelete = new JButton[LogMessageType.values().length-1];
		
		// Button for adding new tag
		JButton btnAdd = new JButton(Config.getIcon("plus-button.png"));
		btnAdd.setBounds(250, 70, 25, 25);
		btnAdd.addActionListener(e -> { 
			// to add the tag and new button
			if(tagPos < LogMessageType.values().length - 1) {
				tagPos++;
				tagList[tagPos].setSelectedIndex(0);
				panel.add(tagList[tagPos]);
				panel.add(btnDelete[tagPos-1]);
				
			}
			// Disable to add button when it reaches the maximum number
			if(tagPos == LogMessageType.values().length - 1) {
				btnAdd.setEnabled(false);
			}
			btnOK.setBounds(140, 100 + 30*tagPos, 70, 25);
			// repaint the panel
			panel.revalidate();
			panel.repaint();
		});
		
		// Create the list of tag
		tagList = new JComboBox[LogMessageType.values().length];
		if (previousState) {
			tagPos = logFile.getLogMessageTypes().length - 1;
			btnOK.setBounds(140, 100 + 30*tagPos, 70, 25);
		}
		for (int i=0; i<LogMessageType.values().length; i++) {
			tagList[i] = new JComboBox<>(LogMessageType.values());
			tagList[i].setBounds(100, 70 + (30*i), 150, 25);
			if (i>0) {
				int pos = i-1;
				// Create the button for deleting the tag and its action
				btnDelete[i-1] = new JButton(Config.getIcon("minus-button.png"));
				btnDelete[i-1].setBounds(250,  70 + (30*i), 25, 25);
				btnDelete[i-1].addActionListener(e -> { 
					
					// store the value to the tag correspondent 
					for (int j=pos+1; j<tagPos; j++) {
						tagList[j].setSelectedItem(tagList[j+1].getSelectedItem());	
					}
					// enable the button ADD
					btnAdd.setEnabled(true);
					btnOK.setBounds(115, btnOK.getBounds().y - 30, 70, 25);
					// remove the tag and its button delete
					panel.remove(tagList[tagPos]);
					tagPos--;
					// repaint the panel
					panel.remove(btnDelete[tagPos]);
					panel.revalidate();
					panel.repaint();
				});	
			}
			if (previousState) {
				if (i < logFile.getLogMessageTypes().length) {
					tagList[i].setSelectedItem(logFile.getLogMessageTypes()[i]);
					if (i>0) {
						panel.add(tagList[i]);
						panel.add(btnDelete[i-1]);
					}
				}
				
			} 
		}
		
		if(tagPos == LogMessageType.values().length - 1) {
			btnAdd.setEnabled(false);
		}
		
		panel.add(labelCriticity);
		panel.add(fieldCriticity);
		panel.add(consoleBox);
		panel.add(labelConsoleBox);
		panel.add(fileBox);
		panel.add(labelFileBox);
		panel.add(labelTag);
		panel.add(tagList[0]);
		panel.add(btnAdd);
		panel.add(btnOK);
		
		Dimension dInfo = panel.getPreferredSize();
		dInfo.width = 300;
		dInfo.height = 400;
		panel.setPreferredSize(dInfo);
		
		logFrame.add(panel);
		logFrame.pack();
		logFrame.setVisible(true);
	}

	/**
	 * Gets the value for log.
	 *
	 * @return the value for log
	 */
	private void getValueForLog() {
		
		LogMessageType[] selectedTags = new LogMessageType[tagPos+1];;
		for (int i=0; i<tagPos+1; i++) {
			selectedTags[i] = (LogMessageType) tagList[i].getSelectedItem();
		}
		
		Set<LogMessageType> temp = new LinkedHashSet<LogMessageType>(Arrays.asList(selectedTags));
		LogMessageType[] types = temp.toArray(new LogMessageType[temp.size()]);
		Boolean isConsole, isFile;
		isConsole = consoleBox.isSelected();
		isFile = fileBox.isSelected();
		boolean parsable = true;
		int criticity = 0;
		try {
			criticity = Integer.parseInt(fieldCriticity.getText());
		} catch (NumberFormatException e) {
			parsable = false;
		}
		if (!parsable) {
			JOptionPane.showMessageDialog(logFrame, "Please check your form again!");
		} else {
			logFrame.dispose();
			logFile.generateLogFile();
			logFile.setConditionsForDebug(criticity, types, isConsole, isFile);
			logFile.setPreviousState(true);
		}
		
	}
	
	/**
	 * Sets the log file.
	 *
	 * @param logfile the new log file
	 */
	public void setLogFile(LogFile logfile) {
		this.logFile = logfile;
	}
}

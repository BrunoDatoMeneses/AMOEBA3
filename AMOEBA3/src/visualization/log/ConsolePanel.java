package visualization.log;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

// TODO: Auto-generated Javadoc
/**
 * The Class ConsolePanel.
 */
public class ConsolePanel extends JPanel {
	
	/** The console pane. */
	private JScrollPane consolePane;
	
	/** The text console. */
	private JTextArea textConsole;
	
	/** The panel button. */
	private JPanel panelButton;
	
	/**
	 * Instantiates a new console panel.
	 */
	public ConsolePanel() {
		this.setMinimumSize(new Dimension(400,400));
		this.setLayout(new BorderLayout());
		textConsole = new JTextArea();
		consolePane = new JScrollPane(textConsole);
		
		textConsole.setAutoscrolls(true);
		textConsole.setEditable(false);
		
		this.add(consolePane, BorderLayout.CENTER);
		
		panelButton = new JPanel();
		JButton btnReset = new JButton("Reset");
		JButton btnSave = new JButton("Save");
		panelButton.add(btnReset);
		panelButton.add(btnSave);
		
		this.add(panelButton, BorderLayout.SOUTH);
		
		btnReset.addActionListener(e -> { textConsole.setText(""); }); 
		btnSave.addActionListener(e -> { saveToFile(); }); 
		
	}
	
	
	/**
	 * Write in console.
	 *
	 * @param message the message
	 */
	public void writeInConsole(String message) {
		textConsole.append(message);
		textConsole.append("\n");
		this.revalidate();
		this.repaint();
	}
	
	/**
	 * Save to file.
	 */
	public void saveToFile() {
		System.out.println("Saving console to file...");
		final JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Console file (.out)", "out");
		fc.setFileFilter(filter);
		fc.setSelectedFile(new File("log.out"));
		int returnVal = fc.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            System.out.println(file);
            
    		try {
    			
    			BufferedWriter out = new BufferedWriter(new FileWriter(file));
    			out.write(textConsole.getText());
    			out.close();
    	 
    	        
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }
        System.out.println("End of saving console to file...");
	}

}

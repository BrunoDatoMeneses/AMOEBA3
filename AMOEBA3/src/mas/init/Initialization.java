package mas.init;

import java.awt.Dimension;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

// TODO: Auto-generated Javadoc
/**
 * The Class Initialization.
 */
public class Initialization {
	
	/** The source XML path. */
	static String sourceXMLPath = null;
	
	/** The agents XML path. */
	static String agentsXMLPath = null;
	
	/**
	 * Initialization files.
	 *
	 * @return the string[]
	 */
	public static String[] initializationFiles() {
		String[] filePaths = new String[2];
		final JFrame frame = new JFrame();
		JButton btnOK, btnCancel, btnSourceFile, btnAgentsFile;
		JPanel panel;
		JLabel label1, label2;
		final JTextField pathFile1, pathFile2;
		
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		panel = new JPanel();
		panel.setLayout(null);
		
		label1 = new JLabel("Source XML: ");
		label1.setBounds(10, 20, 90, 25);
		
		pathFile1 = new JTextField(50);
		pathFile1.setBounds(110, 20, 360, 25);
		
		btnSourceFile = new JButton("Choose File");
		btnSourceFile.setBounds(480, 20, 100, 25);
		
		label2 = new JLabel("Agents XML: ");
		label2.setBounds(10, 50, 90, 25);
		
		pathFile2 = new JTextField(50);
		pathFile2.setBounds(110, 50, 360, 25);
		
		btnAgentsFile = new JButton("Choose File");
		btnAgentsFile.setBounds(480, 50, 100, 25);
		
		btnOK = new JButton("OK");
		btnOK.setBounds(210, 90, 80, 25);
		
		btnCancel = new JButton("Cancel");
		btnCancel.setBounds(310, 90, 80, 25);
		
		btnSourceFile.addActionListener(e -> { popupFileChooser("Source File XML", pathFile1);});
		btnAgentsFile.addActionListener(e -> { popupFileChooser("Agents File XML", pathFile2);});
		btnOK.addActionListener(e -> { getFiles(frame, pathFile1, pathFile2); });
		btnCancel.addActionListener(e -> { System.exit(0); });
		panel.add(label1);
		panel.add(pathFile1);
		panel.add(btnSourceFile);
		panel.add(label2);
		panel.add(pathFile2);
		panel.add(btnAgentsFile);
		panel.add(btnOK);
		panel.add(btnCancel);
		
		Dimension dInfo = panel.getPreferredSize();
		dInfo.width = 600;
		dInfo.height = 150;
		panel.setPreferredSize(dInfo);
		
		frame.setTitle("Initialize files");
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		while (frame.isVisible()) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		filePaths[0] = sourceXMLPath;
		filePaths[1] = agentsXMLPath;
		return filePaths;
	}
	
	/**
	 * Popup file chooser.
	 *
	 * @param fileXML the file XML
	 * @param textField the text field
	 */
	static void popupFileChooser(String fileXML, JTextField textField) {
		final JFrame frame = new JFrame();
		final JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Choose the " + fileXML);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(fileXML + " (.xml)", "xml");
		fc.setFileFilter(filter);
		int returnVal = fc.showOpenDialog(frame);
		
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            file.setReadOnly();
            String path = file.getAbsolutePath();
            textField.setText(path);
            
        } 
	}
	
	/**
	 * Gets the files.
	 *
	 * @param frame the frame
	 * @param pathFile1 the path file 1
	 * @param pathFile2 the path file 2
	 * @return the files
	 */
	static void getFiles(JFrame frame, JTextField pathFile1, JTextField pathFile2) {
		String path1 = pathFile1.getText();
		String path2 = pathFile2.getText();
		if (path1 == null || path2 == null || path1.isEmpty() || path2.isEmpty()) {
			JOptionPane.showMessageDialog(frame, "Please choose your file(s)!");
		} else {
			sourceXMLPath = path1;
			agentsXMLPath = path2;
			frame.setVisible(false); 
			frame.dispose();
		}
	}
	
}

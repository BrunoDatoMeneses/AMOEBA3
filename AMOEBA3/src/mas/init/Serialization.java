package mas.init;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

// TODO: Auto-generated Javadoc
/**
 * The Class Serialization.
 */
public class Serialization {
	
	/**
	 * Open serialization file.
	 *
	 * @return the file
	 */
	public static File openSerializationFile() {
		final JFrame frame = new JFrame();
		final JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Choose the serialization file (if any)");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Serilaization file (.amo)", "amo");
		fc.setFileFilter(filter);
		int returnVal = fc.showOpenDialog(frame);
		
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            file.setReadOnly();
            System.out.println(file);
            return file;
        } else {
        	return null;
        }
	}
	

}

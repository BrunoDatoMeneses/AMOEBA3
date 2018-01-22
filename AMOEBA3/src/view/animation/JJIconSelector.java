package view.animation;

import java.io.File;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;


// TODO: Auto-generated Javadoc
/**
 * The Class JJIconSelector.
 */
public class JJIconSelector extends JComboBox{

	/** The path tab. */
	ArrayList pathTab;
	
	/**
	 * Instantiates a new JJ icon selector.
	 *
	 * @param path the path
	 */
	public JJIconSelector (String path) {
		//for (int i = 0 ; i <)
		//ImageIcon ico;
		super();
		
		File[] fichiersIcones = new File(path).listFiles();
		///ArrayList<ItemPheromone> phero = new ArrayList<ItemPheromone>();
		ArrayList<String> pathTab = new ArrayList<String>();
		for (File file : fichiersIcones) {
			if (!file.isHidden() && file.getName().endsWith(".png")){
		    	ImageIcon ico = new ImageIcon(file.getPath());
		    	this.addItem(ico);
			}
		}	
		
	}
	
	
}

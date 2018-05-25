package visualization.view.system.nDim;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mas.kernel.World;

// TODO: Auto-generated Javadoc
/**
 * The Class PanelParallelCoordinates.
 */
public class PanelParallelCoordinates extends JFXPanel{

	/** The world. */
	World world;
	
	/** The browser. */
	WebView browser;
	
	/** The web engine. */
	WebEngine webEngine;
	
	/**
	 * Instantiates a new panel parallel coordinates.
	 *
	 * @param world the world
	 */
	public PanelParallelCoordinates(World world) {
		this.world = world;
		JFXPanel pan = this;
		
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(pan);
                update();
            }
       });
        
	}


    /**
     * Inits the FX.
     *
     * @param fxPanel the fx panel
     */
    private void initFX(JFXPanel fxPanel) {
        // This method is invoked on the JavaFX thread
        Scene scene = createScene();
        fxPanel.setScene(scene);
    }

    /**
     * Creates the scene.
     *
     * @return the scene
     */
    private Scene createScene() {
        Group  root  =  new  Group();
        Text  text  =  new  Text();
        
        browser = new WebView();
        webEngine = browser.getEngine();
        //webEngine.load("file://" + System.getProperty( "user.dir") + "/bin/view/system/nDim/parallelCoordinates.html");
    
        InputStream input = getClass().getClassLoader().getResourceAsStream("parallelCoordinates.html");
        
		File file = new File(System.getProperty( "user.dir") + "/tmp/parallelCoordinates.html");
		
		inputStreamToFile(input, file);
		
        webEngine.load("file://" + file.getAbsolutePath());

        Scene  scene  =  new  Scene(browser,600,600);

        file.deleteOnExit();
        
        return (scene);
    }
    
    /**
     * Update.
     */
    public void update() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                webEngine.reload();
            }
       });    
    }
    
    /**
     * Input stream to file.
     *
     * @param input the input
     * @param file the file
     */
    private void inputStreamToFile(InputStream input, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte buf[] = new byte[1024];
			int len;
			while((len = input.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    
}

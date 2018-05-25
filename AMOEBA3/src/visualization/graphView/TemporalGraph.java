package visualization.graphView;

import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class TemporalGraph.
 */
public class TemporalGraph {
	
	/** The two dim graph list. */
	private ArrayList<GraphicVisualization2Dim> twoDimGraphList = new ArrayList<>();
	
	/** The n dim graph list. */
	private ArrayList<GraphicVisualizationNDim> nDimGraphList = new ArrayList<>();
	
	/**
	 * Creates the marker.
	 *
	 * @param tick the tick
	 * @param messages the messages
	 */
	public void createMarker(int tick, String messages) {
		 for(int i=0; i<twoDimGraphList.size(); i++) {
			 twoDimGraphList.get(i).createTempMarker(tick, messages); 
		 }
	}
	
	/**
	 * Gets the 2 dim graph list.
	 *
	 * @return the 2 dim graph list
	 */
	public ArrayList<GraphicVisualization2Dim> get2DimGraphList() {
		return twoDimGraphList;
	}
	
	/**
	 * Gets the n dim graph list.
	 *
	 * @return the n dim graph list
	 */
	public ArrayList<GraphicVisualizationNDim> getNDimGraphList() {
		return nDimGraphList;
	}
}

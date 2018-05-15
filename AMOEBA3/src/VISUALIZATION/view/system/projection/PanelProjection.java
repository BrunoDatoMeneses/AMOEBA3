package VISUALIZATION.view.system.projection;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

import MAS.kernel.World;
import VISUALIZATION.view.system.ScheduledItem;
import MAS.agents.Percept;
import MAS.agents.context.Context;
import MAS.agents.context.Range;

// TODO: Auto-generated Javadoc
/**
 * The Class PanelProjection.
 */
public class PanelProjection extends JPanel implements ScheduledItem{

	/** The world. */
	World world;
	
	/** The projection. */
	BufferedImage projection;
	
	/** The pas X. */
	private int pasX;
	
	/** The pas Y. */
	private int pasY;
	
	/** The start X. */
	private double startX;
	
	/** The end X. */
	private double endX;
	
	/** The start Y. */
	private double startY;
	
	/** The end Y. */
	private double endY;
	
	/** The max tol. */
	private double maxTol;
	
	/** The min tol. */
	private double minTol;
	
	/** The p X. */
	private Percept pX;
	
	/** The p Y. */
	private Percept pY;
	
	/** The first time. */
	private boolean firstTime = true;
	

	/**
	 * Instantiates a new panel projection.
	 *
	 * @param world the world
	 * @param pasX the pas X
	 * @param startX the start X
	 * @param endX the end X
	 * @param pX the p X
	 * @param pasY the pas Y
	 * @param startY the start Y
	 * @param endY the end Y
	 * @param pY the p Y
	 * @param minTol the min tol
	 * @param maxTol the max tol
	 */
	public PanelProjection(World world, int pasX, double startX, double endX, Percept pX, int pasY, double startY, double endY, Percept pY, double minTol, double maxTol) {
		projection = new BufferedImage(pasX, pasY, BufferedImage.TYPE_INT_ARGB);
		this.world = world;
		this.pasX = pasX;
		this.pasY = pasY;
		this.startX = startX;
		this.endX = endX;
		this.startY = startY;
		this.endY = endY;
		this.minTol = minTol;
		this.maxTol = maxTol;
		this.pX = pX;
		this.pY = pY;
				
	}
	
	/* (non-Javadoc)
	 * @see view.system.ScheduledItem#update()
	 */
	@Override
	public void update() {
		double incrX = (endX-startX)/pasX;
		double incrY = (endY-startY)/pasY;
		ArrayList<Range> rx = new ArrayList<Range>(), ry = new ArrayList<Range>();
		ArrayList<Context> cx, cy;
		
		double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
		double actions[][] = new double[pasX][pasY];
		
		int xStepInit = 0;
		int yStepInit = 0;
		int xStepFinal = pasX;
		int yStepFinal = pasY;
		
		if (!firstTime) {
			/*To allow faster redraw*/
			ArrayList<Context> alteredContexts = world.getScheduler().getAlteredContexts();
			double startXbis = Double.MAX_VALUE;
			double startYbis = Double.MAX_VALUE;
			double endXbis =  Double.MIN_VALUE;
			double endYbis =  Double.MIN_VALUE;

			for (Context c : alteredContexts) {
				if (c.getRanges().get(pX).getStart() < startXbis) {
					startXbis = c.getRanges().get(pX).getStart();
				}
				if (c.getRanges().get(pY).getStart() < startYbis) {
					startYbis = c.getRanges().get(pY).getStart();
				}
				if (c.getRanges().get(pX).getEnd() > endXbis) {
					endXbis = c.getRanges().get(pX).getEnd();
				}
				if (c.getRanges().get(pY).getEnd() > endYbis) {
					endYbis = c.getRanges().get(pY).getEnd();
				}
			}
			
	//		System.out.println("altered");
	//		startX = startXbis;
	//		startY = startYbis;
	//		endX = endXbis;
	//		endY = endYbis;
			
	//		System.out.println(startX + "   " + startXbis);

			while (startX + (xStepInit*incrX) < startXbis){
				xStepInit++;
			}
	//		System.out.println(startY + "   " + startYbis);

			while (startY + (yStepInit*incrY) < startYbis){
				yStepInit++;
			}
			
	//		System.out.println(endX + "   " + endXbis);

			while (endX - (xStepFinal*incrX) > endXbis){
				xStepFinal--;
			}
			
	//		System.out.println(endY + "   " + endYbis);

			while (endY - (yStepFinal*incrY) > endYbis){
				yStepFinal--;
			}
			
	//		System.out.println("fin modif");

		}
		else{
			firstTime = false;
		}



		


		for (int x = xStepInit ; x < xStepFinal ; x++) {
	
			for (int y = yStepInit ; y < yStepFinal ; y++) {
				rx.clear();
				ry.clear();
				
		//		pX.getTree().search(pX.getTree().top, startX + (x*incrX), rx);
		//		pY.getTree().search(pY.getTree().top, startY + (y*incrY), ry);
				
			//	System.out.println("px : " + rx);
			//	System.out.println("py : " + ry);
				
				cx = new ArrayList<Context>();
				cy = new ArrayList<Context>();
				
				for (Range r : rx) {
					cx.add(r.getContext());
				}
				for (Range r : ry) {
					cy.add(r.getContext());
				}
				
				for (Context c : cx) {
					if (cy.contains(c)) {
		//				double action = c.getActionProposal2D(pX, pY, startX + (x*incrX), startY + (y*incrY));
				//		if (action > 1) action = 1;
				//		if (action < -1) action = -1;
					//	double action = c.getAction();
		//				if (action > max && maxTol != Double.NaN) max = action;
		//				if (action < min && minTol != Double.NaN ) min = action;
		//				actions[x][y] = action;
					break;
					
					}
				}
				

				
	//			System.out.println(x + " : " + y);
			}
			

		}
		
		if (maxTol != Double.NaN) max = maxTol;
		if (minTol != Double.NaN ) min = minTol;
		
		for (int i = xStepInit ; i < xStepFinal ; i++) {
			for (int j = yStepInit ; j < yStepFinal ; j++) {
	//			System.out.println(actions[i][j] + "  " + max + "   " + min);
	//			System.out.println((int) (((actions[i][j] - min)/(max - min))*255));
				if (maxTol != Double.NaN && actions[i][j] > maxTol) {
					projection.setRGB(i, j, Color.red.getRGB());
				} else if (minTol != Double.NaN && actions[i][j] < minTol){
					projection.setRGB(i, j, Color.LIGHT_GRAY.getRGB());
				} else {
					projection.setRGB(i, j, new Color(0,0, (int) (((actions[i][j] - min)/(max - min))*255)).getRGB());
				}
			}
		}
		
		this.repaint();
		
	}
	
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
	public void paint(Graphics g) {
    	super.paint(g);
    	Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(projection,0,0,null);
    }

}

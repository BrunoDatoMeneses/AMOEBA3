package view.system.paving;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import view.animation.JJComponent;
import view.animation.JJPanel;
import agents.context.Context;


// TODO: Auto-generated Javadoc
/**
 * The Class PavingContext.
 */
public class PavingContext extends JJComponent{

	/** The Constant strokeWidth. */
	static final int strokeWidth = 2;

	/** The context. */
	private Context context;
	
	/** The scale. */
	private double scale;
	
	/** The paving. */
	private Panel1DPaving paving;
	
	/** The index. */
	private int index;

	
	/**
	 * Instantiates a new paving context.
	 *
	 * @param parent the parent
	 * @param xx the xx
	 * @param yy the yy
	 * @param context the context
	 * @param scale the scale
	 * @param paving the paving
	 * @param index the index
	 */
	public PavingContext(JJPanel parent, double xx, double yy, Context context, double scale, Panel1DPaving paving, int index) {
		super(parent, xx, 2 + (index*paving.heightPavingContext), (double)paving.heightPavingContext, (double)paving.heightPavingContext);
	//	this.setDragable(true);
		this.context = context;
		this.scale = scale;
		this.paving = paving;
		this.index = index;
	}

	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) 
	{   
		
		Graphics2D g2d = genererContexte(g);
		g2d.setColor(new Color(173,79,9));
		
		double xpos = context.getRanges().get(paving.getVariable()).getStart() - paving.getMin();
		double width = context.getRanges().get(paving.getVariable()).getEnd() - context.getRanges().get(paving.getVariable()).getStart();
	//	double width = 200.0;
		
    	this.setBounds((int)xpos, 2 + (index*paving.heightPavingContext), (int)width, paving.heightPavingContext);
//    	System.out.println("Width : " + width  + "   ::   " + (paving.getMax()-paving.getMin()));
    	this.setW(width);
    	this.setH(paving.heightPavingContext);
    	this.setXx(xpos);
    	this.setYy(index*paving.heightPavingContext);

		g2d.fillRect(0, 0, (int)width, paving.heightPavingContext);
/*		if (context.countGoodPredictions() == 0) {
			g2d.setColor(Color.red);
		} else {
			g2d.setColor(new Color(245,245,220));
		}*/
		g2d.fillRect(0+strokeWidth, 0+strokeWidth, (int)width-(2*+strokeWidth), paving.heightPavingContext-(2*+strokeWidth));
//		g2d.setColor(new Color(209,182,6));
//		g2d.fillOval(border/2, border/2, radius-border, radius-border);

	}


	/**
	 * Gets the context.
	 *
	 * @return the context
	 */
	public Context getContext() {
		return context;
	}


	/**
	 * Sets the context.
	 *
	 * @param context the new context
	 */
	public void setContext(Context context) {
		this.context = context;
	}


	/**
	 * Change index.
	 *
	 * @param i the i
	 */
	public void changeIndex(int i) {
		index += i;
		
	}


	/**
	 * Sets the index.
	 *
	 * @param nContext the new index
	 */
	public void setIndex(int nContext) {
		index = nContext;
		
	}



	
	
	
}

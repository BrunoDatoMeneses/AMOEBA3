package VISUALIZATION.view.system.paving;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import VISUALIZATION.view.animation.JJComponent;
import VISUALIZATION.view.animation.JJPanel;


// TODO: Auto-generated Javadoc
/**
 * The Class MonoDimensionLine.
 */
public class MonoDimensionLine extends JJComponent{

	/** The Constant width. */
	static final int width = 4;
	
	/** The end. */
	private double start, end;
	
	/** The scale. */
	private double scale;

	
	/**
	 * Instantiates a new mono dimension line.
	 *
	 * @param parent the parent
	 * @param xx the xx
	 * @param yy the yy
	 * @param start the start
	 * @param end the end
	 * @param scale the scale
	 */
	public MonoDimensionLine(JJPanel parent, double xx, double yy, double start, double end, double scale) {
		super(parent, xx, yy, (end - start)*scale, (double)width);
		this.start = start;
		this.end = end;
		this.scale = scale;
	}

	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) 
	{   
//		System.out.println("Paint mono dimension line");

		Graphics2D g2d = genererContexte(g);
		g2d.setColor(Color.BLACK);
		setW(((end - start)*scale));
		g2d.fillRect(0, 0, (int)((end - start)*scale), width);
		if (start < 0) {
			g2d.setColor(Color.RED);
			g2d.fillRect((int) -start, 0, (int)2, width);
		}

    	//this.setXx(-1*start);

//		g2d.setColor(new Color(209,182,6));
//		g2d.fillOval(border/2, border/2, radius-border, radius-border);

	}


	/**
	 * Update.
	 *
	 * @param min the min
	 * @param max the max
	 */
	public void update(double min, double max) {
		start = min;
		end = max;
	}



	
	
	
}

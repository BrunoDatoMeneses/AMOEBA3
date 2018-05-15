package VISUALIZATION.view.animation;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;


// TODO: Auto-generated Javadoc
/**
 * The Class JJPanel.
 */
public class JJPanel extends JPanel implements ActionListener{

	/** The elements animes. */
	int elementsAnimes = 0;
	
	/** The delay. */
	int delay = 15;
	
	/** The timer. */
	Timer timer;
	
	/** The size X. */
	int sizeX = 0;
	
	/** The size Y. */
	int sizeY = 0;
	
	/** The max Y. */
	JJComponent maxX, maxY;
	
	/**
	 * Instantiates a new JJ panel.
	 */
	public JJPanel(){
		super();

		
	// this.setDoubleBuffered(true);
	    this.setVisible(true);
	    this.setLayout(null);
	    
        timer = new Timer (delay, this);
        timer.start();
		}


	/**
	 * Animate.
	 */
	public void animate(){
		if (elementsAnimes > 0 ){
			//System.out.println("animate>0");
	        int max = this.getComponentCount();
	        Component[] components = this.getComponents();
	        for (int i = 0; i < max ; i++){
	        	if (components[i] instanceof JJComponent){
		        	if (((JJComponent) components[i]).animate()) {
		        		elementsAnimes--;
		        		if (((JJComponent) components[i]).isSuppressionProgrammee()){
		        			this.remove(components[i]);
		        		}
		        	}
	        		}
	        	}
	        }
		//	this.repaint();
		}
		
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		//System.out.println("getPreferredSize" + this.toString());
		return super.getPreferredSize();
	}
	
	/*
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.clearRect(0, 0, 5000, 5000);

       // redessiner(g);
    	super.paintComponents(g);
        int max = this.getComponentCount();

        Component[] components = this.getComponents();
        
        for (int i = 0; i < max ; i++){
      //  	components[i].paint(g2d);
        }

    }
    */
/*

   public void paintChildren(Graphics g){

    }
*/
    
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		animate();
		repaint();
		//this.setPreferredSize(getMaximumSize());
		revalidate();
		
	}
	
	/**
	 * Animation ajoutee.
	 */
	public void animationAjoutee(){
		elementsAnimes++;
	}


	/**
	 * Gets the delay.
	 *
	 * @return the delay
	 */
	public int getDelay() {
		return delay;
	}


	/**
	 * Sets the delay.
	 *
	 * @param delay the new delay
	 */
	public void setDelay(int delay) {
		this.delay = delay;
		timer.setDelay(delay);
	}
	
	/**
	 * Update preferred size.
	 *
	 * @param compo the compo
	 */
	public void updatePreferredSize(JJComponent compo) {
		if (compo.getXx() + compo.getW() > sizeX) {
			sizeX = (int) (compo.getXx() + compo.getW());
			this.setPreferredSize(new Dimension(sizeX,sizeY));
			maxX = compo;
		}
		if (compo.getYy() + compo.getH() > sizeY) {
			sizeY = (int) (compo.getYy() + compo.getH());
			this.setPreferredSize(new Dimension(sizeX,sizeY));
			maxY = compo;
		}
		if (compo == maxX && compo.getXx() + compo.getW() < sizeX) {
			sizeX = (int) (compo.getXx() + compo.getW());
	        Component[] components = this.getComponents();
	        int max = this.getComponentCount();
	        for (int i = 0; i < max ; i++){
	        	if (components[i] instanceof JJComponent){
		        	this.updatePreferredSize((JJComponent) components[i]);
	        		}
	        	}
	        }
		
	if (compo == maxY && compo.getYy() + compo.getH() < sizeY) {
		sizeY = (int) (compo.getYy() + compo.getH());
        Component[] components = this.getComponents();
        int max = this.getComponentCount();
        for (int i = 0; i < max ; i++){
        	if (components[i] instanceof JJComponent){
	        	this.updatePreferredSize((JJComponent) components[i]);
        		}
        	}
        }
	
	
	}



	
}

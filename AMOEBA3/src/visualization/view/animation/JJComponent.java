package visualization.view.animation;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JComponent;

// TODO: Auto-generated Javadoc
/**
 * The Class JJComponent.
 */
public class JJComponent extends JComponent{


	
	/** The xx. */
	protected double xx;
	
	/** The yy. */
	protected double yy;
	
	/** The w. */
	protected double w;
	
	/** The h. */
	protected double h;
	
	/** The theta. */
	double theta;
	
	/** The opacite. */
	double opacite = 1.0;
	
	/** The suppression programmee. */
	boolean suppressionProgrammee = false;
	
	/** The dragable. */
	boolean dragable = false;
	
	/** The animations. */
	ArrayList<JJAnimation> animations;
	
	/** The parent. */
	protected JJPanel parent;
	
	
	/**
	 * Instantiates a new JJ component.
	 *
	 * @param parent the parent
	 * @param xx the xx
	 * @param yy the yy
	 * @param w the w
	 * @param h the h
	 */
	public JJComponent(JJPanel parent , double xx , double yy , double w , double h)
	{
		super();
		this.xx = xx;
		this.yy = yy;
		//this.setLocation((int)xx, (int)yy);
		//this.setSize((int)w, (int)h);
		this.setBounds((int)xx, (int)yy,(int)w, (int)h);
		parent.updatePreferredSize(this);

		
		this.w = w;
		this.h = h;
		this.parent = parent;
		theta = 0;
		animations = new ArrayList<JJAnimation>();
		this.addMouseListener(new JJComponentMouseListener(this));
		this.addMouseMotionListener(new JJComponentMouseMotionListener(this));

	}
	
	/**
	 * Adds the animation.
	 *
	 * @param anim the anim
	 */
	public void addAnimation(JJAnimation anim){
		animations.add(anim);
		parent.animationAjoutee();
	}
	
	/**
	 * Animate.
	 *
	 * @return true, if successful
	 */
	public boolean animate(){


		if (!animations.isEmpty()){
			for (int i = 0; i < animations.size(); i++){
				if (!animations.get(i).animer()){  //On applique l'animation et si elle est finie on la retire de la liste
					if(animations.get(i).isVieLimite()){
						suppressionProgrammee = true;
						animations.remove(i);
						i--;
						return true;

					}		
					animations.remove(i);
					i--;
				}

			}

			if (animations.isEmpty()){
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			
			return false;
		}
	}
	

	
	/**
	 * Generer contexte.
	 *
	 * @param g the g
	 * @return the graphics 2 D
	 */
	protected Graphics2D genererContexte(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.translate(xx+(w/2),yy+(h/2));
		g2d.rotate(theta);
		g2d.translate(-xx-(w/2),-yy-(h/2));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) opacite));

		return g2d;
	}
    
	

	/**
	 * Gets the xx.
	 *
	 * @return the xx
	 */
	public double getXx() {
		return xx;
	}

	/**
	 * Sets the xx.
	 *
	 * @param xx the new xx
	 */
	public void setXx(double xx) {
		parent.updatePreferredSize(this);
		this.xx = xx;
		this.setLocation((int)xx, (int)yy);

	}

	/**
	 * Gets the yy.
	 *
	 * @return the yy
	 */
	public double getYy() {
		return yy;
	}

	/**
	 * Sets the yy.
	 *
	 * @param yy the new yy
	 */
	public void setYy(double yy) {
		parent.updatePreferredSize(this);
		this.yy = yy;
		this.setLocation((int)xx, (int)yy);

	}

	/**
	 * Gets the w.
	 *
	 * @return the w
	 */
	public double getW() {
		return w;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getHeight()
	 */
	@Override
	public int getHeight(){
		return (int) h;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getWidth()
	 */
	@Override
	public int getWidth(){
		return (int) w;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getX()
	 */
	@Override
	public int getX(){
		return (int) xx;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getY()
	 */
	@Override
	public int getY(){
		return (int) yy;
	}
	
	/**
	 * Sets the w.
	 *
	 * @param w the new w
	 */
	public void setW(double w) {
		parent.updatePreferredSize(this);
		this.w = w;
	}

	/**
	 * Gets the h.
	 *
	 * @return the h
	 */
	public double getH() {
		return h;
	}

	/**
	 * Sets the h.
	 *
	 * @param h the new h
	 */
	public void setH(double h) {
		parent.updatePreferredSize(this);
		this.h = h;
	}

	/**
	 * Gets the theta.
	 *
	 * @return the theta
	 */
	public double getTheta() {
		return theta;
	}

	/**
	 * Sets the theta.
	 *
	 * @param theta the new theta
	 */
	public void setTheta(double theta) {
		this.theta = theta;
	}

	/**
	 * Gets the opacite.
	 *
	 * @return the opacite
	 */
	public double getOpacite() {
		return opacite;
	}

	/**
	 * Sets the opacite.
	 *
	 * @param d the new opacite
	 */
	public void setOpacite(double d) {
		this.opacite = d;
		if (this.opacite < 1.){
	        setOpaque(false);
		}
		else{
	        setOpaque(true);
		}
		if(this.opacite < 0) this.opacite = 0.;
		if(this.opacite > 1) this.opacite = 1.;
	}

	/**
	 * Checks if is suppression programmee.
	 *
	 * @return true, if is suppression programmee
	 */
	public boolean isSuppressionProgrammee() {
		return suppressionProgrammee;
	}

	/**
	 * Sets the suppression programmee.
	 *
	 * @param suppressionProgrammee the new suppression programmee
	 */
	public void setSuppressionProgrammee(boolean suppressionProgrammee) {
		this.suppressionProgrammee = suppressionProgrammee;
	}

	/**
	 * Checks if is dragable.
	 *
	 * @return true, if is dragable
	 */
	public boolean isDragable() {
		return dragable;
	}

	/**
	 * Sets the dragable.
	 *
	 * @param dragable the new dragable
	 */
	public void setDragable(boolean dragable) {
		this.dragable = dragable;
	}
    
	/**
	 * Gets the centre X.
	 *
	 * @return the centre X
	 */
	public double getCentreX(){
		return getXx() + w/2.;
	}
	
	/**
	 * Gets the centre Y.
	 *
	 * @return the centre Y
	 */
	public double getCentreY(){
		return getYy() + h/2.;
	}
	

    
}
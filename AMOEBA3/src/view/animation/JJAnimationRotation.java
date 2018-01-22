package view.animation;

// TODO: Auto-generated Javadoc
/**
 * The Class JJAnimationRotation.
 */
public class JJAnimationRotation extends JJAnimation{

	/** The angle. */
	double angle;
	
	/**
	 * Instantiates a new JJ animation rotation.
	 *
	 * @param pas the pas
	 * @param compo the compo
	 * @param angle the angle
	 * @param vieLimite the vie limite
	 */
	public JJAnimationRotation(int pas , JJComponent compo , double angle, boolean vieLimite) {
		super(pas,compo,vieLimite);
		this.angle = angle;
	}
	
	/* (non-Javadoc)
	 * @see view.animation.JJAnimation#animer()
	 */
	@Override
	public boolean animer(){

		compo.setTheta(compo.getTheta() + angle);



		return super.animer();
	}	

}

package VISUALIZATION.view.animation;

// TODO: Auto-generated Javadoc
/**
 * The Class JJAnimationTranslation.
 */
public class JJAnimationTranslation extends JJAnimation{

	/** The dy. */
	double dx , dy;
	
	/**
	 * Instantiates a new JJ animation translation.
	 *
	 * @param pas the pas
	 * @param compo the compo
	 * @param dx the dx
	 * @param dy the dy
	 * @param vieLimite the vie limite
	 */
	public JJAnimationTranslation(int pas , JJComponent compo , double dx , double dy, boolean vieLimite) {
		super(pas,compo,vieLimite);
		this.dx = dx;
		this.dy = dy;
	}
	
	/* (non-Javadoc)
	 * @see view.animation.JJAnimation#animer()
	 */
	@Override
	public boolean animer(){

		compo.setXx(compo.getXx() + dx);
		compo.setYy(compo.getYy() + dy);

		return super.animer();
	}	

}

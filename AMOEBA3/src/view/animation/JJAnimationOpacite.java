package view.animation;

// TODO: Auto-generated Javadoc
/**
 * The Class JJAnimationOpacite.
 */
public class JJAnimationOpacite extends JJAnimation{

	
	/** The d opacite. */
	double dOpacite;
	
	/**
	 * Instantiates a new JJ animation opacite.
	 *
	 * @param pas the pas
	 * @param compo the compo
	 * @param dOpacite the d opacite
	 * @param vieLimite the vie limite
	 */
	public JJAnimationOpacite(int pas , JJComponent compo , double dOpacite, boolean vieLimite) {
		super(pas,compo,vieLimite);
		this.dOpacite = dOpacite;
	}
	
	/* (non-Javadoc)
	 * @see view.animation.JJAnimation#animer()
	 */
	@Override
	public boolean animer(){

		compo.setOpacite(compo.getOpacite()+dOpacite);

		return super.animer();
	}	
	
}

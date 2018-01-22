package view.animation;

// TODO: Auto-generated Javadoc
/**
 * The Class JJAnimationTranslationEntreDeuxObjets.
 */
public class JJAnimationTranslationEntreDeuxObjets extends JJAnimation{

	/** The pas initial. */
	int pasInitial;
	
	/** The a. */
	JJComponent a;
	
	/** The b. */
	JJComponent b;
	
	/**
	 * Instantiates a new JJ animation translation entre deux objets.
	 *
	 * @param pas the pas
	 * @param compo the compo
	 * @param a the a
	 * @param b the b
	 * @param vieLimite the vie limite
	 */
	public JJAnimationTranslationEntreDeuxObjets(int pas , JJComponent compo , JJComponent a, JJComponent b, boolean vieLimite) {
		super(pas,compo,vieLimite);
		this.a = a;
		this.b = b;
		pasInitial = pas;
	}
	
	/* (non-Javadoc)
	 * @see view.animation.JJAnimation#animer()
	 */
	@Override
	public boolean animer(){

		compo.setXx(a.getCentreX() + (((pasInitial-pas)/(double)pasInitial)*(b.getCentreX() - a.getCentreX())) - (compo.getW()/2.));
		compo.setYy(a.getCentreY() + (((pasInitial-pas)/(double)pasInitial)*(b.getCentreY() - a.getCentreY())) - (compo.getH()/2.));

		//compo.setXx((a.getXx() + b.getXx())/2);
		//compo.setYy((a.getYy() + b.getYy())/2);

		
		return super.animer();
	}	

	
}

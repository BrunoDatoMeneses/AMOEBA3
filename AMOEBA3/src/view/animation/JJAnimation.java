package view.animation;

// TODO: Auto-generated Javadoc
/**
 * The Class JJAnimation.
 */
public abstract class JJAnimation {

	
	/** The pas. */
	int pas;
	
	/** The compo. */
	JJComponent compo;
	
	/** The vie limite. */
	boolean vieLimite;
	
	/**
	 * Instantiates a new JJ animation.
	 *
	 * @param pas , compo , vieLimite
	 * @param compo the compo
	 * @param vieLimite the vie limite
	 */
	public JJAnimation(int pas , JJComponent compo, boolean vieLimite){
		this.pas = pas;
		this.compo = compo;
		this.vieLimite = vieLimite;
	}
	
	/**
	 * Animer.
	 *
	 * @return true, if successful
	 */
	public boolean animer(){
		pas--;
		if (pas == 0){
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Gets the pas.
	 *
	 * @return the pas
	 */
	public int getPas() {
		return pas;
	}

	/**
	 * Sets the pas.
	 *
	 * @param pas the new pas
	 */
	public void setPas(int pas) {
		this.pas = pas;
	}

	/**
	 * Checks if is vie limite.
	 *
	 * @return true, if is vie limite
	 */
	public boolean isVieLimite() {
		return vieLimite;
	}

	/**
	 * Sets the vie limite.
	 *
	 * @param vieLimite the new vie limite
	 */
	public void setVieLimite(boolean vieLimite) {
		this.vieLimite = vieLimite;
	}
	
	/**
	 * Decaler pas.
	 *
	 * @param offset the offset
	 */
	public void decalerPas(int offset){
		pas += offset;
	}

	
	
}

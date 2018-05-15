package VISUALIZATION.view.animation;

import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class JJAnimationForDiagramme.
 */
public class JJAnimationForDiagramme extends JJAnimation{

	/** The increments. */
	ArrayList<Double> increments;
	
	/** The diag. */
	JJStatDiagramme diag;
	
	/**
	 * Instantiates a new JJ animation for diagramme.
	 *
	 * @param pas the pas
	 * @param compo the compo
	 * @param vieLimite the vie limite
	 */
	public JJAnimationForDiagramme(int pas , JJStatDiagramme compo, boolean vieLimite) {
		super(pas,compo,vieLimite);
		diag = compo;
		increments = new ArrayList<Double>();

		//Calcul des increments
		for (int i = 0; i < diag.getListeValeurs().size(); i++){
			/*System.out.println(diag.getValeursPositions().get(i));
			System.out.println(diag.getValeurTotalePosition());
			System.out.println(diag.getListeValeurs().get(i));
			System.out.println(diag.getValeurTotale());*/

			increments.add((-1)*((diag.getValeursPositions().get(i)-diag.getListeValeurs().get(i))*(diag.getValeurTotalePosition()/diag.getValeurTotale()))/pas);

		}
	}
	
	/**
	 * Instantiates a new JJ animation for diagramme.
	 *
	 * @param pas the pas
	 * @param compo the compo
	 * @param dx the dx
	 * @param dy the dy
	 * @param vieLimite the vie limite
	 */
	// Pour rendre inutilisable le constructeur par d_faut
	private JJAnimationForDiagramme(int pas , JJComponent compo , double dx , double dy, boolean vieLimite) {
		super(pas,compo,vieLimite);
	}
	
	/* (non-Javadoc)
	 * @see view.animation.JJAnimation#animer()
	 */
	@Override
	public boolean animer(){

		for (int i = 0; i < diag.getListeValeurs().size(); i++){
			/*System.out.println(diag.getValeursPositions().get(i));
			System.out.println("Incr : " + increments.get(i));*/

			diag.getValeursPositions().set(i, diag.getValeursPositions().get(i) + increments.get(i));
		}
		

		return super.animer();
	}	

}

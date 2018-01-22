package view.animation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class JJStatDiagramme.
 */
public class JJStatDiagramme extends JJComponent{


	/** The liste couleurs. */
	ArrayList<Color> listeCouleurs;
	
	/** The liste noms. */
	ArrayList<String> listeNoms;
	
	/** The liste valeurs. */
	ArrayList<Double> listeValeurs;
	
	/** The valeurs positions. */
	ArrayList<Double> valeursPositions;
	
	/** The valeur totale position. */
	Double valeurTotale , valeurTotalePosition;
	
	/**
	 * Instantiates a new JJ stat diagramme.
	 *
	 * @param parent the parent
	 * @param xx the xx
	 * @param yy the yy
	 * @param w the w
	 * @param h the h
	 */
	public JJStatDiagramme(JJPanel parent , double xx, double yy, double w, double h) {
		super(parent, xx, yy, w, h);
		//System.out.println("Creation du diagramme");
		listeCouleurs = new ArrayList<Color>();
		listeNoms = new ArrayList<String>();
		listeValeurs = new ArrayList<Double>();
		this.setBounds((int)xx,(int)yy,(int)w,(int)h);
		
	}

	/**
	 * Adds the item.
	 *
	 * @param c the c
	 * @param s the s
	 * @param d the d
	 */
	public void addItem(Color c, String s, Double d){
		listeCouleurs.add(c);
		listeNoms.add(s);
		listeValeurs.add(d);
		valeursPositions = listeValeurs;
		valeurTotale = 0.;
		String nouveauToolTip = "<html>";
	
		for (int i = 0; i < listeValeurs.size(); i++){
			valeurTotale += listeValeurs.get(i);
		} 
		
		for (int i = 0; i < listeValeurs.size(); i++){
			String couleurHexa = Integer.toHexString(listeCouleurs.get(i).getRGB()).substring(2);
			nouveauToolTip += "<FONT COLOR=\"#" + couleurHexa
				+ "\" >" + listeNoms.get(i) + "</FONT>"
				+ "<FONT COLOR=\"#000000\">" + " : " + (listeValeurs.get(i)/valeurTotale)*100. + "%" +"</FONT>"
				+ "<br>";
		}
		 nouveauToolTip += "</html>";
		 setToolTipText(nouveauToolTip);
		valeurTotalePosition = valeurTotale;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) 
    {    
		super.paintComponent(g);
		//System.out.println("paint diag" +getXx()+" "+getYy()+" " +getW()+" " +getH() + "  "+valeurTotalePosition + " " + (valeursPositions.get(1)/valeurTotalePosition)*getW());
        Graphics2D g2d = genererContexte(g);
		//System.out.println("Affichage du diagramme");

    	//FontMetrics fm = g2d.getFontMetrics();
    	this.setBounds((int) getXx(),(int) getYy(),(int) getW(),(int) getH());

    	int decalageX = 0;

    	for (int i = 0; i < listeCouleurs.size(); i++){
        	g2d.setColor(listeCouleurs.get(i));
        	g2d.fill(new Rectangle2D.Double(decalageX,0,(int) ((valeursPositions.get(i)/valeurTotalePosition)*getW()),(int) getH()));
        	decalageX += (valeursPositions.get(i)/valeurTotalePosition)*getW();
    	}
    	g2d.setColor(Color.BLACK);
    	g2d.draw(new Rectangle2D.Double(0,0,(int) getW(),(int) getH()));
    	
    	//g2d.drawString(cogniton.getNom(), (float) this.getXx()+margeEcriture, (float) (this.getYy()+(fm.getHeight()*1.3)));

    	//System.out.println("dessin du composant");
    	//validate();
    }
	
	/**
	 * Sets the valeur.
	 *
	 * @param nouvellesValeurs the nouvelles valeurs
	 * @param isAnimated the is animated
	 */
	public void setValeur(ArrayList<Double> nouvellesValeurs , boolean isAnimated){

		valeursPositions = listeValeurs;
		
		valeurTotale = 0.;
		for (int i = 0; i < nouvellesValeurs.size(); i++){
			valeurTotale += nouvellesValeurs.get(i);
		}
		listeValeurs = nouvellesValeurs;

		for (int i = 0; i < valeursPositions.size(); i++){
			valeursPositions.set(i, valeursPositions.get(i) * (valeurTotale/valeurTotalePosition));
		}
		valeurTotalePosition = valeurTotale;

		
		String nouveauToolTip = "<html>";
		for (int i = 0; i < listeValeurs.size(); i++){
			String couleurHexa = Integer.toHexString(listeCouleurs.get(i).getRGB()).substring(2);
			nouveauToolTip += "<FONT COLOR=\"#" + couleurHexa
				+ "\" >" + listeNoms.get(i) + "</FONT>"
				+ "<FONT COLOR=\"#000000\">" + " : " + (listeValeurs.get(i)/valeurTotale)*100. + "%  " + nouvellesValeurs.get(i)  +"</FONT>"
				+ "<br>";
		}
		 nouveauToolTip += "</html>";
		 setToolTipText(nouveauToolTip);
		 
			if (isAnimated){
				this.addAnimation(new JJAnimationForDiagramme(150, this, false));
			}
			else{
				valeursPositions = listeValeurs;
				valeurTotalePosition = valeurTotale;
			}
	}

	/**
	 * Sets the valeur.
	 *
	 * @param valeurs the valeurs
	 * @param isAnimated the is animated
	 */
	public void setValeur(double valeurs[], boolean isAnimated) {
		ArrayList<Double> doubles = new ArrayList<Double>();
		for (int i = 0; i < valeurs.length; i++){
			doubles.add(valeurs[i]);
		}
		this.setValeur(doubles, isAnimated);
		
	}
		
		
	/**
	 * Gets the liste couleurs.
	 *
	 * @return the liste couleurs
	 */
	public ArrayList<Color> getListeCouleurs() {
		return listeCouleurs;
	}

	/**
	 * Gets the liste noms.
	 *
	 * @return the liste noms
	 */
	public ArrayList<String> getListeNoms() {
		return listeNoms;
	}

	/**
	 * Gets the liste valeurs.
	 *
	 * @return the liste valeurs
	 */
	public ArrayList<Double> getListeValeurs() {
		return listeValeurs;
	}

	/**
	 * Gets the valeurs positions.
	 *
	 * @return the valeurs positions
	 */
	public ArrayList<Double> getValeursPositions() {
		return valeursPositions;
	}

	/**
	 * Gets the valeur totale.
	 *
	 * @return the valeur totale
	 */
	public Double getValeurTotale() {
		return valeurTotale;
	}

	/**
	 * Gets the valeur totale position.
	 *
	 * @return the valeur totale position
	 */
	public Double getValeurTotalePosition() {
		return valeurTotalePosition;
	}


	}
    
	

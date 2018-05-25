package visualization.view.animation;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving JJComponentMouse events.
 * The class that is interested in processing a JJComponentMouse
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addJJComponentMouseListener<code> method. When
 * the JJComponentMouse event occurs, that object's appropriate
 * method is invoked.
 *
 * @see JJComponentMouseEvent
 */
public class JJComponentMouseListener implements MouseListener{

	/** The c. */
	JJComponent c;
	
	/**
	 * Instantiates a new JJ component mouse listener.
	 *
	 * @param c the c
	 */
	public JJComponentMouseListener(JJComponent c){
		this.c = c;
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}

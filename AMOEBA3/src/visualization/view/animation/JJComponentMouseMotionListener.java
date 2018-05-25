package visualization.view.animation;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving JJComponentMouseMotion events.
 * The class that is interested in processing a JJComponentMouseMotion
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addJJComponentMouseMotionListener<code> method. When
 * the JJComponentMouseMotion event occurs, that object's appropriate
 * method is invoked.
 *
 * @see JJComponentMouseMotionEvent
 */
public class JJComponentMouseMotionListener implements MouseMotionListener{

	/** The c. */
	JJComponent c;
	
	/**
	 * Instantiates a new JJ component mouse motion listener.
	 *
	 * @param c the c
	 */
	public JJComponentMouseMotionListener(JJComponent c){
		this.c = c;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		if (c.isDragable()){
			if (e.getSource().equals(c)){
				c.setXx(c.getXx() + e.getX());
				c.setYy(c.getYy() + e.getY());
			}
			else
			{
				c.setXx(e.getX());
				c.setYy(e.getY());
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}

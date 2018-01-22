/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * -----------------------
 * XYAnnotationEntity.java
 * -----------------------
 * (C) Copyright 2004-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 29-Sep-2004 : Version 1 (DG);
 *
 */

package graphView.annotations;

import java.awt.Shape;
import java.io.Serializable;
import org.jfree.chart.entity.ChartEntity;

// TODO: Auto-generated Javadoc
/**
 * A chart entity that represents an annotation on an
 * {@link org.jfree.chart.plot.XYPlot}.
 */
public class AxisAnnotationEntity extends ChartEntity
                                implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 2340334068383660799L;

    /** The renderer index. */
    private int rendererIndex;
    
    /** The annotation. */
    //*the annotation
    private AxisAnnotation annotation;

    /**
     * Creates a new entity.
     *
     * @param hotspot  the area.
     * @param rendererIndex  the rendererIndex (zero-based index).
     * @param toolTipText  the tool tip text.
     * @param urlText  the URL text for HTML image maps.
     * @param annotation the annotation
     */
    public AxisAnnotationEntity(Shape hotspot, int rendererIndex,
                              String toolTipText, String urlText, AxisAnnotation annotation) {
        super(hotspot, toolTipText, urlText);
        this.rendererIndex = rendererIndex;
        this.annotation = annotation;
    }

    /**
     * Returns the renderer index.
     *
     * @return The renderer index.
     */
    public int getRendererIndex() {
        return this.rendererIndex;
    }

    /**
     * Gets the annotation.
     *
     * @return the annotation
     */
    public AxisAnnotation getAnnotation() {
        return this.annotation;
    }

    /**
     * Sets the renderer index.
     *
     * @param index  the item index (zero-based).
     */
    public void setRendererIndex(int index) {
        this.rendererIndex = index;
    }

    /**
     * Tests the entity for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof AxisAnnotationEntity)) {
            return false;
        }
        AxisAnnotationEntity that = (AxisAnnotationEntity) obj;
        if (this.rendererIndex != that.rendererIndex) {
            return false;
        }
        return true;
    }

}

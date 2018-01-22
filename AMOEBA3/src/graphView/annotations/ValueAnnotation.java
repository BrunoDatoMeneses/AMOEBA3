package graphView.annotations;

import java.awt.Paint;
import java.awt.Stroke;

// TODO: Auto-generated Javadoc
/**
 * The Class ValueAnnotation.
 *
 * @author peter
 */
public abstract class ValueAnnotation extends AxisAnnotation {

    /** The value. */
    private double value;

    /**
     * Instantiates a new value annotation.
     */
    protected ValueAnnotation() {
        super();
    }

    /**
     * Instantiates a new value annotation.
     *
     * @param stroke the stroke
     * @param paint the paint
     * @param value the value
     */
    protected ValueAnnotation(Stroke stroke, Paint paint, double value) {
        super(stroke, paint);
        this.value = value;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public double getValue() {
        return this.value;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(double value) {
        this.value = value;
        fireAnnotationChanged();
    }
}

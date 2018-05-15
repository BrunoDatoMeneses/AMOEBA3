package VISUALIZATION.graphView.annotations;

import java.awt.Paint;
import java.awt.Stroke;

// TODO: Auto-generated Javadoc
/**
 * The Class IntervalAnnotation.
 *
 * @author peter
 */
public abstract class IntervalAnnotation extends AxisAnnotation {

    /** The start value. */
    private double startValue;
    
    /** The end value. */
    private double endValue;

    /**
     * Instantiates a new interval annotation.
     */
    protected IntervalAnnotation() {
        super();
    }

    /**
     * Instantiates a new interval annotation.
     *
     * @param startValue the start value
     * @param endValue the end value
     */
    protected IntervalAnnotation(double startValue, double endValue) {
        super();
        this.startValue = startValue;
        this.endValue = endValue;
    }

    /**
     * Instantiates a new interval annotation.
     *
     * @param stroke the stroke
     * @param paint the paint
     * @param startValue the start value
     * @param endValue the end value
     */
    protected IntervalAnnotation(Stroke stroke, Paint paint, double startValue, double endValue) {
        super(stroke, paint);
        this.startValue = startValue;
        this.endValue = endValue;
    }

    /**
     * Gets the start value.
     *
     * @return the start value
     */
    public double getStartValue() {
        return this.startValue;
    }

    /**
     * Sets the start value.
     *
     * @param value the new start value
     */
    public void setStartValue(double value) {
        this.startValue = value;
        fireAnnotationChanged();
    }

    /**
     * Gets the end value.
     *
     * @return the end value
     */
    public double getEndValue() {
        return this.endValue;
    }

    /**
     * Sets the end value.
     *
     * @param value the new end value
     */
    public void setEndValue(double value) {
        this.endValue = value;
        fireAnnotationChanged();
    }
}

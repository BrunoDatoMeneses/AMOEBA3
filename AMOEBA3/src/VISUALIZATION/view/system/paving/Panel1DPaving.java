package VISUALIZATION.view.system.paving;

import java.util.ArrayList;

import MAS.kernel.World;
import VISUALIZATION.view.animation.JJPanel;
import VISUALIZATION.view.system.ScheduledItem;
import MAS.agents.Percept;
import MAS.agents.context.Context;

// TODO: Auto-generated Javadoc
/**
 * The Class Panel1DPaving.
 */
public class Panel1DPaving extends JJPanel implements ScheduledItem{

	/** The variable. */
	private Percept variable;
	
	/** The world. */
	private World world;
	
	/** The referenced contexts. */
	private ArrayList<Context> referencedContexts = new ArrayList<Context>();
	
	/** The paving contexts. */
	private ArrayList<PavingContext> pavingContexts = new ArrayList<PavingContext>();
	
	/** The mono dimension line. */
	private MonoDimensionLine monoDimensionLine;
	
	/** The current value line. */
	private CurrentValueLine currentValueLine;
	
	/** The Constant heightPavingContext. */
	public static final int heightPavingContext = 20;

	/** The length. */
	private double length;
	
	/** The min. */
	private double min;
	
	/** The max. */
	private double max;
	
	/** The n context. */
	private int nContext = 0;
	
	/**
	 * Instantiates a new panel 1 D paving.
	 *
	 * @param variable the variable
	 * @param world the world
	 */
	public Panel1DPaving(Percept variable, World world) {
		this.world = world;
		this.variable = variable;
		
		drawPaving();

	}

	
	/**
	 * Draw paving.
	 */
	private void drawPaving() {
		
		
		ArrayList<Context> contexts = (ArrayList<Context>) world.getAllAgentInstanceOf(Context.class);
		double temp;
		
		System.out.println("Size : " + contexts.size());
		
		if (contexts.size() > 0) {
			
			/*Init min et max*/
			min = contexts.get(0).getRanges().get(variable).getStart();
			max = contexts.get(0).getRanges().get(variable).getEnd();
			for (Context context : contexts) {
				
				if (!context.isFirstTimePeriod())
				{
					temp = context.getRanges().get(variable).getStart();
					if (temp < min) {
						min = temp;
					}
					temp = context.getRanges().get(variable).getEnd();
					if (temp > max) {
						max = temp;
					}
				}
			}
		
			System.out.println(min + " " + max);
			if (monoDimensionLine == null) {
				monoDimensionLine = new MonoDimensionLine(this, 0, 0, min, max,1.0);
				this.add(monoDimensionLine);
			}
			else {
				monoDimensionLine.update(min,max);
			}

			/*Draw the context*/
			for (Context context : contexts) {
				if (!referencedContexts.contains(context)) {
					PavingContext pavingContext = new PavingContext(this, 0, 0, context, 1.0, this, nContext);  //TODO : no perf!
					this.add(pavingContext);
					this.referencedContexts.add(context);
					this.pavingContexts.add(pavingContext);
					nContext++;
				}
				//TODO remove context!
			}
			
			nContext = 0;
			ArrayList<PavingContext> toRemove = new ArrayList<PavingContext>();
			for (PavingContext pc : pavingContexts) {
				
				if(!contexts.contains(pc.getContext())) {
					this.remove(pc);
					this.referencedContexts.remove(pc.getContext());
					toRemove.add(pc);
				}
				else {
					pc.setIndex(nContext);
					nContext++;
				}
			}
			
			for (PavingContext pc : toRemove) {
				pavingContexts.remove(pc);
			}
			
			if (currentValueLine == null) {
				System.out.println("Create current value line");
				currentValueLine = new CurrentValueLine(this, 0, 0,this, 1.0, variable);
				this.add(currentValueLine);
			}

			
		}
		
		
		
	//	this.add(new MonoDimensionLine(this, 0, 0, 600));
	//	this.add(new PavingContext(this, 0, 0));
	}
	
	/* (non-Javadoc)
	 * @see view.system.ScheduledItem#update()
	 */
	@Override
	public void update() {

		drawPaving();
	}


	/**
	 * Gets the length.
	 *
	 * @return the length
	 */
	public double getLength() {
		return length;
	}


	/**
	 * Sets the length.
	 *
	 * @param length the new length
	 */
	public void setLength(double length) {
		this.length = length;
	}


	/**
	 * Gets the min.
	 *
	 * @return the min
	 */
	public double getMin() {
		return min;
	}


	/**
	 * Sets the min.
	 *
	 * @param min the new min
	 */
	public void setMin(double min) {
		this.min = min;
	}


	/**
	 * Gets the max.
	 *
	 * @return the max
	 */
	public double getMax() {
		return max;
	}


	/**
	 * Sets the max.
	 *
	 * @param max the new max
	 */
	public void setMax(double max) {
		this.max = max;
	}


	/**
	 * Gets the variable.
	 *
	 * @return the variable
	 */
	public Percept getVariable() {
		return variable;
	}


	/**
	 * Sets the variable.
	 *
	 * @param variable the new variable
	 */
	public void setVariable(Percept variable) {
		this.variable = variable;
	}


	/**
	 * Gets the n context.
	 *
	 * @return the n context
	 */
	public int getnContext() {
		return nContext;
	}


	/**
	 * Sets the n context.
	 *
	 * @param nContext the new n context
	 */
	public void setnContext(int nContext) {
		this.nContext = nContext;
	}
	
	

}

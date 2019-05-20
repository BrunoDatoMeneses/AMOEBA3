package agents;

import java.io.Serializable;

import fr.irit.smac.amak.Agent;
import gui.RenderStrategy;
import kernel.AMOEBA;
import kernel.World;

/**
 * The base class for all AMOEBA agents
 */
public abstract class AmoebaAgent extends Agent<AMOEBA, World> implements Serializable {
	private static final long serialVersionUID = 1L;
	// Attributes
	protected String name;
	private boolean dying;
	
	protected RenderStrategy renderStrategy;

	/**
	 * Instantiate a new agent attached to an amoeba
	 * @param the amoeba
	 */
	public AmoebaAgent(AMOEBA amas) {
		super(amas);
		this.dying = false;
	}

	@Override
	protected void onDecide() {
	}

	@Override
	protected void onRenderingInitialization() {
		if(renderStrategy != null)
			renderStrategy.initialize(this);
	}
	
	@Override
	public void onUpdateRender() {
		amas.getEnvironment().incrementNbActivatedAgent();
		if(renderStrategy != null) {
			if (amas.isRenderUpdate()) {
				renderStrategy.render(this);
			}
		}
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public void destroy() {
		dying = true;
		super.destroy();
		if(renderStrategy != null) {
			renderStrategy.delete(this);
		}
	}

	public String getName() {
		return name;
	}

	public boolean isDying() {
		return dying;
	}
	
	public void setRenderStrategy(RenderStrategy renderStrategy) {
		this.renderStrategy = renderStrategy;
	}
}

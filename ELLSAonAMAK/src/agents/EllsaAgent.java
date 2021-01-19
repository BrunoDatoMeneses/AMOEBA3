package agents;

import agents.percept.Percept;
import fr.irit.smac.amak.Agent;
import fr.irit.smac.amak.tools.Loggable;
import gui.RenderStrategy;
import kernel.ELLSA;
import kernel.World;
import utils.TRACE_LEVEL;

/**
 * The base class for all AMOEBA agents
 */
public abstract class EllsaAgent extends Agent<ELLSA, World> implements Loggable {
	// Attributes
	protected String name;
	private boolean dying;
	
	protected RenderStrategy renderStrategy;

	/**
	 * Instantiate a new agent attached to an amoeba
	 * @param the amoeba
	 */
	public EllsaAgent(ELLSA amas, Object... params) {
		super(amas, params);
		this.dying = false;
	}
	
	@Override
	protected void onReady() {
		super.onReady();
		getEnvironment().print(TRACE_LEVEL.DEBUG, "CYCLE "+getAmas().getCycle(), "Agent "+ toString() +" ready.");
		//logger().debug("CYCLE "+getAmas().getCycle(), "Agent %s ready.", toString());
	}

	@Override
	protected void onDecide() {
	}

	@Override
	protected void onRenderingInitialization() {
		if(renderStrategy != null) {
			renderStrategy.initialize(getAmas().getVUIMulti());
			
		}
	}
	
	@Override
	public void onUpdateRender() {
		amas.getEnvironment().incrementNbActivatedAgent();
		if(renderStrategy != null && !isDying()) {
			if (amas.isRenderUpdate()) {
				renderStrategy.render();
			}
		}
	}

	/**
	 * Set the name of the agent. Useful for visualization, and essential for {@link Percept}.
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public void destroy() {
		dying = true;
		if(renderStrategy != null) {
			renderStrategy.delete();
		}
		super.destroy();
		getEnvironment().print(TRACE_LEVEL.DEBUG, "CYCLE "+getAmas().getCycle(), "Agent "+ toString()  +" destroyed.");
		//logger().debug("CYCLE "+getAmas().getCycle(), "Agent %s destroyed.", toString());
	}

	/**
	 * Get the name of the agent. Useful for visualization, and essential for {@link Percept}.
	 * @param name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Tell if the agent is dying. A dying agent no longer perform any useful action, but is not yet removed from its system.
	 * @return
	 */
	public boolean isDying() {
		return dying;
	}
	
	/**
	 * Set the render strategy of an agent.<br/>
	 * {@link RenderStrategy#delete()} the old one, and {@link RenderStrategy#initialize()} the new one.
	 * @param renderStrategy
	 * @see RenderStrategy
	 */
	public void setRenderStrategy(RenderStrategy renderStrategy) {
		if(this.renderStrategy != null) this.renderStrategy.delete();
		this.renderStrategy = renderStrategy;
		if(this.renderStrategy != null) this.renderStrategy.initialize(getAmas().getVUIMulti());
	}
	
	/**
	 * Get the render strategy of an agent.
	 * @return
	 */
	public RenderStrategy getRenderStrategy() {
		return renderStrategy;
	}
}

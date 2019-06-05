package agents;

import fr.irit.smac.amak.Agent;
import fr.irit.smac.amak.tools.Loggable;
import gui.RenderStrategy;
import kernel.AMOEBA;
import kernel.World;

/**
 * The base class for all AMOEBA agents
 */
public abstract class AmoebaAgent extends Agent<AMOEBA, World> implements Loggable {
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
	protected void onReady() {
		// TODO Auto-generated method stub
		super.onReady();
		logger().debug("CYCLE "+getAmas().getCycle(), "Agent %s ready.", toString());
	}

	@Override
	protected void onDecide() {
	}

	@Override
	protected void onRenderingInitialization() {
		if(renderStrategy != null) {
			renderStrategy.initialize();
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
		logger().debug("CYCLE "+getAmas().getCycle(), "Agent %s destroyed.", toString());
	}

	public String getName() {
		return name;
	}

	public boolean isDying() {
		return dying;
	}
	
	public void setRenderStrategy(RenderStrategy renderStrategy) {
		if(this.renderStrategy != null) this.renderStrategy.delete();
		this.renderStrategy = renderStrategy;
		if(this.renderStrategy != null) this.renderStrategy.initialize();
	}
	
	public RenderStrategy getRenderStrategy() {
		return renderStrategy;
	}
}

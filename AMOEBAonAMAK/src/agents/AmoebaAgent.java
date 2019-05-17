package agents;

import java.io.Serializable;

import fr.irit.smac.amak.Agent;
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
	public void onUpdateRender() {
		super.onUpdateRender();
		amas.getEnvironment().incrementNbActivatedAgent();
		if (amas.isRenderUpdate()) {
			updateRender();
		}
	}

	/**
	 * Rendering that can be deactivated at runtime
	 */
	protected void updateRender() {
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public void destroy() {
		dying = true;
		super.destroy();
	}

	public String getName() {
		return name;
	}

	public boolean isDying() {
		return dying;
	}
}

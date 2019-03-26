package agents;

import java.io.Serializable;

import fr.irit.smac.amak.CommunicatingAgent;
import kernel.AMOEBA;
import kernel.World;

/**
 * The base class for all AMOEBA agents
 */
public abstract class AmoebaAgent extends CommunicatingAgent<AMOEBA, World> implements Serializable {
	private static final long serialVersionUID = 1L;
	// Attributes
	protected String name;
	private boolean dying;

	public AmoebaAgent(AMOEBA amas) {
		super(amas);
		this.dying = false;
	}

	@Override
	protected void onDecide() {
	}

	@Override
	protected void onUpdateRender() {
		super.onUpdateRender();
		if (amas.isRenderUpdate()) {
			updateRender();
		}
	}

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

package agents;

import fr.irit.smac.amak.CommunicatingAgent;
import kernel.AMOEBA;
import kernel.World;

/**
 * The base class for all AMOEBA agents
 *
 */
public abstract class AmoebaAgent extends CommunicatingAgent<AMOEBA, World> {

	protected String name;
	
	public AmoebaAgent(AMOEBA amas) {
		super(amas);
	}
	
	@Override
	protected void onDecide() {
	}
	
	@Override
		protected void onUpdateRender() {
			super.onUpdateRender();
			if(!amas.isNoRenderUpdate()) {
				updateRender();
			}
		}

	protected void updateRender() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
	

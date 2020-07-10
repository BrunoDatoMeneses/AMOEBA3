package agents;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import agents.context.Context;
import agents.percept.Percept;
import utils.TestSetup;

public class TestContext extends TestSetup {

	/**
	 * Check that the context projection has the same range as the context
	 */
	@Test
	public void testCoherenceProjection() {
		for(Context c : amoeba.getContexts()) {
			for(Percept p : amoeba.getPercepts()) {
				assertEquals(c.getRanges().get(p).getStart(), p.contextProjections.get(c).getStart());
				assertEquals(c.getRanges().get(p).getEnd(), p.contextProjections.get(c).getEnd());
			}
		}
	}
}

package kernel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import utils.RAND_REPEATABLE;
import utils.Round;

/**
 * Tests for {@link ELLSA#maximize(HashMap)}
 * @author daavve
 *
 */
public class TestMaximize {

	ELLSA ellsa;
	
	@BeforeEach
	public void setup() {
		/*Configuration.allowedSimultaneousAgentsExecution = 1;
		Configuration.commandLineMode = true;
		amoeba = new AMOEBA("tests/kernel/simple_with_context.xml", null);
		amoeba.saver = new SaveHelperDummy();*/
	}
	
	@Test
	public void testNoResult() {
		HashMap<String, Double> req = new HashMap<String, Double>();
		req.put("px0", -30.0);
		HashMap<String, Double> sol = ellsa.maximize(req);
		assertEquals(Double.NEGATIVE_INFINITY, sol.get("oracle"));
	}
	
	@Test
	public void testResult() {
		HashMap<String, Double> req = new HashMap<String, Double>();
		req.put("px0", 10.0);
		HashMap<String, Double> sol = ellsa.maximize(req);
		assertEquals(Round.round(2.429498684425507, 8), Round.round(sol.get("oracle"), 8));
	}
	
	@Test
	public void testCoherenceRandom() {
		// test coherence of result from request/maximize
		Random rand = RAND_REPEATABLE.getGeneratorWithoutSeed();
		for(int i = 0; i < 100; i++) {
			HashMap<String, Double> req = new HashMap<String, Double>();
			double px0 = rand.nextDouble();
			req.put("px0", px0);
			HashMap<String, Double> solMax = ellsa.maximize(req);
			req.put("px1", solMax.get("px1"));
			req.put("px2", solMax.get("px2"));
			req.put("oracle", 0.0);
			double res = ellsa.request(req);
			assertEquals(Round.round(solMax.get("oracle"), 8), Round.round(res, 8));			
		}
	}
	
}

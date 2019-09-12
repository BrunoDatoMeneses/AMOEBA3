package savesystem;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import experiments.F_XY_System;
import kernel.AMOEBA;
import kernel.StudiedSystem;
import utils.Round;
import utils.TestSetup;

/**
 * AMOEBA is a chaotic system, small change at start can lead to vastly different result.
 * Some of these test may fail for insignificant reason, in that case you can rerun the test, or ignore it.<br/>
 * A possible reason for test failure happen when multiple context are valid, but it's usually rare, 
 * and unlikely to happen on small test sample.
 * 
 * @author Hugo
 *
 */
public class TestBackupSystem extends TestSetup{

	@Test
	public void testSize() throws IOException {
		File tempFile = File.createTempFile("testSave", "xml");
		tempFile.deleteOnExit();

		amoeba.saver.save(tempFile);

		StudiedSystem studiedSystem = new F_XY_System(50.0);
		AMOEBA amoebaLoad = new AMOEBA(tempFile.getAbsolutePath(), studiedSystem);
		/*
		 * Improvement idea : defining equals on agent, and then test equality
		 */
		assertEquals(amoeba.getAgents().size(), amoebaLoad.getAgents().size());
	}

	@Test
	public void testRequestSameAmoeba() throws IOException {
		File tempFile = File.createTempFile("testSave", "xml");
		tempFile.deleteOnExit();
		Double[] requestOriginal = new Double[SIZE];
		Double[] requestLoaded = new Double[SIZE];

		amoeba.saver.save(tempFile);
		for (int i = 0; i < test.length; i++) {
			requestOriginal[i] = Round.round(amoeba.request(test[i]), ROUNDING_DECIMAL);
		}

		amoeba.saver.load(tempFile);
		for (int i = 0; i < test.length; i++) {
			requestLoaded[i] = Round.round(amoeba.request(test[i]), ROUNDING_DECIMAL);
		}

		assertArrayEquals(requestOriginal, requestLoaded);
	}

	@Test
	public void testRequestDiffAmoeba() throws IOException {
		File tempFile = File.createTempFile("testSave", "xml");
		tempFile.deleteOnExit();
		Double[] requestOriginal = new Double[SIZE];
		Double[] requestLoaded = new Double[SIZE];

		amoeba.saver.save(tempFile);
		for (int i = 0; i < test.length; i++) {
			requestOriginal[i] = Round.round(amoeba.request(test[i]), ROUNDING_DECIMAL);
		}

		StudiedSystem studiedSystem = new F_XY_System(50.0);
		AMOEBA amoebaLoad = new AMOEBA(tempFile.getAbsolutePath(), studiedSystem);
		amoebaLoad.saver.load(tempFile);
		for (int i = 0; i < test.length; i++) {
			requestLoaded[i] = Round.round(amoebaLoad.request(test[i]), ROUNDING_DECIMAL);
		}

		assertArrayEquals(requestOriginal, requestLoaded);
	}
	
	@Test
	public void testRequestDiffAmoebaNew() throws IOException {
		File tempFile = File.createTempFile("testSave", "xml");
		tempFile.deleteOnExit();
		Double[] requestLoaded1 = new Double[SIZE];
		Double[] requestLoaded2 = new Double[SIZE];

		amoeba.saver.save(tempFile);

		AMOEBA amoebaLoad1 = new AMOEBA(tempFile.getAbsolutePath(), null);
		amoebaLoad1.saver.load(tempFile);
		for (int i = 0; i < test.length; i++) {
			requestLoaded1[i] = Round.round(amoebaLoad1.request(test[i]), ROUNDING_DECIMAL);
		}

		AMOEBA amoebaLoad2 = new AMOEBA(tempFile.getAbsolutePath(), null);
		amoebaLoad2.saver.load(tempFile);
		for (int i = 0; i < test.length; i++) {
			requestLoaded2[i] = Round.round(amoebaLoad2.request(test[i]), ROUNDING_DECIMAL);
		}

		assertArrayEquals(requestLoaded1, requestLoaded2);
	}

}

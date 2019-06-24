package savesystem;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import experiments.F_XY_System;
import fr.irit.smac.amak.Configuration;
import kernel.AMOEBA;
import kernel.StudiedSystem;
import utils.DeleteDirectory;
import utils.Round;

public class TestBackupSystem {

	AMOEBA amoeba;
	static final int SIZE = 100;
	static final int ROUNDING_DECIMAL = 5;
	static HashMap<String, Double>[] train = new HashMap[SIZE];
	static HashMap<String, Double>[] test = new HashMap[SIZE];

	@BeforeAll
	public static void setupTrainTestValues() {
		StudiedSystem studiedSystem = new F_XY_System(50.0);
		for (int i = 0; i < train.length; i++) {
			studiedSystem.playOneStep();
			train[i] = studiedSystem.getOutput();
		}
		for (int i = 0; i < test.length; i++) {
			studiedSystem.playOneStep();
			test[i] = studiedSystem.getOutput();
		}
	}

	@BeforeEach
	public void setup() {
		Configuration.allowedSimultaneousAgentsExecution = 1;
		Configuration.commandLineMode = true;
		StudiedSystem studiedSystem = new F_XY_System(50.0);
		amoeba = new AMOEBA("resources/twoDimensionsLauncher.xml", studiedSystem);
		for (int i = 0; i < train.length; i++) {
			studiedSystem.playOneStep();
			amoeba.learn(train[i]);
		}
	}

	@Test
	public void testSaveLoadSize() throws IOException {
		File tempFile = File.createTempFile("testSave", "xml");
		tempFile.deleteOnExit();

		amoeba.saver.save(tempFile);

		StudiedSystem studiedSystem = new F_XY_System(50.0);
		AMOEBA amoebaLoad = new AMOEBA(tempFile.getAbsolutePath(), studiedSystem);
		/*
		 * Improvement idea : defining equals on agent, and then test equality
		 */
		assertEquals(amoeba.getAgents().size(), amoebaLoad.getAgents().size());
		
		try {
			DeleteDirectory.deleteDirectoryRecursion(amoeba.saver.dir);
			DeleteDirectory.deleteDirectoryRecursion(amoebaLoad.saver.dir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test @Disabled
	public void testSaveLoadRequestSameAmoeba() throws IOException {
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
		try {
			DeleteDirectory.deleteDirectoryRecursion(amoeba.saver.dir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test @Disabled
	public void testSaveLoadRequestDiffAmoeba() throws IOException {
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
		try {
			DeleteDirectory.deleteDirectoryRecursion(amoeba.saver.dir);
			DeleteDirectory.deleteDirectoryRecursion(amoebaLoad.saver.dir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test @Disabled
	public void testSaveLoadRequestDiffAmoebaNew() throws IOException {
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
		try {
			DeleteDirectory.deleteDirectoryRecursion(amoeba.saver.dir);
			DeleteDirectory.deleteDirectoryRecursion(amoebaLoad1.saver.dir);
			DeleteDirectory.deleteDirectoryRecursion(amoebaLoad2.saver.dir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

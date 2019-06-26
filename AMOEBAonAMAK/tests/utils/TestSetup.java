package utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import experiments.F_XY_System;
import fr.irit.smac.amak.Configuration;
import kernel.AMOEBA;
import kernel.SaveHelper;
import kernel.StudiedSystem;

public class TestSetup {
	protected AMOEBA amoeba;
	/**
	 * Testing sample : increasing this number lead to slower test and more chance of failure.
	 */
	public static final int SIZE = 50;
	public static final int ROUNDING_DECIMAL = 5;
	public static HashMap<String, Double>[] train = new HashMap[SIZE];
	public static HashMap<String, Double>[] test = new HashMap[SIZE];

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
		amoeba.saver.autoSave = false;
		for (int i = 0; i < train.length; i++) {
			studiedSystem.playOneStep();
			amoeba.learn(train[i]);
		}
	}
	
	@AfterAll
	public static void clean() {
		try {
			DeleteDirectory.deleteDirectoryRecursion(Paths.get(SaveHelper.savesRoot));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

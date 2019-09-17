package fr.irit.smac.amak.tests;

import fr.irit.smac.amak.ui.MainWindow;
import fr.irit.smac.amak.ui.VUI;

public class VisibleUI {
	public static void main(String[] args) {
		
		MainWindow mainWindow = new MainWindow();		
		
		mainWindow.setWindowTitle("VUI Example");
		mainWindow.setWindowIcon("file:Resources/ant.png");
		VUI.get(mainWindow).createAndAddRectangle(0, 0, 200, 150);
	}
}

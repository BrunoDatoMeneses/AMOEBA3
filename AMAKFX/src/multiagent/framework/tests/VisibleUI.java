package multiagent.framework.tests;

import multiagent.framework.ui.MainWindow;
import multiagent.framework.ui.VUI;

public class VisibleUI {
	public static void main(String[] args) {
		MainWindow.setWindowTitle("VUI Example");
		MainWindow.setWindowIcon("file:Resources/ant.png");
		VUI.get().createAndAddRectangle(0, 0, 200, 150);
	}
}

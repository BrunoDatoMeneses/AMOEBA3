package experiments;

import java.io.File;

import kernel.AMOEBA;
import kernel.StudiedSystem;
import kernel.World;

public class Main {

	public static void main(String[] args) {
		File file = new File("Ressources\\twoDimensionsLauncher.xml");
		System.out.println(file);
		World world = new World();
		StudiedSystem studiedSystem = new F_XY_System(50.0);

		AMOEBA amoeba = new AMOEBA(world, studiedSystem, file);
		amoeba.setDataForErrorMargin(1000, 5, 0.4, 0.1, 40, 80);
		amoeba.setDataForInexactMargin(500, 2.5, 0.2, 0.05, 40, 80);
	}
}

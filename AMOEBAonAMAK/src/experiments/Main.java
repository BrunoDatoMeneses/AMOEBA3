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
		
		new AMOEBA(world, studiedSystem, file);
	}
}
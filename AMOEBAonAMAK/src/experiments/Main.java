package experiments;

import java.io.File;

import kernel.AMOEBA;
import kernel.StudiedSystem;
import kernel.World;

public class Main {

	public static void main(String[] args) {
		File f = new File("D:\\hugor\\Documents\\AMOEBA3\\AMOEBAonAMAK\\Ressources\\twoDimensionsLauncher.xml");
		System.out.println(f);
		World w = new World();
		StudiedSystem s = new F_XY_System(50.0);
		AMOEBA a =new AMOEBA(w, s, f);
	}

}

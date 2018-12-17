package experiments.droneControl;


//Packages à importer afin d'utiliser l'objet File

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class FILE {
	
	PrintWriter pw;

	
	public FILE(String folder, String name){
		    try {
				pw = new PrintWriter("C:/Users/dato/Documents/THESE/XP/DroneControlUnity/"+ folder + "/" + name + ".csv");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  	
	}
	
	
	public void write(Double f1, Double f2){
          	pw.println(f1.toString() + ";" + f2.toString());
	}
	
	public void write(Double f1, Double f2, Double f3){
      	pw.println(f1.toString() + ";" + f2.toString() + ";" + f3.toString());
	}
	
	public void write(Double f1, Double f2, Double f3, Double f4){
      	pw.println(f1.toString() + ";" + f2.toString() + ";" + f3.toString() + ";" + f4.toString());
	}
	
	public void close(){
		pw.close();
	}
	
public static void main(String[] args) {
	
  FILE test = new FILE("19122017","test");

  test.write(0.0000d, 0.0045d);
  test.write(0.0000d, 0.0045d);

  
  
  test.close();
}
  
}

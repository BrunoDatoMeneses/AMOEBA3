package experiments;


//Packages à importer afin d'utiliser l'objet File

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class FILE {
	
	PrintWriter pw;

	
	public FILE(String folder, String name){
		    try {
				pw = new PrintWriter("C:/Users/dato/Documents/THESE/XP/"+ folder + "/" + name + ".csv");
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
	
	public void write(ArrayList<String> infos) {
		String message = "";
		for(String info : infos) {
			message += info + ";"  ;
		}
		pw.println(message);
	}
	
	public void write(ArrayList<String> context, ArrayList<String> infos) {
		String message = "";
		for(String ctxt : context) {
			message += ctxt + ";"  ;
		}
		for(String info : infos) {
			message += info + ";"  ;
		}
		pw.println(message);
	}
	
public static void main(String[] args) {
	
  FILE test = new FILE("Regression","test");

  test.write(0.0000d, 0.0045d);
  test.write(0.0000d, 0.0045d);

  test.write(new ArrayList<String>(Arrays.asList(""+185.0, ""+0.1111,""+0.5454)));
  test.write(new ArrayList<String>(Arrays.asList(""+185.0, ""+0.1111,""+0.5454,""+0.545)));
  
  test.close();
}
  
}

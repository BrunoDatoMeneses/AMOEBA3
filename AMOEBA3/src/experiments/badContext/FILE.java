package experiments.badContext;


//Packages à importer afin d'utiliser l'objet File

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class FILE {
	
	PrintWriter pw;

	
	public FILE(String path, String folder, String name){
		    try {
				pw = new PrintWriter(path + folder + "/" + name + ".csv");
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
	
	public void write(Double f1, Double f2, Double f3, Double f4, Double f5){
      	pw.println(f1.toString() + ";" + f2.toString() + ";" + f3.toString() + ";" + f4.toString() + ";" + f5.toString());
	}
	
	public void write(String f1, String f2, String f3, String f4, String f5){
      	pw.println(f1 + ";" + f2 + ";" + f3 + ";" + f4 + ";" + f5);
	}
	
	public void write(String f1, String f2, String f3, String f4){
      	pw.println(f1 + ";" + f2 + ";" + f3 + ";" + f4);
	}
	
	public void write(String f1, String f2, String f3){
      	pw.println(f1 + ";" + f2 + ";" + f3);
	}
	
	public void write(String f1, String f2){
      	pw.println(f1 + ";" + f2);
	}
	
	public void close(){
		pw.close();
	}
	
public static void main(String[] args) {
	
  FILE test = new FILE("","19122017","test");

  test.write(0.0000d, 0.0045d);
  test.write(0.0000d, 0.0045d);

  
  
  test.close();
}
  
}

package VISUALIZATION.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import MAS.agents.SystemAgent;
import MAS.agents.head.Head;
import MAS.kernel.World;
import VISUALIZATION.view.system.ScheduledItem;

// TODO: Auto-generated Javadoc
/**
 * The Class CsvFileWriter.
 */
public class CsvFileWriter implements ScheduledItem {
	
	/** The file date. */
	private static String fileDate = new SimpleDateFormat("yyMMdd-hhmmss").format(new Date());
	
	/** The output file. */
	private static String outputFile = System.getProperty("user.dir")+"/tmp/output"+fileDate+".csv";
	
	/** The world. */
	private World world;
	
	/** The percept name. */
	List<String> perceptName;
	
	/** The pw. */
	PrintWriter pw = null;
	
	/**
	 * Creates the csv file.
	 */
	private void CreateCsvFile() {
		
		StringBuilder builder = new StringBuilder();
		try {
			pw = new PrintWriter(new File(outputFile));	
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		}
		
		String ColumnNameList = "Tick,";
		for(int i=0; i<perceptName.size();i++) {
			ColumnNameList += perceptName.get(i);
			ColumnNameList += ",";
		}
		ColumnNameList+="Oracle";
		
		builder.append(ColumnNameList + "\n");
		pw.write(builder.toString());
		pw.close();
	}
	

	/**
	 * Write csv file.
	 *
	 * @param content the content
	 */
	private void writeCsvFile(List<String> content) {
		

		System.out.println("output");

		try {
			pw = new PrintWriter(new FileOutputStream(new File(outputFile), true));
			StringBuilder builder = new StringBuilder();
			for(int i=0; i<content.size();i++) {
				builder.append(content.get(i));
				if (i<content.size()-1) {
					builder.append(",");
				} else {
					builder.append("\n");
				}
				
			}
			pw.write(builder.toString());
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
	
	/**
	 * Sets the world.
	 *
	 * @param world the new world
	 */
	public void setWorld(World world) {
		this.world = world;
	}
	
	/**
	 * Inits the header CSV.
	 */
	// Initialize the header of csv file from the percept name
	public void initHeaderCSV() {
		List<String> perceptName = new ArrayList<>();
		for (int i = 0; i < world.getAllPercept().size(); i++) {
			perceptName.add(world.getAllPercept().get(i).getName());
		}
		this.perceptName = perceptName;
		CreateCsvFile();
	}	

	/* (non-Javadoc)
	 * @see view.system.ScheduledItem#update()
	 */
	@Override
	public void update() {
		// TODO Auto-generated method stub
		if (world.getAmoeba().getCSV()) {
			List<String> contentCSV = new ArrayList<>();
			contentCSV.add(0, Integer.toString(world.getScheduler().getTick()));
			
			for(int i=0; i< world.getAllPercept().size(); i++) {
				contentCSV.add(i+1, Double.toString(world.getAllPercept().get(i).getValue()));
			}
			for (String name : world.getAgents().keySet()) {
				SystemAgent a = world.getAgents().get(name);
				if (a instanceof Head) {
					Head n = (Head)a;
					contentCSV.add(world.getAllPercept().size()+1, Double.toString(n.getOracleValue()));
				}
			}
			writeCsvFile(contentCSV);
		}
	}
	
}

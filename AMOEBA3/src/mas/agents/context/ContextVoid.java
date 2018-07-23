package mas.agents.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import mas.agents.head.Head;
import mas.agents.percept.Percept;
import mas.kernel.World;
import mas.ncs.NCS;

public class ContextVoid implements Serializable{

	World world;
	ArrayList<Context> surroundingContexts = new ArrayList<Context>();
	HashMap<Percept,Double> position = new HashMap<Percept,Double>();
	HashMap<Percept,Double> width = new HashMap<Percept,Double>();
	HashMap<String,Double> positionByString = new HashMap<String,Double>();
	HashMap<String,Double> widthByString = new HashMap<String,Double>();
	String name;
	
	
	public ContextVoid(World world, Context context1, Context context2, HashMap<Percept,Double> position, HashMap<Percept,Double> width) {
		
		this.world = world;
		this.name = context1.getName() + context2.getName();
		this.surroundingContexts.add(context1);
		this.surroundingContexts.add(context2);
		this.position = position;
		this.width = width;
		
		for(Percept percept : position.keySet()) {
			positionByString.put(percept.getName(), position.get(percept));
			widthByString.put(percept.getName(), width.get(percept));
		}
		
	}
	
	
	public HashMap<Percept,Double> getPosition() {
		return position;
	}
	
	public double getPositionByString(Object perceptString) {
		return positionByString.get(perceptString);
	}
	
	public double getWidthByString(Object perceptString) {
		return widthByString.get(perceptString);
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		String s = "";
		
		s += "Name : " + name + "\n";
		s += "Surrounding Contexts : \n";
		for(Context ctxt : surroundingContexts) {
			s += ctxt.getName() + "\n";
		}
		s += "Position : \n";
		for(Percept percept : position.keySet()) {
			s += percept.getName() + " : " + position.get(percept) + "\n";
		}
		
		return s;
		
	}
	

	
	public void solveNCS_Void() {
		
		this.die();
	}
	
	
	
	private void die() {
		//context1.deleteOverlap(this);
		//context2.deleteOverlap(this);
		//world.getScheduler().removeContextOverlap(this);
	}
	
	public boolean voidComputedBy(Context context) {
		return surroundingContexts.contains(context);
	}
	
	public ArrayList<Context> getSurroundingContexts(){
		return surroundingContexts;
	}
	
	public void addSurroundingContext(Context context){
		surroundingContexts.add(context);
	}
	
	
}

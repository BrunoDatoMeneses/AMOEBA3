package experiments.Tests;

import java.io.Serializable;

import mas.agents.context.AbstractContext;
import mas.kernel.World;

public class Bidon extends AbstractContext implements Serializable{

	public Double start;
	private Double end;
	
	
	public Bidon(World world,Double start, Double end) {
		super(world);
		this.start = start;
		this.end = end;
	}
	
	public String toString() {
        return ""+this.start;
    }
	
	public Double getStart() {
		return start;
	}
}

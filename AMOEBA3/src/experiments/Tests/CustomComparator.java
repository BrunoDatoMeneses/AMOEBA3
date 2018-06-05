package experiments.Tests;

import java.util.Comparator;

public class CustomComparator implements Comparator<Bidon> {
	
	
	
	
    @Override
    public int compare(Bidon o1, Bidon o2) {
        return o1.getStart().compareTo(o2.getStart());
    }
}



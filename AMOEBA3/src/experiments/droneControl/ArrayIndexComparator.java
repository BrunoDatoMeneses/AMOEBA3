package experiments.droneControl;

import java.util.Arrays;
import java.util.Comparator;

public class ArrayIndexComparator implements Comparator<Integer>{

	private final Double[] array;

    public ArrayIndexComparator(Double[] array)
    {
        this.array = array;
    }

    public Integer[] createIndexArray()
    {
        Integer[] indexes = new Integer[array.length];
        for (int i = 0; i < array.length; i++)
        {
            indexes[i] = i; // Autoboxing
        }
        return indexes;
    }
	
	
	



	@Override
	public int compare(Integer arg0, Integer arg1) {
		// Autounbox from Integer to int to use as array indexes
        return array[arg0].compareTo(array[arg1]);
	}
	
	/*public static void main(String[] args) {
		String[] countries = { "France", "Spain", "England" };
		ArrayIndexComparator comparator = new ArrayIndexComparator(countries);
		Integer[] indexes = comparator.createIndexArray();
		Arrays.sort(indexes, comparator);
		Arrays.sort(countries);
		
		
		for(int i = 0; i<countries.length;i++){
			System.out.print(countries[i]+"\t");
		}
		System.out.print("\n");
		
		for(int i = 0; i<countries.length;i++){
			System.out.print(indexes[i]+"\t");
		}
		System.out.print("\n");
		
		
	}*/
}

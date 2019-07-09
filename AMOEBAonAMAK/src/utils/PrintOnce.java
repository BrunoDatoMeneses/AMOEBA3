package utils;

import java.util.HashSet;

public class PrintOnce {

	static private HashSet<String> done = new HashSet<String>();
	
	public static void print(String str) {
		if(!done.contains(str)) {
			System.err.println(str);
			done.add(str);
		}
	}
}

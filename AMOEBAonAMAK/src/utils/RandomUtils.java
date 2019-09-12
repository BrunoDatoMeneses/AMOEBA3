package utils;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {
	
	/**
	 * Pick N random element from the list. if n is bigger than the list, return the list.
	 * Use Durstenfeld's algorithm
	 * @param <E>
	 * @param list
	 * @param n
	 * @param r
	 * @return
	 */
	public static <E> List<E> pickNRandomElements(List<E> list, int n, Random r) {
	    int length = list.size();

	    if (length < n) return list;

	    //We don't need to shuffle the whole list
	    for (int i = length - 1; i >= length - n; --i)
	    {
	        Collections.swap(list, i , r.nextInt(i + 1));
	    }
	    return list.subList(length - n, length);
	}

	/**
	 * Pick N random element from the list. if n is bigger than the list, return the list.
	 * Use Durstenfeld's algorithm
	 * @param <E>
	 * @param list
	 * @param n
	 * @return
	 */
	public static <E> List<E> pickNRandomElements(List<E> list, int n) {
	    return pickNRandomElements(list, n, ThreadLocalRandom.current());
	}
	
	/**
	 * Generate a pseudorandom double values, conforming to the given origin (inclusive) and bound(exclusive).
	 * @param rand
	 * @param origin the origin (inclusive) of the random value
	 * @param bound the bound (exclusive) of the random value
	 * @return
	 */
	public static double nextDouble(Random rand, double origin, double bound) {
		   double r = rand.nextDouble();
		   r = r * (bound - origin) + origin;
		   if (r >= bound) // correct for rounding
		     r = Math.nextDown(bound);
		   return r;
	}
}

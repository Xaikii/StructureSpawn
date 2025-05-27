package net.perpetualeve.structurespawn;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

/**
 * @author: raws {@link} https://gist.github.com/raws/1667807
 */
public class WeightedRandom<E> {

	private NavigableMap<Double, E> map = new TreeMap<Double, E>();
	private Random random;
	private double total = 0;

	public WeightedRandom() {
		this(new Random());
	}

	public WeightedRandom(Random random) {
		this.random = random;
	}

	public void add(double weight, E object) {
		if (weight <= 0)
			return;
		total += weight;
		map.put(total, object);
	}

	public E next() {
		if (map.size() == 1)
			return map.firstEntry().getValue();
		double value = random.nextDouble(total);
		return map.floorEntry(value).getValue();
	}

}
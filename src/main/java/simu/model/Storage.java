package simu.model;

import java.util.LinkedList;

public class Storage {
	private final double capacity;
	private final LinkedList<Customer> items = new LinkedList<>();
	private double level = 0;
	private double maxLevel = 0;
	private double levelTimeIntegral = 0;
	private double lastChange = 0;

	public Storage(double capacity) { this.capacity = capacity; }

	public boolean hasSpaceFor(Customer c) { return level + c.getQuantity() <= capacity; }

	public void put(Customer c, double now) {
		accumulate(now);
		items.add(c);
		level += c.getQuantity();
		maxLevel = Math.max(maxLevel, level);
	}

	public Customer take(double now) {
		accumulate(now);
		Customer c = items.poll();
		if (c != null) level -= c.getQuantity();
		return c;
	}

	public boolean isEmpty() { return items.isEmpty(); }
	public double getLevel() { return level; }
	public double getCapacity() { return capacity; }
	public double getMaxLevel() { return maxLevel; }
	public int getBatchCount() { return items.size(); }

	private void accumulate(double now) {
		levelTimeIntegral += level * (now - lastChange);
		lastChange = now;
	}

	public double getMeanLevel(double now) {
		accumulate(now);
		return now == 0 ? 0 : levelTimeIntegral / now;
	}
}

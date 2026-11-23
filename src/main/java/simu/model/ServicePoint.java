package simu.model;

import eduni.distributions.ContinuousGenerator;
import simu.framework.*;

import java.util.*;


public class ServicePoint {
	private static class Slot {
		final Customer customer; final double start, end;
		Slot(Customer c, double s, double e) { customer = c; start = s; end = e; }
	}

	private final String name;
	private final int servers;
	private final ContinuousGenerator generator;
	private final EventList eventList;
	private final EventType eventTypeScheduled;

	private final LinkedList<Customer> queue = new LinkedList<>();
	private final LinkedList<Double> enqueueTimes = new LinkedList<>();
	private final PriorityQueue<Slot> inService = new PriorityQueue<>(Comparator.comparingDouble(s -> s.end));
	private final LinkedList<Slot> blocked = new LinkedList<>();

	private int started = 0;
	private int served = 0;
	private double servedQuantity = 0;
	private double busyTime = 0;
	private double totalWait = 0;
	private int maxQueue = 0;
	private double queueIntegral = 0, lastQueueChange = 0;

	public ServicePoint(String name, int servers, ContinuousGenerator generator, EventList eventList, EventType type) {
		this.name = name; this.servers = servers; this.generator = generator;
		this.eventList = eventList; this.eventTypeScheduled = type;
	}

	private void updateQueueIntegral(double now) {
		queueIntegral += queue.size() * (now - lastQueueChange);
		lastQueueChange = now;
	}

	public void addQueue(Customer c) {
		double now = Clock.getInstance().getClock();
		updateQueueIntegral(now);
		queue.add(c);
		enqueueTimes.add(now);
		maxQueue = Math.max(maxQueue, queue.size());
	}

	public Customer completeService() {
		Slot s = inService.poll();
		busyTime += s.end - s.start;
		served++;
		servedQuantity += s.customer.getQuantity();
		return s.customer;
	}

	public void block(Customer c) { blocked.add(new Slot(c, 0, 0)); }
	public boolean hasBlocked() { return !blocked.isEmpty(); }
	public Customer peekBlocked() { return blocked.peek().customer; }
	public Customer releaseBlocked() { return blocked.poll().customer; }

	public boolean canBegin() { return !queue.isEmpty() && inService.size() + blocked.size() < servers; }

	public void beginService() {
		double now = Clock.getInstance().getClock();
		updateQueueIntegral(now);
		Customer c = queue.poll();
		totalWait += now - enqueueTimes.poll();
		started++;
		double t = Math.max(0.1, generator.sample());
		inService.add(new Slot(c, now, now + t));
		Trace.out(Trace.Level.INFO, "[" + name + "] start batch #" + c.getId() + ", service time " + String.format("%.2f", t));
		eventList.add(new Event(eventTypeScheduled, now + t));
	}

	/** Call when the simulation ends so unfinished work counts toward utilisation. */
	public void finalizeStats(double now) {
		for (Slot s : inService) busyTime += Math.min(now, s.end) - s.start;
		updateQueueIntegral(now);
	}

	public String getName() { return name; }
	public int getServers() { return servers; }
	public int getServed() { return served; }
	public double getServedQuantity() { return servedQuantity; }
	public int getQueueLength() { return queue.size(); }
	public int getBusyServers() { return inService.size() + blocked.size(); }
	public int getMaxQueue() { return maxQueue; }
	public double getMeanWait() { return started == 0 ? 0 : totalWait / started; }
	public double getUtilization(double total) { return total == 0 ? 0 : busyTime / (servers * total); }
	public double getMeanQueueLength(double total) { return total == 0 ? 0 : queueIntegral / total; }
	public int getRemaining() { return queue.size() + inService.size() + blocked.size(); }
}

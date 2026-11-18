package simu.model;

import eduni.distributions.ContinuousGenerator;
import java.util.LinkedList;
import simu.framework.*;

public class ServicePoint {

    private final LinkedList<Customer> queue = new LinkedList<>();
    private final ContinuousGenerator generator;
    private final EventList eventList;
    private final EventType eventTypeScheduled;
    private final String name;
    private boolean reserved = false;

    private int served = 0;
    private double busyTime = 0;
    private double totalWaitTime = 0;
    private int maxQueue = 0;
    private double serviceStartedAt;
    private final LinkedList<Double> enqueueTimes = new LinkedList<>();

    public ServicePoint(String name, ContinuousGenerator generator, EventList eventList, EventType type) {
        this.name = name;
        this.eventList = eventList;
        this.generator = generator;
        this.eventTypeScheduled = type;
    }

    public void addQueue(Customer a) {
        queue.add(a);
        enqueueTimes.add(Clock.getInstance().getClock());
        maxQueue = Math.max(maxQueue, queue.size());
    }

    public Customer removeQueue() {
        reserved = false;
        served++;
        busyTime += Clock.getInstance().getClock() - serviceStartedAt;
        enqueueTimes.poll();
        return queue.poll();
    }

    public void beginService() {
        Customer c = queue.peek();
        double now = Clock.getInstance().getClock();
        totalWaitTime += now - enqueueTimes.peek();
        Trace.out(Trace.Level.INFO, "[" + name + "] starting service for customer #" + c.getId());

        reserved = true;
        serviceStartedAt = now;
        double serviceTime = Math.max(0.1, generator.sample());
        eventList.add(new Event(eventTypeScheduled, now + serviceTime));
    }

    public boolean isReserved() {
        return reserved;
    }

    public boolean isOnQueue() {
        return !queue.isEmpty();
    }

    public String getName() {
        return name;
    }

    public int getServed() {
        return served;
    }

    public double getMeanWaitTime() {
        return served == 0 ? 0 : totalWaitTime / served;
    }

    public double getUtilization(double totalTime) {
        return totalTime == 0 ? 0 : busyTime / totalTime;
    }

    public int getMaxQueue() {
        return maxQueue;
    }
}

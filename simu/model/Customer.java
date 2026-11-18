package simu.model;

import simu.framework.*;

public class Customer {

    private double arrivalTime;
    private double removalTime;
    private final int id;
    private boolean needsServiceA;
    private static int i = 1;
    private static double sum = 0;
    private static int completed = 0;

    public Customer() {
        id = i++;
        arrivalTime = Clock.getInstance().getClock();
        Trace.out(Trace.Level.INFO, "New customer #" + id + " arrived at " + arrivalTime);
    }

    public double getRemovalTime() {
        return removalTime;
    }

    public void setRemovalTime(double removalTime) {
        this.removalTime = removalTime;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getId() {
        return id;
    }

    public boolean needsServiceA() {
        return needsServiceA;
    }

    public void setNeedsServiceA(boolean a) {
        this.needsServiceA = a;
    }

    public static int getCompleted() {
        return completed;
    }

    public static double getMeanTimeInSystem() {
        return completed == 0 ? 0 : sum / completed;
    }

    public void reportResults() {
        double stayed = removalTime - arrivalTime;
        Trace.out(Trace.Level.INFO, "\nCustomer " + id + " ready!");
        Trace.out(Trace.Level.INFO, "Customer " + id + " arrived: " + arrivalTime);
        Trace.out(Trace.Level.INFO, "Customer " + id + " removed: " + removalTime);
        Trace.out(Trace.Level.INFO, "Customer " + id + " route: check-in -> service "
                + (needsServiceA ? "A" : "B") + " -> checkout");
        Trace.out(Trace.Level.INFO, "Customer " + id + " stayed: " + stayed);

        sum += stayed;
        completed++;
        System.out.println("Current mean time in system: " + getMeanTimeInSystem());
    }
}

package simu.model;

import simu.framework.Clock;
import simu.framework.Trace;

public class Customer {

    private final int id;
    private final WasteType type;
    private final double quantity;
    private final double arrivalTime;
    private double removalTime;
    private boolean rejected;

    public Customer(int id, WasteType type, double quantity) {
        this.id = id;
        this.type = type;
        this.quantity = quantity;
        this.arrivalTime = Clock.getInstance().getClock();
        Trace.out(Trace.Level.INFO, String.format("New batch #%d (%s, %.0f kg) arrived at %.2f",
                id, type.getLabel(), quantity, arrivalTime));
    }

    public int getId() {
        return id;
    }

    public WasteType getType() {
        return type;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public double getRemovalTime() {
        return removalTime;
    }

    public void setRemovalTime(double t) {
        removalTime = t;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(boolean r) {
        rejected = r;
    }

    public double getTimeInSystem() {
        return removalTime - arrivalTime;
    }

    public void reportResults() {
        Trace.out(Trace.Level.INFO, String.format("Batch #%d (%s) left at %.2f, time in system %.2f%s",
                id, type.getLabel(), removalTime, getTimeInSystem(), rejected ? " [rejected]" : ""));
    }
}

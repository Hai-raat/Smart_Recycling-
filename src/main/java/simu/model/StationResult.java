package simu.model;

public class StationResult {

    public final String name;
    public final int servers, served, maxQueue, remaining;
    public final double utilization, meanWait, meanQueueLength;

    public StationResult(ServicePoint p, double endTime) {
        name = p.getName();
        servers = p.getServers();
        served = p.getServed();
        maxQueue = p.getMaxQueue();
        remaining = p.getRemaining();
        utilization = p.getUtilization(endTime);
        meanWait = p.getMeanWait();
        meanQueueLength = p.getMeanQueueLength(endTime);
    }
}

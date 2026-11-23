package simu.model;

import java.util.ArrayList;
import java.util.List;

public class SimulationResults {

    public double endTime;
    public int batchesReceived, batchesDispatched, batchesRejected, batchesRemaining;
    public double receivedKg, processedKg, dispatchedKg, rejectedKg, remainingKg;
    public double meanTimeInSystem;      // finished batches (dispatched or rejected)
    public double throughputKgPerMin;    // dispatched kg per simulated minute
    public double recoveryRate;          // dispatched / received
    public double storageLevel, storageCapacity, storageMaxLevel, storageMeanLevel;
    public final List<StationResult> stations = new ArrayList<>();

    public String toText() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Simulation ended at %.1f min%n", endTime));
        sb.append(String.format("Batches received %d | dispatched %d | rejected %d | still in facility %d%n",
                batchesReceived, batchesDispatched, batchesRejected, batchesRemaining));
        sb.append(String.format("Waste received %.0f kg | processed %.0f kg | dispatched %.0f kg | rejected %.0f kg | remaining %.0f kg%n",
                receivedKg, processedKg, dispatchedKg, rejectedKg, remainingKg));
        sb.append(String.format("Mean time in system %.2f min | throughput %.2f kg/min | recovery rate %.1f %%%n",
                meanTimeInSystem, throughputKgPerMin, recoveryRate * 100));
        sb.append(String.format("Storage: now %.0f / %.0f kg (max %.0f, mean %.0f)%n%n",
                storageLevel, storageCapacity, storageMaxLevel, storageMeanLevel));
        sb.append(String.format("%-10s %3s %7s %6s %9s %8s %8s %6s%n",
                "Station", "n", "served", "util", "meanWait", "meanQ", "maxQ", "left"));
        for (StationResult s : stations) {
            sb.append(String.format("%-10s %3d %7d %5.0f%% %9.2f %8.2f %8d %6d%n",
                    s.name, s.servers, s.served, s.utilization * 100, s.meanWait, s.meanQueueLength, s.maxQueue, s.remaining));
        }
        return sb.toString();
    }
}

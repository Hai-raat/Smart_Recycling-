package simu.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class ResultsCsv {
	public static void write(Path file, SimulationConfig c, SimulationResults r) throws IOException {
		try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8))) {
			w.println("section;key;value");
			w.println(row("config", "duration", c.duration));
			w.println(row("config", "meanArrivalInterval", c.meanArrivalInterval));
			w.println(row("config", "seed", c.seed));
			w.println(row("config", "storageCapacity", c.storageCapacity));
			w.println(row("config", "minQuantity", c.minQuantity));
			w.println(row("config", "maxQuantity", c.maxQuantity));
			for (int i = 0; i < c.proportions.length; i++)
				w.println(row("config", "share_" + WasteType.values()[i].getLabel(), c.proportions[i]));
			for (int i = 0; i < c.servers.length; i++) {
				String n = SimulationConfig.STATION_NAMES[i];
				w.println(row("config", n + "_servers", c.servers[i]));
				w.println(row("config", n + "_meanTime", c.serviceMean[i]));
				w.println(row("config", n + "_stdTime", c.serviceStd[i]));
			}
			w.println(row("result", "batchesReceived", r.batchesReceived));
			w.println(row("result", "batchesDispatched", r.batchesDispatched));
			w.println(row("result", "batchesRejected", r.batchesRejected));
			w.println(row("result", "receivedKg", r.receivedKg));
			w.println(row("result", "processedKg", r.processedKg));
			w.println(row("result", "dispatchedKg", r.dispatchedKg));
			w.println(row("result", "rejectedKg", r.rejectedKg));
			w.println(row("result", "remainingKg", r.remainingKg));
			w.println(row("result", "meanTimeInSystem", r.meanTimeInSystem));
			w.println(row("result", "throughputKgPerMin", r.throughputKgPerMin));
			w.println(row("result", "recoveryRate", r.recoveryRate));
			w.println(row("result", "storageMeanLevel", r.storageMeanLevel));
			w.println(row("result", "storageMaxLevel", r.storageMaxLevel));
			for (StationResult s : r.stations) {
				w.println(row("station", s.name + "_served", s.served));
				w.println(row("station", s.name + "_utilization", s.utilization));
				w.println(row("station", s.name + "_meanWait", s.meanWait));
				w.println(row("station", s.name + "_meanQueue", s.meanQueueLength));
				w.println(row("station", s.name + "_maxQueue", s.maxQueue));
			}
		}
	}

	private static String row(String section, String key, double v) {
		return section + ";" + key + ";" + String.format(Locale.US, "%.4f", v);
	}
	private static String row(String section, String key, long v) { return section + ";" + key + ";" + v; }
}

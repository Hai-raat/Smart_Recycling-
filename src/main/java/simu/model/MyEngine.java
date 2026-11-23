package simu.model;

import eduni.distributions.*;
import simu.framework.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;


public class MyEngine extends Engine {
	private static final EventType[] DEP = {
		EventType.DEP_RECEPTION, EventType.DEP_WEIGHING, EventType.DEP_SORTING,
		EventType.DEP_PLASTIC, EventType.DEP_PAPER, EventType.DEP_GLASS, EventType.DEP_METAL,
		EventType.DEP_REJECT, EventType.DEP_DISPATCH
	};

	private final SimulationConfig config;
	private final ArrivalProcess arrivalProcess;
	private final ServicePoint[] servicePoints = new ServicePoint[DEP.length];
	private final Map<EventType, ServicePoint> byEvent = new EnumMap<>(EventType.class);
	private final ServicePoint reception, weighing, sorting, reject, dispatch;
	private final ServicePoint[] processing = new ServicePoint[4];   // plastic, paper, glass, metal
	private final Storage storage;
	private final Random typeRandom, quantityRandom;

	private int nextId = 1, received = 0, dispatchedCount = 0, rejectedCount = 0;
	private double receivedKg, processedKg, dispatchedKg, rejectedKg, timeInSystemSum;
	private SimulationResults results;

	public MyEngine(SimulationConfig config) {
		this.config = config;
		config.validate();
		Clock.getInstance().setClock(0);

		Random seeder = config.seed == 0 ? new Random() : new Random(config.seed);
		for (int i = 0; i < servicePoints.length; i++) {
			double std = config.serviceStd[i];
			servicePoints[i] = new ServicePoint(SimulationConfig.STATION_NAMES[i], config.servers[i],
					new Normal(config.serviceMean[i], std * std, seed(seeder)), eventList, DEP[i]);
			byEvent.put(DEP[i], servicePoints[i]);
		}
		reception = servicePoints[0]; weighing = servicePoints[1]; sorting = servicePoints[2];
		for (int i = 0; i < 4; i++) processing[i] = servicePoints[3 + i];
		reject = servicePoints[7]; dispatch = servicePoints[8];

		storage = new Storage(config.storageCapacity);
		typeRandom = new Random(seed(seeder));
		quantityRandom = new Random(seed(seeder));
		arrivalProcess = new ArrivalProcess(new Negexp(config.meanArrivalInterval, seed(seeder)), eventList, EventType.ARRIVAL);
		setSimulationTime(config.duration);
	}

	private static long seed(Random r) { return Integer.toUnsignedLong(r.nextInt()); }

	@Override
	protected void initialize() { arrivalProcess.generateNextEvent(); }

	private WasteType drawType() {
		double sum = 0;
		for (double p : config.proportions) sum += p;
		double x = typeRandom.nextDouble() * sum;
		WasteType[] types = WasteType.values();
		for (int i = 0; i < types.length; i++) {
			x -= config.proportions[i];
			if (x < 0) return types[i];
		}
		return types[types.length - 1];
	}

	@Override
	protected void runEvent(Event t) {
		EventType type = (EventType) t.getType();
		double now = Clock.getInstance().getClock();
		if (type == EventType.ARRIVAL) {
			double q = config.minQuantity + quantityRandom.nextDouble() * (config.maxQuantity - config.minQuantity);
			Customer c = new Customer(nextId++, drawType(), q);
			received++;
			receivedKg += q;
			reception.addQueue(c);
			arrivalProcess.generateNextEvent();
			return;
		}
		ServicePoint sp = byEvent.get(type);
		Customer c = sp.completeService();
		switch (type) {
		case DEP_RECEPTION: weighing.addQueue(c); break;
		case DEP_WEIGHING:  sorting.addQueue(c); break;
		case DEP_SORTING:
			if (c.getType() == WasteType.CONTAMINATED) reject.addQueue(c);
			else processing[c.getType().ordinal()].addQueue(c);
			break;
		case DEP_PLASTIC: case DEP_PAPER: case DEP_GLASS: case DEP_METAL:
			processedKg += c.getQuantity();
			if (storage.hasSpaceFor(c)) storage.put(c, now);
			else sp.block(c);
			break;
		case DEP_REJECT:
			c.setRejected(true);
			rejectedCount++; rejectedKg += c.getQuantity();
			finish(c, now);
			break;
		case DEP_DISPATCH:
			dispatchedCount++; dispatchedKg += c.getQuantity();
			finish(c, now);
			break;
		default: break;
		}
	}

	private void finish(Customer c, double now) {
		c.setRemovalTime(now);
		timeInSystemSum += c.getTimeInSystem();
		c.reportResults();
	}

	@Override
	protected void tryCEvents() {
		double now = Clock.getInstance().getClock();
		boolean progress = true;
		while (progress) {
			progress = false;
			for (ServicePoint p : processing) {
				while (p.hasBlocked() && storage.hasSpaceFor(p.peekBlocked())) {
					storage.put(p.releaseBlocked(), now);
					progress = true;
				}
			}
			while (!storage.isEmpty() && dispatch.getBusyServers() + dispatch.getQueueLength() < dispatch.getServers()) {
				dispatch.addQueue(storage.take(now));
				progress = true;
			}
			for (ServicePoint p : servicePoints) {
				while (p.canBegin()) { p.beginService(); progress = true; }
			}
		}
	}

	@Override
	protected void results() {
		double end = Clock.getInstance().getClock();
		for (ServicePoint p : servicePoints) p.finalizeStats(end);

		SimulationResults r = new SimulationResults();
		r.endTime = end;
		r.batchesReceived = received;
		r.batchesDispatched = dispatchedCount;
		r.batchesRejected = rejectedCount;
		r.batchesRemaining = received - dispatchedCount - rejectedCount;
		r.receivedKg = receivedKg;
		r.processedKg = processedKg;
		r.dispatchedKg = dispatchedKg;
		r.rejectedKg = rejectedKg;
		r.remainingKg = receivedKg - dispatchedKg - rejectedKg;
		int finished = dispatchedCount + rejectedCount;
		r.meanTimeInSystem = finished == 0 ? 0 : timeInSystemSum / finished;
		r.throughputKgPerMin = end == 0 ? 0 : dispatchedKg / end;
		r.recoveryRate = receivedKg == 0 ? 0 : dispatchedKg / receivedKg;
		r.storageLevel = storage.getLevel();
		r.storageCapacity = storage.getCapacity();
		r.storageMaxLevel = storage.getMaxLevel();
		r.storageMeanLevel = storage.getMeanLevel(end);
		for (ServicePoint p : servicePoints) r.stations.add(new StationResult(p, end));
		this.results = r;
	}

	public SimulationResults getResults() { return results; }
}

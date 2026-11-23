package simu.model;

import org.junit.jupiter.api.Test;
import simu.framework.Trace;

import static org.junit.jupiter.api.Assertions.*;

class MyEngineTest {
	private SimulationResults run(SimulationConfig c) {
		Trace.setTraceLevel(Trace.Level.ERR);
		MyEngine e = new MyEngine(c);
		e.run();
		return e.getResults();
	}

	@Test
	void massBalanceHolds() {
		SimulationConfig c = new SimulationConfig();
		c.seed = 42;
		SimulationResults r = run(c);
		assertTrue(r.batchesReceived > 0);
		assertEquals(r.receivedKg, r.dispatchedKg + r.rejectedKg + r.remainingKg, 1e-6);
		assertEquals(r.batchesReceived, r.batchesDispatched + r.batchesRejected + r.batchesRemaining);
	}

	@Test
	void sameSeedGivesSameResult() {
		SimulationConfig a = new SimulationConfig(); a.seed = 7;
		SimulationConfig b = new SimulationConfig(); b.seed = 7;
		assertEquals(run(a).receivedKg, run(b).receivedKg, 1e-9);
	}

	@Test
	void utilizationWithinBounds() {
		SimulationConfig c = new SimulationConfig(); c.seed = 3;
		for (StationResult s : run(c).stations) {
			assertTrue(s.utilization >= 0 && s.utilization <= 1.0001, s.name);
		}
	}

	@Test
	void storageNeverExceedsCapacity() {
		SimulationConfig c = new SimulationConfig(); c.seed = 5;
		c.storageCapacity = 600; c.meanArrivalInterval = 2;
		SimulationResults r = run(c);
		assertTrue(r.storageMaxLevel <= 600 + 1e-9);
	}

	@Test
	void invalidConfigIsRejected() {
		SimulationConfig c = new SimulationConfig();
		c.servers[2] = 0;
		assertThrows(IllegalArgumentException.class, c::validate);
	}
}

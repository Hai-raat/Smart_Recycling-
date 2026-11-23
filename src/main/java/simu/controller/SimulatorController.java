package simu.controller;

import simu.framework.Trace;
import simu.model.MyEngine;
import simu.model.ResultsCsv;
import simu.model.SimulationConfig;
import simu.model.SimulationResults;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Controller of the MVC structure: the link between the view and the simulation model.
 *
 *   View --(SimulationConfig)--> Controller --> MyEngine        (top -> bottom)
 *   MyEngine --(SimulationResults)--> Controller --> View       (bottom -> top)
 */
public class SimulatorController {
	private final ISimulatorUI ui;
	private SimulationConfig lastConfig;
	private SimulationResults lastResults;
	private volatile boolean running = false;

	public SimulatorController(ISimulatorUI ui) {
		this.ui = ui;
		Trace.setTraceLevel(Trace.Level.WAR);   // keep console quiet in the GUI version
	}

	/** Default parameters from the model, so the view can populate its fields. */
	public SimulationConfig getDefaultConfig() { return new SimulationConfig(); }

	/** Called by the view when the user presses Start. Runs the simulation in a background thread. */
	public void startSimulation(SimulationConfig config) {
		if (running) return;
		try {
			config.validate();
		} catch (IllegalArgumentException e) {
			ui.showError(e.getMessage());
			return;
		}
		running = true;
		ui.setRunning(true);
		Thread worker = new Thread(() -> {
			try {
				MyEngine engine = new MyEngine(config);
				engine.run();
				lastConfig = config;
				lastResults = engine.getResults();
				ui.showResults(lastResults);
			} catch (RuntimeException e) {
				ui.showError("Simulation failed: " + e.getMessage());
			} finally {
				running = false;
				ui.setRunning(false);
			}
		}, "simulation");
		worker.setDaemon(true);
		worker.start();
	}

	public boolean hasResults() { return lastResults != null; }

	/** Save configuration and results of the latest run to a CSV file. */
	public void saveResults(Path file) {
		if (lastResults == null) { ui.showError("Run a simulation first."); return; }
		try {
			ResultsCsv.write(file, lastConfig, lastResults);
		} catch (IOException e) {
			ui.showError("Could not save file: " + e.getMessage());
		}
	}
}

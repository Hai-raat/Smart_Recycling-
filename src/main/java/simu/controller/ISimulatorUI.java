package simu.controller;

import simu.model.SimulationConfig;
import simu.model.SimulationResults;

public interface ISimulatorUI {
	void showConfig(SimulationConfig config);
	void showResults(SimulationResults results);
	void showError(String message);

	void setRunning(boolean running);
}

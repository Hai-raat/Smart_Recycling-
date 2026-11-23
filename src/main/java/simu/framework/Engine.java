package simu.framework;


public abstract class Engine {
	private double simulationTime = 0;
	private Clock clock;
	protected EventList eventList;


	public Engine() {
		clock = Clock.getInstance();	// to improve the speed of the simulation
		eventList = new EventList();
	}

	public void setSimulationTime(double time) {	// define how long we will run the simulation
		simulationTime = time;
	}

	public void run(){
		initialize();

		while (simulate()) {
			Trace.out(Trace.Level.INFO, "\nA-phase: time is " + currentTime());
			clock.setClock(currentTime());
			
			Trace.out(Trace.Level.INFO, "\nB-phase:" );
			runBEvents();
			
			Trace.out(Trace.Level.INFO, "\nC-phase:" );
			tryCEvents();

		}

		results();
	}

	private void runBEvents() {
		while (eventList.getNextEventTime() == clock.getClock()){
			runEvent(eventList.remove());
		}
	}


	private double currentTime(){
		return eventList.getNextEventTime();
	}

	private boolean simulate(){
		return clock.getClock() < simulationTime;
	}

	protected abstract void runEvent(Event t);

	protected abstract void tryCEvents();

	protected abstract void initialize();

	protected abstract void results();
}
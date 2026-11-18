package simu.model;

import eduni.distributions.Negexp;
import eduni.distributions.Normal;
import eduni.distributions.Uniform;
import java.util.Random;
import simu.framework.*;

public class MyEngine extends Engine {

	private static final double P_A = 0.6;

	private final ArrivalProcess arrivalProcess;
	private final ServicePoint checkIn, serviceA, serviceB, checkout;
	private final ServicePoint[] servicePoints;
	private final Random routing = new Random();

	public MyEngine() {
		Random r = new Random();
		checkIn = new ServicePoint("Check-in", new Normal(4, 1, seed(r)), eventList, EventType.DEP_CHECKIN);
		serviceA = new ServicePoint("Service A", new Normal(10, 3, seed(r)), eventList, EventType.DEP_A);
		serviceB = new ServicePoint("Service B", new Normal(7, 2, seed(r)), eventList, EventType.DEP_B);
		checkout = new ServicePoint("Checkout", new Uniform(2, 5, seed(r)), eventList, EventType.DEP_CHECKOUT);
		servicePoints = new ServicePoint[]{checkIn, serviceA, serviceB, checkout};

		arrivalProcess = new ArrivalProcess(new Negexp(8, seed(r)), eventList, EventType.ARR_CHECKIN);
	}

	private static long seed(Random r) {
		return Integer.toUnsignedLong(r.nextInt());
	}

	@Override
	protected void initialize() {
		arrivalProcess.generateNextEvent();
	}

	@Override
	protected void runEvent(Event t) {
		Customer c;
		switch ((EventType) t.getType()) {
			case ARR_CHECKIN:
				checkIn.addQueue(new Customer());
				arrivalProcess.generateNextEvent();
				break;
			case DEP_CHECKIN:
				c = checkIn.removeQueue();
				c.setNeedsServiceA(routing.nextDouble() < P_A);
				(c.needsServiceA() ? serviceA : serviceB).addQueue(c);
				break;
			case DEP_A:
				checkout.addQueue(serviceA.removeQueue());
				break;
			case DEP_B:
				checkout.addQueue(serviceB.removeQueue());
				break;
			case DEP_CHECKOUT:
				c = checkout.removeQueue();
				c.setRemovalTime(Clock.getInstance().getClock());
				c.reportResults();
				break;
		}
	}

	@Override
	protected void tryCEvents() {
		for (ServicePoint p : servicePoints) {
			if (!p.isReserved() && p.isOnQueue()) {
				p.beginService();
			}
		}
	}

	@Override
	protected void results() {
		double end = Clock.getInstance().getClock();
		System.out.println("\nSimulation ended at " + end);
		System.out.println("Customers completed: " + Customer.getCompleted());
		System.out.println("Mean time in system: " + Customer.getMeanTimeInSystem());
		for (ServicePoint p : servicePoints) {
			System.out.printf("%-10s served=%d  utilization=%.2f  meanWait=%.2f  maxQueue=%d%n",
					p.getName(), p.getServed(), p.getUtilization(end), p.getMeanWaitTime(), p.getMaxQueue());
		}
	}
}
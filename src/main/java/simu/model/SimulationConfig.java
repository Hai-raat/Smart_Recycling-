package simu.model;

public class SimulationConfig {

    public double duration = 1000;            // simulated minutes
    public double meanArrivalInterval = 6;    // minutes between batches (Negexp mean)
    public long seed = 0;                     // 0 = random seed
    public double minQuantity = 50;           // kg
    public double maxQuantity = 500;          // kg

    public double[] proportions = {30, 30, 15, 15, 10};
    public double storageCapacity = 5000;     // kg

    public double[] serviceMean = {1, 1, 3, 8, 6, 7, 7, 4, 5};
    public double[] serviceStd = {0.3, 0.3, 1, 2, 1.5, 2, 2, 1, 1.5};
    public int[] servers = {1, 1, 1, 1, 1, 1, 1, 1, 1};

    public static final String[] STATION_NAMES = {
        "Reception", "Weighing", "Sorting", "Plastic", "Paper", "Glass", "Metal", "Reject", "Dispatch"
    };

    public void validate() {
        if (duration <= 0) {
            throw new IllegalArgumentException("Simulation duration must be positive");
        }
        if (meanArrivalInterval <= 0) {
            throw new IllegalArgumentException("Arrival interval must be positive");
        }
        if (minQuantity <= 0 || maxQuantity < minQuantity) {
            throw new IllegalArgumentException("Quantity range is invalid");
        }
        if (storageCapacity < maxQuantity) {
            throw new IllegalArgumentException("Storage capacity must be at least the maximum batch quantity");
        }
        double sum = 0;
        for (double p : proportions) {
            if (p < 0) {
                throw new IllegalArgumentException("Proportions cannot be negative");
            }
            sum += p;
        }
        if (sum <= 0) {
            throw new IllegalArgumentException("At least one proportion must be positive");
        }
        for (int i = 0; i < serviceMean.length; i++) {
            if (serviceMean[i] <= 0) {
                throw new IllegalArgumentException("Processing time must be positive (" + STATION_NAMES[i] + ")");
            }
            if (serviceStd[i] <= 0) {
                throw new IllegalArgumentException("Std-dev must be positive (" + STATION_NAMES[i] + ")");
            }
            if (servers[i] < 1) {
                throw new IllegalArgumentException("Station capacity must be at least 1 (" + STATION_NAMES[i] + ")");
            }
        }
    }
}

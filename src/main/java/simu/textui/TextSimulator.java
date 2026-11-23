package simu.textui;

import simu.framework.Trace;
import simu.model.MyEngine;
import simu.model.SimulationConfig;

public class TextSimulator {

    public static void main(String[] args) {
        Trace.setTraceLevel(Trace.Level.WAR);
        SimulationConfig config = new SimulationConfig();
        MyEngine engine = new MyEngine(config);
        engine.run();
        System.out.println(engine.getResults().toText());
    }
}

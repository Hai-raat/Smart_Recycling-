# Smart Recycling Facility Simulator

Java three-phase discrete-event simulator of a recycling facility (batches of plastic, paper, glass,
metal and contaminated waste) with a JavaFX GUI built on the MVC pattern. Maven project.

`simu/framework` and `eduni` are the supplied framework and are **not modified**. Own work is in
`simu/model`, `simu/controller`, `simu/view`, `simu/textui`.

## Model

    ARRIVAL -> Reception -> Weighing -> Sorting -+-> Plastic -+
                                                 +-> Paper   -+-> Storage -> Dispatch -> exit
                                                 +-> Glass   -+
                                                 +-> Metal   -+
                                                 +-> Reject -> exit   (contaminated)

- 9 service points (`ServicePoint`): FIFO queue, N parallel servers, Normal service times
- `Storage`: finite capacity (kg); a finished batch stays **blocked** in its station until there is room
- `Customer` = waste batch (type, quantity kg, arrival/removal time)
- Measurements: received / processed / dispatched / rejected / remaining kg, mean time in system,
  throughput, recovery rate, storage level (now/max/mean), per station: served, utilization,
  mean wait, mean & max queue
- Inputs (`SimulationConfig`) are validated (no negative capacities, positive times, ...)

## MVC data flow

    View (SimulatorGUI)  --SimulationConfig-->  SimulatorController  -->  MyEngine   (top -> bottom)
    View (SimulatorGUI)  <--SimulationResults-- SimulatorController  <--  MyEngine   (bottom -> top)

- The controller only knows the `ISimulatorUI` interface (no JavaFX types).
- The simulation runs in a worker thread; the view updates via `Platform.runLater`.
- On startup the view asks the controller for default values (model -> view) and fills its fields.
- Results can be saved as CSV (`ResultsCsv`).

## Run

    mvn javafx:run                          # GUI
    mvn exec:java -Dexec.mainClass=simu.textui.TextSimulator   # text version (or run it from the IDE)
    mvn test                                # JUnit tests

## Git

    git init
    git add .
    git commit -m "Recycling simulator: model + Maven JavaFX MVC GUI"
    git branch -M main
    git remote add origin <your-repo-url>
    git push -u origin main

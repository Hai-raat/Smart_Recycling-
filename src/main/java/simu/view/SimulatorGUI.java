package simu.view;

import java.io.File;
import java.util.Locale;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import simu.controller.ISimulatorUI;
import simu.controller.SimulatorController;
import simu.model.SimulationConfig;
import simu.model.SimulationResults;
import simu.model.WasteType;

public class SimulatorGUI extends Application implements ISimulatorUI {

    private SimulatorController controller;

    private final TextField duration = field(), arrival = field(), seed = field(),
            storage = field(), minQty = field(), maxQty = field();
    private final TextField[] shares = new TextField[WasteType.values().length];
    private final TextField[] servers = new TextField[SimulationConfig.STATION_NAMES.length];
    private final TextField[] means = new TextField[servers.length];
    private final TextField[] stds = new TextField[servers.length];
    private final Button startButton = new Button("Start");
    private final Button saveButton = new Button("Save results (CSV)");
    private final Label status = new Label("Ready");
    private final TextArea resultArea = new TextArea();

    private static TextField field() {
        TextField t = new TextField();
        t.setPrefColumnCount(6);
        return t;
    }

    @Override
    public void init() {
        controller = new SimulatorController(this);
    }

    @Override
    public void start(Stage stage) {
        // --- general parameters
        GridPane general = new GridPane();
        general.setHgap(8);
        general.setVgap(6);
        addRow(general, 0, "Duration (min)", duration);
        addRow(general, 1, "Mean arrival interval (min)", arrival);
        addRow(general, 2, "Random seed (0 = random)", seed);
        addRow(general, 3, "Storage capacity (kg)", storage);
        addRow(general, 4, "Batch size min (kg)", minQty);
        addRow(general, 5, "Batch size max (kg)", maxQty);

        // --- waste-type proportions
        GridPane mix = new GridPane();
        mix.setHgap(8);
        mix.setVgap(6);
        for (int i = 0; i < shares.length; i++) {
            shares[i] = field();
            addRow(mix, i, WasteType.values()[i].getLabel() + " share", shares[i]);
        }

        // --- station table
        GridPane stations = new GridPane();
        stations.setHgap(8);
        stations.setVgap(6);
        stations.add(new Label("Station"), 0, 0);
        stations.add(new Label("Capacity"), 1, 0);
        stations.add(new Label("Mean (min)"), 2, 0);
        stations.add(new Label("Std-dev"), 3, 0);
        for (int i = 0; i < servers.length; i++) {
            servers[i] = field();
            means[i] = field();
            stds[i] = field();
            stations.add(new Label(SimulationConfig.STATION_NAMES[i]), 0, i + 1);
            stations.add(servers[i], 1, i + 1);
            stations.add(means[i], 2, i + 1);
            stations.add(stds[i], 3, i + 1);
        }

        VBox inputs = new VBox(12,
                titled("General", general), titled("Material mix", mix), titled("Stations", stations));
        inputs.setPadding(new Insets(10));
        ScrollPane left = new ScrollPane(inputs);
        left.setFitToWidth(true);

        // --- results
        resultArea.setEditable(false);
        resultArea.setStyle("-fx-font-family: 'monospaced';");
        BorderPane.setMargin(resultArea, new Insets(10));

        startButton.setOnAction(e -> onStart());
        saveButton.setOnAction(e -> onSave(stage));
        saveButton.setDisable(true);
        HBox bar = new HBox(10, startButton, saveButton, status);
        bar.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(bar);
        root.setLeft(left);
        root.setCenter(resultArea);

        showConfig(controller.getDefaultConfig());   // model -> view

        stage.setTitle("Smart Recycling Facility Simulator");
        stage.setScene(new Scene(root, 1000, 650));
        stage.show();
    }

    private static void addRow(GridPane g, int row, String label, TextField f) {
        g.add(new Label(label), 0, row);
        g.add(f, 1, row);
    }

    private static TitledPane titled(String title, javafx.scene.Node content) {
        TitledPane p = new TitledPane(title, content);
        p.setCollapsible(false);
        return p;
    }

    // ---------- View -> Controller
    private void onStart() {
        try {
            controller.startSimulation(readConfig());
        } catch (NumberFormatException ex) {
            showError("Please enter valid numbers in all fields.");
        }
    }

    private SimulationConfig readConfig() {
        SimulationConfig c = new SimulationConfig();
        c.duration = d(duration);
        c.meanArrivalInterval = d(arrival);
        c.seed = Long.parseLong(seed.getText().trim());
        c.storageCapacity = d(storage);
        c.minQuantity = d(minQty);
        c.maxQuantity = d(maxQty);
        for (int i = 0; i < shares.length; i++) {
            c.proportions[i] = d(shares[i]);
        }
        for (int i = 0; i < servers.length; i++) {
            c.servers[i] = Integer.parseInt(servers[i].getText().trim());
            c.serviceMean[i] = d(means[i]);
            c.serviceStd[i] = d(stds[i]);
        }
        return c;
    }

    private static double d(TextField f) {
        return Double.parseDouble(f.getText().trim().replace(',', '.'));
    }

    private void onSave(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("simulation_results.csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File f = fc.showSaveDialog(stage);
        if (f != null) {
            controller.saveResults(f.toPath());
        }
    }

    @Override
    public void showConfig(SimulationConfig c) {
        runFx(() -> {
            duration.setText(fmt(c.duration));
            arrival.setText(fmt(c.meanArrivalInterval));
            seed.setText(Long.toString(c.seed));
            storage.setText(fmt(c.storageCapacity));
            minQty.setText(fmt(c.minQuantity));
            maxQty.setText(fmt(c.maxQuantity));
            for (int i = 0; i < shares.length; i++) {
                shares[i].setText(fmt(c.proportions[i]));
            }
            for (int i = 0; i < servers.length; i++) {
                servers[i].setText(Integer.toString(c.servers[i]));
                means[i].setText(fmt(c.serviceMean[i]));
                stds[i].setText(fmt(c.serviceStd[i]));
            }
        });
    }

    @Override
    public void showResults(SimulationResults r) {
        runFx(() -> {
            resultArea.setText(r.toText());
            status.setText("Finished");
            saveButton.setDisable(false);
        });
    }

    @Override
    public void showError(String message) {
        runFx(() -> {
            status.setText("Error");
            new Alert(Alert.AlertType.ERROR, message).showAndWait();
        });
    }

    @Override
    public void setRunning(boolean running) {
        runFx(() -> {
            startButton.setDisable(running);
            if (running) {
                status.setText("Running...");
            }
        });
    }

    private static String fmt(double v) {
        return v == Math.rint(v) ? String.format(Locale.US, "%.0f", v) : String.format(Locale.US, "%s", v);
    }

    private static void runFx(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }
}

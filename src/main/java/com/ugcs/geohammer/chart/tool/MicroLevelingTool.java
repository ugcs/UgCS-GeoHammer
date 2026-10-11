package com.ugcs.geohammer.chart.tool;

import com.ugcs.geohammer.chart.csv.SensorLineChart;
import com.ugcs.geohammer.format.SgyFile;
import com.ugcs.geohammer.format.csv.CsvFile;
import com.ugcs.geohammer.map.layer.GridLayer;
import com.ugcs.geohammer.model.Model;
import com.ugcs.geohammer.model.event.FileSelectedEvent;
import com.ugcs.geohammer.model.event.GridUpdatedEvent;
import com.ugcs.geohammer.model.event.SeriesUpdatedEvent;
import com.ugcs.geohammer.service.gridding.GriddingResult;
import com.ugcs.geohammer.service.magnetics.GridMicroLevelingOptions;
import com.ugcs.geohammer.service.magnetics.GridMicroLevelingResult;
import com.ugcs.geohammer.service.magnetics.MagneticProcessingWorkflow;
import com.ugcs.geohammer.service.magnetics.MicroLevelingSuggestions;
import com.ugcs.geohammer.service.magnetics.MicroLevelingOptions;
import com.ugcs.geohammer.service.magnetics.MicroLevelingService;
import com.ugcs.geohammer.service.quality.SurveyGeometryReport;
import com.ugcs.geohammer.service.quality.SurveyGeometryService;
import com.ugcs.geohammer.view.Dialogs;
import com.ugcs.geohammer.view.control.InputWithTopLabel;
import com.ugcs.geohammer.view.control.NodeWithTopLabel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
public class MicroLevelingTool extends FilterToolView {

    private final Model model;

    private final MicroLevelingService levelingService;

    private final MagneticProcessingWorkflow processingWorkflow;

    private final GridLayer gridLayer;

    private final SurveyGeometryService surveyGeometryService;

    private final ComboBox<String> inputSeriesSelector;

    private final ComboBox<String> noiseGridSelector;

    private final TextField smoothingWavelengthInput;

    private final TextField maximumCorrectionInput;

    private final Label suggestionDetails;

    private final TextField outputSeriesInput;

    private boolean applyingSuggestions;

    public MicroLevelingTool(Model model, MicroLevelingService levelingService, MagneticProcessingWorkflow processingWorkflow,
                             GridLayer gridLayer, SurveyGeometryService surveyGeometryService, ExecutorService executor) {
        super(executor);
        this.model = model;
        this.levelingService = levelingService;
        this.processingWorkflow = processingWorkflow;
        this.gridLayer = gridLayer;
        this.surveyGeometryService = surveyGeometryService;

        InputWithTopLabel smoothingWavelength = new InputWithTopLabel("Correction smoothing wavelength (m)");
        smoothingWavelengthInput = smoothingWavelength.getInput();
        smoothingWavelengthInput.setPromptText("Derived from primary-line spacing");
        smoothingWavelengthInput.textProperty().addListener((observable, oldValue, newValue) -> validateInput());

        InputWithTopLabel maximumCorrection = new InputWithTopLabel("Maximum correction (nT)");
        maximumCorrectionInput = maximumCorrection.getInput();
        maximumCorrectionInput.setPromptText("Derived from sampled noise");
        maximumCorrectionInput.textProperty().addListener((observable, oldValue, newValue) -> validateInput());

        InputWithTopLabel outputSeries = new InputWithTopLabel("Micro-leveled series");
        outputSeriesInput = outputSeries.getInput();
        outputSeriesInput.textProperty().addListener((observable, oldValue, newValue) -> validateInput());

        inputSeriesSelector = new ComboBox<>();
        inputSeriesSelector.setMaxWidth(Double.MAX_VALUE);
        inputSeriesSelector.setOnAction(event -> {
            String series = inputSeriesSelector.getValue();
            if (series != null) {
                outputSeriesInput.setText(series + "_MICRO");
            }
            validateInput();
        });

        noiseGridSelector = new ComboBox<>();
        noiseGridSelector.setMaxWidth(Double.MAX_VALUE);
        noiseGridSelector.setOnAction(event -> loadSuggestions());

        suggestionDetails = new Label();
        suggestionDetails.setWrapText(true);

        inputContainer.getChildren().setAll(
                new NodeWithTopLabel<>("Magnetic series", inputSeriesSelector),
                new NodeWithTopLabel<>("Extracted noise grid", noiseGridSelector),
                smoothingWavelength,
                maximumCorrection,
                suggestionDetails,
                new Label("Samples the noise grid only onto detected primary lines; tie lines remain unchanged."),
                outputSeries
        );
        showApply(true);
    }

    @Override
    public boolean isVisibleFor(SgyFile file) {
        return file instanceof CsvFile;
    }

    @Override
    public void updateView() {
        updateInputSeries(null);
        updateNoiseGrids();
    }

    private void updateInputSeries(String preferredSeries) {
        String selectedSeries = preferredSeries;
        if (selectedSeries == null && model.getChart(selectedFile) instanceof SensorLineChart chart) {
            selectedSeries = chart.getSelectedSeriesName();
        }
        MagneticSeriesSelector.update(inputSeriesSelector, selectedFile, selectedSeries);
        validateInput();
    }

    private void validateInput() {
        boolean invalid = inputSeriesSelector.getValue() == null || outputSeriesInput.getText().isBlank()
                || outputSeriesInput.getText().equals(inputSeriesSelector.getValue()) || noiseGridSelector.getValue() == null
                || parsePositive(smoothingWavelengthInput) == null || parsePositive(maximumCorrectionInput) == null;
        disableActions(invalid);
    }

    private Double parsePositive(TextField input) {
        try {
            double value = Double.parseDouble(input.getText().trim());
            return value > 0 && Double.isFinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void updateNoiseGrids() {
        String selected = noiseGridSelector.getValue();
        noiseGridSelector.getItems().clear();
        if (selectedFile != null) {
            for (GriddingResult grid : gridLayer.getResults(selectedFile)) {
                if (grid.seriesName().endsWith("_NOISE")) noiseGridSelector.getItems().add(grid.seriesName());
            }
        }
        if (selected != null && noiseGridSelector.getItems().contains(selected)) noiseGridSelector.setValue(selected);
        else if (!noiseGridSelector.getItems().isEmpty()) noiseGridSelector.setValue(noiseGridSelector.getItems().getFirst());
        else {
            smoothingWavelengthInput.clear();
            maximumCorrectionInput.clear();
            suggestionDetails.setText("Extract a directional noise grid before micro-leveling.");
        }
    }

    private void loadSuggestions() {
        SgyFile file = selectedFile;
        String noiseGrid = noiseGridSelector.getValue();
        if (file == null || noiseGrid == null || applyingSuggestions) {
            validateInput();
            return;
        }
        submitAction(() -> {
            try {
                GriddingResult grid = gridLayer.getResult(file, noiseGrid);
                if (grid == null) throw new IllegalArgumentException("The selected noise grid is unavailable");
                SurveyGeometryReport report = surveyGeometryService.analyze(file.getGeoData());
                if (report.primaryLineSpacing().isEmpty()) throw new IllegalArgumentException("Primary-line spacing could not be determined");
                MicroLevelingSuggestions suggestions = levelingService.suggestParameters(file.getGeoData(), grid,
                        new java.util.LinkedHashSet<>(report.primaryLineIds()), report.primaryLineSpacing().median());
                Platform.runLater(() -> applySuggestions(file, noiseGrid, report, suggestions));
            } catch (IllegalArgumentException e) {
                Platform.runLater(() -> Dialogs.showError("Micro-leveling", e.getMessage()));
            }
            return null;
        });
    }

    private void applySuggestions(SgyFile file, String noiseGrid, SurveyGeometryReport report,
                                  MicroLevelingSuggestions suggestions) {
        if (!file.equals(selectedFile) || !noiseGrid.equals(noiseGridSelector.getValue())) return;
        applyingSuggestions = true;
        smoothingWavelengthInput.setText("%.0f".formatted(suggestions.smoothingWavelength()));
        maximumCorrectionInput.setText("%.2f".formatted(suggestions.maximumCorrection()));
        applyingSuggestions = false;
        suggestionDetails.setText("Defaults: 4 × %.0f m primary-line spacing; correction cap = 3 × sampled-noise MAD."
                .formatted(report.primaryLineSpacing().median()));
        validateInput();
    }

    @Override
    protected void onApply(ActionEvent event) {
        if (!(model.getChart(selectedFile) instanceof SensorLineChart chart)) {
            return;
        }
        String inputSeries = inputSeriesSelector.getValue();
        String outputSeries = outputSeriesInput.getText().trim();
        String noiseGrid = noiseGridSelector.getValue();
        Double smoothing = parsePositive(smoothingWavelengthInput);
        Double maximum = parsePositive(maximumCorrectionInput);
        if (inputSeries == null || noiseGrid == null || smoothing == null || maximum == null) {
            return;
        }
        submitAction(() -> {
            try {
                GriddingResult grid = gridLayer.getResult(chart.getFile(), noiseGrid);
                if (grid == null) throw new IllegalArgumentException("Extract the selected noise grid before micro-leveling");
                SurveyGeometryReport report = surveyGeometryService.analyze(chart.getFile().getGeoData());
                GridMicroLevelingOptions options = new GridMicroLevelingOptions(inputSeries, outputSeries, noiseGrid,
                        smoothing, maximum, new java.util.LinkedHashSet<>(report.primaryLineIds()));
                GridMicroLevelingResult result = levelingService.level(chart.getFile().getGeoData(), grid, options);
                chart.createDerivedSeries(outputSeries + "_CORRECTION", inputSeries, result.corrections());
                chart.createDerivedSeries(outputSeries, inputSeries, result.values());
            } catch (IllegalArgumentException e) {
                Platform.runLater(() -> Dialogs.showError("Micro-leveling", e.getMessage()));
            }
            return null;
        });
    }

    @EventListener
    private void onFileSelected(FileSelectedEvent event) {
        Platform.runLater(() -> selectFile(event.getFile()));
    }

    @EventListener
    private void onSeriesUpdated(SeriesUpdatedEvent event) {
        if (!event.getFile().equals(selectedFile)) {
            return;
        }
        Platform.runLater(() -> {
            if (event.getFile().equals(selectedFile)) {
                updateInputSeries(event.getSeriesName());
            }
        });
    }

    @EventListener
    private void onGridUpdated(GridUpdatedEvent event) {
        if (event.getFile().equals(selectedFile)) Platform.runLater(this::updateNoiseGrids);
    }
}

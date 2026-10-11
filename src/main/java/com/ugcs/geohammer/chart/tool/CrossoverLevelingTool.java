package com.ugcs.geohammer.chart.tool;

import com.ugcs.geohammer.chart.csv.SensorLineChart;
import com.ugcs.geohammer.format.SgyFile;
import com.ugcs.geohammer.format.csv.CsvFile;
import com.ugcs.geohammer.model.Model;
import com.ugcs.geohammer.model.event.FileSelectedEvent;
import com.ugcs.geohammer.model.event.SeriesUpdatedEvent;
import com.ugcs.geohammer.service.magnetics.ConventionalLevelingOptions;
import com.ugcs.geohammer.service.magnetics.ConventionalLevelingResult;
import com.ugcs.geohammer.service.magnetics.ConventionalLevelingService;
import com.ugcs.geohammer.service.magnetics.MagneticProcessingWorkflow;
import com.ugcs.geohammer.service.magnetics.TieLineCorrectionModel;
import com.ugcs.geohammer.service.quality.SurveyGeometryReport;
import com.ugcs.geohammer.service.quality.SurveyGeometryService;
import com.ugcs.geohammer.view.Dialogs;
import com.ugcs.geohammer.view.control.InputWithTopLabel;
import com.ugcs.geohammer.view.control.NodeWithTopLabel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;

@Component
public class CrossoverLevelingTool extends FilterToolView {

    private final Model model;

    private final ConventionalLevelingService levelingService;

    private final MagneticProcessingWorkflow processingWorkflow;

    private final SurveyGeometryService surveyGeometryService;

    private final ComboBox<String> inputSeriesSelector;

    private final TextField tieLinesInput;

    private final TextField outputSeriesInput;

    private final ComboBox<TieLineCorrectionModel> correctionModelSelector;

    private final ComboBox<Integer> polynomialOrderSelector;

    private final Label availableLines;

    private final Label diagnostics;

    public CrossoverLevelingTool(Model model, ConventionalLevelingService levelingService,
                                 MagneticProcessingWorkflow processingWorkflow, SurveyGeometryService surveyGeometryService,
                                 ExecutorService executor) {
        super(executor);
        this.model = model;
        this.levelingService = levelingService;
        this.processingWorkflow = processingWorkflow;
        this.surveyGeometryService = surveyGeometryService;

        InputWithTopLabel outputSeries = new InputWithTopLabel("Leveled magnetic series");
        outputSeriesInput = outputSeries.getInput();
        outputSeriesInput.textProperty().addListener((observable, oldValue, newValue) -> validateInput());

        inputSeriesSelector = new ComboBox<>();
        inputSeriesSelector.setMaxWidth(Double.MAX_VALUE);
        inputSeriesSelector.setOnAction(event -> {
            String series = inputSeriesSelector.getValue();
            if (series != null) {
                outputSeriesInput.setText(series + "_XOVER");
            }
            validateInput();
        });

        InputWithTopLabel tieLines = new InputWithTopLabel("Tie-line IDs");
        tieLinesInput = tieLines.getInput();
        tieLinesInput.setPromptText("For example: 5, 10, 15");
        tieLinesInput.textProperty().addListener((observable, oldValue, newValue) -> validateInput());

        correctionModelSelector = new ComboBox<>();
        correctionModelSelector.getItems().setAll(TieLineCorrectionModel.values());
        correctionModelSelector.setValue(TieLineCorrectionModel.MEDIAN);
        correctionModelSelector.setMaxWidth(Double.MAX_VALUE);
        correctionModelSelector.setOnAction(event -> updateCorrectionModel());

        polynomialOrderSelector = new ComboBox<>();
        polynomialOrderSelector.getItems().setAll(0, 1, 2, 3);
        polynomialOrderSelector.setValue(1);
        polynomialOrderSelector.setMaxWidth(Double.MAX_VALUE);

        Button useDetectedTieLines = new Button("Use detected tie lines");
        useDetectedTieLines.setOnAction(event -> detectTieLines());

        availableLines = new Label();
        diagnostics = new Label();

        inputContainer.getChildren().setAll(
                new NodeWithTopLabel<>("Magnetic series", inputSeriesSelector),
                tieLines,
                useDetectedTieLines,
                availableLines,
                new NodeWithTopLabel<>("Tie-line correction", correctionModelSelector),
                new NodeWithTopLabel<>("Polynomial order", polynomialOrderSelector),
                outputSeries,
                diagnostics
        );
        updateCorrectionModel();
        showApply(true);
    }

    @Override
    public boolean isVisibleFor(SgyFile file) {
        return file instanceof CsvFile;
    }

    @Override
    public void updateView() {
        diagnostics.setText("");
        availableLines.setText("");
        updateInputSeries(null);
        if (model.getChart(selectedFile) instanceof SensorLineChart chart) {
            availableLines.setText("Available line IDs: " + chart.getFile().getLineRanges().keySet());
        }
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
        Set<Integer> tieLines = parseTieLines();
        boolean invalid = inputSeriesSelector.getValue() == null || tieLines.isEmpty()
                || outputSeriesInput.getText().isBlank() || outputSeriesInput.getText().equals(inputSeriesSelector.getValue());
        disableActions(invalid);
    }

    private Set<Integer> parseTieLines() {
        Set<Integer> lines = new LinkedHashSet<>();
        String[] values = tieLinesInput.getText().split(",");
        for (String value : values) {
            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                lines.add(Integer.parseInt(trimmed));
            } catch (NumberFormatException e) {
                return Set.of();
            }
        }
        return lines;
    }

    private void updateCorrectionModel() {
        boolean polynomial = correctionModelSelector.getValue() == TieLineCorrectionModel.POLYNOMIAL;
        polynomialOrderSelector.setDisable(!polynomial);
    }

    private void detectTieLines() {
        SgyFile file = selectedFile;
        if (file == null) {
            return;
        }
        submitAction(() -> {
            SurveyGeometryReport report = surveyGeometryService.analyze(file.getGeoData());
            Platform.runLater(() -> {
                if (!Objects.equals(file, selectedFile)) {
                    return;
                }
                tieLinesInput.setText(formatLineIds(report.tieLineIds()));
                diagnostics.setText("Detected %d non-primary line IDs; review and edit before applying."
                        .formatted(report.tieLineIds().size()));
            });
            return report;
        });
    }

    private static String formatLineIds(Iterable<Integer> lineIds) {
        StringBuilder text = new StringBuilder();
        for (Integer lineId : lineIds) {
            if (!text.isEmpty()) {
                text.append(", ");
            }
            text.append(lineId);
        }
        return text.toString();
    }

    @Override
    protected void onApply(ActionEvent event) {
        if (!(model.getChart(selectedFile) instanceof SensorLineChart chart)) {
            return;
        }
        String inputSeries = inputSeriesSelector.getValue();
        String outputSeries = outputSeriesInput.getText().trim();
        Set<Integer> tieLines = parseTieLines();
        TieLineCorrectionModel correctionModel = correctionModelSelector.getValue();
        Integer polynomialOrder = polynomialOrderSelector.getValue();
        if (inputSeries == null || tieLines.isEmpty() || correctionModel == null || polynomialOrder == null) {
            return;
        }
        submitAction(() -> {
            try {
                ConventionalLevelingOptions options = new ConventionalLevelingOptions(inputSeries, outputSeries, tieLines,
                        correctionModel, polynomialOrder);
                ConventionalLevelingResult result = levelingService.level(chart.getFile().getGeoData(), options);
                chart.createDerivedSeries(outputSeries + "_TIE_CORRECTION", inputSeries, result.tieCorrections());
                chart.createDerivedSeries(outputSeries + "_LINE_CORRECTION", inputSeries, result.surveyCorrections());
                chart.createDerivedSeries(outputSeries, inputSeries, result.values());
                processingWorkflow.recordConventionalLeveling(chart.getFile(), options);
                Platform.runLater(() -> diagnostics.setText("Crossovers: %d; RMS mis-tie: %.2f -> %.2f"
                        .formatted(result.crossovers().size(), result.before().rms(), result.after().rms())));
            } catch (IllegalArgumentException e) {
                Platform.runLater(() -> Dialogs.showError("Crossover leveling", e.getMessage()));
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
}

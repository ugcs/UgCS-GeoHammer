package com.ugcs.geohammer.chart.tool;

import com.ugcs.geohammer.format.SgyFile;
import com.ugcs.geohammer.format.csv.CsvFile;
import com.ugcs.geohammer.map.layer.GridLayer;
import com.ugcs.geohammer.model.Model;
import com.ugcs.geohammer.model.event.FileSelectedEvent;
import com.ugcs.geohammer.model.event.GridUpdatedEvent;
import com.ugcs.geohammer.service.gridding.GriddingFilter;
import com.ugcs.geohammer.service.gridding.GriddingResult;
import com.ugcs.geohammer.service.magnetics.NoiseExtractionService;
import com.ugcs.geohammer.service.quality.SurveyGeometryReport;
import com.ugcs.geohammer.service.quality.SurveyGeometryService;
import com.ugcs.geohammer.view.Dialogs;
import com.ugcs.geohammer.view.control.NodeWithTopLabel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
public class ExtractNoiseTool extends FilterToolView {

    private final Model model;
    private final GridLayer gridLayer;
    private final NoiseExtractionService noiseExtractionService;
    private final SurveyGeometryService surveyGeometryService;
    private final ComboBox<String> sourceGridSelector;
    private final Label details;

    public ExtractNoiseTool(Model model, GridLayer gridLayer, NoiseExtractionService noiseExtractionService,
                            SurveyGeometryService surveyGeometryService, ExecutorService executor) {
        super(executor);
        this.model = model;
        this.gridLayer = gridLayer;
        this.noiseExtractionService = noiseExtractionService;
        this.surveyGeometryService = surveyGeometryService;
        sourceGridSelector = new ComboBox<>();
        sourceGridSelector.setMaxWidth(Double.MAX_VALUE);
        sourceGridSelector.setOnAction(event -> validateInput());
        details = new Label("Uses a sixth-order directional high-pass filter. Cutoff defaults to four times primary-line spacing.");
        details.setWrapText(true);
        inputContainer.getChildren().setAll(new NodeWithTopLabel<>("Leveled input grid", sourceGridSelector), details);
        showApply(true);
    }

    @Override
    public boolean isVisibleFor(SgyFile file) {
        return file instanceof CsvFile;
    }

    @Override
    public void updateView() {
        String selected = gridLayer.getDisplayedSeries(selectedFile);
        sourceGridSelector.getItems().clear();
        if (selectedFile != null) {
            for (GriddingResult result : gridLayer.getResults(selectedFile)) sourceGridSelector.getItems().add(result.seriesName());
        }
        if (selected != null && sourceGridSelector.getItems().contains(selected)) sourceGridSelector.setValue(selected);
        else if (!sourceGridSelector.getItems().isEmpty()) sourceGridSelector.setValue(sourceGridSelector.getItems().getFirst());
        validateInput();
    }

    private void validateInput() {
        disableActions(sourceGridSelector.getValue() == null || sourceGridSelector.getValue().endsWith("_NOISE"));
    }

    @Override
    protected void onApply(ActionEvent event) {
        SgyFile file = selectedFile;
        String series = sourceGridSelector.getValue();
        if (file == null || series == null) return;
        submitAction(() -> {
            try {
                GriddingResult source = gridLayer.getResult(file, series);
                if (source == null) throw new IllegalArgumentException("Generate the selected grid before extracting noise");
                SurveyGeometryReport geometry = surveyGeometryService.analyze(file.getGeoData());
                if (geometry.primaryLineSpacing().isEmpty()) throw new IllegalArgumentException("Primary line spacing could not be determined");
                GriddingResult noise = noiseExtractionService.extract(source, geometry.primaryLineOrientation(),
                        geometry.primaryLineSpacing().median());
                GriddingFilter filter = gridLayer.getFilter(file, series);
                if (filter == null) throw new IllegalArgumentException("The source grid has no display settings");
                gridLayer.setFilter(file, noise.seriesName(), filter);
                gridLayer.setResult(file, noise);
                Platform.runLater(() -> details.setText("Noise grid created: %s. Heading %.1f°, cutoff %.0f m."
                        .formatted(noise.seriesName(), geometry.primaryLineOrientation(), 4 * geometry.primaryLineSpacing().median())));
            } catch (IllegalArgumentException e) {
                Platform.runLater(() -> Dialogs.showError("Extract noise", e.getMessage()));
            }
            return null;
        });
    }

    @EventListener
    private void onFileSelected(FileSelectedEvent event) {
        Platform.runLater(() -> selectFile(event.getFile()));
    }

    @EventListener
    private void onGridUpdated(GridUpdatedEvent event) {
        if (event.getFile().equals(selectedFile)) Platform.runLater(this::updateView);
    }
}

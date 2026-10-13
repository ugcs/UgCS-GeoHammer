package com.ugcs.geohammer.chart.tool;

import com.ugcs.geohammer.AppContext;
import com.ugcs.geohammer.chart.csv.SensorLineChart;
import com.ugcs.geohammer.format.SgyFile;
import com.ugcs.geohammer.format.csv.CsvFile;
import com.ugcs.geohammer.model.Model;
import com.ugcs.geohammer.model.event.FileSelectedEvent;
import com.ugcs.geohammer.model.event.MagneticProcessingUpdatedEvent;
import com.ugcs.geohammer.service.magnetics.MagneticProcessingHistory;
import com.ugcs.geohammer.service.magnetics.MagneticProcessingRecipeStore;
import com.ugcs.geohammer.service.magnetics.MagneticProcessingStep;
import com.ugcs.geohammer.service.magnetics.RecipeValidationResult;
import com.ugcs.geohammer.service.magnetics.RecipeValidationService;
import com.ugcs.geohammer.service.magnetics.RecipeReplayService;
import com.ugcs.geohammer.view.Dialogs;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

@Component
public class MagneticProcessingRecipeTool extends ToolView {

    private final Model model;

    private final MagneticProcessingHistory processingHistory;

    private final MagneticProcessingRecipeStore recipeStore;

    private final RecipeValidationService validationService;

    private final RecipeReplayService replayService;

    private final ExecutorService executor;

    private final ListView<MagneticProcessingStep> recipeSteps;

    private final Button removeStepButton;

    private final Button clearRecipeButton;

    private final Button exportButton;

    private final Button importButton;

    private final Button replayButton;

    private final Label importStatus;

    private RecipeValidationResult validatedRecipe;

    private boolean displayingImportedRecipe;

    public MagneticProcessingRecipeTool(Model model, MagneticProcessingHistory processingHistory,
                                        MagneticProcessingRecipeStore recipeStore, RecipeValidationService validationService,
                                        RecipeReplayService replayService, ExecutorService executor) {
        this.model = model;
        this.processingHistory = processingHistory;
        this.recipeStore = recipeStore;
        this.validationService = validationService;
        this.replayService = replayService;
        this.executor = executor;

        recipeSteps = new ListView<>();
        recipeSteps.setPrefHeight(160);
        recipeSteps.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(MagneticProcessingStep step, boolean empty) {
                super.updateItem(step, empty);
                setText(empty || step == null ? null : formatStep(getIndex(), step));
            }
        });
        recipeSteps.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateRecipeActionButtons());

        removeStepButton = new Button("Remove selected step");
        removeStepButton.setOnAction(event -> removeSelectedStep());

        clearRecipeButton = new Button("Clear recipe");
        clearRecipeButton.setOnAction(event -> clearRecipe());

        exportButton = new Button("Export recipe");
        exportButton.setOnAction(event -> exportRecipe());

        importButton = new Button("Import and validate recipe");
        importButton.setOnAction(event -> importRecipe());

        replayButton = new Button("Replay validated recipe");
        replayButton.setOnAction(event -> replayRecipe());

        importStatus = new Label();

        getChildren().add(Tools.createToolContainer(
                new Label("Recipes are saved automatically beside the survey file."),
                new Label("Recorded recipe steps"),
                recipeSteps,
                removeStepButton,
                clearRecipeButton,
                importStatus,
                importButton,
                replayButton,
                exportButton
        ));
    }

    @Override
    public boolean isVisibleFor(SgyFile file) {
        return file instanceof CsvFile;
    }

    @Override
    public void updateView() {
        List<MagneticProcessingStep> steps = selectedFile != null ? processingHistory.getSteps(selectedFile) : List.of();
        displayingImportedRecipe = false;
        recipeSteps.getItems().setAll(steps);
        recipeSteps.getSelectionModel().clearSelection();
        exportButton.setDisable(selectedFile == null || selectedFile.getFile() == null || steps.isEmpty());
        importButton.setDisable(selectedFile == null);
        validateCurrentRecipe(steps);
        updateRecipeActionButtons();
    }

    private void validateCurrentRecipe(List<MagneticProcessingStep> steps) {
        validatedRecipe = null;
        replayButton.setDisable(true);
        importStatus.setText("");
        if (selectedFile == null || steps.isEmpty()) {
            return;
        }
        RecipeValidationResult result = validationService.validate(selectedFile, steps);
        if (result.isValid()) {
            validatedRecipe = result;
            replayButton.setDisable(false);
            importStatus.setText("Saved recipe is valid for this survey and ready to replay.");
        } else {
            importStatus.setText("Saved recipe cannot be replayed:\n" + String.join("\n", result.errors()));
        }
    }

    private void exportRecipe() {
        if (selectedFile == null || selectedFile.getFile() == null) {
            return;
        }
        File source = selectedFile.getFile();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export magnetic processing recipe");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON recipe (*.json)", "*.json"));
        chooser.setInitialFileName(source.getName() + ".magnetic-recipe.json");
        File parent = source.getParentFile();
        if (parent != null && parent.isDirectory()) {
            chooser.setInitialDirectory(parent);
        }
        File target = chooser.showSaveDialog(AppContext.stage);
        if (target == null) {
            return;
        }
        SgyFile file = selectedFile;
        List<MagneticProcessingStep> steps = processingHistory.getSteps(file);
        executor.submit(() -> {
            try {
                recipeStore.export(file, steps, Path.of(target.getPath()));
            } catch (IOException e) {
                Platform.runLater(() -> Dialogs.showError("Recipe export", e));
            }
        });
    }

    private void importRecipe() {
        if (selectedFile == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import magnetic processing recipe");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON recipe (*.json)", "*.json"));
        File recipeFile = chooser.showOpenDialog(AppContext.stage);
        if (recipeFile == null) {
            return;
        }
        validatedRecipe = null;
        replayButton.setDisable(true);
        SgyFile file = selectedFile;
        executor.submit(() -> {
            try {
                RecipeValidationResult result = validationService.validate(file, recipeStore.read(Path.of(recipeFile.getPath())));
                Platform.runLater(() -> showImportedRecipe(file, recipeFile, result));
            } catch (IOException e) {
                Platform.runLater(() -> Dialogs.showError("Recipe import", e));
            }
        });
    }

    private void showImportedRecipe(SgyFile file, File recipeFile, RecipeValidationResult result) {
        if (selectedFile != file) {
            return;
        }
        displayingImportedRecipe = true;
        recipeSteps.getItems().setAll(result.steps());
        recipeSteps.getSelectionModel().clearSelection();
        updateRecipeActionButtons();
        if (result.isValid()) {
            importStatus.setText("Recipe '" + recipeFile.getName() + "' is valid for this survey. No steps were run.");
            validatedRecipe = result;
            replayButton.setDisable(false);
        } else {
            importStatus.setText("Recipe cannot be replayed:\n" + String.join("\n", result.errors()));
        }
    }

    private void removeSelectedStep() {
        if (selectedFile == null || displayingImportedRecipe) {
            return;
        }
        int index = recipeSteps.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            processingHistory.removeStep(selectedFile, index);
        }
    }

    private void clearRecipe() {
        if (selectedFile == null || displayingImportedRecipe || recipeSteps.getItems().isEmpty()) {
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Clear all recorded recipe steps? This does not delete any derived series.",
                ButtonType.CANCEL, ButtonType.OK);
        confirmation.initOwner(AppContext.stage);
        Optional<ButtonType> response = confirmation.showAndWait();
        if (response.isPresent() && response.get() == ButtonType.OK) {
            processingHistory.clear(selectedFile);
        }
    }

    private void updateRecipeActionButtons() {
        boolean editable = selectedFile != null && !displayingImportedRecipe;
        removeStepButton.setDisable(!editable || recipeSteps.getSelectionModel().getSelectedIndex() < 0);
        clearRecipeButton.setDisable(!editable || recipeSteps.getItems().isEmpty());
    }

    private void replayRecipe() {
        if (!(model.getChart(selectedFile) instanceof SensorLineChart chart) || validatedRecipe == null) {
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Replay will create the validated derived series. Existing data will not be overwritten.",
                ButtonType.CANCEL, ButtonType.OK);
        confirmation.initOwner(AppContext.stage);
        Optional<ButtonType> response = confirmation.showAndWait();
        if (response.isEmpty() || response.get() != ButtonType.OK) {
            return;
        }
        List<MagneticProcessingStep> steps = validatedRecipe.steps();
        replayButton.setDisable(true);
        executor.submit(() -> {
            try {
                replayService.replay(chart, steps);
                Platform.runLater(() -> importStatus.setText("Recipe replay completed."));
            } catch (IllegalArgumentException e) {
                Platform.runLater(() -> Dialogs.showError("Recipe replay", e.getMessage()));
            }
        });
    }

    private static String formatStep(int index, MagneticProcessingStep step) {
        StringBuilder text = new StringBuilder();
        text.append(index + 1).append(". ").append(step.type()).append(": ")
                .append(step.inputSeries()).append(" -> ").append(step.outputSeries());
        if (!step.settings().isEmpty()) {
            text.append("  ").append(step.settings());
        }
        return text.toString();
    }

    @EventListener
    private void onFileSelected(FileSelectedEvent event) {
        Platform.runLater(() -> selectFile(event.getFile()));
    }

    @EventListener
    private void onProcessingUpdated(MagneticProcessingUpdatedEvent event) {
        if (event.getFile() == selectedFile) {
            Platform.runLater(this::updateView);
        }
    }
}

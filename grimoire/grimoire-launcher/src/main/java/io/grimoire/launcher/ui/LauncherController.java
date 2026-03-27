package io.grimoire.launcher.ui;

import io.grimoire.launcher.service.LauncherService;
import jakarta.inject.Singleton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.RequiredArgsConstructor;

/**
 * Controller for the launcher UI.
 */
@Singleton
@RequiredArgsConstructor
public class LauncherController {
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private ProgressBar progressBar;
    
    private final LauncherService launcherService;
    
    /**
     * Initializes the controller after FXML loading.
     */
    @FXML
    public void initialize() {
        // Set up callbacks
        launcherService.setStatusUpdateCallback(this::updateStatus);
        launcherService.setProgressUpdateCallback(this::updateProgress);
        
        // Start the update process
        launcherService.startUpdateProcess();
    }
    
    /**
     * Updates the status label.
     */
    private void updateStatus(String status) {
        statusLabel.setText(status);
    }
    
    /**
     * Updates the progress bar.
     */
    private void updateProgress(double progress) {
        progressBar.setProgress(progress);
    }
}

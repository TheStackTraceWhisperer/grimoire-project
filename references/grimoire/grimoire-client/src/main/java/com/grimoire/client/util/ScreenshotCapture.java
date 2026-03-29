package com.grimoire.client.util;

import com.grimoire.client.JavaFxApplication;
import com.grimoire.shared.dto.CharacterListResponse;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility to capture screenshots of the UI for documentation.
 */
public class ScreenshotCapture {

    public static void captureLoginScreen(JavaFxApplication app, Stage stage, String outputPath) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> exceptionHolder = new AtomicReference<>();
        
        Platform.runLater(() -> {
            try {
                app.start(stage);
                stage.show();
                
                // Use PauseTransition for reliable rendering completion
                PauseTransition pause = new PauseTransition(Duration.millis(300));
                pause.setOnFinished(event -> {
                    try {
                        Scene scene = stage.getScene();
                        WritableImage image = scene.snapshot(null);
                        
                        File outputFile = new File(outputPath);
                        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", outputFile);
                        System.out.println("Login screenshot saved to: " + outputFile.getAbsolutePath());
                        
                        latch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                        exceptionHolder.set(e);
                        latch.countDown();
                    }
                });
                pause.play();
            } catch (Exception e) {
                e.printStackTrace();
                exceptionHolder.set(e);
                latch.countDown();
            }
        });
        
        latch.await(10, TimeUnit.SECONDS);
        
        if (exceptionHolder.get() != null) {
            throw new RuntimeException("Failed to capture login screenshot", exceptionHolder.get());
        }
    }

    public static void captureCharacterSelectionScreen(JavaFxApplication app, Stage stage, CharacterListResponse response, String outputPath) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> exceptionHolder = new AtomicReference<>();
        
        Platform.runLater(() -> {
            try {
                // Start app first to set primaryStage
                app.start(stage);
                app.showCharacterSelectionScene(response);
                stage.show();
                
                // Use PauseTransition for reliable rendering completion
                PauseTransition pause = new PauseTransition(Duration.millis(300));
                pause.setOnFinished(event -> {
                    try {
                        Scene scene = stage.getScene();
                        WritableImage image = scene.snapshot(null);
                        
                        File outputFile = new File(outputPath);
                        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", outputFile);
                        System.out.println("Character selection screenshot saved to: " + outputFile.getAbsolutePath());
                        
                        latch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                        exceptionHolder.set(e);
                        latch.countDown();
                    }
                });
                pause.play();
            } catch (Exception e) {
                e.printStackTrace();
                exceptionHolder.set(e);
                latch.countDown();
            }
        });
        
        latch.await(10, TimeUnit.SECONDS);
        
        if (exceptionHolder.get() != null) {
            throw new RuntimeException("Failed to capture character selection screenshot", exceptionHolder.get());
        }
    }

    public static void captureGameScreen(JavaFxApplication app, Stage stage, String outputPath) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> exceptionHolder = new AtomicReference<>();
        
        Platform.runLater(() -> {
            try {
                // Start app with stage (this sets primaryStage)
                app.start(stage);
                // Then show game scene (which uses the primaryStage we just set)
                app.showGameScene();
                stage.show();
                
                // Use PauseTransition for reliable rendering completion (longer for game animation loop)
                PauseTransition pause = new PauseTransition(Duration.millis(500));
                pause.setOnFinished(event -> {
                    try {
                        Scene scene = stage.getScene();
                        WritableImage image = scene.snapshot(null);
                        
                        File outputFile = new File(outputPath);
                        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", outputFile);
                        System.out.println("Game screenshot saved to: " + outputFile.getAbsolutePath());
                        
                        latch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                        exceptionHolder.set(e);
                        latch.countDown();
                    }
                });
                pause.play();
            } catch (Exception e) {
                e.printStackTrace();
                exceptionHolder.set(e);
                latch.countDown();
            }
        });
        
        latch.await(10, TimeUnit.SECONDS);
        
        if (exceptionHolder.get() != null) {
            throw new RuntimeException("Failed to capture game screenshot", exceptionHolder.get());
        }
    }
}

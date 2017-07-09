/*
 * Copyright 2017 Michael Mitchell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/*
 * Description: Simple error window that automatically resizes based on the messages
 * sent to the window via addMesage() and the 1-arg constructor
 */

public class ErrorWindow {

    private static Stage primaryStage = new Stage();

    private static ScrollPane scrollPane = new ScrollPane();

    private static VBox contents = new VBox(),
            rootPane = new VBox();

    private static HBox menuButtonHolder = new HBox();

    private static BorderPane titleBar = new BorderPane();

    private static StackPane closeButtonRoot = new StackPane(),
            minimizeButtonRoot = new StackPane();

    private static Circle closeButton = new Circle(),
            minimizeButton = new Circle(),
            closeButtonSealer = new Circle(),
            minimizeButtonSealer = new Circle();

    private static Text windowTitle = new Text("Error Log");

    private static Scene root = new Scene(rootPane, 100, 250);

    private static boolean isInitialized = false;

    private static double mouseDragStartX, mouseDragStartY;

    private static Line closeSymbol1 = new Line(),
            closeSymbol2 = new Line(),
            minimizeSymbol = new Line();

    // 0-arg constructor just to initialize
    public ErrorWindow() { }

    // 1-arg constructor to add a message to the window prior to opening the stage
    public ErrorWindow(String initialErrorMessage) {
        addMessage(initialErrorMessage);

    }

    private void initUI() {
        primaryStage.getIcons().addAll(
                new Image(FileEncryptor.class.getResourceAsStream("img/file-encryptor.32.png")),
                new Image(FileEncryptor.class.getResourceAsStream("img/file-encryptor.48.png")),
                new Image(FileEncryptor.class.getResourceAsStream("img/file-encryptor.64.png")),
                new Image(FileEncryptor.class.getResourceAsStream("img/file-encryptor.128.png"))
        );

        scrollPane.setContent(contents);

        closeSymbol1.setVisible(false);
        closeSymbol1.setStartX(5.5);
        closeSymbol1.setEndX(-1.0);
        closeSymbol1.setStartY(-3.5);
        closeSymbol1.setEndY(3.0);
        closeSymbol1.setStrokeWidth(1.5);
        closeSymbol2.setVisible(false);
        closeSymbol2.setStartX(5.5);
        closeSymbol2.setEndX(-1.0);
        closeSymbol2.setStartY(3.0);
        closeSymbol2.setEndY(-3.5);
        closeSymbol2.setStrokeWidth(1.5);

        minimizeSymbol.setVisible(false);
        minimizeSymbol.setStartX(7.0);
        minimizeSymbol.setEndX(1.0);
        minimizeSymbol.setStrokeWidth(1.75);

        closeButtonSealer.setOnMouseEntered(e -> closeSymbol1.setVisible(true));
        closeButtonSealer.setOnMouseExited(e -> closeSymbol1.setVisible(false));
        closeSymbol2.visibleProperty().bind(closeSymbol1.visibleProperty());

        minimizeButtonSealer.setOnMouseEntered(e -> minimizeSymbol.setVisible(true));
        minimizeButtonSealer.setOnMouseExited(e -> minimizeSymbol.setVisible(false));

        rootPane.getChildren().addAll(titleBar, scrollPane);
        primaryStage.setTitle("Error Log");
        rootPane.setStyle("-fx-background-color: whitesmoke");
        titleBar.setRight(menuButtonHolder);
        titleBar.setCenter(windowTitle);
        menuButtonHolder.getChildren().addAll(minimizeButtonRoot, closeButtonRoot);
        minimizeButtonRoot.getChildren().addAll(minimizeButton, minimizeSymbol, minimizeButtonSealer);
        closeButtonRoot.getChildren().addAll(closeButton, closeSymbol1, closeSymbol2, closeButtonSealer);
        menuButtonHolder.setSpacing(8f);
        BorderPane.setMargin(menuButtonHolder, new Insets(7, 7, 0, 0));
        BorderPane.setMargin(windowTitle, new Insets(7, 0, 0, 35));
        closeButton.setRadius(8f);
        closeButtonSealer.setRadius(8f);
        minimizeButton.setRadius(8f);
        minimizeButtonSealer.setRadius(8f);
        closeButton.setStyle("-fx-fill: red");
        closeButtonSealer.setStyle("-fx-fill: transparent");
        minimizeButton.setStyle("-fx-fill: orange");
        minimizeButtonSealer.setStyle("-fx-fill: transparent");
        VBox.setMargin(scrollPane, new Insets(10, 0, 10, 10));
        scrollPane.setStyle("-fx-border-color: whitesmoke;" +
                "-fx-faint-focus-color: transparent;" +
                "-fx-focus-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setMinHeight(220f);
        scrollPane.setMaxHeight(220f);
        scrollPane.setPrefHeight(220f);
        primaryStage.setScene(root);
        primaryStage.initStyle(StageStyle.UNDECORATED);

    }

    private void initActions() {
        // Focus effect for user clarity
        primaryStage.focusedProperty().addListener(e -> {
            if (primaryStage.isFocused()) {
                closeButton.setStyle("-fx-fill: red");
                minimizeButton.setStyle("-fx-fill: orange");
                windowTitle.setStyle("-fx-fill: black");

            }
            else {
                closeButton.setStyle("-fx-fill: dimgrey");
                minimizeButton.setStyle("-fx-fill: dimgrey");
                windowTitle.setStyle("-fx-fill: dimgrey");

            }
        });

        // Moveable window
        titleBar.setOnMousePressed(e -> {
            if (e.getButton() != MouseButton.MIDDLE) {
                mouseDragStartX = e.getX();
                mouseDragStartY = e.getY();
            }
        });

        titleBar.setOnMouseDragged(e -> {
            if (e.getButton() != MouseButton.MIDDLE) {
                titleBar.getScene().getWindow().setX(e.getScreenX() - mouseDragStartX);
                titleBar.getScene().getWindow().setY(e.getScreenY() - mouseDragStartY);
            }
        });

        closeButtonSealer.setOnMouseClicked(e -> close());
        minimizeButtonSealer.setOnMouseClicked(e -> primaryStage.hide());

    }

    private void updateWidth() { primaryStage.setWidth(contents.getBoundsInParent().getWidth() + 60); }

    public void show() {
        if (! isInitialized) {
            initUI();
            initActions();

        }
        primaryStage.show();
        primaryStage.requestFocus();
        isInitialized = true;

    }

    public void close() {
        primaryStage.close();

    }

    public void addMessage(String message) {
        if (message.length() > 55) {
            message = message.substring(0, 52);
            message += "...";

        }

        Text newMessage = new Text(message);
        contents.getChildren().add(newMessage);
        updateWidth();

    }

    public void removeAllMessages() {
        contents.getChildren().removeAll(contents.getChildren());

    }

    public void removeMessage(String message) {
        for (Node displayedMessage : contents.getChildren()) {
            if (displayedMessage instanceof Text) {
                if (((Text) displayedMessage).getText().equals(message)) {
                    contents.getChildren().remove(displayedMessage);
                }
            }
        }

        updateWidth();

    }
}

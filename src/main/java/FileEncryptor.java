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

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/*
 * Description: This is a simple graphical file encryption tool
 */


public class FileEncryptor extends Application {
    // Stage
    private Stage primaryStage;

    // Thread executor
    private ExecutorService executor = Executors.newFixedThreadPool(1);

    // File objects
    private ArrayList<File> inputFiles = new ArrayList<>();

    // File widget array list
    private ArrayList<FileWidgetWithProgressBar> fileWidgets = new ArrayList<>();

    // Window drag properties
    private Double mouseDragStartX, mouseDragStartY;

    // Containers
    private VBox root = new VBox();
    private HBox titleBar = new HBox(),
            mainScene = new HBox();
    private VBox leftVBox = new VBox(),
            rightVBox = new VBox();
    private HBox leftTopHBox = new HBox(),
            advSeperatorHBox = new HBox(),
            algoTypeContainer = new HBox(),
            keySizeContainer = new HBox(),
            AESContainer = new HBox(),
            DESContainer = new HBox(),
            bit128Container = new HBox(),
            bit192Container = new HBox(),
            bit256Container = new HBox(),
            topBarHBox = new HBox();
    private StackPane decryptButtonPane = new StackPane(),
            chooseFileButtonPane = new StackPane(),
            encryptButtonPane = new StackPane(),
            closeButtonPane = new StackPane(),
            minimizeButtonPane = new StackPane();
    private ScrollPane fileWindow = new ScrollPane();
    private VBox fileWindowContents = new VBox();

    // Nodes
    private Tooltip chooseFileToolTip = new Tooltip(),
            decryptFileToolTip = new Tooltip(),
            encryptFileToolTip = new Tooltip();
    private Text windowTitle = new Text("File Encryptor"),
            fileNameText = new Text("File Name"),
            fileSizeText = new Text("File Size");
    private Label keySize = new Label("Encryption Strength"),
            algoType = new Label("Algorithm"),
            AES = new Label("Advanced Encryption"),
            DES = new Label("Triple DES (DESede)"),
            bit128 = new Label("128"),
            bit192 = new Label("192"),
            bit256 = new Label("256"),
            advLabel = new Label("Advanced"),
            dropFilesHere = new Label("Drag n' Drop files here");
    private PasswordField passwordField = new PasswordField();
    private Circle decryptButton = new Circle(),
            decryptButtonSealer = new Circle(),
            chooseFileButton = new Circle(),
            chooseFileButtonSealer = new Circle(),
            encryptButton = new Circle(),
            encryptButtonSealer = new Circle(),
            closeButton = new Circle(),
            closeButtonSealer = new Circle(),
            minimizeButton = new Circle(),
            minimizeButtonSealer = new Circle(),
            statusCircle1 = new Circle(),
            statusCircle2 = new Circle(),
            statusCircle3 = new Circle(),
            statusCircle4 = new Circle(),
            statusCircle5 = new Circle(),
            statusCircle6 = new Circle(),
            statusCircle7 = new Circle();
    private Rectangle colorAddition = new Rectangle(),
            colorAddition2 = new Rectangle(),
            lockBase1 = new Rectangle(),
            lockBase2 = new Rectangle();
    private Arc lockBar1 = new Arc(),
            lockBar2 = new Arc();
    private Line addFileSymbol1 = new Line(),
            addFileSymbol2 = new Line(),
            closeSymbol1 = new Line(),
            closeSymbol2 = new Line(),
            minimizeSymbol = new Line(),
            advLabelSpacer1 = new Line(),
            advLabelSpacer2 = new Line(),
            topLine1 = new Line(),
            topLine2 = new Line(),
            topLine3 = new Line(),
            bottomLine = new Line();


    // Encryption items
    private String aes1 = "AES/CBC/PKCS5Padding",
            desede1 = "DESede/CBC/PKCS5Padding",
            algoSpec, algorithm, password;
    private Integer keyStrength,
            fileCount;

    // Animation properties
    private static int dropAnimationCount = 0;

    // 0-arg constructor
    public FileEncryptor() {

        // Set values to null to test later
        algoSpec = "AES";
        algorithm = aes1;
        password = null;
        keyStrength = 128;
        fileCount = 0;
    }

    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;

        initUI();

        // Dropdown animation
        // Keeping this here because I haven't decided if I even want animations
        // in this application.
        EventHandler<ActionEvent> animate = e -> {
            dropAnimationCount++;
            VBox.setMargin(keySizeContainer, new Insets(dropAnimationCount, 0, 0, 30));
        };

        Timeline dropAnimationTimeLine = new Timeline(
                new KeyFrame(Duration.millis(4), animate)
        );

        dropAnimationTimeLine.setOnFinished(e -> {
            leftVBox.getChildren().addAll(4, Arrays.asList(AESContainer, DESContainer));
            VBox.setMargin(keySizeContainer, new Insets(VBox.getMargin(keySizeContainer).getTop() - 50, 0, 0, 30));
        });

        dropAnimationTimeLine.setCycleCount(55);

        // Dropdown reverse animation
        EventHandler<ActionEvent> reverse = e -> {
            dropAnimationCount--;
            VBox.setMargin(keySizeContainer, new Insets(dropAnimationCount, 0, 0, 30));
        };

        Timeline dropAnimationTileLineReverse = new Timeline(
                new KeyFrame(Duration.millis(4), reverse)
        );

        dropAnimationTileLineReverse.setCycleCount(55);

        algoType.setOnMouseClicked(e -> {
            if (VBox.getMargin(keySizeContainer).getTop() == 0) {
                dropAnimationTimeLine.play();
            }
            else {
                if (VBox.getMargin(keySizeContainer).getTop() != 55 - 50) {
                    dropAnimationTileLineReverse.setCycleCount((int) VBox.getMargin(keySizeContainer).getTop());
                    dropAnimationTimeLine.stop();
                    dropAnimationTileLineReverse.play();
                }
                else {
                    dropAnimationTileLineReverse.setCycleCount(55);
                    leftVBox.getChildren().removeAll(Arrays.asList(AESContainer, DESContainer));
                    VBox.setMargin(keySizeContainer, new Insets(VBox.getMargin(keySizeContainer).getTop() + 55, 0, 0, 30));
                    dropAnimationTileLineReverse.play();
                }
            }

        });

        // Add the main parent to the scene
        Scene primaryScene = new Scene(root, 725, 450);

        // Add the css stylesheet to the scene
        primaryScene.getStylesheets().add("css/StyleSheet.css");

        // Add the scene to the stage and name the stage
        this.primaryStage.setTitle("File Encryptor");
        this.primaryStage.setScene(primaryScene);
        this.primaryStage.initStyle(StageStyle.UNDECORATED);
        this.primaryStage.show(); // Show the stage

        // Request the focus so the interface is nice and unfocused
        // for first time viewing
        windowTitle.requestFocus();
    }

    // Shutdown the thread executor when the application is closed
    public void stop() { executor.shutdownNow(); }

    // For IDE's of limited capability
    public static void main(String[] args) { launch(args); }

    private void initUI() {

        assignStyleClass();
        setVisibleProperties();
        setInteractions();
        addNodesToParents();

    }

    private void assignStyleClass() {
        root.getStyleClass().add("white-based-background");
        leftVBox.getStyleClass().add("side-panel-based-color");
        rightVBox.getStyleClass().add("white-based-backgroung");
        closeButtonSealer.getStyleClass().add("toolbar-button-sealer");
        minimizeButtonSealer.getStyleClass().add("toolbar-button-sealer");
        chooseFileToolTip.getStyleClass().add("tool-tip");
        decryptFileToolTip.getStyleClass().add("tool-tip");
        encryptFileToolTip.getStyleClass().add("tool-tip");
        keySize.getStyleClass().add("advanced-ui-controls");
        algoType.getStyleClass().add("advanced-ui-controls");
        AES.getStyleClass().add("advanced-ui-controls");
        DES.getStyleClass().add("advanced-ui-controls");
        bit128.getStyleClass().add("advanced-ui-controls");
        bit192.getStyleClass().add("advanced-ui-controls");
        bit256.getStyleClass().add("advanced-ui-controls");
        decryptButton.getStyleClass().add("main-buttons");
        decryptButtonSealer.getStyleClass().add("main-button-sealer");
        chooseFileButton.getStyleClass().add("main-buttons");
        chooseFileButtonSealer.getStyleClass().add("main-button-sealer");
        encryptButton.getStyleClass().add("main-buttons");
        encryptButtonSealer.getStyleClass().add("main-button-sealer");
        colorAddition.getStyleClass().add("main-button-content");
        colorAddition2.getStyleClass().add("main-button-content");
        lockBase1.getStyleClass().add("main-button-content");
        lockBase2.getStyleClass().add("main-button-content");
        advLabel.getStyleClass().add("advanced-ui-controls");
        lockBar1.getStyleClass().add("main-button-arcs");
        lockBar2.getStyleClass().add("main-button-arcs");
        addFileSymbol1.getStyleClass().add("main-button-content");
        addFileSymbol2.getStyleClass().add("main-button-content");
        advLabelSpacer1.getStyleClass().add("control-menu-separators");
        advLabelSpacer2.getStyleClass().add("control-menu-separators");
        topLine1.getStyleClass().add("line");
        topLine2.getStyleClass().add("line");
        topLine3.getStyleClass().add("line");
        bottomLine.getStyleClass().add("line");
        fileWindow.setId("fileWindow");
        dropFilesHere.setId("fileWindow-info-widget");
        windowTitle.setId("title-text");
        passwordField.setId("password-field");

    }

    private void setVisibleProperties() {

        chooseFileToolTip.setText("Choose files");
        decryptFileToolTip.setText("Decrypt files");
        encryptFileToolTip.setText("Encrypt files");
        passwordField.setPromptText("Password");

        topLine1.setStartX(0f);
        topLine1.setEndX(25f);
        topLine2.setStartX(0f);
        topLine2.setEndX(270f);
        topLine3.setStartX(0f);
        topLine3.setEndX(25f);
        bottomLine.setStartX(0f);
        bottomLine.setEndX(465f);

        statusCircle1.setStyle("-fx-fill: green");
        statusCircle2.setStyle("-fx-fill: green");
        statusCircle3.setStyle("-fx-fill: gray");
        statusCircle4.setStyle("-fx-fill: green");
        statusCircle5.setStyle("-fx-fill: green");
        statusCircle6.setStyle("-fx-fill: gray");
        statusCircle7.setStyle("-fx-fill: gray");

        bit128.setVisible(false);
        statusCircle5.setVisible(false);
        bit192.setVisible(false);
        statusCircle6.setVisible(false);
        bit256.setVisible(false);
        statusCircle7.setVisible(false);

        decryptButton.setRadius(25);
        decryptButtonSealer.setRadius(25);
        chooseFileButton.setRadius(30);
        chooseFileButtonSealer.setRadius(30);
        encryptButton.setRadius(25);
        encryptButtonSealer.setRadius(25);

        colorAddition.setHeight(24);
        colorAddition.setWidth(219);
        colorAddition2.setHeight(225);
        colorAddition2.setWidth(24);

        lockBase1.setWidth(29);
        lockBase1.setHeight(20);
        lockBase2.setWidth(29);
        lockBase2.setHeight(20);

        closeButton.setRadius(8);
        closeButtonSealer.setRadius(8);
        minimizeButton.setRadius(8);
        minimizeButtonSealer.setRadius(8);

        lockBar1.setLength(-140f);
        lockBar1.setStartAngle(0f);
        lockBar1.setRadiusX(11f);
        lockBar1.setRadiusY(13f);
        lockBar1.setType(ArcType.OPEN);
        lockBar1.setRotate(180f);
        lockBar2.setLength(180f);
        lockBar2.setStartAngle(0f);
        lockBar2.setRadiusX(11f);
        lockBar2.setRadiusY(12f);
        lockBar2.setType(ArcType.OPEN);

        addFileSymbol1.setStartX(40.0);
        addFileSymbol1.setEndX(1.0);
        addFileSymbol1.setStrokeWidth(9.0);
        addFileSymbol2.setStartY(40.0);
        addFileSymbol2.setEndY(1.0);
        addFileSymbol2.setStrokeWidth(9.0);

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

        leftTopHBox.setSpacing(10);
        titleBar.setSpacing(2);
        titleBar.setMinHeight(22);
        titleBar.setAlignment(Pos.TOP_LEFT);

        advLabelSpacer1.setStartX(0.0);
        advLabelSpacer1.setEndX(51);
        advLabelSpacer2.setStartX(0.0);
        advLabelSpacer2.setEndX(51);

        statusCircle1.setRadius(4f);
        statusCircle2.setRadius(4f);
        statusCircle3.setRadius(4f);
        statusCircle4.setRadius(4f);
        statusCircle5.setRadius(4f);
        statusCircle6.setRadius(4f);
        statusCircle7.setRadius(4f);

        VBox.setMargin(passwordField, new Insets(10, 20, 20, 20));
        VBox.setMargin(algoTypeContainer, new Insets(0, 0, 0, 30));
        HBox.setMargin(statusCircle1, new Insets(9, 0, 0, 0));
        VBox.setMargin(keySizeContainer, new Insets(0, 0, 0, 30));
        HBox.setMargin(statusCircle2, new Insets(9, 0, 0, 0));
        VBox.setMargin(AESContainer, new Insets(0, 0, 0, 50));
        HBox.setMargin(statusCircle3, new Insets(9, 0, 0, 0));
        VBox.setMargin(DESContainer, new Insets(0, 0, 0, 50));
        HBox.setMargin(statusCircle4, new Insets(9, 0, 0, 0));
        VBox.setMargin(bit128Container, new Insets(0, 0, 0, 50));
        HBox.setMargin(statusCircle5, new Insets(9, 0, 0, 0));
        VBox.setMargin(bit192Container, new Insets(0, 0, 0, 50));
        HBox.setMargin(statusCircle6, new Insets(9, 0, 0, 0));
        VBox.setMargin(bit256Container, new Insets(0, 0, 0, 50));
        HBox.setMargin(statusCircle7, new Insets(9, 0, 0, 0));
        VBox.setMargin(advSeperatorHBox, new Insets(0, 0, 10, 20));
        HBox.setMargin(advLabelSpacer1, new Insets(13, 0, 0, 0));
        HBox.setMargin(advLabelSpacer2, new Insets(13, 0, 0, 0));
        HBox.setMargin(windowTitle, new Insets(9, 245, 0, 0));
        HBox.setMargin(colorAddition, new Insets(0, 115, 0, 0));
        HBox.setMargin(closeButtonPane, new Insets(8, 0, 0, 0));
        HBox.setMargin(minimizeButtonPane, new Insets(8, 8, 0, 0));
        HBox.setMargin(decryptButtonPane, new Insets(10, 0, 20, 20));
        HBox.setMargin(chooseFileButtonPane, new Insets(10, 0, 20, 0));
        HBox.setMargin(encryptButtonPane, new Insets(10, 20, 20, 0));
        StackPane.setMargin(closeSymbol1, new Insets(1.4, 0, 0, 1));
        StackPane.setMargin(closeSymbol2, new Insets(1.4, 0, 0, 1));
        StackPane.setMargin(lockBase1, new Insets(13, 0, 0, 0));
        StackPane.setMargin(lockBase2, new Insets(13, 0, 0, 0));
        StackPane.setMargin(lockBar1, new Insets(0, 2, 24, 0));
        StackPane.setMargin(lockBar2, new Insets(0, 0, 22, 0));

        // VBox.setMargin(new Insets(30, 20, 15, 20));
        topBarHBox.setPadding(new Insets(30, 20, 0, 20));
        HBox.setMargin(topLine1, new Insets(7, 5, 0, 3));
        HBox.setMargin(topLine2, new Insets(7, 5, 0, 5));
        HBox.setMargin(topLine3, new Insets(7, 0, 0, 5));
        VBox.setMargin(fileWindow, new Insets(0, 20, 0, 20));
        VBox.setMargin(bottomLine, new Insets(15, 20, 0, 20));

        fileWindow.setMinHeight(340f);
        fileWindow.setMaxHeight(340f);
        fileWindow.setPrefHeight(340f);

        fileWindow.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        fileWindow.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox.setMargin(dropFilesHere, new Insets(160, 0, 0, 150));


    }

    private void setInteractions() {

        this.primaryStage.focusedProperty().addListener(e -> {
            if (primaryStage.isFocused()) {
                closeButton.setStyle("-fx-fill: red");
                minimizeButton.setStyle("-fx-fill: goldenrod");
                windowTitle.setStyle("-fx-fill: black");
            }
            else {
                closeButton.setStyle("-fx-fill: dimgrey");
                minimizeButton.setStyle("-fx-fill: dimgrey");
                windowTitle.setStyle("-fx-fill: dimgrey");
            }
        });

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

        closeButton.setStyle("-fx-fill: red");
        closeButton.setOnMouseEntered(e -> closeSymbol1.setVisible(true));
        closeButton.setOnMouseExited(e -> closeSymbol1.setVisible(false));
        closeButton.setOnMouseClicked(e -> Platform.exit());
        closeSymbol2.visibleProperty().bind(closeSymbol1.visibleProperty());
        closeButtonSealer.onMouseEnteredProperty().bind(closeButton.onMouseEnteredProperty());
        closeButtonSealer.onMouseExitedProperty().bind(closeButton.onMouseExitedProperty());
        closeButtonSealer.onMouseClickedProperty().bind(closeButton.onMouseClickedProperty());

        minimizeButton.setStyle("-fx-fill: goldenrod");
        minimizeButton.setOnMouseEntered(e -> minimizeSymbol.setVisible(true));
        minimizeButton.setOnMouseExited(e -> minimizeSymbol.setVisible(false));
        minimizeButton.setOnMouseClicked(e -> primaryStage.setIconified(true));
        minimizeButtonSealer.onMouseEnteredProperty().bind(minimizeButton.onMouseEnteredProperty());
        minimizeButtonSealer.onMouseExitedProperty().bind(minimizeButton.onMouseExitedProperty());
        minimizeButtonSealer.onMouseClickedProperty().bind(minimizeButton.onMouseClickedProperty());

        // Decrypt button functionality
        decryptButton.setOnMouseEntered(e ->
            decryptButton.setStyle(
                    "-fx-effect: dropshadow(three-pass-box, derive(whitesmoke, 20%), 10, 0, 0, 0)"
            )
        );
        decryptButton.setOnMouseExited(e -> decryptButton.setStyle("-fx-effect: null"));
        decryptButtonSealer.onMouseEnteredProperty().bind(decryptButton.onMouseEnteredProperty());
        decryptButtonSealer.onMouseExitedProperty().bind(decryptButton.onMouseExitedProperty());
        decryptButtonSealer.setOnMouseClicked(e -> {
            password = passwordField.getText();
            doDecrypt();
        });
        Tooltip.install(decryptButtonSealer, decryptFileToolTip);

        // Choose file button functionality
        chooseFileButton.setOnMouseEntered(e ->
            chooseFileButton.setStyle(
                    "-fx-effect: dropshadow(three-pass-box, derive(whitesmoke, 20%), 10, 0, 0, 0)"
            )
        );
        chooseFileButton.setOnMouseExited(e -> chooseFileButton.setStyle("-fx-effect: null"));
        chooseFileButtonSealer.onMouseEnteredProperty().bind(chooseFileButton.onMouseEnteredProperty());
        chooseFileButtonSealer.onMouseExitedProperty().bind(chooseFileButton.onMouseExitedProperty());
        chooseFileButtonSealer.setOnMouseClicked(e -> choseFile());
        Tooltip.install(chooseFileButtonSealer, chooseFileToolTip);

        // Encrypt button functionality
        encryptButton.setOnMouseEntered(e ->
            encryptButton.setStyle(
                    "-fx-effect: dropshadow(three-pass-box, derive(whitesmoke, 20%), 10, 0, 0, 0)"
            )
        );
        encryptButton.setOnMouseExited(e -> encryptButton.setStyle("-fx-effect: null;"));
        encryptButtonSealer.onMouseEnteredProperty().bind(encryptButton.onMouseEnteredProperty());
        encryptButtonSealer.onMouseExitedProperty().bind(encryptButton.onMouseExitedProperty());
        encryptButtonSealer.setOnMouseClicked(e -> {
            password = passwordField.getText();
            doEncrypt();
        });
        Tooltip.install(encryptButtonSealer, encryptFileToolTip);

        AES.setOnMouseClicked(e -> {

            algorithm = aes1;
            algoSpec = "AES";
            statusCircle1.setStyle("-fx-fill: green");
            statusCircle2.setStyle("-fx-fill: green");
            statusCircle3.setStyle("-fx-fill: gray");
            if (! (keyStrength == 128 ||  keyStrength == 256)) {

                keyStrength = null;
                statusCircle4.setStyle("-fx-fill: orange");
                statusCircle5.setStyle("-fx-fill: orange");
                statusCircle6.setStyle("-fx-fill: gray");
                statusCircle7.setStyle("-fx-fill: orange");

            }

        });

        DES.setOnMouseClicked(e -> {

            algorithm = desede1;
            algoSpec = "DESede";
            statusCircle1.setStyle("-fx-fill: green");
            statusCircle2.setStyle("-fx-fill: gray");
            keyStrength = 192;
            statusCircle3.setStyle("-fx-fill: green");
            statusCircle4.setStyle("-fx-fill: green");
            statusCircle5.setStyle("-fx-fill: gray");
            statusCircle6.setStyle("-fx-fill: green");
            statusCircle7.setStyle("-fx-fill: gray");

        });

        bit128.setOnMouseClicked(e -> {

            keyStrength = 128;
            algorithm = aes1;
            algoSpec = "AES";
            statusCircle1.setStyle("-fx-fill: green");
            statusCircle2.setStyle("-fx-fill: green");
            statusCircle3.setStyle("-fx-fill: gray");
            statusCircle4.setStyle("-fx-fill: green");
            statusCircle5.setStyle("-fx-fill: green");
            statusCircle6.setStyle("-fx-fill: gray");
            statusCircle7.setStyle("-fx-fill: orange");

        });

        bit192.setOnMouseClicked(e -> {

            keyStrength = 192;
            algorithm = desede1;
            algoSpec = "DESede";
            statusCircle1.setStyle("-fx-fill: green");
            statusCircle2.setStyle("-fx-fill: gray");
            statusCircle3.setStyle("-fx-fill: green");
            statusCircle4.setStyle("-fx-fill: green");
            statusCircle5.setStyle("-fx-fill: gray");
            statusCircle6.setStyle("-fx-fill: green");
            statusCircle7.setStyle("-fx-fill: gray");

        });

        bit256.setOnMouseClicked(e -> {

            keyStrength = 256;
            algorithm = aes1;
            algoSpec = "AES";
            statusCircle1.setStyle("-fx-fill: green");
            statusCircle2.setStyle("-fx-fill: green");
            statusCircle3.setStyle("-fx-fill: gray");
            statusCircle4.setStyle("-fx-fill: green");
            statusCircle5.setStyle("-fx-fill: orange");
            statusCircle6.setStyle("-fx-fill: gray");
            statusCircle7.setStyle("-fx-fill: green");

        });

        // Encryption Strength dropdown
        keySize.setOnMouseClicked(e -> {
            if (bit128.isVisible() && bit192.isVisible() && bit256.isVisible()) {
                bit128.setVisible(false);
                statusCircle5.setVisible(false);
                bit192.setVisible(false);
                statusCircle6.setVisible(false);
                bit256.setVisible(false);
                statusCircle7.setVisible(false);
            }
            else {
                bit128.setVisible(true);
                statusCircle5.setVisible(true);
                bit192.setVisible(true);
                statusCircle6.setVisible(true);
                bit256.setVisible(true);
                statusCircle7.setVisible(true);
            }
        });

        fileWindow.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasFiles()) {
                e.acceptTransferModes(TransferMode.COPY);
            }
            else {
                e.consume();
            }
        });

        fileWindow.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean sucess = false;
            if (db.hasFiles()) {
                sucess = true;

                if (fileWindowContents.getChildren().contains(dropFilesHere))
                    fileWindowContents.getChildren().remove(dropFilesHere);

                int startingLocation = inputFiles.size();
                inputFiles.addAll(db.getFiles());

                do {
                    fileWidgets.add(new FileWidgetWithProgressBar(inputFiles.get(startingLocation)));
                    fileWindowContents.getChildren().add(fileWidgets.get(startingLocation++));

                } while (startingLocation < inputFiles.size());
            }

            e.setDropCompleted(sucess);
            e.consume();

        });

    }

    private void addNodesToParents() {

        root.getChildren().addAll(titleBar, mainScene);

        // Title bar
        titleBar.getChildren().addAll(colorAddition, windowTitle, minimizeButtonPane, closeButtonPane);
        closeButtonPane.getChildren().addAll(closeButton, closeSymbol1, closeSymbol2, closeButtonSealer);
        minimizeButtonPane.getChildren().addAll(minimizeButton, minimizeSymbol, minimizeButtonSealer);

        // Main window (under the title bar)
        mainScene.getChildren().addAll(leftVBox, rightVBox);

        // Left VBox
        leftVBox.getChildren().addAll(leftTopHBox, passwordField, advSeperatorHBox,
                algoTypeContainer, keySizeContainer, bit128Container,
                bit192Container, bit256Container, colorAddition2);
        algoTypeContainer.getChildren().addAll(statusCircle1, algoType);
        AESContainer.getChildren().addAll(statusCircle2, AES);
        DESContainer.getChildren().addAll(statusCircle3, DES);
        keySizeContainer.getChildren().addAll(statusCircle4, keySize);
        bit128Container.getChildren().addAll(statusCircle5, bit128);
        bit192Container.getChildren().addAll(statusCircle6, bit192);
        bit256Container.getChildren().addAll(statusCircle7, bit256);
        leftTopHBox.getChildren().addAll(decryptButtonPane, chooseFileButtonPane, encryptButtonPane);
        decryptButtonPane.getChildren().addAll(decryptButton, lockBar1, lockBase1, decryptButtonSealer);
        chooseFileButtonPane.getChildren().addAll(chooseFileButton, addFileSymbol1,
                addFileSymbol2, chooseFileButtonSealer);
        encryptButtonPane.getChildren().addAll(encryptButton, lockBar2, lockBase2, encryptButtonSealer);
        advSeperatorHBox.getChildren().addAll(advLabelSpacer1, advLabel, advLabelSpacer2);

        // Right VBox
        rightVBox.getChildren().addAll(topBarHBox, fileWindow, bottomLine);
        topBarHBox.getChildren().addAll(topLine1, fileNameText, topLine2, fileSizeText, topLine3);
        fileWindow.setContent(fileWindowContents);
        fileWindowContents.getChildren().add(dropFilesHere);

    }

    // Implementation for the choose file button
    private void choseFile() {

        FileChooser fileChooser = new FileChooser();

        // File chooser object, this opens a graphical file chooser for the user
        fileChooser.setTitle("Open File to Encrypt");

        // make a second stage for the file chooser, so it can't take over the primary stage
        Stage secondaryStage = new Stage(StageStyle.UTILITY);

        int startingLocation = 0;

        // Try and get a file from the file chooser, fails if the user cancels choosing a file
        try {
                startingLocation = inputFiles.size();
                inputFiles.addAll(fileChooser.showOpenMultipleDialog(secondaryStage));

        } catch (Exception fileChooserException) {
            // Do nothing

        } finally {
            if (! inputFiles.isEmpty()) {

                if (fileWindowContents.getChildren().contains(dropFilesHere))
                    fileWindowContents.getChildren().remove(dropFilesHere);

                do {
                    fileWidgets.add(new FileWidgetWithProgressBar(inputFiles.get(startingLocation)));
                    fileWindowContents.getChildren().add(fileWidgets.get(startingLocation++));

                } while (startingLocation < inputFiles.size());
            }
        }
    }

    private File determineOutFile(boolean encrypt, File inputFile) {

        File outputFile = null;

        if (encrypt) {
            if (inputFile.getName().contains(".decrypted")) {
                String fileName = inputFile.getName(),
                        filePath = inputFile.getParent() + "/",
                        newFile = filePath + fileName.replace(".decrypted", ".encrypted");

                outputFile = new File(newFile);

            }

            else {
                outputFile = new File(inputFile.getAbsolutePath() + ".encrypted");
            }

        }

        else {

            if (!inputFile.toString().contains(".encrypted"))
                throw new IllegalArgumentException("Not a file encrypted by this program");

            String fileName = inputFile.getName(),
                   filePath = inputFile.getParent() + "/",
                   newFile = filePath + fileName.replace(".encrypted", ".decrypted");

            outputFile = new File(newFile);

        }

        return outputFile;
    }

    private void doFinalCrypAction() {

        // Remove files behind the scene quickly cuz spam clickers
        inputFiles.remove(0);
        fileCount--;

        // Animation to run
        EventHandler<ActionEvent> removeFileWidget = y -> {
            try {
                // Remove graphical items
                fileWindowContents.getChildren().remove(0);
                fileWidgets.remove(0);

            } catch (IndexOutOfBoundsException e) {
                // Do nothing

            }
        };

        // Duration before running animation
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(750), removeFileWidget));
        // How many times to run the animation (0 = 1)
        timeline.setCycleCount(0);
        // Play the animation
        timeline.play();

    }

    // Encrypt method button implementation for the encrypt button.
    // This method also determines the name of the file to write/ read from
    private void doEncrypt() {

        // Advanced section probs
        if (keyStrength == null)
            return;

        // Prevent spam clickers!
        if (fileCount > 0)
            return;

        fileCount = inputFiles.size();

        // For each file the user has entered, encrypt and bind the progress to the encryption progress
        for (int count = 0 ; count < inputFiles.size() ; count++) {
            DoEncryption encryptTask = new DoEncryption(password, algorithm, algoSpec, keyStrength,
                    inputFiles.get(count), determineOutFile(true, inputFiles.get(count)));

            // Bind progress to encryption
            fileWidgets.get(count).getProgressProperty().bind(encryptTask.progressProperty());

            // Remember to remove the files after action
            encryptTask.setOnSucceeded(e -> doFinalCrypAction());

            // MultiThread to keep GUI alive!
            executor.execute(encryptTask);
        }
    }

    private void doDecrypt() {

        // Advanced section probs
        if (keyStrength == null)
            return;

        // Prevent spam clickers!
        if (fileCount > 0)
            return;

        fileCount = inputFiles.size();

        // For each file the user has entered, decrypt and bind the progress to the decryption progress
        for (int count = 0 ; count < inputFiles.size() ; count++) {
            DoDecryption decryptTask = new DoDecryption(password, algorithm, algoSpec, keyStrength,
                    inputFiles.get(count), determineOutFile(false, inputFiles.get(count)));

            // Bind progress to decryption
            fileWidgets.get(count).getProgressProperty().bind(decryptTask.progressProperty());

            // Remember to remove the files after action
            decryptTask.setOnSucceeded(e -> doFinalCrypAction());

            // MultiThread to keep GUI alive!
            executor.execute(decryptTask);

        }
    }

}


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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import javax.tools.Tool;
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
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

    // ArrayList to hold nodes
    private ArrayList<Node> nodeArrayList = new ArrayList<>();

    // BorderPane
    private BorderPane borderPane = new BorderPane();

    // GridPanes
    private GridPane centerGridPane = new GridPane();

    // Tool Bar
    private ToolBar toolBar = new ToolBar();

    // Hbox
    private HBox buttonHbox = new HBox(),
                 algoRowHbox = new HBox(),
                 choseFileRowHbox = new HBox(),
                 passwordFieldHbox = new HBox(),
                 modeHbox = new HBox();

    // Buttons
    private Button buttonEncrypt = new Button("Encrypt"),
                   buttonDecrypt = new Button("Decrypt"),
                   choseFileButton = new Button("Choose File");

    // ComboBox
    private ComboBox algoComboBox = new ComboBox(),
                     keyStrengthComboBox = new ComboBox(),
                     modeComboBox = new ComboBox();

    // File Chooser
    private FileChooser fileChooser;

    // Progress Bar
    private ProgressBar progressBar = new ProgressBar(0.0);

    // Text labels and such
    private Font defaultFont = new Font(15);
    private Text statuslbl = new Text(),
                 algoType = new Text("Algo:"),
                 modeType = new Text("Mode:");

    // File object
    private File inputFile = null;

    // Text fields
    private PasswordField passwordField = new PasswordField();
    private TextField fileField = new TextField();

    private Integer keyStrength;
    private Double mouseDragStartX, mouseDragStartY;

    // String items
    private String aes1 = "AES/CBC/PKCS5Padding",
                   desede1 = "DESede/CBC/PKCS5Padding",
                   algoSpec,
                   algorithm,
                   password,
                   mode1 = "Simple Mode",
                   mode2 = "Advanced Mode";

    private static int dropAnimationCount = 0;

    // Threads
    private ScheduledFuture resetLblThread = null;
    // Dont need a resetFieldsThread because that ends to fast to be needed.


    private boolean isSimpleMode = true;

    // 0-arg constructor
    public FileEncryptor() {

        // Set values to null to test later
        algoSpec = null;
        algorithm = null;
        password = null;
        keyStrength = null;

        // Add main scene components to the array list to make building the scene easier later
        nodeArrayList.addAll(Arrays.asList(
                // Texts
                statuslbl, algoType, modeType,
                // HBoxes
                modeHbox, algoRowHbox, choseFileRowHbox,
                passwordFieldHbox, buttonHbox
        ));

        // Set the default font for all the text objects
        for (Node node : nodeArrayList) {
            if (node instanceof Text) {
                ((Text) node).setFont(defaultFont);
            }
            if (node instanceof HBox) {
                ((HBox) node).setAlignment(Pos.CENTER);
            }
        }

        // Add content to the dropdowns
        algoComboBox.getItems().addAll(aes1, desede1);

        keyStrengthComboBox.getItems().addAll(128, 192, 256);

        modeComboBox.getItems().addAll(mode1, mode2);

        // Set the prompt on the password field
        passwordField.setPromptText("Password");

        // Make the file field non-editable
        fileField.setEditable(false);

        // Set prompt text for dropdowns
        algoComboBox.setPromptText("Choose Algo");
        keyStrengthComboBox.setPromptText("Key Size");
        modeComboBox.setValue(mode1);

    }

    // Shutdown the thread executor when the application is closed
    public void stop() {

        executor.shutdownNow();

    }

    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;

        // Add nodes to parents for organization and screen arangement
        buttonHbox.getChildren().addAll(buttonDecrypt, buttonEncrypt);

        // Set spacing between the nodes on the hboxes
        modeHbox.setSpacing(5.0);
        buttonHbox.setSpacing(5.0);
        algoRowHbox.setSpacing(10.0);
        choseFileRowHbox.setSpacing(5.0);
        passwordFieldHbox.setSpacing(5.0);

        // Give the buttons functionality
        choseFileButton.setOnAction(e -> choseFile());
        buttonEncrypt.setOnAction(e -> doEncrypt());
        buttonDecrypt.setOnAction(e -> doDecrypt());
        algoComboBox.setOnAction(e -> doAlgoCombBox());
        modeComboBox.setOnAction(e -> doModeCombBox());

        // Build the center scene
        centerGridPane.add(choseFileButton, 0, 2, 3, 1);
        centerGridPane.add(fileField, 4, 2, 4, 1);
        centerGridPane.add(passwordField, 1, 3, 3, 1);
        centerGridPane.add(buttonHbox,0,4,3,1);

        // Set the gaps around parent nodes in the center gridpane
        centerGridPane.setHgap(25);
        centerGridPane.setVgap(25);

        // Align the gridpanes to the center of the stage
        centerGridPane.setAlignment(Pos.CENTER);
        centerGridPane.setGridLinesVisible(true);

        VBox control = new VBox();
        HBox titleBar = new HBox();
        HBox main = new HBox();
        VBox leftVBox = new VBox();
        VBox rightVBox = new VBox();
        HBox leftTopHBox = new HBox();
        StackPane decryptButtonPane = new StackPane();
        StackPane chooseFileButtonPane = new StackPane();
        StackPane encryptButtonPane = new StackPane();
        StackPane closeButtonPane = new StackPane();
        StackPane minimizeButtonPane = new StackPane();

        Tooltip chooseFileToolTip = new Tooltip();
        chooseFileToolTip.getStyleClass().add("tool-tip");
        chooseFileToolTip.setText("Choose files");

        Tooltip decryptFileToolTip = new Tooltip();
        decryptFileToolTip.getStyleClass().add("tool-tip");
        decryptFileToolTip.setText("Decrypt files");

        Tooltip encryptFileToolTip = new Tooltip();
        encryptFileToolTip.getStyleClass().add("tool-tip");
        encryptFileToolTip.setText("Encrypt files");

        Text windowTitle = new Text("File Encryptor");
        windowTitle.setId("title-text");

        Label keySize = new Label();
        keySize.getStyleClass().add("advanced-ui-controls");
        keySize.setText("Encryption Strength");
        keySize.setDisable(true);

        Label algoType = new Label();
        algoType.getStyleClass().add("advanced-ui-controls");
        algoType.setText("Algorithm");
        algoType.setDisable(true);

        Label AES = new Label("Advanced Encryption");
        AES.getStyleClass().add("advanced-ui-controls");

        Label DES = new Label("Triple DES (DESede)");
        DES.getStyleClass().add("advanced-ui-controls");

        EventHandler<ActionEvent> animate = e -> {
            dropAnimationCount++;
            VBox.setMargin(keySize, new Insets(dropAnimationCount, 0, 0, 30));
        };

        KeyFrame kf1 = new KeyFrame(Duration.millis(4), animate);
        Timeline dropAnimationTimeLine = new Timeline(kf1);
        dropAnimationTimeLine.setCycleCount(55);
        dropAnimationTimeLine.setOnFinished(e -> {
            leftVBox.getChildren().addAll(4, Arrays.asList(AES, DES));
            VBox.setMargin(keySize, new Insets(VBox.getMargin(keySize).getTop() - 50, 0, 0, 30));
        });

        EventHandler<ActionEvent> reverse = e -> {
            dropAnimationCount--;
            VBox.setMargin(keySize, new Insets(dropAnimationCount, 0, 0, 30));
        };

        Timeline dropAnimationTileLineReverse = new Timeline(
                new KeyFrame(Duration.millis(4), reverse)
        );
        dropAnimationTileLineReverse.setCycleCount(55);

        algoType.setOnMouseClicked(e -> {
            if (VBox.getMargin(keySize).getTop() == 0) {
                dropAnimationTimeLine.play();
            }
            else {
                if (VBox.getMargin(keySize).getTop() != 55 - 50) {
                    dropAnimationTileLineReverse.setCycleCount((int) VBox.getMargin(keySize).getTop());
                    dropAnimationTimeLine.stop();
                    dropAnimationTileLineReverse.play();
                }
                else {
                    dropAnimationTileLineReverse.setCycleCount(55);
                    leftVBox.getChildren().removeAll(Arrays.asList(AES, DES));
                    VBox.setMargin(keySize, new Insets(VBox.getMargin(keySize).getTop() + 55, 0, 0, 30));
                    dropAnimationTileLineReverse.play();
                }
            }

        });

        PasswordField passwordField = new PasswordField();
        passwordField.setId("password-field");
        passwordField.setPromptText("Password");

        Circle decryptButton = new Circle();
        decryptButton.getStyleClass().add("main-buttons");
        decryptButton.setRadius(25);
        decryptButton.setOnMouseEntered(e -> {
            decryptButton.setStyle(
                    "-fx-effect: dropshadow(three-pass-box, derive(whitesmoke, 20%), 10, 0, 0, 0)"
            );
        });
        decryptButton.setOnMouseExited(e -> decryptButton.setStyle("-fx-effect: null"));
        Tooltip.install(decryptButton, decryptFileToolTip);

        Circle chooseFileButton = new Circle();
        chooseFileButton.getStyleClass().add("main-buttons");
        chooseFileButton.setRadius(30);
        chooseFileButton.setOnMouseEntered(e -> {
            chooseFileButton.setStyle(
                    "-fx-effect: dropshadow(three-pass-box, derive(whitesmoke, 20%), 10, 0, 0, 0)"
            );
        });
        chooseFileButton.setOnMouseExited(e -> chooseFileButton.setStyle("-fx-effect: null"));
        chooseFileButton.setOnMouseClicked(e -> choseFile());
        Tooltip.install(chooseFileButton, chooseFileToolTip);

        Circle encryptButton = new Circle();
        encryptButton.getStyleClass().add("main-buttons");
        encryptButton.setRadius(25);
        encryptButton.setOnMouseEntered(e -> {
            encryptButton.setStyle(
                    "-fx-effect: dropshadow(three-pass-box, derive(whitesmoke, 20%), 10, 0, 0, 0)"
            );
        });
        encryptButton.setOnMouseExited(e -> encryptButton.setStyle("-fx-effect: null;"));
        Tooltip.install(encryptButton, encryptFileToolTip);

        Rectangle colorAddition = new Rectangle();
        colorAddition.getStyleClass().add("main-button-content");
        colorAddition.setHeight(24);
        colorAddition.setWidth(219);

        Rectangle colorAddition2 = new Rectangle();
        colorAddition2.getStyleClass().add("main-button-content");
        colorAddition2.setHeight(225);
        colorAddition2.setWidth(24);

        Rectangle lockBase1 = new Rectangle();
        lockBase1.getStyleClass().add("main-button-content");
        lockBase1.setWidth(29);
        lockBase1.setHeight(20);
        lockBase1.onMouseEnteredProperty().bind(decryptButton.onMouseEnteredProperty());
        lockBase1.onMouseExitedProperty().bind(decryptButton.onMouseExitedProperty());
        Tooltip.install(lockBase1, decryptFileToolTip);

        Rectangle lockBase2 = new Rectangle();
        lockBase2.getStyleClass().add("main-button-content");
        lockBase2.setWidth(29);
        lockBase2.setHeight(20);
        lockBase2.onMouseEnteredProperty().bind(encryptButton.onMouseEnteredProperty());
        lockBase2.onMouseExitedProperty().bind(encryptButton.onMouseExitedProperty());
        Tooltip.install(lockBase2, encryptFileToolTip);

        Label modeButton = new Label();
        modeButton.setText("Advanced");
        modeButton.getStyleClass().add("advanced-ui-controls");
        modeButton.setStyle("-fx-base: #3f424d;" +
                "-fx-fill: whitesmoke;" +
                "-fx-border-color: transparent;" +
                "-fx-border-width: 5"
        );
        modeButton.setOnMouseClicked(e -> {
            if (algoType.isDisabled() && keySize.isDisabled()) {
                algoType.setDisable(false);
                keySize.setDisable(false);
            }
            else {
                algoType.setDisable(true);
                keySize.setDisable(true);
            }
        });

        Circle closeButton = new Circle();
        closeButton.setStyle("-fx-fill: red");
        closeButton.setRadius(8);
        closeButton.setOnMouseClicked(e -> Platform.exit());

        Circle minimizeButton = new Circle();
        minimizeButton.setStyle("-fx-fill: goldenrod");
        minimizeButton.setRadius(8);
        minimizeButton.setOnMouseClicked(e -> primaryStage.setIconified(true));

        Arc lockBar1 = new Arc();
        lockBar1.getStyleClass().add("main-button-arcs");
        lockBar1.setLength(-140f);
        lockBar1.setStartAngle(0f);
        lockBar1.setRadiusX(11f);
        lockBar1.setRadiusY(13f);
        lockBar1.setType(ArcType.OPEN);
        lockBar1.setRotate(180f);
        lockBar1.onMouseEnteredProperty().bind(decryptButton.onMouseEnteredProperty());
        lockBar1.onMouseExitedProperty().bind(decryptButton.onMouseExitedProperty());
        Tooltip.install(lockBar1, decryptFileToolTip);

        Arc lockBar2 = new Arc();
        lockBar2.getStyleClass().add("main-button-arcs");
        lockBar2.setLength(180f);
        lockBar2.setStartAngle(0f);
        lockBar2.setRadiusX(11f);
        lockBar2.setRadiusY(12f);
        lockBar2.setType(ArcType.OPEN);
        lockBar2.onMouseEnteredProperty().bind(encryptButton.onMouseEnteredProperty());
        lockBar2.onMouseExitedProperty().bind(encryptButton.onMouseExitedProperty());
        Tooltip.install(lockBar2, encryptFileToolTip);

        Line addFileSymbol1 = new Line();
        addFileSymbol1.getStyleClass().add("main-button-content");
        addFileSymbol1.setStartX(40.0);
        addFileSymbol1.setEndX(1.0);
        addFileSymbol1.setStrokeWidth(9.0);
        addFileSymbol1.onMouseEnteredProperty().bind(chooseFileButton.onMouseEnteredProperty());
        addFileSymbol1.onMouseExitedProperty().bind(chooseFileButton.onMouseExitedProperty());
        addFileSymbol1.onMouseClickedProperty().bind(chooseFileButton.onMouseClickedProperty());
        Tooltip.install(addFileSymbol1, chooseFileToolTip);

        Line addFileSymbol2 = new Line();
        addFileSymbol2.getStyleClass().add("main-button-content");
        addFileSymbol2.setStartY(40.0);
        addFileSymbol2.setEndY(1.0);
        addFileSymbol2.setStrokeWidth(9.0);
        addFileSymbol2.onMouseEnteredProperty().bind(chooseFileButton.onMouseEnteredProperty());
        addFileSymbol2.onMouseExitedProperty().bind(chooseFileButton.onMouseExitedProperty());
        addFileSymbol2.onMouseClickedProperty().bind(chooseFileButton.onMouseClickedProperty());
        Tooltip.install(addFileSymbol2, chooseFileToolTip);

        Line closeSymbol1 = new Line();
        closeSymbol1.setVisible(false);
        closeSymbol1.setOnMouseEntered(e -> closeSymbol1.setVisible(true));
        closeSymbol1.setOnMouseExited(e -> closeSymbol1.setVisible(false));
        closeSymbol1.setOnMouseClicked(e -> Platform.exit());
        closeSymbol1.setStartX(5.5);
        closeSymbol1.setEndX(-1.0);
        closeSymbol1.setStartY(-3.5);
        closeSymbol1.setEndY(3.0);
        closeSymbol1.setStrokeWidth(1.5);

        Line closeSymbol2 = new Line();
        closeSymbol2.setVisible(false);
        closeSymbol2.visibleProperty().bind(closeSymbol1.visibleProperty());
        closeSymbol2.onMouseEnteredProperty().bind(closeSymbol1.onMouseEnteredProperty());
        closeSymbol2.onMouseExitedProperty().bind(closeSymbol1.onMouseExitedProperty());
        closeSymbol2.setPickOnBounds(false);
        closeSymbol2.setOnMouseClicked(e -> Platform.exit());
        closeSymbol2.setStartX(5.5);
        closeSymbol2.setEndX(-1.0);
        closeSymbol2.setStartY(3.0);
        closeSymbol2.setEndY(-3.5);
        closeSymbol2.setStrokeWidth(1.5);

        Line minimizeSymbol = new Line();
        minimizeSymbol.setVisible(false);
        minimizeSymbol.setOnMouseEntered(e -> minimizeSymbol.setVisible(true));
        minimizeSymbol.setOnMouseExited(e -> minimizeSymbol.setVisible(false));
        minimizeSymbol.setOnMouseClicked(e -> Platform.exit());
        minimizeSymbol.setStartX(7.0);
        minimizeSymbol.setEndX(1.0);
        minimizeSymbol.setStrokeWidth(1.75);

        control.getChildren().addAll(titleBar, main);
        control.setStyle("-fx-background-color: whitesmoke");
        // control.setStyle("-fx-background-color: #3f424d"); // Dark theme

        main.getChildren().addAll(leftVBox, rightVBox);

        leftVBox.getChildren().addAll(leftTopHBox, passwordField, modeButton,
                algoType, keySize, colorAddition2);
        VBox.setMargin(passwordField, new Insets(10, 20, 20, 20));
        VBox.setMargin(modeButton, new Insets(0, 0, 20, 18));
        VBox.setMargin(algoType, new Insets(0, 0, 0, 30));
        VBox.setMargin(keySize, new Insets(0, 0, 0, 30));
        VBox.setMargin(AES, new Insets(0, 0, 0, 50));
        VBox.setMargin(DES, new Insets(0, 0, 0, 50));
        // vBox.setStyle("-fx-background-color: whitesmoke"); // Dark theme
        leftVBox.setStyle("-fx-background-color: #3f424d");
        leftTopHBox.getChildren().addAll(decryptButtonPane, chooseFileButtonPane, encryptButtonPane);
        leftTopHBox.setSpacing(10);

        rightVBox.setStyle("-fx-background-color: whitesmoke");

        titleBar.getChildren().addAll(colorAddition, windowTitle, minimizeButtonPane, closeButtonPane);
        titleBar.setSpacing(2);
        titleBar.setMinHeight(22);
        titleBar.setAlignment(Pos.TOP_LEFT);
        HBox.setMargin(windowTitle, new Insets(4, (700 / 3) + 15, 4, 0));
        HBox.setMargin(colorAddition, new Insets(0, 88, 0, 0));
        StackPane.setMargin(closeSymbol1, new Insets(2, 0, 0, 1));
        StackPane.setMargin(closeSymbol2, new Insets(2, 0, 0, 1));
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

        closeButtonPane.getChildren().addAll(closeButton, closeSymbol1, closeSymbol2);
        closeButton.setOnMouseEntered(e -> closeSymbol1.setVisible(true));
        closeButton.setOnMouseExited(e -> closeSymbol1.setVisible(false));
        minimizeButtonPane.getChildren().addAll(minimizeButton, minimizeSymbol);
        minimizeButton.setOnMouseEntered(e -> minimizeSymbol.setVisible(true));
        minimizeButton.setOnMouseExited(e -> minimizeSymbol.setVisible(false));
        HBox.setMargin(closeButtonPane, new Insets(4));
        HBox.setMargin(minimizeButtonPane, new Insets(4));

        decryptButtonPane.getChildren().addAll(decryptButton, lockBar1, lockBase1);
        HBox.setMargin(decryptButtonPane, new Insets(10, 0, 20, 20));
        StackPane.setMargin(lockBase1, new Insets(13, 0, 0, 0));
        StackPane.setMargin(lockBar1, new Insets(0, 2, 24, 0));

        chooseFileButtonPane.getChildren().addAll(chooseFileButton, addFileSymbol1, addFileSymbol2);
        HBox.setMargin(chooseFileButtonPane, new Insets(10, 0, 20, 0));

        encryptButtonPane.getChildren().addAll(encryptButton, lockBar2, lockBase2);
        HBox.setMargin(encryptButtonPane, new Insets(10, 20, 20, 0));
        StackPane.setMargin(lockBase2, new Insets(13, 0, 0, 0));
        StackPane.setMargin(lockBar2, new Insets(0, 0, 22, 0));

        // Add the main parent to the scene
        Scene root = new Scene(control, 700, 450);
        root.getStylesheets().add("css/StyleSheet.css");

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

        // Add the scene to the stage and name the stage
        this.primaryStage.setTitle("File Encryptor");
        this.primaryStage.setScene(root);
        this.primaryStage.initStyle(StageStyle.UNDECORATED);
        this.primaryStage.show(); // Show the stage

        // Set the width of the progress bar to the width of the gridpane
        // can only be done once the gridpane is shown on the scene
        progressBar.setPrefWidth(centerGridPane.getWidth());

        windowTitle.requestFocus();
    }

    // For IDE's of limited capability
    public static void main(String[] args) {

        launch(args);

    }


    // Implementation for the choose file button
    private void choseFile() {

        // File chooser object, this opens a graphical file chooser for the user
        fileChooser = new FileChooser();
        fileChooser.setTitle("Open File to Encrypt");

        // make a second stage for the file chooser, so it can't take over the primary stage
        Stage secondaryStage = new Stage(StageStyle.UTILITY);

        // Try and get a file from the file chooser, fails if the user cancels choosing a file
        try {
            inputFile = fileChooser.showOpenDialog(secondaryStage);

        } catch (Exception fileChooserException) {
            fileChooserException.printStackTrace(System.out);

        }

        // Make sure the file is a file and not a folder
        if (!inputFile.isFile()) {
            updateStatus("Please only select a file", true);
            inputFile = null;

        }

        // If the length of the file and its path are over the size of the file
        // text field, shorted to the filename
        else if (inputFile.getAbsoluteFile().length() > 20 ) {
            fileField.setText(inputFile.getName());

        }

        else
            fileField.setText(inputFile.getAbsolutePath());

    }

    // When the algorithm combo box selects a algo, refine the algo
    // spec box to the available algo specs based on the algorithm
    // chosen by the user
    private void doAlgoCombBox() {

        if (((String)algoComboBox.getValue()).contains("AES")) {
            keyStrengthComboBox.getItems().removeAll(128, 192, 256);
            keyStrengthComboBox.getItems().addAll(128, 256);

        }

        else {
            keyStrengthComboBox.getItems().removeAll(128, 192, 256);
            keyStrengthComboBox.getItems().addAll(192);

        }
    }

    private void doModeCombBox() {

        if (((String)modeComboBox.getValue()).contains("Simple")) {
            buildScene(true);
            isSimpleMode = true;
        }

        if (((String)modeComboBox.getValue()).contains("Advanced")) {
            buildScene(false);
            isSimpleMode = false;
        }
    }

    private void resetAnimations() {

        resetFields();
        resetLabel();

    }

    private void resetFields() {

        inputFile = null; // Reset the input file
        executor.schedule(new ResetFields(progressBar, fileField), 750, TimeUnit.MILLISECONDS);

    }

    private void resetLabel() {

        System.out.println("Queing status label reset");
        resetLblThread = executor.schedule(new ResetLabel(statuslbl), 5, TimeUnit.SECONDS);

    }

    private void updateStatus(String message, boolean reset) {

        statuslbl.setVisible(true);
        statuslbl.setText(message);

        if (reset)
            resetLabel();

    }

    private void updateStatus(String message) {

        statuslbl.setVisible(true);
        statuslbl.setText(message);

    }

    private void buildScene(Boolean simpleMode) {

        if (simpleMode) {
            for (Node node : nodeArrayList) {
                if (node instanceof HBox)
                    centerGridPane.getChildren().remove(node);
            }
            passwordFieldHbox.getChildren().remove(keyStrengthComboBox);

            // Build the scene
            centerGridPane.add(modeHbox,0,0,3,1);
            centerGridPane.add(choseFileRowHbox,0,1,3,1);
            centerGridPane.add(passwordFieldHbox, 0, 2, 3, 1);
            centerGridPane.add(buttonHbox,0,3,3,1);

        }
        else {
            for (Node node : nodeArrayList) {
                if (node instanceof HBox) {
                    centerGridPane.getChildren().remove(node);
                }
            }
            passwordFieldHbox.getChildren().add(keyStrengthComboBox);

            //Build the scene
            centerGridPane.add(modeHbox, 0, 0, 3, 1);
            centerGridPane.add(algoRowHbox, 0, 1, 3, 1);
            centerGridPane.add(choseFileRowHbox, 0, 2, 3, 1);
            centerGridPane.add(passwordFieldHbox, 0, 3, 3, 1);
            centerGridPane.add(buttonHbox, 0, 4, 3, 1);

        }

        // The combo box like to freeze after switching rebuild for some reason.
        // A focus removal and reapply fixes the issue
        if (modeComboBox.isFocused()) {
            choseFileButton.requestFocus();
            modeComboBox.requestFocus();
        }

    }

    // Test to see if all the fields have values before attempting any action or assigning memory.
    private boolean isReady(boolean isEncryptButton) {

        // This animates the status label
        if (!(resetLblThread == null)) { // if the thread holder is initialized
            // Cancel the thread even if there is no thread running
            System.out.println("Canceling status label reset");
            resetLblThread.cancel(true);

        }

        statuslbl.setVisible(false);
        statuslbl.setText("");

        // If no file is selected
        if (inputFile == null) {
            if (isEncryptButton)
                updateStatus("Please choose a file to encrypt!", true);

            else
                updateStatus("Please choose a file to decrypt!", true);

            return false;

        }

        // If the file selected has alreay been encrypted by this program
        // No support for multiple encryption
        if (isEncryptButton)
            if (inputFile.toString().contains(".encrypted")) {
                updateStatus("File already encrypted!", true);
                return false;

            }

        // If the file is not encrypted and the user is trying to decrypt
        if (!isEncryptButton)
            if (!inputFile.toString().contains(".encrypted")) {
                updateStatus("File is not encrypted!", true);
                return false;
            }

        // If no algorithm in the algo drop down has been chosen
        if (!isSimpleMode) {
            if (algoComboBox.getValue() == null) {
                if (isEncryptButton)
                    updateStatus("Please choose an algorithm to encrypt with!", true);

                else
                    updateStatus("Please choose an algorithm to decrypt with!", true);

                return false;

            }
        }

        // If no key size has been chosen in the key size drop down
        if (!isSimpleMode) {
            if (keyStrengthComboBox.getValue() == null) {
                updateStatus("Please chose a key size!", true);
                return false;

            }
        }

        // If no password has been entered
        if ((passwordField.getText().length() == 0)) {
            if (isEncryptButton)
                updateStatus("Please enter a password to encrypt the file!", true);

            else
                updateStatus("Please enter a password to decrypt the file!", true);

            return false;

        }

        // Setup the memory for the values entered by the user now that everything is verified
        setupVars();

        return true;
    }

    // Setup the memory for the user entered values
    private void setupVars() {

        password = passwordField.getText();

        if (!isSimpleMode) {
            algorithm = (String) algoComboBox.getValue();
        }
        else {
            algorithm = aes1;
        }

        // Determine the Algo spec based on the algorithm chosen by the user
        if (algorithm.contains("AES"))
            algoSpec = "AES";

        else
            algoSpec = "DESede";

        if (!isSimpleMode) {
            keyStrength = (int) keyStrengthComboBox.getValue();
        }
        else {
            keyStrength = 128;
        }

    }

    private File determineOutFile(boolean encrypt) {

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

    // Encrypt method button implementation for the encrypt button.
    // This method also determines the name of the file to write/ read from
    private void doEncrypt() {

        // Test if all the values are initialized/ assigned memory
        if (!isReady(true))
            return;

        DoEncryption encryptTask = new DoEncryption(password, algorithm, algoSpec, keyStrength,
                inputFile, determineOutFile(true));

        // Bind the progress bar to the Task progress property for a cleaner
        // and faster progress update
        progressBar.progressProperty().bind(encryptTask.progressProperty());

        encryptTask.setOnSucceeded( sucess -> {
            updateStatus("File encrypted!");
            progressBar.progressProperty().unbind();
            resetAnimations();

        });

        encryptTask.setOnFailed( failed -> {
            updateStatus("Failed to encrypt!");
            progressBar.progressProperty().unbind();
            resetAnimations();

        });

        // Do all of the method in a thread to maintain useability of the main stage
        executor.execute(encryptTask);

    }

    private void doDecrypt() {

        // Test if all the values are initialized/ assigned memory
        if (!isReady(false))
            return;

        DoDecryption decryptTask = new DoDecryption(password, algorithm, algoSpec, keyStrength,
                inputFile, determineOutFile(false));

        // Bind the progress bar to the Task progress property for a cleaner
        // and faster progress update
        progressBar.progressProperty().bind(decryptTask.progressProperty());

        decryptTask.setOnSucceeded( sucess -> {
            updateStatus("File decrypted!");
            progressBar.progressProperty().unbind();
            resetAnimations();

        });

        decryptTask.setOnFailed(y -> {
            updateStatus("Failed to decrypt file!");
            progressBar.progressProperty().unbind();
            resetAnimations();

        });

        // Do all of the method in a thread to maintain useability of the main stage
        executor.execute(decryptTask);

    }

}


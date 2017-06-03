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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.stage.StageStyle;

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
        BorderPane borderPane = new BorderPane();
        VBox leftVbox = new VBox();
        HBox leftTopHBox = new HBox();
        StackPane decryptButtonPane = new StackPane();
        StackPane chooseFileButtonPane = new StackPane();
        StackPane encryptButtonPane = new StackPane();
        StackPane closeButtonPane = new StackPane();
        StackPane minimizeButtonPane = new StackPane();

        Text windowTitle = new Text("File Encryptor");
        windowTitle.setId("title-text");
        windowTitle.setStyle("-fx-fill: #c5c7c8");

        PasswordField passwordField = new PasswordField();
        passwordField.setId("password-field");
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-background-color: whitesmoke");

        Line addFileSymbol1 = new Line();
        Line addFileSymbol2 = new Line();
        Line closeSymbol1 = new Line();
        Line closeSymbol2 = new Line();
        Line minimizeSymbol = new Line();

        addFileSymbol1.setStartX(40.0);
        addFileSymbol1.setEndX(1.0);
        addFileSymbol1.setStrokeWidth(9.0);
        addFileSymbol1.setStroke(Paint.valueOf("#3f424d"));

        addFileSymbol2.setStartY(40.0);
        addFileSymbol2.setEndY(1.0);
        addFileSymbol2.setStrokeWidth(9.0);
        addFileSymbol2.setStroke(Paint.valueOf("#3f424d"));

        closeSymbol1.setVisible(false);
        closeSymbol1.setOnMouseEntered(e -> closeSymbol1.setVisible(true));
        closeSymbol1.setOnMouseExited(e -> closeSymbol1.setVisible(false));
        closeSymbol1.setOnMouseClicked(e -> Platform.exit());
        closeSymbol1.setStartX(7.0);
        closeSymbol1.setEndX(1.0);
        closeSymbol1.setStartY(7.0);
        closeSymbol1.setEndY(1.0);
        closeSymbol1.setStrokeWidth(1.5);

        closeSymbol2.setVisible(false);
        closeSymbol2.visibleProperty().bind(closeSymbol1.visibleProperty());
        closeSymbol2.onMouseEnteredProperty().bind(closeSymbol1.onMouseEnteredProperty());
        closeSymbol2.onMouseExitedProperty().bind(closeSymbol1.onMouseExitedProperty());
        closeSymbol2.setPickOnBounds(false);
        closeSymbol2.setOnMouseClicked(e -> Platform.exit());
        closeSymbol2.setStartX(7.0);
        closeSymbol2.setEndX(1.0);
        closeSymbol2.setStartY(-7.0);
        closeSymbol2.setEndY(-1.0);
        closeSymbol2.setStrokeWidth(1.5);

        minimizeSymbol.setVisible(false);
        minimizeSymbol.setOnMouseEntered(e -> minimizeSymbol.setVisible(true));
        minimizeSymbol.setOnMouseExited(e -> minimizeSymbol.setVisible(false));
        minimizeSymbol.setOnMouseClicked(e -> Platform.exit());
        minimizeSymbol.setStartX(7.0);
        minimizeSymbol.setEndX(1.0);
        minimizeSymbol.setStrokeWidth(1.75);

        Circle decryptButton = new Circle();
        decryptButton.setRadius(25);
        decryptButton.setStyle("-fx-fill: whitesmoke");

        Circle chooseFileButton = new Circle();
        chooseFileButton.setRadius(30);
        chooseFileButton.setStyle("-fx-fill: whitesmoke");

        Circle encryptButton = new Circle();
        encryptButton.setRadius(25);
        encryptButton.setStyle("-fx-fill: whitesmoke");

        ToggleButton modeButton = new ToggleButton();
        modeButton.setText("Advanced");
        modeButton.setId("mode-toggle");
        modeButton.setStyle("-fx-base: #3f424d;" +
                "-fx-text-fill: whitesmoke");

        Circle closeButton = new Circle();
        closeButton.setStyle("-fx-fill: red");
        closeButton.setRadius(8);
        closeButton.setOnMouseEntered(e -> closeSymbol1.setVisible(true));
        closeButton.setOnMouseExited(e -> closeSymbol1.setVisible(false));
        closeButton.setOnMouseClicked(e -> Platform.exit());

        Circle minimizeButton = new Circle();
        minimizeButton.setStyle("-fx-fill: goldenrod");
        minimizeButton.setRadius(8);
        minimizeButton.setOnMouseEntered(e -> minimizeSymbol.setVisible(true));
        minimizeButton.setOnMouseExited(e -> minimizeSymbol.setVisible(false));
        minimizeButton.setOnMouseClicked(e -> primaryStage.setIconified(true));

        control.getChildren().addAll(titleBar, borderPane);
        control.setStyle("-fx-background-color: whitesmoke");
        // control.setStyle("-fx-background-color: #3f424d"); // Dark theme
        titleBar.getChildren().addAll(windowTitle, minimizeButtonPane, closeButtonPane);
        titleBar.setStyle("-fx-background-color: black");
        titleBar.setAlignment(Pos.TOP_RIGHT);
        titleBar.setSpacing(2);
        titleBar.setMinHeight(22);
        closeButtonPane.getChildren().addAll(closeButton, closeSymbol1, closeSymbol2);
        minimizeButtonPane.getChildren().addAll(minimizeButton, minimizeSymbol);
        HBox.setMargin(closeButtonPane, new Insets(4));
        HBox.setMargin(minimizeButtonPane, new Insets(4));
        HBox.setMargin(windowTitle, new Insets(4, 700/3, 4, 0));
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
        borderPane.setLeft(leftVbox);
        leftVbox.getChildren().addAll(leftTopHBox, passwordField, modeButton);
        VBox.setMargin(passwordField, new Insets(10, 20, 20, 20));
        VBox.setMargin(modeButton, new Insets(0, 20, 20 ,20));
        // vBox.setStyle("-fx-background-color: whitesmoke"); // Dark theme
        leftVbox.setStyle("-fx-background-color: #3f424d");
        leftTopHBox.getChildren().addAll(decryptButtonPane, chooseFileButtonPane, encryptButtonPane);
        leftTopHBox.setSpacing(10);
        decryptButtonPane.getChildren().addAll(decryptButton);
        chooseFileButtonPane.getChildren().addAll(chooseFileButton, addFileSymbol1, addFileSymbol2);
        encryptButtonPane.getChildren().addAll(encryptButton);
        HBox.setMargin(decryptButtonPane, new Insets(10, 0, 20, 20));
        HBox.setMargin(encryptButtonPane, new Insets(10, 0, 20, 0));
        HBox.setMargin(encryptButtonPane, new Insets(10, 20, 20, 0));

        // Add the main parent to the scene
        Scene root = new Scene(control, 700, 450);

        this.primaryStage.focusedProperty().addListener(e -> {
            if (primaryStage.isFocused()) {
                closeButton.setStyle("-fx-fill: red");
                minimizeButton.setStyle("-fx-fill: goldenrod");
                windowTitle.setStyle("-fx-fill: #c5c7c8");
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


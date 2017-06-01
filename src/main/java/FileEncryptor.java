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
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.stage.StageStyle;

import javax.crypto.Cipher;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import cryptoUtils.CryptoUtils;

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

    // GridPane
    private GridPane gridPane = new GridPane();

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
        modeHbox.getChildren().addAll(modeType, modeComboBox);
        buttonHbox.getChildren().addAll(buttonDecrypt, buttonEncrypt);
        algoRowHbox.getChildren().addAll(algoType, algoComboBox);
        choseFileRowHbox.getChildren().addAll(choseFileButton, fileField);
        passwordFieldHbox.getChildren().addAll(passwordField);

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

        // Build the scene
        gridPane.add(modeHbox,0,0,3,1);
        gridPane.add(choseFileRowHbox,0,1,3,1);
        gridPane.add(passwordFieldHbox, 0, 2, 3, 1);
        gridPane.add(buttonHbox,0,3,3,1);

        // Set the gaps around parents in the gridpane
        gridPane.setHgap(5.0);
        gridPane.setVgap(20.0);

        // Align the gridpane to the ceneter
        gridPane.setAlignment(Pos.CENTER);

        // Align and pad the borderpane
        borderPane.setPadding(new Insets(20));
        borderPane.setCenter(gridPane);
        borderPane.setBottom(progressBar);
        borderPane.setTop(statuslbl);
        BorderPane.setAlignment(statuslbl, Pos.CENTER);
        BorderPane.setAlignment(progressBar, Pos.CENTER);

        // Add the main parent to the scene
        Scene root = new Scene(borderPane, 400, 350);

        // Add the scene to the stage and name the stage
        this.primaryStage.setTitle("FileEncryptor");
        this.primaryStage.setScene(root);
        this.primaryStage.show(); // Show the stage

        // Set the width of the progress bar to the width of the gridpane
        // can only be done once the gridpane is shown on the scene
        progressBar.setPrefWidth(gridPane.getWidth());

    }

    // For IDE's of limited capability
    public static void main(String[] args) {

        launch(args);

    }

    // Test to see if all the fields have values before attempting any action or assigning memory.
    private boolean isReady(boolean isEncryptButton) {

        // This animates the status label
        if (!(resetLblThread == null)) { // if the thread holder is initialized
            // Cancel the thread even if there is no thread running
            resetLblThread.cancel(false);

        }

        statuslbl.setVisible(false);
        statuslbl.setText("");

        // If no file is selected
        if (inputFile == null) {
            statuslbl.setVisible(true);
            if (isEncryptButton)
                statuslbl.setText("Please choose a file to encrypt!");

            else
                statuslbl.setText("Please choose a file to decrypt!");

            return false;

        }

        // If the file selected has alreay been encrypted by this program
        // No support for multiple encryption
        if (isEncryptButton)
            if (inputFile.toString().contains(".encrypted")) {
                statuslbl.setVisible(true);
                statuslbl.setText("File already encrypted!");
                return false;

            }

        // If the file is not encrypted and the user is trying to decrypt
        if (!isEncryptButton)
            if (!inputFile.toString().contains(".encrypted")) {
                statuslbl.setVisible(true);
                statuslbl.setText("File is not encrypted!");
                return false;
            }

        // If no algorithm in the algo drop down has been chosen
        if (!isSimpleMode) {
            if (algoComboBox.getValue() == null) {
                statuslbl.setVisible(true);
                if (isEncryptButton)
                    statuslbl.setText("Please choose an algorithm to encrypt with!");

                else
                    statuslbl.setText("Please choose an algorithm to decrypt with!");

                return false;

            }
        }

        // If no key size has been chosen in the key size drop down
        if (!isSimpleMode) {
            if (keyStrengthComboBox.getValue() == null) {
                statuslbl.setVisible(true);
                statuslbl.setText("Please chose a key size!");
                return false;

            }
        }

        // If no password has been entered
        if ((passwordField.getText().length() == 0)) {
            statuslbl.setVisible(true);
            if (isEncryptButton)
                statuslbl.setText("Please enter a password to encrypt the file!");

            else
                statuslbl.setText("Please enter a password to decrypt the file!");

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
            statuslbl.setVisible(true);
            statuslbl.setText("Please only select a file");
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

    private void buildScene(Boolean simpleMode) {

        if (simpleMode) {
            for (Node node : nodeArrayList) {
                if (node instanceof HBox)
                    gridPane.getChildren().remove(node);
            }
            passwordFieldHbox.getChildren().remove(keyStrengthComboBox);

            // Build the scene
            gridPane.add(modeHbox,0,0,3,1);
            gridPane.add(choseFileRowHbox,0,1,3,1);
            gridPane.add(passwordFieldHbox, 0, 2, 3, 1);
            gridPane.add(buttonHbox,0,3,3,1);

        }
        else {
            for (Node node : nodeArrayList) {
                if (node instanceof HBox) {
                    gridPane.getChildren().remove(node);
                }
            }
            passwordFieldHbox.getChildren().add(keyStrengthComboBox);

            //Build the scene
            gridPane.add(modeHbox, 0, 0, 3, 1);
            gridPane.add(algoRowHbox, 0, 1, 3, 1);
            gridPane.add(choseFileRowHbox, 0, 2, 3, 1);
            gridPane.add(passwordFieldHbox, 0, 3, 3, 1);
            gridPane.add(buttonHbox, 0, 4, 3, 1);

        }

        // The combo box like to freeze after switching rebuild for some reason.
        // A focus removal and reapply fixes the issue
        if (modeComboBox.isFocused()) {
            choseFileButton.requestFocus();
            modeComboBox.requestFocus();
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

    private void resetEncryptProgress() {

        executor.schedule(new ResetFields(progressBar, fileField, inputFile), 500, TimeUnit.MILLISECONDS);

        resetLabel();

    }

    private void resetLabel() {

        resetLblThread = executor.schedule(new ResetLabel(statuslbl), 5, TimeUnit.SECONDS);

    }

    // Encrypt method button implementation for the encrypt button.
    // This method also determines the name of the file to write/ read from
    private void doEncrypt() {

        // Test if all the values are initialized/ assigned memory
        if (!isReady(true))
            return;

        Task encrypTask = new Task() {
            @Override
            protected Object call() throws Exception {

                CryptoUtils encryptFile = new CryptoUtils(password, Cipher.ENCRYPT_MODE,
                                                          algorithm, algoSpec, keyStrength);

                updateProgress(1, 3);

                File outputFile = null;

                if (inputFile.getName().contains(".decrypted")) {
                    String fileName = inputFile.getName(),
                            filePath = inputFile.getParent() + "/",
                            newFile = filePath + fileName.replace(".decrypted", ".encrypted");

                    outputFile = new File(newFile);

                }

                else {
                    outputFile = new File(inputFile.getAbsolutePath() + ".encrypted");

                }


                updateProgress(2, 3);

                encryptFile.doEncryption(encryptFile.getInitializedCipher(), inputFile, outputFile);

                updateProgress(3, 3);

                Platform.runLater( () -> {
                    statuslbl.setVisible(true);
                    statuslbl.setText("File Encrypted!");
                    progressBar.progressProperty().unbind();
                    resetEncryptProgress();

                });

                return null;
            }
        };

        // Set the status label to show that the operation failed based on the task report
        encrypTask.setOnFailed(y -> {
            statuslbl.setVisible(true);
            statuslbl.setText("Failed to encrypt file!");
            progressBar.progressProperty().unbind();
            resetEncryptProgress();

        });

        // Bind the progress bar to the Task progress property for a cleaner
        // and faster progress update
        progressBar.progressProperty().bind(encrypTask.progressProperty());

        // Do all of the method in a thread to maintain useability of the main stage
        executor.execute(encrypTask);

    }

    private void doDecrypt() {

        // Test if all the values are initialized/ assigned memory
        if (!isReady(false))
            return;

        Task decrypTask = new Task() {
            @Override
            protected Object call() throws Exception {

                CryptoUtils decryptFile = new CryptoUtils(password, Cipher.DECRYPT_MODE,
                                                          algorithm, algoSpec, keyStrength);

                updateProgress(1, 3);

                if (!inputFile.toString().contains(".encrypted"))
                    throw new IllegalArgumentException("Not a file encrypted by this program");

                String fileName = inputFile.getName(),
                       filePath = inputFile.getParent() + "/",
                       newFile = filePath + fileName.replace(".encrypted", ".decrypted");

                File outputFile = new File(newFile);

                updateProgress(2, 3);

                decryptFile.doDecryption(decryptFile.getInitializedCipher(), inputFile, outputFile);

                updateProgress(3, 3);

                Platform.runLater(() -> {
                    statuslbl.setVisible(true);
                    statuslbl.setText("File decrypted!");
                    progressBar.progressProperty().unbind();
                    resetEncryptProgress();

                });

                return null;
            }
        };

        // Set the status label to show that the operation failed based on the task report
        decrypTask.setOnFailed(y -> {
            statuslbl.setVisible(true);
            statuslbl.setText("Failed to decrypt file!");
            progressBar.progressProperty().unbind();
            resetEncryptProgress();

        });

        // Bind the progress bar to the Task progress property for a cleaner
        // and faster progress update
        progressBar.progressProperty().bind(decrypTask.progressProperty());

        // Do all of the method in a thread to maintain useability of the main stage
        executor.execute(decrypTask);

    }

}


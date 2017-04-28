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

package fileEncryptor;

import javafx.application.Application;
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
import java.util.ArrayList;
import java.util.Arrays;

import cryptoUtils.CryptoUtils;

/*
 * Description: This is a simple graphical file encryption tool
 */


public class FileEncryptor extends Application {

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
                 passwordFieldHbox = new HBox();

    // Buttons
    private Button buttonEncrypt = new Button("Encrypt"),
                   buttonDecrypt = new Button("Decrypt"),
                   choseFileButton = new Button("Choose File");

    // ComboBox
    private ComboBox algoComboBox = new ComboBox(),
                     keyStrengthComboBox = new ComboBox();

    // File Chooser
    private FileChooser fileChooser;

    // Progress Bar
    private ProgressBar progressBar = new ProgressBar(0.0);

    // Text labels and such
    private Font defaultFont = new Font(15);
    private Text statuslbl = new Text(),
                 algoType = new Text("Algo:");

    // File object
    private File inputFile = null;

    // Text fields
    private PasswordField passwordField = new PasswordField();
    private TextField fileField = new TextField();

    private Integer keyStrength;

    // Algos
    private String aes1 = "AES/CBC/PKCS5Padding",
                   desede1 = "DESede/CBC/PKCS5Padding",
                   algoSpec,
                   algorithm,
                   password;

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
                statuslbl, algoType,
                // HBoxes
                algoRowHbox, choseFileRowHbox,
                passwordFieldHbox, buttonHbox
        ));

        // Set the default font for all the text objects
        for (Node node : nodeArrayList) {
            if (node instanceof Text) {
                ((Text) node).setFont(defaultFont);
            }
        }

        // Add content to the dropdowns
        algoComboBox.getItems().addAll(aes1, desede1);

        keyStrengthComboBox.getItems().addAll(128, 192, 256);

        // Set the prompt on the password field
        passwordField.setPromptText("Password");

        // Make the file field non-editable
        fileField.setEditable(false);

        // Set prompt text for dropdowns
        algoComboBox.setPromptText("Algo to Use");
        keyStrengthComboBox.setPromptText("Key Size");

    }


    public void start(Stage primaryStage) {

        // Add nodes to parents for organization and screen arangement
        buttonHbox.getChildren().addAll(buttonDecrypt, buttonEncrypt);
        algoRowHbox.getChildren().addAll(algoType, algoComboBox);
        choseFileRowHbox.getChildren().addAll(choseFileButton, fileField);
        passwordFieldHbox.getChildren().addAll(passwordField, keyStrengthComboBox);

        // Set spacing between the nodes on the hboxes
        buttonHbox.setSpacing(5.0);
        algoRowHbox.setSpacing(10.0);
        choseFileRowHbox.setSpacing(5.0);
        passwordFieldHbox.setSpacing(5.0);

        // Give the buttons functionality
        choseFileButton.setOnAction(e -> choseFile());

        buttonEncrypt.setOnAction(e -> doEncrypt());

        buttonDecrypt.setOnAction(e -> doDecrypt());

        algoComboBox.setOnAction(e -> doAlgoCombBox());

        // Build the scene
        int count = 0;
        for (Node node : nodeArrayList) {
            if (node instanceof HBox) {
                ((HBox) node).setAlignment(Pos.CENTER);
                gridPane.add(node, 0, count++, 3, 1);

            }
        }

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
        primaryStage.setTitle("FileEncryptor");
        primaryStage.setScene(root);
        primaryStage.show(); // Show the stage

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
        if (algoComboBox.getValue() == null) {
            statuslbl.setVisible(true);
            if (isEncryptButton)
                statuslbl.setText("Please choose an algorithm to encrypt with!");

            else
                statuslbl.setText("Please choose an algorithm to decrypt with!");

            return false;

        }

        // If no key size has been chosen in the key size drop down
        if (keyStrengthComboBox.getValue() == null) {
            statuslbl.setVisible(true);
            statuslbl.setText("Please chose a key size!");
            return false;

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
    private boolean setupVars() {

        this.password = passwordField.getText();

        this.algorithm = (String)algoComboBox.getValue();

        // Determine the Algo spec based on the algorithm chosen by the user
        if (this.algorithm.contains("AES"))
            this.algoSpec = "AES";

        else
            this.algoSpec = "DESede";

        this.keyStrength = (int)keyStrengthComboBox.getValue();

        return true;

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

                return null;
            }
        };

        // Set the status label to show that the operation succeded based on the task report
        encrypTask.setOnSucceeded(t -> {
            statuslbl.setVisible(true);
            statuslbl.setText("File Encrypted!");

        });

        // Set the status label to show that the operation failed based on the task report
        encrypTask.setOnFailed(y -> {
            statuslbl.setVisible(true);
            statuslbl.setText("Failed to encrypt file!");

        });

        // Bind the progress bar to the Task progress property for a cleaner
        // and faster progress update
        progressBar.progressProperty().bind(encrypTask.progressProperty());

        // Do all of the method in a thread to maintain useability of the main stage
        new Thread(encrypTask).start();

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

                return null;
            }
        };

        // Set the status label to show that the operation succeded based on the task report
        decrypTask.setOnSucceeded(t -> {
            statuslbl.setVisible(true);
            statuslbl.setText("File decrypted!");

        });

        // Set the status label to show that the operation failed based on the task report
        decrypTask.setOnFailed(y -> {
            statuslbl.setVisible(true);
            statuslbl.setText("Failed to decrypt file!");

        });

        // Bind the progress bar to the Task progress property for a cleaner
        // and faster progress update
        progressBar.progressProperty().bind(decrypTask.progressProperty());

        // Do all of the method in a thread to maintain useability of the main stage
        new Thread(decrypTask).start();

    }

}


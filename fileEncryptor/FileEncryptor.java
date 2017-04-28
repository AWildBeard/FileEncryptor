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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.AlgorithmParameters;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;

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
    private HBox buttonHbox = new HBox();
    private HBox algoRowHbox = new HBox();
    private HBox choseFileRowHbox = new HBox();
    private HBox passwordFieldHbox = new HBox();

    // Buttons
    private Button buttonEncrypt = new Button("Encrypt");
    private Button buttonDecrypt = new Button("Decrypt");
    private Button choseFileButton = new Button("Choose File");

    // ComboBox
    private ComboBox algoComboBox = new ComboBox();
    private ComboBox keyStrengthComboBox = new ComboBox();

    // File Chooser
    private FileChooser fileChooser;

    // Progress Bar
    private ProgressBar progressBar = new ProgressBar(0.0);

    // Text labels and such
    private Font defaultFont = new Font(15);
    private Text statuslbl = new Text();
    private Text algoType = new Text("Algo:");

    // File object
    private File selectedFile = null;

    // Text fields
    private PasswordField passwordField = new PasswordField();
    private TextField fileField = new TextField();

    // Algos
    private String aes1 = "AES/CBC/PKCS5Padding",
                   desede1 = "DESede/CBC/PKCS5Padding";



    // Salt
    private byte[] salt = {((byte)69), ((byte)43), ((byte)-103),
                           ((byte)-10), ((byte)5), ((byte)-26),
                           ((byte)-98), ((byte)77)};

    public FileEncryptor() {

        nodeArrayList.addAll(Arrays.asList(
                // Texts
                statuslbl, algoType,
                // HBoxes
                algoRowHbox, choseFileRowHbox,
                passwordFieldHbox, buttonHbox
        ));

        for (Node node : nodeArrayList) {
            if (node instanceof Text) {
                ((Text) node).setFont(defaultFont);
            }
        }

        algoComboBox.getItems().addAll(
                aes1,
                desede1
        );

        keyStrengthComboBox.getItems().addAll(
                128,
                192,
                256
        );

        passwordField.setPromptText("Password");

        fileField.setEditable(false);

        algoComboBox.setPromptText("Algo to Use");

        keyStrengthComboBox.setPromptText("Key Size");
    }


    public void start(Stage primaryStage) {

        buttonHbox.getChildren().addAll(buttonDecrypt, buttonEncrypt);
        algoRowHbox.getChildren().addAll(algoType, algoComboBox);
        choseFileRowHbox.getChildren().addAll(choseFileButton, fileField);
        passwordFieldHbox.getChildren().addAll(passwordField, keyStrengthComboBox);

        buttonHbox.setSpacing(5.0);
        algoRowHbox.setSpacing(10.0);
        choseFileRowHbox.setSpacing(5.0);
        passwordFieldHbox.setSpacing(5.0);

        choseFileButton.setOnAction(e -> {

            fileChooser = new FileChooser();
            fileChooser.setTitle("Open File to Encrypt");

            try {
                selectedFile = fileChooser.showOpenDialog(primaryStage);

            } catch (Exception fileChooserException) {
                fileChooserException.printStackTrace(System.out);

            }

            if (!selectedFile.isFile()) {
                statuslbl.setVisible(true);
                statuslbl.setText("Please only select a file");
                selectedFile = null;

            }

            else if (selectedFile.getAbsoluteFile().length() > 20 ) {
                fileField.setText(selectedFile.getName());

            }

            else {
                fileField.setText(selectedFile.getAbsolutePath());

            }

        });

        buttonEncrypt.setOnAction(e -> {



            if (!isReady(true))
                return;

            try {
                encrypt(Cipher.ENCRYPT_MODE, passwordField.getText(),
                        this.selectedFile, ((String)algoComboBox.getValue()));

            } catch (Exception encryptionException) {
                encryptionException.printStackTrace(System.out);
                statuslbl.setVisible(true);
                statuslbl.setText("Failed to encrypt file!");

            }

        });

        buttonDecrypt.setOnAction(e -> {

            if (!isReady(false))
                return;

            try {
                decrypt(Cipher.DECRYPT_MODE, passwordField.getText(), this.selectedFile,
                        ((String)algoComboBox.getValue()));

            } catch (Exception encryptionException) {
                encryptionException.printStackTrace(System.out);
                statuslbl.setVisible(true);
                statuslbl.setText("Failed to decrypt file!");

            }

        });

        algoComboBox.setOnAction(e -> {

            if (((String)algoComboBox.getValue()).contains("AES")) {
                keyStrengthComboBox.getItems().removeAll(
                        128,
                        192,
                        256
                );

                keyStrengthComboBox.getItems().addAll(
                        128,
                        256
                );

            }

            else {
                keyStrengthComboBox.getItems().removeAll(
                        128,
                        192,
                        256
                );

                keyStrengthComboBox.getItems().addAll(
                        192
                );

            }

        });

        int count = 0;
        for (Node node : nodeArrayList) {
            if (node instanceof HBox) {
                ((HBox) node).setAlignment(Pos.CENTER);
                gridPane.add(node, 0, count++, 3, 1);

            }
        }

        gridPane.setHgap(5.0);
        gridPane.setVgap(20.0);

        gridPane.setAlignment(Pos.CENTER);
        borderPane.setPadding(new Insets(20));
        borderPane.setCenter(gridPane);
        borderPane.setBottom(progressBar);
        borderPane.setTop(statuslbl);
        BorderPane.setAlignment(statuslbl, Pos.CENTER);
        BorderPane.setAlignment(progressBar, Pos.CENTER);

        Scene root = new Scene(borderPane, 400, 350);

        primaryStage.setTitle("FileEncryptor");
        primaryStage.setScene(root);
        primaryStage.show();

        progressBar.setPrefWidth(gridPane.getWidth());

    }

    public static void main(String[] args) {

        launch(args);

    }

    private boolean isReady(boolean isEncryptButton) {

        statuslbl.setVisible(false);
        statuslbl.setText("");

        if (! (passwordField.getText().length() > 0)) {
            statuslbl.setVisible(true);
            if (isEncryptButton)
                statuslbl.setText("Please enter a password to encrypt the file!");

            else
                statuslbl.setText("Please enter a password to decrypt the file!");

            return false;

        }

        if (selectedFile == null) {
            statuslbl.setVisible(true);
            if (isEncryptButton)
                statuslbl.setText("Please choose a file to encrypt!");

            else
                statuslbl.setText("Please choose a file to decrypt!");

            return false;

        }

        if (keyStrengthComboBox.getValue() == null) {
            statuslbl.setVisible(true);
            statuslbl.setText("Please chose a key strength!");
            return false;

        }

        if (algoComboBox.getValue() == null) {
            statuslbl.setVisible(true);
            if (isEncryptButton)
                statuslbl.setText("Please choose an algorithm to encrypt with!");

            else
                statuslbl.setText("Please choose an algorithm to decrypt with!");

            return false;

        }

        return true;
    }

    private void encrypt(int cipherMode, String password,
                          File selectedFile, String algorithm) throws Exception {

        String actualAlgo = "AES";
        int keyStrength = (int)keyStrengthComboBox.getValue();

        if (algorithm.contains("DES")) {
            actualAlgo = "DESede";
            keyStrength = 192;

        }

        int remainingLength = 16;

        if (password.length() < remainingLength) {
            remainingLength = remainingLength + password.length();
        }

        System.out.println("Using " + algorithm + " as algorithm");
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, keyStrength);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), actualAlgo);
        Cipher cipher = Cipher.getInstance(algorithm);
        AlgorithmParameters params = cipher.getParameters();
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        cipher.init(cipherMode, secret, new IvParameterSpec(iv));

        Object streamType = new FileInputStream(selectedFile);
        BufferedInputStream inputStream = new BufferedInputStream((FileInputStream)streamType);
        progressBar.setProgress(0.2);

        byte[] fileBytes = new byte[(int) selectedFile.length()];

        inputStream.read(fileBytes);
        progressBar.setProgress(0.4);

        byte[] throwaway = new byte[(int) selectedFile.length() + remainingLength];

        for (int count = 0; count <= remainingLength ; count++) {
            throwaway[count] = (byte)11;

        }

        for (int count = remainingLength; (count - remainingLength) < fileBytes.length ; count++) {
            throwaway[count] = fileBytes[(count - remainingLength)];

        }
        progressBar.setProgress(0.6);

        byte[] encryptedFileBytes = cipher.doFinal(throwaway);
        progressBar.setProgress(0.8);

        File outputFile = new File(selectedFile.getAbsolutePath() + ".encrypted");
        System.out.println("Using " + outputFile.toString() + " as file to write to");
        streamType = new FileOutputStream(outputFile);
        BufferedOutputStream outputStream = new BufferedOutputStream((FileOutputStream)streamType);

        outputStream.write(encryptedFileBytes);
        progressBar.setProgress(1.0);

        inputStream.close();
        outputStream.flush();
        outputStream.close();

        statuslbl.setVisible(true);
        statuslbl.setText("File Encrypted!");

    }

    private void decrypt(int cipherMode, String password,
                         File selectedFile, String algorithm) throws Exception {

        String actualAlgo = "AES";
        int keyStrength = (int)keyStrengthComboBox.getValue();

        if (algorithm.contains("DES")) {
            actualAlgo = "DESede";
            keyStrength = 192;

        }

        int remainingLength = 16;

        if (password.length() < remainingLength) {
            remainingLength = remainingLength + password.length();
        }

        System.out.println("Using " + algorithm + " as algorithm");
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, keyStrength);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), actualAlgo);
        Cipher cipher = Cipher.getInstance(algorithm);

        AlgorithmParameters params = cipher.getParameters();
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();

        cipher.init(cipherMode, secret, new IvParameterSpec(iv));

        Object streamType = new FileInputStream(selectedFile);
        BufferedInputStream inputStream = new BufferedInputStream((FileInputStream)streamType);
        progressBar.setProgress(0.2);

        byte[] fileBytes = new byte[(int) selectedFile.length()];

        inputStream.read(fileBytes);
        progressBar.setProgress(0.4);


        byte[] encryptedFileBytes = cipher.doFinal(fileBytes);
        progressBar.setProgress(0.6);

        byte[] throwaway = new byte[(int) encryptedFileBytes.length - remainingLength];

        for (int count = remainingLength; count < encryptedFileBytes.length ; count++) {
            throwaway[(count - remainingLength)] = encryptedFileBytes[count];

        }
        progressBar.setProgress(0.8);

        String fileName = selectedFile.getName(),
               filePath = selectedFile.getParent() + "/",
               newFileName;

        newFileName = filePath + fileName.substring(0, fileName.length() - 10) + ".decrypted";

        File outputFile = new File(newFileName);
        System.out.println("Using " + newFileName + " as file to write to");
        streamType = new FileOutputStream(outputFile);
        BufferedOutputStream outputStream = new BufferedOutputStream((FileOutputStream)streamType);

        outputStream.write(throwaway);
        progressBar.setProgress(1.0);

        inputStream.close();
        outputStream.flush();
        outputStream.close();

        statuslbl.setVisible(true);
        statuslbl.setText("File Decrypted!");

    }

}


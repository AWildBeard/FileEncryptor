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

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.File;

public class FileWidgetWithProgressBar extends HBox {

    private File file;
    private VBox leftSide = new VBox();
    private HBox rightSide = new HBox();
    private VBox rightSideFileSize = new VBox();
    private ProgressBar fileProgress = new ProgressBar();
    private Text fileName,
            fileSizeText;


    public FileWidgetWithProgressBar(File file) {
        this.file = file;
        determinefileName(file);
        setVisualProperties();
        getFileSize();
        addNodes();

    }

    private void determinefileName(File fileName) {
        String result = fileName.getName();
        if (result.length() > 29) {
            result = fileName.getName().substring(0, 25) + "...";
        }
        this.fileName =  new Text(result);

    }

    private void setVisualProperties() {
        fileProgress.setProgress(0.0);
        fileProgress.setMinWidth(380f);
        fileProgress.setMaxWidth(380f);
        fileProgress.setPrefWidth(380f);

        VBox.setMargin(fileName, new Insets(0, 0, 0, 6));
        this.setPadding(new Insets(20, 0, 0, 0));
        rightSideFileSize.setPadding(new Insets(19, 0, 0, 3));
        rightSideFileSize.setMinWidth(70);
        rightSideFileSize.setMaxWidth(70);
        rightSideFileSize.setPrefWidth(70);

    }

    private void getFileSize() {
        double fileSize = file.length();

        String fileSizeEnding = "B";

        if (fileSize > 1000000) {
            fileSizeEnding = "mB";
            fileSize /= 1000000;
        }

        if (fileSize > 1000) {
            fileSizeEnding = "kB";
            fileSize /= 1000;
        }

        fileSizeText = new Text(String.format("%.2f%s", fileSize, fileSizeEnding));
    }

    private void addNodes() {
        this.getChildren().addAll(leftSide, rightSide);
        leftSide.getChildren().addAll(fileName, fileProgress);
        rightSide.getChildren().addAll(rightSideFileSize);
        rightSideFileSize.getChildren().add(fileSizeText);

    }

    public DoubleProperty getProgressProperty() {
        return fileProgress.progressProperty();

    }
}

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
        rightSideFileSize.setPadding(new Insets(17, 0, 0, 3));
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

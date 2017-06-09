import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.File;

public class FileWidgetWithProgressBar extends HBox {

    private VBox leftSide = new VBox();
    private HBox rightSide = new HBox();
    private VBox rightSideinfo = new VBox();
    private ProgressBar fileProgress = new ProgressBar();
    private Text statusText = new Text("Ready"),
            fileName;


    public FileWidgetWithProgressBar(File fileName) {
        determinefileName(fileName);
        setVisualProperties();
        addNodes();

    }

    private void setVisualProperties() {
        fileProgress.setProgress(0.0);
        fileProgress.setMinWidth(300f);
        fileProgress.setMaxWidth(300f);
        fileProgress.setPrefWidth(300f);

        VBox.setMargin(fileName, new Insets(0, 0, 0, 6));
        HBox.setMargin(rightSide, new Insets(0, 0, 0, 75));

        // HBox.setMargin(this, new Insets(0, 0, 0, 15));

    }

    private void addNodes() {
        this.getChildren().addAll(leftSide, rightSide);
        leftSide.getChildren().addAll(fileName, fileProgress);
        rightSide.getChildren().add(rightSideinfo);
        rightSideinfo.getChildren().addAll(statusText);

    }

    private void determinefileName(File fileName) {
        String result = fileName.getName();
        if (result.length() > 29) {
            result = fileName.getName().substring(0, 25) + "...";
        }
        this.fileName =  new Text(result);

    }

    public DoubleProperty getProgressProperty() {
        return fileProgress.progressProperty();

    }

    public void setStatusText(String newStatus) {
        statusText.setText(newStatus);
    }
}

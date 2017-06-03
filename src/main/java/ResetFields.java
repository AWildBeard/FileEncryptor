import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

import java.io.File;

public class ResetFields implements Runnable{

    private ProgressBar progressBar;
    private TextField fileField;

    public ResetFields(ProgressBar progressBar, TextField fileField) {

        this.progressBar = progressBar;
        this.fileField = fileField;

    }

    public void run() {

        progressBar.setProgress(0.0);
        fileField.setText("");

    }

}

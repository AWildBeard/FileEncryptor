import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

import java.io.File;

public class ResetFields implements Runnable{

    private ProgressBar progressBar;
    private TextField fileField;
    private File file;

    public ResetFields(ProgressBar progressBar, TextField fileField, File file) {

        this.progressBar = progressBar;
        this.fileField = fileField;
        this.file = file;

    }

    public void run() {

        progressBar.setProgress(0.0);
        fileField.setText("");
        file = null;

    }

}

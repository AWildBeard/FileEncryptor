import javafx.scene.text.Text;

public class ResetLabel implements Runnable {

    private Text lbl;

    public ResetLabel(Text lbl) {
        this.lbl = lbl;
    }

    public void run() {
        if (!Thread.currentThread().isInterrupted()) {

            lbl.setVisible(false);
            lbl.setText("");

        }
    }
}

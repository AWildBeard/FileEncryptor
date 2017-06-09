import cryptoUtils.CryptoUtils;
import javafx.concurrent.Task;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

public class DoEncryption extends Task {

    private File inputFile,
                 outputFile;

    private String password,
                   algorithm,
                   algoSpec;

    private Integer keyStrength;

    public DoEncryption(String password, String algorithm, String algoSpec,
                        Integer keyStrength, File inputFile, File outputFile) {
        this.password = password;
        this.algorithm = algorithm;
        this.algoSpec = algoSpec;
        this.keyStrength = keyStrength;
        this.inputFile = inputFile;
        this.outputFile = outputFile;

    }

    public synchronized Void call() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidParameterSpecException, InvalidKeyException,
            InvalidAlgorithmParameterException, IOException {

        updateProgress(1, 3);

        CryptoUtils encryptFile = new CryptoUtils(password, Cipher.ENCRYPT_MODE,
                algorithm, algoSpec, keyStrength);

        updateProgress(2, 3);

        encryptFile.doEncryption(encryptFile.getInitializedCipher(), inputFile, outputFile);

        updateProgress(3, 3);

        return null;

    }
}

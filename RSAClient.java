import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Scanner;

public class RSAClient {
    private static final String ALGORITHM = "RSA";
    private static final int RSA_KEY_SIZE = 2048;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream clientOut = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream clientIn = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("Welcome to the Symmetric Key Distribution Client.");

            // Generate RSA key pair for client
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(RSA_KEY_SIZE);
            KeyPair keyPair = keyGen.generateKeyPair();
            PublicKey clientPublicKey = keyPair.getPublic();
            PrivateKey clientPrivateKey = keyPair.getPrivate();

            // Send client's public key to the server
            clientOut.writeObject(Base64.getEncoder().encodeToString(clientPublicKey.getEncoded()));
            System.out.println("Requesting symmetric key from server...");

            // Receive encrypted symmetric key from the server
            String encryptedSymmetricKeyBase64 = (String) clientIn.readObject();
            byte[] encryptedSymmetricKey = Base64.getDecoder().decode(encryptedSymmetricKeyBase64);

            // Decrypt the symmetric key using the client's private key
            Cipher rsaCipher = Cipher.getInstance(ALGORITHM);
            rsaCipher.init(Cipher.DECRYPT_MODE, clientPrivateKey);
            byte[] decryptedSymmetricKey = rsaCipher.doFinal(encryptedSymmetricKey);

            System.out.println("Symmetric key received and successfully decrypted using RSA.");

            // Use the symmetric key for further encryption/decryption
            SecretKey symmetricKey = new SecretKeySpec(decryptedSymmetricKey, 0, decryptedSymmetricKey.length, "AES");

            // Example usage: Encrypt and send a message to the server
            Scanner scanner = new Scanner(System.in);
            System.out.print("Please enter the message to encrypt with symmetric key:\n> ");
            String message = scanner.nextLine();

            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, symmetricKey);
            byte[] encryptedMessage = aesCipher.doFinal(message.getBytes());
            String encryptedMessageBase64 = Base64.getEncoder().encodeToString(encryptedMessage);

            clientOut.writeObject(encryptedMessageBase64);
            System.out.println("-".repeat(30));
            System.out.println("Encrypted message sent to server: " + encryptedMessageBase64);

        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

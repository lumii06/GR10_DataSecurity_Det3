import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAServer {
    private static final String ALGORITHM = "RSA";
    private static final int RSA_KEY_SIZE = 2048;
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Starting Key Distribution Server...");
            System.out.println("Awaiting client requests for symmetric keys...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     ObjectOutputStream serverOut = new ObjectOutputStream(clientSocket.getOutputStream());
                     ObjectInputStream serverIn = new ObjectInputStream(clientSocket.getInputStream())) {

                    System.out.println("Connected to client.");

                    // Generate RSA key pair for server
                    KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
                    keyGen.initialize(RSA_KEY_SIZE);
                    KeyPair keyPair = keyGen.generateKeyPair();
                    PublicKey serverPublicKey = keyPair.getPublic();
                    PrivateKey serverPrivateKey = keyPair.getPrivate();

                    // Receive client's public key
                    String clientPublicKeyBase64 = (String) serverIn.readObject();
                    byte[] clientPublicKeyBytes = Base64.getDecoder().decode(clientPublicKeyBase64);
                    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(clientPublicKeyBytes);
                    KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
                    PublicKey clientPublicKey = keyFactory.generatePublic(keySpec);

                    // Generate a symmetric key (AES)
                    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                    keyGenerator.init(128); // AES key size can be 128, 192, or 256 bits
                    SecretKey symmetricKey = keyGenerator.generateKey();

                    // Encrypt the symmetric key with the client's public key
                    Cipher rsaCipher = Cipher.getInstance(ALGORITHM);
                    rsaCipher.init(Cipher.ENCRYPT_MODE, clientPublicKey);
                    byte[] encryptedSymmetricKey = rsaCipher.doFinal(symmetricKey.getEncoded());
                    String encryptedSymmetricKeyBase64 = Base64.getEncoder().encodeToString(encryptedSymmetricKey);

                    // Send the encrypted symmetric key to the client
                    serverOut.writeObject(encryptedSymmetricKeyBase64);
                    System.out.println("Encrypted symmetric key sent to client.");

                    // Example usage: Receive and decrypt a message from the client
                    String encryptedMessageBase64 = (String) serverIn.readObject();
                    byte[] encryptedMessage = Base64.getDecoder().decode(encryptedMessageBase64);

                    System.out.println("Encrypted message received from client: " + encryptedMessageBase64);

                    Cipher aesCipher = Cipher.getInstance("AES");
                    aesCipher.init(Cipher.DECRYPT_MODE, symmetricKey);
                    byte[] decryptedMessage = aesCipher.doFinal(encryptedMessage);
                    String decryptedMessageStr = new String(decryptedMessage);

                    System.out.println("Client received and decrypted the symmetric key successfully.");
                    System.out.println("Decrypted message: " + decryptedMessageStr);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

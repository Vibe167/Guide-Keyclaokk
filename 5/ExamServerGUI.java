import java.awt.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class ExamServerGUI extends JFrame {

    private ServerSocket serverSocket;

    private Map<String, String> qpDatabase;

    private JTextArea logArea;

    // RSA KEY PAIR
    private KeyPair keyPair;

    public ExamServerGUI() {

        qpDatabase = new HashMap<>();

        qpDatabase.put(
                "10-CS101-1",
                "Question 1: Explain RSA.\nQuestion 2: Explain Digital Signature."
        );

        qpDatabase.put(
                "10-CS301-3",
                "Question 1: Explain Authentication.\nQuestion 2: Explain Non Repudiation."
        );

        setTitle(
                "IS Lab Exam Server - RSA Digital Signature"
        );

        setSize(600, 400);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // =====================================
        // CREATE LOG AREA FIRST
        // =====================================

        logArea = new JTextArea();

        logArea.setEditable(false);

        add(
                new JScrollPane(logArea),
                BorderLayout.CENTER
        );

        // =====================================
        // GENERATE RSA KEYS
        // =====================================

        generateRSAKeys();

        JButton startBtn =
                new JButton("Start Server");

        startBtn.addActionListener(
                e -> startServer()
        );

        add(startBtn, BorderLayout.SOUTH);

        setVisible(true);
    }

    // =====================================
    // GENERATE RSA KEYS
    // =====================================

    private void generateRSAKeys() {

        try {

            KeyPairGenerator keyGen =
                    KeyPairGenerator.getInstance(
                            "RSA"
                    );

            keyGen.initialize(2048);

            keyPair =
                    keyGen.generateKeyPair();

            appendLog(
                    "RSA Key Pair Generated"
            );

        } catch (Exception ex) {

            appendLog(ex.getMessage());
        }
    }

    private void startServer() {

        new Thread(() -> {

            try {

                serverSocket =
                        new ServerSocket(8080);

                appendLog(
                        "Server Started on Port 8080"
                );

                while (true) {

                    Socket client =
                            serverSocket.accept();

                    appendLog(
                            "Client Connected"
                    );

                    handleClient(client);
                }

            } catch (Exception ex) {

                appendLog(
                        ex.getMessage()
                );
            }

        }).start();
    }

    private void handleClient(Socket client) {

        new Thread(() -> {

            try (

                    BufferedReader in =
                            new BufferedReader(
                                    new InputStreamReader(
                                            client.getInputStream()
                                    )
                            );

                    DataOutputStream out =
                            new DataOutputStream(
                                    client.getOutputStream()
                            );

            ) {

                // =====================================
                // RECEIVE CLIENT DETAILS
                // =====================================

                String cls =
                        in.readLine();

                String code =
                        in.readLine();

                String name =
                        in.readLine();

                String sem =
                        in.readLine();

                String dbKey =
                        cls + "-"
                                + code
                                + "-"
                                + sem;

                appendLog(
                        "Request Received: "
                                + dbKey
                );

                String qpContent =
                        qpDatabase.getOrDefault(
                                dbKey,
                                "ERROR: QP NOT FOUND"
                        );

                // =====================================
                // CREATE DIGITAL SIGNATURE
                // =====================================

                Signature signature =
                        Signature.getInstance(
                                "SHA256withRSA"
                        );

                signature.initSign(
                        keyPair.getPrivate()
                );

                signature.update(
                        qpContent.getBytes()
                );

                byte[] digitalSignature =
                        signature.sign();

                String signatureText =
                        Base64.getEncoder()
                                .encodeToString(
                                        digitalSignature
                                );

                // =====================================
                // ENCODE PUBLIC KEY
                // =====================================

                String publicKey =
                        Base64.getEncoder()
                                .encodeToString(
                                        keyPair.getPublic()
                                                .getEncoded()
                                );

                // =====================================
                // SEND DATA
                // =====================================

                out.writeUTF(qpContent);

                out.writeUTF(signatureText);

                out.writeUTF(publicKey);

                appendLog(
                        "Question Paper Sent"
                );

                appendLog(
                        "Digital Signature Sent"
                );

            } catch (Exception ex) {

                appendLog(
                        ex.getMessage()
                );
            }

        }).start();
    }

    private void appendLog(String msg) {

        logArea.append(msg + "\n");
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(
                ExamServerGUI::new
        );
    }
}
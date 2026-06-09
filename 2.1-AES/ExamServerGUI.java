import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.util.Base64;

public class ExamServerGUI extends JFrame {

    private ServerSocket serverSocket;
    private Map<String, String> qpDatabase;
    private JTextArea logArea;

    // AES SECRET KEY (16 bytes)
    private final String SECRET_KEY = "1234567890123456";

    public ExamServerGUI() {

        qpDatabase = new HashMap<>();

        qpDatabase.put(
                "10-CS101-1",
                "Question 1: What is Java?\nQuestion 2: Explain AES."
        );

        setTitle("AES Server");

        setSize(600, 400);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        logArea = new JTextArea();

        add(new JScrollPane(logArea), BorderLayout.CENTER);

        JButton startBtn = new JButton("Start Server");

        startBtn.addActionListener(e -> startServer());

        add(startBtn, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void startServer() {

        new Thread(() -> {

            try {

                serverSocket = new ServerSocket(8080);

                appendLog("AES Server Started");

                while (true) {

                    Socket client = serverSocket.accept();

                    handleClient(client);
                }

            } catch (Exception ex) {

                appendLog(ex.getMessage());
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

                    PrintWriter out =
                            new PrintWriter(
                                    client.getOutputStream(),
                                    true
                            );

            ) {

                String cls = in.readLine();
                String code = in.readLine();
                String name = in.readLine();
                String sem = in.readLine();

                String dbKey =
                        cls + "-" + code + "-" + sem;

                String qp =
                        qpDatabase.getOrDefault(
                                dbKey,
                                "ERROR"
                        );

                // =================================
                // AES ENCRYPTION
                // =================================

                String encrypted =
                        aesEncrypt(qp);

                out.println(encrypted);

                appendLog("Encrypted QP Sent");

            } catch (Exception ex) {

                appendLog(ex.getMessage());
            }

        }).start();
    }

    // ==========================================
    // AES ENCRYPTION METHOD
    // ==========================================

    private String aesEncrypt(String data)
            throws Exception {

        SecretKeySpec key =
                new SecretKeySpec(
                        SECRET_KEY.getBytes(),
                        "AES"
                );

        Cipher cipher =
                Cipher.getInstance("AES");

        cipher.init(
                Cipher.ENCRYPT_MODE,
                key
        );

        byte[] encryptedBytes =
                cipher.doFinal(data.getBytes());

        return Base64.getEncoder()
                .encodeToString(encryptedBytes);
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
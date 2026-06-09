import java.awt.*;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class ExamServerGUI extends JFrame {

    private ServerSocket serverSocket;

    private Map<String, String> qpDatabase;

    private JTextArea logArea;

    public ExamServerGUI() {

        qpDatabase = new HashMap<>();

        qpDatabase.put(
                "10-CS101-1",
                "Question 1: Explain SHA."
        );

        setTitle("SHA Server");

        setSize(600, 400);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        logArea = new JTextArea();

        logArea.setEditable(false);

        add(
                new JScrollPane(logArea),
                BorderLayout.CENTER
        );

        JButton startBtn =
                new JButton("Start Server");

        startBtn.addActionListener(
                e -> startServer()
        );

        add(startBtn, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void startServer() {

        new Thread(() -> {

            try {

                serverSocket =
                        new ServerSocket(8080);

                appendLog("Server Started");

                while (true) {

                    Socket client =
                            serverSocket.accept();

                    appendLog("Client Connected");

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

                    DataOutputStream out =
                            new DataOutputStream(
                                    client.getOutputStream()
                            );

            ) {

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

                String qpContent =
                        qpDatabase.getOrDefault(
                                dbKey,
                                "QP NOT FOUND"
                        );

                String hash =
                        generateSHA(qpContent);

                out.writeUTF(qpContent);

                out.writeUTF(hash);

                appendLog("Hash Sent");

            } catch (Exception ex) {

                appendLog(ex.getMessage());
            }

        }).start();
    }

    private String generateSHA(String data)
            throws Exception {

        MessageDigest md =
                MessageDigest.getInstance(
                        "SHA-256"
                );

        byte[] hashBytes =
                md.digest(data.getBytes());

        StringBuilder sb =
                new StringBuilder();

        for (byte b : hashBytes) {

            sb.append(
                    String.format("%02x", b)
            );
        }

        return sb.toString();
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
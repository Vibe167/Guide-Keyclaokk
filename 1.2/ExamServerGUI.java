import java.awt.*;
import java.io.*;
import java.net.*;
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
                "Question 1: What is Java? (5 marks)\nQuestion 2: Explain OOP. (10 marks)"
        );

        setTitle("IS Lab Exam Server - Transposition Cipher");

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

                appendLog("Server started");

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

                    DataOutputStream dos =
                            new DataOutputStream(
                                    client.getOutputStream()
                            )

            ) {

                String cls = in.readLine();
                String courseCode = in.readLine();
                String courseName = in.readLine();
                String sem = in.readLine();

                String dbKey =
                        cls + "-" + courseCode + "-" + sem;

                String qpContent =
                        qpDatabase.getOrDefault(
                                dbKey,
                                "ERROR"
                        );

                // ==================================
                // TRANSPOSITION ENCRYPTION
                // ==================================

                String encryptedQP =
                        transpositionEncrypt(qpContent, 4);

                out.println("OK");

                out.println(encryptedQP.length());

                out.println(4);

                dos.write(encryptedQP.getBytes("UTF-8"));

            } catch (Exception ex) {

                appendLog(ex.getMessage());
            }

        }).start();
    }

    // ==========================================
    // TRANSPOSITION CIPHER ENCRYPTION
    // ==========================================

    private String transpositionEncrypt(
            String text,
            int cols
    ) {

        int rows =
                (int) Math.ceil(
                        (double) text.length() / cols
                );

        char[][] matrix =
                new char[rows][cols];

        int index = 0;

        // FILL ROW-WISE

        for (int i = 0; i < rows; i++) {

            for (int j = 0; j < cols; j++) {

                if (index < text.length()) {

                    matrix[i][j] =
                            text.charAt(index++);

                } else {

                    matrix[i][j] = 'X';
                }
            }
        }

        StringBuilder cipher =
                new StringBuilder();

        // READ COLUMN-WISE

        for (int j = 0; j < cols; j++) {

            for (int i = 0; i < rows; i++) {

                cipher.append(matrix[i][j]);
            }
        }

        return cipher.toString();
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
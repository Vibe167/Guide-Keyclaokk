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

    // ==========================================
    // PLAYFAIR CIPHER KEY
    // ==========================================

    private static final String PLAYFAIR_KEY = "MONARCHY";

    public ExamServerGUI() {

        qpDatabase = new HashMap<>();

        qpDatabase.put(
                "10-CS101-1",
                "Question 1: What is Java? (5 marks)\nQuestion 2: Explain OOP. (10 marks)"
        );

        setTitle("IS Lab Exam Server - Playfair Cipher");

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
                // PLAYFAIR ENCRYPTION
                // ==================================

                String encryptedQP =
                        playfairEncrypt(qpContent, PLAYFAIR_KEY);

                out.println("OK");

                out.println(encryptedQP.length());

                out.println(PLAYFAIR_KEY);

                dos.write(encryptedQP.getBytes("UTF-8"));

            } catch (Exception ex) {

                appendLog(ex.getMessage());
            }

        }).start();
    }

    // ==========================================
    // PLAYFAIR CIPHER — HELPER: BUILD 5x5 MATRIX
    // ==========================================

    private char[][] buildPlayfairMatrix(String key) {

        // Treat I and J as the same letter (standard Playfair rule)

        boolean[] used = new boolean[26];

        StringBuilder sb = new StringBuilder();

        // Process key first

        for (char c : key.toUpperCase().toCharArray()) {

            if (c == 'J') c = 'I';

            if (Character.isLetter(c) && !used[c - 'A']) {

                used[c - 'A'] = true;

                sb.append(c);
            }
        }

        // Fill remaining letters of the alphabet

        for (char c = 'A'; c <= 'Z'; c++) {

            if (c == 'J') continue;

            if (!used[c - 'A']) {

                sb.append(c);
            }
        }

        char[][] matrix = new char[5][5];

        int idx = 0;

        for (int i = 0; i < 5; i++) {

            for (int j = 0; j < 5; j++) {

                matrix[i][j] = sb.charAt(idx++);
            }
        }

        return matrix;
    }

    // ==========================================
    // PLAYFAIR CIPHER — HELPER: FIND POSITION
    // ==========================================

    private int[] findPosition(char[][] matrix, char c) {

        if (c == 'J') c = 'I';

        for (int i = 0; i < 5; i++) {

            for (int j = 0; j < 5; j++) {

                if (matrix[i][j] == c) return new int[]{i, j};
            }
        }

        return null;
    }

    // ==========================================
    // PLAYFAIR CIPHER — HELPER: PREPARE TEXT
    // ==========================================

    private String prepareText(String text) {

        // Keep only letters, uppercase, replace J with I

        StringBuilder sb = new StringBuilder();

        for (char c : text.toUpperCase().toCharArray()) {

            if (c == 'J') c = 'I';

            if (Character.isLetter(c)) sb.append(c);
        }

        // Insert 'X' between repeated letters in a digraph

        StringBuilder result = new StringBuilder();

        int i = 0;

        while (i < sb.length()) {

            result.append(sb.charAt(i));

            if (i + 1 < sb.length()) {

                if (sb.charAt(i) == sb.charAt(i + 1)) {

                    result.append('X'); // filler between duplicates

                    i++;

                } else {

                    result.append(sb.charAt(i + 1));

                    i += 2;
                }

            } else {

                result.append('X'); // pad odd-length

                i++;
            }
        }

        return result.toString();
    }

    // ==========================================
    // PLAYFAIR CIPHER ENCRYPTION
    // ==========================================

    private String playfairEncrypt(String text, String key) {

        char[][] matrix = buildPlayfairMatrix(key);

        String prepared = prepareText(text);

        StringBuilder cipher = new StringBuilder();

        for (int i = 0; i < prepared.length(); i += 2) {

            char a = prepared.charAt(i);
            char b = prepared.charAt(i + 1);

            int[] posA = findPosition(matrix, a);
            int[] posB = findPosition(matrix, b);

            int r1 = posA[0], c1 = posA[1];
            int r2 = posB[0], c2 = posB[1];

            if (r1 == r2) {

                // Same row — shift columns right

                cipher.append(matrix[r1][(c1 + 1) % 5]);
                cipher.append(matrix[r2][(c2 + 1) % 5]);

            } else if (c1 == c2) {

                // Same column — shift rows down

                cipher.append(matrix[(r1 + 1) % 5][c1]);
                cipher.append(matrix[(r2 + 1) % 5][c2]);

            } else {

                // Rectangle rule — swap columns

                cipher.append(matrix[r1][c2]);
                cipher.append(matrix[r2][c1]);
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

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import javax.swing.*;

public class ExamClientGUI extends JFrame {

    private JTextField classField;
    private JTextField courseCodeField;
    private JTextField courseNameField;
    private JTextField semField;

    private JTextArea qpArea;

    private JButton requestBtn;
    private JButton decryptBtn;

    private String encryptedQP;

    // ==========================================
    // PLAYFAIR KEY received from server
    // ==========================================

    private String receivedKey;

    public ExamClientGUI() {

        setTitle("IS Lab Exam Client - Playfair Cipher");

        setSize(500, 500);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        JPanel inputPanel =
                new JPanel(new GridLayout(5, 2));

        inputPanel.add(new JLabel("Class:"));

        classField = new JTextField("10");

        inputPanel.add(classField);

        inputPanel.add(new JLabel("Course Code:"));

        courseCodeField = new JTextField("CS101");

        inputPanel.add(courseCodeField);

        inputPanel.add(new JLabel("Course Name:"));

        courseNameField =
                new JTextField("Programming");

        inputPanel.add(courseNameField);

        inputPanel.add(new JLabel("Semester:"));

        semField = new JTextField("1");

        inputPanel.add(semField);

        requestBtn =
                new JButton("Request Question Paper");

        requestBtn.addActionListener(e -> requestQP());

        inputPanel.add(requestBtn);

        decryptBtn =
                new JButton("Decrypt & Download");

        decryptBtn.setEnabled(false);

        decryptBtn.addActionListener(
                e -> decryptAndDownload()
        );

        inputPanel.add(decryptBtn);

        add(inputPanel, BorderLayout.NORTH);

        qpArea = new JTextArea();

        add(new JScrollPane(qpArea), BorderLayout.CENTER);

        setVisible(true);
    }

    private void requestQP() {

        new Thread(() -> {

            try (

                    Socket socket =
                            new Socket("localhost", 8080);

                    PrintWriter out =
                            new PrintWriter(
                                    socket.getOutputStream(),
                                    true
                            );

                    BufferedReader in =
                            new BufferedReader(
                                    new InputStreamReader(
                                            socket.getInputStream()
                                    )
                            );

                    DataInputStream dis =
                            new DataInputStream(
                                    socket.getInputStream()
                            )

            ) {

                out.println(classField.getText());

                out.println(courseCodeField.getText());

                out.println(courseNameField.getText());

                out.println(semField.getText());

                in.readLine(); // "OK"

                int len =
                        Integer.parseInt(in.readLine());

                receivedKey = in.readLine(); // Playfair key

                byte[] buffer = new byte[len];

                dis.readFully(buffer);

                encryptedQP =
                        new String(buffer, "UTF-8");

                qpArea.setText(
                        "Encrypted QP:\n\n"
                                + encryptedQP
                );

                decryptBtn.setEnabled(true);

            } catch (Exception ex) {

                qpArea.setText(ex.getMessage());
            }

        }).start();
    }

    private void decryptAndDownload() {

        try {

            // ====================================
            // PLAYFAIR DECRYPTION
            // ====================================

            String decrypted =
                    playfairDecrypt(
                            encryptedQP,
                            receivedKey
                    );

            qpArea.setText(
                    "DECRYPTED QP:\n\n"
                            + decrypted
            );

            Files.write(
                    Paths.get("QP.txt"),
                    decrypted.getBytes()
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Downloaded Successfully"
            );

        } catch (Exception ex) {

            qpArea.setText(ex.getMessage());
        }
    }

    // ==========================================
    // PLAYFAIR CIPHER — HELPER: BUILD 5x5 MATRIX
    // ==========================================

    private char[][] buildPlayfairMatrix(String key) {

        boolean[] used = new boolean[26];

        StringBuilder sb = new StringBuilder();

        for (char c : key.toUpperCase().toCharArray()) {

            if (c == 'J') c = 'I';

            if (Character.isLetter(c) && !used[c - 'A']) {

                used[c - 'A'] = true;

                sb.append(c);
            }
        }

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
    // PLAYFAIR CIPHER DECRYPTION
    // ==========================================

    private String playfairDecrypt(String text, String key) {

        char[][] matrix = buildPlayfairMatrix(key);

        StringBuilder plain = new StringBuilder();

        for (int i = 0; i < text.length(); i += 2) {

            char a = text.charAt(i);
            char b = text.charAt(i + 1);

            int[] posA = findPosition(matrix, a);
            int[] posB = findPosition(matrix, b);

            int r1 = posA[0], c1 = posA[1];
            int r2 = posB[0], c2 = posB[1];

            if (r1 == r2) {

                // Same row — shift columns left

                plain.append(matrix[r1][(c1 + 4) % 5]);
                plain.append(matrix[r2][(c2 + 4) % 5]);

            } else if (c1 == c2) {

                // Same column — shift rows up

                plain.append(matrix[(r1 + 4) % 5][c1]);
                plain.append(matrix[(r2 + 4) % 5][c2]);

            } else {

                // Rectangle rule — swap columns

                plain.append(matrix[r1][c2]);
                plain.append(matrix[r2][c1]);
            }
        }

        return plain.toString();
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(
                ExamClientGUI::new
        );
    }
}

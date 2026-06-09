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

    public ExamClientGUI() {

        setTitle("IS Lab Exam Client - Transposition Cipher");

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

                in.readLine();

                int len =
                        Integer.parseInt(in.readLine());

                int cols =
                        Integer.parseInt(in.readLine());

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
            // TRANSPOSITION DECRYPTION
            // ====================================

            String decrypted =
                    transpositionDecrypt(
                            encryptedQP,
                            4
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

    // =======================================
    // TRANSPOSITION DECRYPTION
    // =======================================

    private String transpositionDecrypt(
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

        // FILL COLUMN-WISE

        for (int j = 0; j < cols; j++) {

            for (int i = 0; i < rows; i++) {

                if (index < text.length()) {

                    matrix[i][j] =
                            text.charAt(index++);
                }
            }
        }

        StringBuilder plain =
                new StringBuilder();

        // READ ROW-WISE

        for (int i = 0; i < rows; i++) {

            for (int j = 0; j < cols; j++) {

                plain.append(matrix[i][j]);
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

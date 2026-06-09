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
    private JTextField keyField;

    private JTextArea qpArea;

    private JButton requestBtn;
    private JButton decryptBtn;

    private String encryptedQP;

    public ExamClientGUI() {

        setTitle("IS Lab Exam Client - Caesar Cipher");

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
                new JTextField("Intro to Programming");

        inputPanel.add(courseNameField);

        inputPanel.add(new JLabel("Semester:"));

        semField = new JTextField("1");

        inputPanel.add(semField);

        inputPanel.add(new JLabel("Decrypt Key:"));

        keyField = new JTextField("3");

        inputPanel.add(keyField);

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

        qpArea.setEditable(false);

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

                String status = in.readLine();

                if ("ERROR".equals(status)) {

                    qpArea.setText(
                            "Error: " + in.readLine()
                    );

                    return;
                }

                int len =
                        Integer.parseInt(in.readLine());

                int shift =
                        Integer.parseInt(in.readLine());

                byte[] buffer = new byte[len];

                dis.readFully(buffer);

                encryptedQP =
                        new String(buffer, "UTF-8");

                SwingUtilities.invokeLater(() -> {

                    qpArea.setText(
                            "Encrypted QP:\n\n"
                                    + encryptedQP
                    );

                    decryptBtn.setEnabled(true);

                    keyField.setText(
                            String.valueOf(shift)
                    );
                });

            } catch (Exception ex) {

                qpArea.setText(
                        "Connection error: "
                                + ex.getMessage()
                );
            }

        }).start();
    }

    private void decryptAndDownload() {

        try {

            int shift =
                    Integer.parseInt(keyField.getText());

            // ==========================
            // CAESAR CIPHER DECRYPTION
            // ==========================

            String decrypted =
                    caesarDecrypt(encryptedQP, shift);

            qpArea.setText(
                    "DECRYPTED QP:\n\n"
                            + decrypted
            );

            String filename =
                    "QP.txt";

            Files.write(
                    Paths.get(filename),
                    decrypted.getBytes()
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Downloaded Successfully"
            );

        } catch (Exception ex) {

            qpArea.setText(
                    "Decrypt error: "
                            + ex.getMessage()
            );
        }
    }

    // =======================================
    // CAESAR CIPHER DECRYPTION
    // =======================================

    private String caesarDecrypt(String text, int shift) {

        return caesarEncrypt(
                text,
                26 - (shift % 26)
        );
    }

    private String caesarEncrypt(String text, int shift) {

        StringBuilder result =
                new StringBuilder();

        for (char c : text.toCharArray()) {

            if (Character.isLetter(c)) {

                char base =
                        Character.isUpperCase(c)
                                ? 'A'
                                : 'a';

                result.append(
                        (char) (
                                (c - base + shift) % 26 + base
                        )
                );

            } else {

                result.append(c);
            }
        }

        return result.toString();
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(
                ExamClientGUI::new
        );
    }
}
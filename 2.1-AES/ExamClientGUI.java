import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.util.Base64;

public class ExamClientGUI extends JFrame {

    private JTextField classField;
    private JTextField courseCodeField;
    private JTextField courseNameField;
    private JTextField semField;

    private JTextArea qpArea;

    private JButton requestBtn;
    private JButton decryptBtn;

    private String encryptedQP;

    // SAME AES KEY
    private final String SECRET_KEY =
            "1234567890123456";

    public ExamClientGUI() {

        setTitle("AES Client");

        setSize(500, 500);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        JPanel inputPanel =
                new JPanel(new GridLayout(5, 2));

        inputPanel.add(new JLabel("Class:"));

        classField = new JTextField("10");

        inputPanel.add(classField);

        inputPanel.add(new JLabel("Course Code:"));

        courseCodeField =
                new JTextField("CS101");

        inputPanel.add(courseCodeField);

        inputPanel.add(new JLabel("Course Name:"));

        courseNameField =
                new JTextField("Programming");

        inputPanel.add(courseNameField);

        inputPanel.add(new JLabel("Semester:"));

        semField = new JTextField("1");

        inputPanel.add(semField);

        requestBtn =
                new JButton("Request QP");

        requestBtn.addActionListener(
                e -> requestQP()
        );

        inputPanel.add(requestBtn);

        decryptBtn =
                new JButton("Decrypt");

        decryptBtn.setEnabled(false);

        decryptBtn.addActionListener(
                e -> decryptAndDownload()
        );

        inputPanel.add(decryptBtn);

        add(inputPanel, BorderLayout.NORTH);

        qpArea = new JTextArea();

        add(new JScrollPane(qpArea),
                BorderLayout.CENTER);

        setVisible(true);
    }

    private void requestQP() {

        new Thread(() -> {

            try (

                    Socket socket =
                            new Socket(
                                    "localhost",
                                    8080
                            );

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

            ) {

                out.println(classField.getText());

                out.println(courseCodeField.getText());

                out.println(courseNameField.getText());

                out.println(semField.getText());

                encryptedQP = in.readLine();

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

            // =================================
            // AES DECRYPTION
            // =================================

            String decrypted =
                    aesDecrypt(encryptedQP);

            qpArea.setText(
                    "DECRYPTED QP:\n\n"
                            + decrypted
            );

            Files.write(
                    Paths.get("AES_QP.txt"),
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
    // AES DECRYPTION METHOD
    // ==========================================

    private String aesDecrypt(String encrypted)
            throws Exception {

        SecretKeySpec key =
                new SecretKeySpec(
                        SECRET_KEY.getBytes(),
                        "AES"
                );

        Cipher cipher =
                Cipher.getInstance("AES");

        cipher.init(
                Cipher.DECRYPT_MODE,
                key
        );

        byte[] decodedBytes =
                Base64.getDecoder()
                        .decode(encrypted);

        byte[] decryptedBytes =
                cipher.doFinal(decodedBytes);

        return new String(decryptedBytes);
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(
                ExamClientGUI::new
        );
    }
}
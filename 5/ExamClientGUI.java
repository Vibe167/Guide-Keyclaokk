import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.swing.*;

public class ExamClientGUI extends JFrame {

    private JTextField classField;

    private JTextField courseCodeField;

    private JTextField courseNameField;

    private JTextField semField;

    private JTextField verifyField;

    private JTextArea qpArea;

    private JButton requestBtn;

    private JButton verifyBtn;

    private String receivedQP;

    private String receivedSignature;

    private String receivedPublicKey;

    public ExamClientGUI() {

        setTitle(
                "IS Lab Exam Client - RSA Signature"
        );

        setSize(500, 500);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        JPanel inputPanel =
                new JPanel(
                        new GridLayout(5, 2)
                );

        inputPanel.add(
                new JLabel("Class:")
        );

        classField =
                new JTextField("10");

        inputPanel.add(classField);

        inputPanel.add(
                new JLabel("Course Code:")
        );

        courseCodeField =
                new JTextField("CS101");

        inputPanel.add(courseCodeField);

        inputPanel.add(
                new JLabel("Course Name:")
        );

        courseNameField =
                new JTextField(
                        "Programming"
                );

        inputPanel.add(courseNameField);

        inputPanel.add(
                new JLabel("Semester:")
        );

        semField =
                new JTextField("1");

        inputPanel.add(semField);

        inputPanel.add(
                new JLabel("Verification:")
        );

        verifyField =
                new JTextField();

        verifyField.setEditable(false);

        inputPanel.add(verifyField);

        requestBtn =
                new JButton(
                        "Request Question Paper"
                );

        requestBtn.addActionListener(
                e -> requestQP()
        );

        inputPanel.add(requestBtn);

        verifyBtn =
                new JButton(
                        "Verify Signature"
                );

        verifyBtn.setEnabled(false);

        verifyBtn.addActionListener(
                e -> verifySignature()
        );

        inputPanel.add(verifyBtn);

        add(inputPanel, BorderLayout.NORTH);

        qpArea =
                new JTextArea();

        qpArea.setEditable(false);

        add(
                new JScrollPane(qpArea),
                BorderLayout.CENTER
        );

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

                    DataInputStream in =
                            new DataInputStream(
                                    socket.getInputStream()
                            );

            ) {

                // =====================================
                // SEND CLIENT DETAILS
                // =====================================

                out.println(
                        classField.getText()
                );

                out.println(
                        courseCodeField.getText()
                );

                out.println(
                        courseNameField.getText()
                );

                out.println(
                        semField.getText()
                );

                // =====================================
                // RECEIVE DATA
                // =====================================

                receivedQP =
                        in.readUTF();

                receivedSignature =
                        in.readUTF();

                receivedPublicKey =
                        in.readUTF();

                qpArea.setText(
                        "QUESTION PAPER:\n\n"
                                + receivedQP
                                + "\n\nDIGITAL SIGNATURE:\n"
                                + receivedSignature
                );

                verifyBtn.setEnabled(true);

            } catch (Exception ex) {

                qpArea.setText(
                        ex.getMessage()
                );
            }

        }).start();
    }

    private void verifySignature() {

        try {

            // =====================================
            // REBUILD PUBLIC KEY
            // =====================================

            byte[] publicKeyBytes =
                    Base64.getDecoder()
                            .decode(
                                    receivedPublicKey
                            );

            KeyFactory keyFactory =
                    KeyFactory.getInstance(
                            "RSA"
                    );

            PublicKey publicKey =
                    keyFactory.generatePublic(
                            new X509EncodedKeySpec(
                                    publicKeyBytes
                            )
                    );

            // =====================================
            // VERIFY DIGITAL SIGNATURE
            // =====================================

            Signature signature =
                    Signature.getInstance(
                            "SHA256withRSA"
                    );

            signature.initVerify(
                    publicKey
            );

            signature.update(
                    receivedQP.getBytes()
            );

            boolean verified =
                    signature.verify(
                            Base64.getDecoder()
                                    .decode(
                                            receivedSignature
                                    )
                    );

            // =====================================
            // DISPLAY RESULT
            // =====================================

            if (verified) {

                verifyField.setText(
                        "Verified"
                );

                qpArea.append(
                        "\n\nAuthentication Successful"
                );

            } else {

                verifyField.setText(
                        "Failed"
                );

                qpArea.append(
                        "\n\nData Tampered"
                );
            }

            // =====================================
            // SAVE FILE
            // =====================================

            Files.write(
                    Paths.get("RSA_QP.txt"),
                    receivedQP.getBytes()
            );

        } catch (Exception ex) {

            qpArea.setText(
                    ex.getMessage()
            );
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(
                ExamClientGUI::new
        );
    }
}
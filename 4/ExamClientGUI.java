import java.awt.*;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import javax.swing.*;

public class ExamClientGUI extends JFrame {

    private JTextField classField;

    private JTextField courseCodeField;

    private JTextField courseNameField;

    private JTextField semField;

    private JTextField hashField;

    private JTextArea qpArea;

    private JButton requestBtn;

    private JButton verifyBtn;

    private String receivedQP;

    private String receivedHash;

    public ExamClientGUI() {

        setTitle("SHA Client");

        setSize(500, 500);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        JPanel inputPanel =
                new JPanel(
                        new GridLayout(5, 2)
                );

        inputPanel.add(new JLabel("Class:"));

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
                new JTextField("Programming");

        inputPanel.add(courseNameField);

        inputPanel.add(
                new JLabel("Semester:")
        );

        semField =
                new JTextField("1");

        inputPanel.add(semField);

        inputPanel.add(
                new JLabel("SHA Hash:")
        );

        hashField =
                new JTextField();

        hashField.setEditable(false);

        inputPanel.add(hashField);

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
                        "Verify Integrity"
                );

        verifyBtn.setEnabled(false);

        verifyBtn.addActionListener(
                e -> verifyIntegrity()
        );

        inputPanel.add(verifyBtn);

        add(inputPanel, BorderLayout.NORTH);

        qpArea = new JTextArea();

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

                receivedQP =
                        in.readUTF();

                receivedHash =
                        in.readUTF();

                qpArea.setText(
                        "QUESTION PAPER:\n\n"
                                + receivedQP
                );

                hashField.setText(
                        receivedHash
                );

                verifyBtn.setEnabled(true);

            } catch (Exception ex) {

                qpArea.setText(
                        ex.getMessage()
                );
            }

        }).start();
    }

    private void verifyIntegrity() {

        try {

            String newHash =
                    generateSHA(receivedQP);

            if (newHash.equals(receivedHash)) {

                qpArea.append(
                        "\n\nIntegrity Verified"
                );

            } else {

                qpArea.append(
                        "\n\nData Tampered"
                );
            }

        } catch (Exception ex) {

            qpArea.setText(
                    ex.getMessage()
            );
        }
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

    public static void main(String[] args) {

        SwingUtilities.invokeLater(
                ExamClientGUI::new
        );
    }
}
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.file.*;
import java.util.Random;
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

    // =====================================
    // DIFFIE HELLMAN PUBLIC VALUES
    // =====================================

    private final BigInteger p =
            new BigInteger("23");

    private final BigInteger g =
            new BigInteger("5");

    public ExamClientGUI() {

        setTitle(
                "IS Lab Exam Client - Diffie Hellman"
        );

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
                new JTextField(
                        "Intro to Programming"
                );

        inputPanel.add(courseNameField);

        inputPanel.add(new JLabel("Semester:"));

        semField =
                new JTextField("1");

        inputPanel.add(semField);

        inputPanel.add(
                new JLabel("Shared Key:")
        );

        keyField =
                new JTextField();

        keyField.setEditable(false);

        inputPanel.add(keyField);

        requestBtn =
                new JButton(
                        "Request Question Paper"
                );

        requestBtn.addActionListener(
                e -> requestQP()
        );

        inputPanel.add(requestBtn);

        decryptBtn =
                new JButton(
                        "Download QP"
                );

        decryptBtn.setEnabled(false);

        decryptBtn.addActionListener(
                e -> downloadQP()
        );

        inputPanel.add(decryptBtn);

        add(inputPanel, BorderLayout.NORTH);

        qpArea = new JTextArea();

        qpArea.setEditable(false);

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
                // DIFFIE HELLMAN CLIENT SIDE
                // =====================================

                Random random =
                        new Random();

                // CLIENT PRIVATE KEY

                int a =
                        random.nextInt(10) + 1;

                qpArea.append(
                        "Client Private Key a = "
                                + a + "\n"
                );

                // CLIENT PUBLIC KEY
                // A = g^a mod p

                BigInteger A =
                        g.pow(a).mod(p);

                qpArea.append(
                        "Client Public Key A = "
                                + A + "\n"
                );

                // SEND CLIENT PUBLIC KEY

                out.println(A);

                // RECEIVE SERVER PUBLIC KEY

                BigInteger B =
                        new BigInteger(
                                in.readLine()
                        );

                qpArea.append(
                        "Received Server Public Key B = "
                                + B + "\n"
                );

                // SHARED SECRET KEY
                // K = B^a mod p

                BigInteger sharedKey =
                        B.pow(a).mod(p);

                qpArea.append(
                        "Shared Secret Key = "
                                + sharedKey + "\n\n"
                );

                keyField.setText(
                        sharedKey.toString()
                );

                // RECEIVE QUESTION PAPER

                String qp =
                        in.readLine();

                qpArea.append(
                        "QUESTION PAPER:\n\n"
                                + qp
                );

                decryptBtn.setEnabled(true);

            } catch (Exception ex) {

                qpArea.setText(
                        "Connection error: "
                                + ex.getMessage()
                );
            }

        }).start();
    }

    private void downloadQP() {

        try {

            String filename =
                    "DH_QP.txt";

            Files.write(
                    Paths.get(filename),
                    qpArea.getText().getBytes()
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Downloaded: "
                            + filename
            );

        } catch (Exception ex) {

            qpArea.setText(
                    "Download error: "
                            + ex.getMessage()
            );
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(
                ExamClientGUI::new
        );
    }
}
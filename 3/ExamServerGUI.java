import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.Random;
import javax.swing.*;

public class ExamServerGUI extends JFrame {

    private JTextArea logArea;

    private ServerSocket serverSocket;

    // PUBLIC VALUES
    private final BigInteger p =
            new BigInteger("23");

    private final BigInteger g =
            new BigInteger("5");

    public ExamServerGUI() {

        setTitle("Diffie Hellman Server");

        setSize(500, 400);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        logArea = new JTextArea();

        logArea.setEditable(false);

        add(new JScrollPane(logArea),
                BorderLayout.CENTER);

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

                    PrintWriter out =
                            new PrintWriter(
                                    client.getOutputStream(),
                                    true
                            );

            ) {

                // ===================================
                // SERVER PRIVATE KEY
                // ===================================

                Random random = new Random();

                int b =
                        random.nextInt(10) + 1;

                appendLog(
                        "Server Private Key b = "
                                + b
                );

                // ===================================
                // SERVER PUBLIC KEY
                // B = g^b mod p
                // ===================================

                BigInteger B =
                        g.pow(b).mod(p);

                appendLog(
                        "Server Public Key B = "
                                + B
                );

                // RECEIVE CLIENT PUBLIC KEY

                BigInteger A =
                        new BigInteger(
                                in.readLine()
                        );

                appendLog(
                        "Received Client Public Key A = "
                                + A
                );

                // SEND SERVER PUBLIC KEY

                out.println(B);

                // ===================================
                // SHARED SECRET KEY
                // K = A^b mod p
                // ===================================

                BigInteger sharedKey =
                        A.pow(b).mod(p);

                appendLog(
                        "Shared Secret Key = "
                                + sharedKey
                );

            } catch (Exception ex) {

                appendLog(ex.getMessage());
            }

        }).start();
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
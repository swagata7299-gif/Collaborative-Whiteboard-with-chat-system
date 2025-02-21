package src;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClient extends JFrame {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private JTextArea messageArea;
    private JTextField inputField;
    private JButton sendButton;
    private String username;

    public ChatClient() {
        String serverAddress = JOptionPane.showInputDialog(this, "Enter server IP address:");
        try {
            socket = new Socket(serverAddress, 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            username = JOptionPane.showInputDialog(this, "Enter your username:");
            out.println(username);

            setTitle("Chat Client - " + username);
            setSize(400, 300);
            setDefaultCloseOperation(EXIT_ON_CLOSE);

            messageArea = new JTextArea();
            messageArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(messageArea);

            inputField = new JTextField();
            inputField.addActionListener(e -> sendMessage());

            sendButton = new JButton("Send");
            sendButton.addActionListener(e -> sendMessage());

            JButton privateButton = new JButton("Private Message");
            privateButton.addActionListener(e -> sendPrivateMessage());

            JPanel inputPanel = new JPanel(new BorderLayout());
            inputPanel.add(inputField, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);
            inputPanel.add(privateButton, BorderLayout.WEST);

            add(scrollPane, BorderLayout.CENTER);
            add(inputPanel, BorderLayout.SOUTH);

            new Thread(() -> {
                String message;
                try {
                    while ((message = in.readLine()) != null) {
                        if ("/disconnect".equals(message)) {
                            JOptionPane.showMessageDialog(this, "You have been disconnected from the server.");
                            break;
                        }
                        messageArea.append(message + "\n");
                        messageArea.setCaretPosition(messageArea.getDocument().getLength());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the server.");
            System.exit(1);
        }
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            inputField.setText("");
        }
    }

    private void sendPrivateMessage() {
        String recipient = JOptionPane.showInputDialog(this, "Enter recipient's username:");
        if (recipient != null && !recipient.isEmpty()) {
            String message = inputField.getText().trim();
            if (!message.isEmpty()) {
                out.println("/private " + recipient + " " + message);
                inputField.setText("");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClient().setVisible(true));
    }
}


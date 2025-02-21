package src;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private Map<String, PrintWriter> clients = new HashMap<>();

    public static void main(String[] args) {
        new ChatServer().startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void broadcastMessage(String message, String sender) {
        String timestampedMessage = "[" + getTimeStamp() + "] " + sender + ": " + message;
        for (PrintWriter client : clients.values()) {
            client.println(timestampedMessage);
        }
    }

    public synchronized void privateMessage(String message, String sender, String recipient) {
        String timestampedMessage = "[" + getTimeStamp() + "] " + sender + " (private): " + message;
        PrintWriter recipientWriter = clients.get(recipient);
        if (recipientWriter != null) {
            recipientWriter.println(timestampedMessage);
        } else {
            clients.get(sender).println("Error: User '" + recipient + "' not found or offline.");
        }
    }

    private String getTimeStamp() {
        return java.time.LocalDateTime.now().toString();
    }

    class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                out.println("Welcome to the chat! Please enter your username:");
                username = in.readLine();
                if (username == null || username.isEmpty() || clients.containsKey(username)) {
                    out.println("Error: Username is invalid or already in use. Please try again.");
                    return;
                }
                out.println("Welcome, " + username + "! You are now connected.");
                clients.put(username, out);
                broadcastMessage(username + " has joined the chat.", "Server");

                String message;
                while ((message = in.readLine()) != null) {
                    if ("/disconnect".equals(message)) {
                        break;
                    } else if (message.startsWith("/private")) {
                        String[] parts = message.split(" ", 3);
                        if (parts.length == 3) {
                            privateMessage(parts[2], username, parts[1]);
                        } else {
                            out.println("Error: Invalid private message format. Use '/private <recipient> <message>'.");
                        }
                    } else {
                        broadcastMessage(message, username);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (username != null) {
                    clients.remove(username);
                    broadcastMessage(username + " has left the chat.", "Server");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


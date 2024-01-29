import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class server {
    private static List<Socket> clients = new ArrayList<>();

    public static void main(String[] args) {
        try {
            // Create a server socket on a specific port
            ServerSocket serverSocket = new ServerSocket(4004);
            System.out.println("Server is listening on port 4004...");

            while (true) {
                // Wait for a client to connect
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Add the new client to the list
                clients.add(clientSocket);

                // Start a new thread for each client
                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            // Create input and output streams for communication with the client
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            // Start a new thread for receiving messages from the client
            Thread receiveThread = new Thread(() -> {
                try {
                    while (true) {
                        byte[] buffer = new byte[1024];
                        int bytesRead = input.read(buffer);
                        if (bytesRead == -1) {
                            break; // Connection closed by client
                        }
                        String receivedMessage = new String(buffer, 0, bytesRead);
                        System.out.println("Client: " + receivedMessage);

                        // Broadcast the message to all clients
                        broadcastMessage(clientSocket, receivedMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void broadcastMessage(Socket sender, String message) {
        for (Socket client : clients) {
            if (client != sender) {
                try {
                    OutputStream clientOutput = client.getOutputStream();
                    clientOutput.write(message.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

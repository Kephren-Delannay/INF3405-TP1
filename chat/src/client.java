import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class client {
    public static void main(String[] args) {
        try {
        	
        	BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));
        	System.out.println("Server address : ");
            String serverAddress = reader.readLine();
            System.out.println("Server port : ");
        	int serverPort = Integer.parseInt(reader.readLine());
        	
        	InetAddress serverIP = InetAddress.getByName(serverAddress);
        	
            // Connect to the server with a specific IP address and port
            Socket socket = new Socket(serverAddress, serverPort);
            System.out.println("Connected to server.");

            // Create input and output streams for communication
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            // Start a new thread for receiving messages
            Thread receiveThread = new Thread(() -> {
                try {
                    while (true) {
                        byte[] buffer = new byte[1024];
                        int bytesRead = input.read(buffer);
                        if (bytesRead == -1) {
                            break; // Connection closed by server
                        }
                        String receivedMessage = new String(buffer, 0, bytesRead);
                        System.out.println("Server: " + receivedMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();

            // Main thread for sending messages
            while (true) {
                // Get input from console
                String message = System.console().readLine();
                // Send the message to the server
                output.write(message.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

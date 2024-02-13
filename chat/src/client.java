import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;



public class client {	
	
    public static void main(String[] args) {
    	try {
        	
        	BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));
        	
        	System.out.println("Server address : ");
            String serverAddress = reader.readLine();
            while(!validateIP(serverAddress)) {
            	System.out.println("Please enter a valid server address : ");
                serverAddress = reader.readLine();
            }
            
            System.out.println("Server port : ");
        	String serverPortInput = reader.readLine();
        	while(!validatePort(serverPortInput)) {
            	System.out.println("Please enter a valid port between 5000 and 5050 : ");
            	serverPortInput = reader.readLine();
            }
        	int serverPort = Integer.parseInt(serverPortInput);
            
        	
        	// Connect to the server with a specific IP address and port
            Socket socket = new Socket(serverAddress, serverPort);
            System.out.println("Connected to server.");
            
         // Create input and output streams for communication
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            System.out.println("Username : ");
            String username = reader.readLine();
            System.out.println("Password : ");
            String password = reader.readLine();
            
            String accInfos = username + " " + password;
            
            output.write(accInfos.getBytes());
            
            

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
                        // System.out.println("Server: " + receivedMessage);
                        System.out.println(receivedMessage);
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
            
            // boolean connected = false;
           
           
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static boolean validateIP(String ip) {
    	if(ip.equals("localhost"))
    		return true;
    	
    	String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false; // IP address should have exactly 4 parts
        }

        for (String part : parts) {
            try {
                int value = Integer.parseInt(part);
                if (value < 0 || value > 255) {
                    return false; // Each part should be in the range [0, 255]
                }
            } catch (NumberFormatException e) {
                return false; // Unable to parse a part as an integer
            }
        }

        return true;
    }
    
    private static boolean validatePort(String port) {
    	try {
    		int value = Integer.parseInt(port);
            if (value < 5000 || value > 5050) {
                return false; // Each part should be in the range [0, 255]
            }
        } catch (NumberFormatException e) {
            return false; // Unable to parse a part as an integer
        } 
    	return true;
    }
    
}


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class server {
	private static final String HISTORY_FILE = "historique.json";
	
    private static List<Socket> clients = new ArrayList<>();
    private static List<Message> history = new ArrayList<>();

    public static void main(String[] args) {
        try {
        	
        	
        	
        	BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));
        	System.out.println("Server address : ");
            String serverAddress = reader.readLine();
            System.out.println("Server port : ");
        	int serverPort = Integer.parseInt(reader.readLine());
        	
            // Create a server socket on a specific port
        	InetAddress serverIP = InetAddress.getByName(serverAddress);
        	
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(serverIP, serverPort));
 
            System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
            
            history = getHistoryFromJSON();

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
    
	@SuppressWarnings("deprecation")
	private static void handleClient(Socket clientSocket) {
        try {
            // Create input and output streams for communication with the client
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
            //show 15 first messages in history
			broadcastHistory(clientSocket);

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
                        
                        //TODO: ajouter le username du client
                        Message message = new Message("username", clientSocket.getInetAddress().toString(),clientSocket.getPort() ,receivedMessage);
                        addToHistory(message);
                        

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
    
	private static void broadcastHistory(Socket client) {
		try {
			OutputStream clientOutput = client.getOutputStream();
			for (Message message : history) {
				StringBuilder messageToSend = new StringBuilder();
				messageToSend.append("[" + message.username + " - " + message.userIP.substring(1) + ":"
						+ message.userPort + " - ");
				messageToSend.append(message.timeSent.get(Calendar.YEAR) + "-"
						+ message.monthSent + "-" + message.timeSent.get(Calendar.DATE)
						+ "@" + message.timeSent.get(Calendar.HOUR) + ":"
						+ message.timeSent.get(Calendar.MINUTE) + ":" + message.timeSent.get(Calendar.SECOND)
						+ "]:");
				messageToSend.append(message.content + "\n");
				clientOutput.write((messageToSend.toString()).getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
	private static void addToHistory(Message message) {
		history.add(0, message);
		try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(HISTORY_FILE))) {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
    
	private static void saveToJSON(List<Message> message) {
		try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(HISTORY_FILE))) {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private static List<Message> getHistoryFromJSON() {
		try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(HISTORY_FILE))) {
			return (List<Message>) objectInputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			return new ArrayList<>();
		}
	}
    
    private static class Message implements java.io.Serializable{
        private String username;
        private String userIP;
        private int userPort;
        private String content;
        private Calendar timeSent;
        private int monthSent;

        public Message(String username, String userIP, int userPort, String content) {
            this.username = username;
            this.userIP = userIP;
            this.userPort = userPort;
            this.content = content;
            this.timeSent = Calendar.getInstance(); 
            this.monthSent = timeSent.get(Calendar.MONTH) + 1;
        }
    }
}

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class server {
	private static final String HISTORY_FILE = "historique.json";
	private static final String ACCOUNTS_FILE = "accounts.txt";
	
	
    private static List<Socket> clients = new ArrayList<>();
    private static List<Message> history = new ArrayList<>();

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

            // Start a new thread for receiving messages from the client
            Thread receiveThread = new Thread(() -> {
                try {    
                	String user;
                	while (true) {
                		 byte[] buffer = new byte[1024];
                         int bytesRead = input.read(buffer);
                         String receivedCredentials = new String(buffer, 0, bytesRead);
                         
                         if(!receivedCredentials.isEmpty())
                         {
                        	 String[] credentials = receivedCredentials.split(" ");
                        	 user = credentials[0];
                        	 String pass = credentials[1];
                        	 accountInfos acc = new accountInfos(user, pass);
                        	 if(validateAccount(acc)) {
                        		String message = "Connected to the server as " + acc.username;
                        		clientSocket.getOutputStream().write(message.getBytes());
                        		//show 15 first messages in history
                    			broadcastHistory(clientSocket);
                        		break;
                        	 } else {
                        		String message = "Invalid password for user " + acc.username;
                         		clientSocket.getOutputStream().write(message.getBytes());
                        	 }
                         }
                	}
                	
                    while (true) {
                        byte[] buffer = new byte[1024];
                        int bytesRead = input.read(buffer);
                        if (bytesRead == -1) {
                            break; // Connection closed by client
                        }
                        String receivedMessage = new String(buffer, 0, bytesRead);
                        
                        //TODO: ajouter le username du client
                        Message message = new Message(user, clientSocket.getInetAddress().toString(),clientSocket.getPort() ,receivedMessage);
                        System.out.println(message);
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
    
    private static class Message implements Serializable{
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
    
    private static void ajouterTexteDansFichier(accountInfos account) {
        try (FileWriter fileWriter = new FileWriter(ACCOUNTS_FILE, true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
             PrintWriter printWriter = new PrintWriter(bufferedWriter)) {

            // Écriture du texte dans le fichier
            printWriter.println(account.username + " " + account.password);
            // System.out.println("Le texte a été ajouté avec succès dans le fichier.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    private static List<accountInfos> lireFichier(String nomFichier) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(nomFichier))) {
            // System.out.println("Contenu du fichier :");
            // Lecture et affichage de chaque ligne du fichier
            String ligne;
            List<accountInfos> listeAccount = new ArrayList<>();
            while ((ligne = bufferedReader.readLine()) != null) {
                // System.out.println(ligne);
                String[] accountInfosdata = ligne.split(" ");
                listeAccount.add(new accountInfos(accountInfosdata[0], accountInfosdata[1]));
            }
            return listeAccount;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static boolean validateAccount(accountInfos acc) {
        List<accountInfos> listeAccount = lireFichier(ACCOUNTS_FILE);
        if(listeAccount.size() == 0) {
        	ajouterTexteDansFichier(acc);
        	return true;
        } else {
        	for(accountInfos infos: listeAccount) {
	        	if(acc.username.equals(infos.username)) {
	        		if(acc.password.equals(infos.password)) {
	        			return true;
	        		} else {
	        			return false;
	        		}
	        	}
        	}
        	// new account
        	ajouterTexteDansFichier(acc);
        	return true;
        }
    }

    
    private static class accountInfos implements Serializable{
    	private static final long serialVersionUID = 1L;
		private String username;
    	private String password;
    	
    	public accountInfos(String _username, String _password){
    		this.username = _username;
    		this.password = _password;
    	}
    	
    }
}

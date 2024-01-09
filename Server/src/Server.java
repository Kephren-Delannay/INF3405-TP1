import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Server {
	private static ServerSocket Listener; // Application Serveur
	public static void main(String[] args) 
	throws Exception {
	// Compteur incrémenté à chaque connexion d'un client au serveur
	int clientNumber = 0;
	
	BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in));
	System.out.println("Server address : ");
    String serverAddress = reader.readLine();
    System.out.println("Server port : ");
	int serverPort = Integer.parseInt(reader.readLine());
// Création de la connexien pour communiquer ave les, clients
	Listener = new ServerSocket();
	Listener.setReuseAddress(true);
	InetAddress serverIP = InetAddress.getByName(serverAddress);
// Association de l'adresse et du port à la connexien
	Listener.bind(new InetSocketAddress(serverIP, serverPort));
	System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
	try {
// À chaque fois qu'un nouveau client se, connecte, on exécute la fonstion
// run() de l'objet ClientHandler
		while (true) {
// Important : la fonction accept() est bloquante: attend qu'un prochain client se connecte
// Une nouvetle connection : on incémente le compteur clientNumber 
			new ClientHandler(Listener.accept(), clientNumber++).start();
		}
	} finally {
// Fermeture de la connexion
		Listener.close();
		} 
	}
}
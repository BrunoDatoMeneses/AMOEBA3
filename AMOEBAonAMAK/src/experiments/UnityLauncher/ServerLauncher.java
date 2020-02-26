package experiments.UnityLauncher;

import javafx.application.Application;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerLauncher implements Serializable{

	private static ServerSocket socketserver = null;
	private static Socket socket = null;
	public static Thread t;

	public static void main(String[] zero) {

		try {
			socketserver = new ServerSocket(2009);
			System.out.println("Server ready...");	
			socket = socketserver.accept(); // Un client se connecte on
											// l'accepte
			System.out.println("Client connected...");
			t = new Thread(new Main(socketserver, socket));
			t.start();

		} catch (IOException e) {
			System.err.println("Le port " + socket.getLocalPort() + " est déjà utilisé !");
			e.printStackTrace();
		}
	}
	
}

package experiments.UnityLauncher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {

	private Socket socket = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private int counter;
	
	public SocketServer(ServerSocket ss, Socket s) {
		socket = s;
		counter = 0;
	}
	
	public void sendMessage(String message) {
		try {
			// socket = socketserver.accept(); // Un client se connecte on
			// l'accepte
			out = new PrintWriter(socket.getOutputStream());
			out.println(message);
			out.flush();
			// out.close();

		} catch (IOException e) {
			System.err.println("Déconnection ");
		}

	}
	
	public void close(){
			out.close();
	}
	
	public String readMessage() {
		String message = "";
		try {
			// socket = socketserver.accept(); // Un client se connecte on
			// l'accepte
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			message = in.readLine();
			//System.out.println(message);
		} catch (IOException e) {
			System.err.println("Error getOutPut");
			e.printStackTrace();

		}
		
		counter ++;
		System.out.println("MSG :\t" + message);
		String delimsTags = "[ _~]+";
		String[] tokens = message.split(delimsTags);
		if ((tokens.length != 19) && (tokens.length != 22)) return "";
		
		return message;
	}
	
	public int getMessageCounter(){
		return counter;
	}
}

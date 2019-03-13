package experiments.UnityLauncher;

import java.net.ServerSocket;
import java.net.Socket;

public class Main implements Runnable {

	private SocketServer server;
	
	/* GUI or not */
	public static final boolean viewer = false;
	private String message = "";


	private int cylceCounter;

	
	private Boolean shutDown;
	


	
	public Main(ServerSocket ss, Socket s) {
		server = new SocketServer(ss, s);

		
		
		shutDown = false;
		
		cylceCounter = 0;
	}
	
	public void run() {

	 

		while (shutDown != true) {
			

			
			server.sendMessage("CMPT_"+Integer.toString(cylceCounter));

			cylceCounter++;
		}

		
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	private void quit(){
		
		server.close();		
		shutDown = true;
	}
	
	private void ack(){
		//System.out.println("ACK...");
		//message = readMessage();
		//System.out.println(message);
		if(!message.contentEquals("")){
				server.sendMessage("ACK_" + Integer.toString(server.getMessageCounter())); 
				//System.out.println("ACK");
		}
		else{
			server.sendMessage("ERR_"+Integer.toString(server.getMessageCounter()));
		}
	}
	
	
	

}


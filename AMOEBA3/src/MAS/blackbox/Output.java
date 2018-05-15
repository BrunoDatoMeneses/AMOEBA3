package MAS.blackbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import MAS.agents.messages.Message;
import MAS.agents.messages.MessageType;
import MAS.blackbox.constraints.ConstraintOutOneLinkIn;

// TODO: Auto-generated Javadoc
/**
 * An output from the simulator.
 *
 */

public class Output extends BlackBoxAgent implements Serializable {

	/** The func. */
	private BBFunction func;
	
	/** The value. */
	private double value;
	
	/** The input. */
	private Input input;
	
	/** The port. */
	private int port;
	
	/** The socket. */
	Socket socket;
	
	/** The out. */
	PrintWriter out;
	
	/** The in. */
	BufferedReader in;
	
	/** The std in. */
	BufferedReader stdIn;
	
	/**
	 * Instantiates a new output.
	 */
	public Output() {
		this.addConstraint(new ConstraintOutOneLinkIn(this));
	}
	
	/* (non-Javadoc)
	 * @see agents.Agent#play()
	 */
	public void play() {
		super.play();
	}

	
	/* (non-Javadoc)
	 * @see blackbox.BlackBoxAgent#fastPlay()
	 */
	public void fastPlay() {
		if (socket == null && port > 0) {
			initSocket();
		}
		if (socket != null) {
			try {
				String s = in.readLine();
	//			System.out.println(s);
				NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
			    Number number;
				try {
					String[] sp = s.split("##");
					number = format.parse(sp[sp.length-1]);
					if (sp.length > 1) {
						System.out.println(sp[0]);
						this.name = sp[0];
					}
					value = number.doubleValue();

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (input != null) {
			input.setNextValue(value);
		}
	}
	
	/**
	 * Inits the socket.
	 */
	public void initSocket() {
		System.out.println("Init socket for " + name);
				try {
					System.out.println("Trying to connect to port : " + port);
					socket = new Socket(InetAddress.getByName(null), port);  //Loopback host
					out = new PrintWriter(socket.getOutputStream(), true);
					in = new BufferedReader(
					        new InputStreamReader(socket.getInputStream()));
					stdIn = new BufferedReader(
					        new InputStreamReader(System.in));
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
	}

	/* (non-Javadoc)
	 * @see blackbox.BlackBoxAgent#computeAMessage(agents.messages.Message)
	 */
	@Override
	public void computeAMessage(Message m) {
		if (m.getType() == MessageType.VALUE) {
			value = (double) m.getContent();
		}
	}

	/**
	 * Gets the func.
	 *
	 * @return the func
	 */
	public BBFunction getFunc() {
		return func;
	}

	/**
	 * Sets the func.
	 *
	 * @param func the new func
	 */
	public void setFunc(BBFunction func) {
		this.func = func;
	}

	/* (non-Javadoc)
	 * @see blackbox.BlackBoxAgent#getValue()
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * Gets the input.
	 *
	 * @return the input
	 */
	public Input getInput() {
		return input;
	}

	/**
	 * Sets the input.
	 *
	 * @param input the new input
	 */
	public void setInput(Input input) {
		this.input = input;
	}

	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the port.
	 *
	 * @param port the new port
	 */
	public void setPort(int port) {
		this.port = port;
	}

}

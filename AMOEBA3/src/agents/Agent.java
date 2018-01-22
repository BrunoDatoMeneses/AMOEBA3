package agents;

import java.io.Serializable;
import java.util.Stack;

import agents.messages.Message;
import agents.messages.MessageType;


// TODO: Auto-generated Javadoc
/**
 * The Class Agent.
 */
public abstract class Agent implements Serializable {
	

	
	protected String name;
	protected Stack<Message> messages = new Stack<Message>();
	protected Stack<Message> messagesBin = new Stack<Message>();
	
	protected int ID;
	protected boolean isDying = false;

	// --- BEHAVIOR --- //
	
	public void play() {
	}


	public void die() {
		isDying = true;
	}
	
		
	
	// --- MESSAGES --- //
	public void sendMessage(Message message, Agent a) {
		a.receiveMessage(message);
	}
	
	public void sendMessage(Object object, MessageType type, Agent a) {
		a.receiveMessage(new Message(object,type,this));
	}
	
	private void receiveMessage(Message message) {
		messages.push(message);
	}
	

	public void sendExpressMessage(Object object, MessageType type, Agent a) {
		a.receiveExpressMessage(new Message(object,type,this));
	}

	private void receiveExpressMessage(Message message) {
		computeAMessage(message);
	}
	
	public abstract void computeAMessage (Message m);
	

	public void readMessage() {
		messagesBin.clear();
		while (!messages.isEmpty()) {
			Message m = messages.pop();
			computeAMessage(m);
			messagesBin.push(m);
		}
	}
	
	
	// --- SET / GET --- //
	
	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	public Stack<Message> getMessages() {
		return messages;
	}

	public void setMessages(Stack<Message> messages) {
		this.messages = messages;
	}

	public Stack<Message> getMessagesBin() {
		return messagesBin;
	}



	public void setMessagesBin(Stack<Message> messagesBin) {
		this.messagesBin = messagesBin;
	}


	public boolean isDying() {
		return isDying;
	}



	public void setDying(boolean isDying) {
		this.isDying = isDying;
	}

	

}

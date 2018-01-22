package agents.messages;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * The Enum MessageType.
 */
public enum MessageType implements Serializable {

	/** The value. */
	VALUE, 
	
	/** The agent. */
	AGENT, 
 /** The proposal. */
 /*The message is an agent*/
	PROPOSAL, 
	
	/** The register. */
	REGISTER, 
 /** The bad proposition. */
 /*Allow the agent to be memorized by another agent*/
	BAD_PROPOSITION,
	
	/** The selection. */
	SELECTION,
	
	/** The kill. */
	KILL,
	
	/** The instable. */
	INSTABLE,
	
	/** The stable. */
	STABLE,
	
	/** The add to tree. */
	ADD_TO_TREE,
	
	/** The remove from tree. */
	REMOVE_FROM_TREE,
	
	/** The bestcontext. */
	BESTCONTEXT,
	
	/** The change. */
	CHANGE,
	
	/** The validate. */
	VALIDATE, 
 /** The unregister. */
 /*Used by variable-agent to awake or sleep context agents*/
	UNREGISTER, 
 /** The abort. */
 /*The agent want to be forget. Before being destroyed for exemple...*/
	ABORT /*When a controller reject a Context*/;
	
}

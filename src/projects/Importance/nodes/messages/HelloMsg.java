package projects.Importance.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 *	Mensagem do Flooding inicial
 */
public class HelloMsg extends Message {

	private int hopsToSink;
	private int pathsToSinkThroughMe;
	private int senderID;
		
	public HelloMsg() {
		hopsToSink = 0;
		pathsToSinkThroughMe = 0;
		senderID = 0;
	}
	
	public HelloMsg(int hopsToSink, int pathsToSinkThroughMe, int senderID) {
		this.hopsToSink = hopsToSink;
		this.pathsToSinkThroughMe = pathsToSinkThroughMe;
		this.senderID = senderID;
	}
	
	@Override
	public Message clone() {
		// This message requires a read-only policy
		return this;
	}

	public int getSenderID() {
		return senderID;
	}
	public void setSenderID(int senderID) {
		this.senderID = senderID;
	}

	public int getHopsToSink() {
		return hopsToSink;
	}

	public void setHopsToSink(int hopsToSink) {
		this.hopsToSink = hopsToSink;
	}

	public int getPathsToSinkThroughMe() {
		return pathsToSinkThroughMe;
	}

	public void setPathsToSinkThroughMe(int pathsToSinkThroughMe) {
		this.pathsToSinkThroughMe = pathsToSinkThroughMe;
	}
	
	public String toString (){
		return "H: " + Integer.toString(this.getHopsToSink()) + " P: " + Integer.toString(this.getPathsToSinkThroughMe()) + " ID: " + Integer.toString(this.getSenderID());
	}
}
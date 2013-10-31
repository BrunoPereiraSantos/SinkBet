package projects.etxBet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class PackEventEtxBet extends Message {

	private int senderID;
	private int destination;
	private int nHop;
	private int previousHop;
	
	/**
	 * @param senderID
	 * @param destination
	 */
	public PackEventEtxBet(int senderID, int destination, int nHop, int previousHop) {
		this.senderID = senderID;
		this.destination = destination;
		this.nHop = nHop;
		this.previousHop = previousHop;

	}

	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return new PackEventEtxBet(this.senderID, this.destination, this.nHop, this.previousHop);
	}

	public int getSenderID() {return senderID;}
	public void setSenderID(int senderID) {this.senderID = senderID;}
	public int getDestination() {return destination;}
	public void setDestination(int destination) {this.destination = destination;}
	public int getnHop() {return nHop;}
	public void setnHop(int nHop) {	this.nHop = nHop;}

	public int getPreviousHop() {
		return previousHop;
	}

	public void setPreviousHop(int previousHop) {
		this.previousHop = previousHop;
	}

	@Override
	public String toString() {
		return "PackEventEtxBet [senderID=" + senderID + ", destination="
				+ destination + ", nHop=" + nHop + ", previousHop="
				+ previousHop + "]";
	}
	
	
}

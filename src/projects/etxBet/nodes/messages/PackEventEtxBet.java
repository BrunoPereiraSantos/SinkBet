package projects.etxBet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class PackEventEtxBet extends Message {

	private int senderID;
	private int destination;
	private int nHop;
	
	/**
	 * @param senderID
	 * @param destination
	 */
	public PackEventEtxBet(int senderID, int destination, int nHop) {
		this.senderID = senderID;
		this.destination = destination;
		this.nHop = nHop;
	}

	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return new PackEventEtxBet(this.senderID, this.destination, this.nHop);
	}

	public int getSenderID() {return senderID;}
	public void setSenderID(int senderID) {this.senderID = senderID;}
	public int getDestination() {return destination;}
	public void setDestination(int destination) {this.destination = destination;}
	public int getnHop() {return nHop;}
	public void setnHop(int nHop) {	this.nHop = nHop;}

	@Override
	public String toString() {
		return "PackEventEtxBet [senderID=" + senderID + ", destination="
				+ destination + ", nHop=" + nHop + "]";
	}
	
	
}

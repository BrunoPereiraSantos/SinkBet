package projects.etxBet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class PackAckEtxBet extends Message {
	int destination;
	
	public PackAckEtxBet(int destination) {
		super();
		this.destination = destination;
	}

	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return new PackAckEtxBet(this.destination);
	}

	public int getDestination() {
		return destination;
	}

	public void setDestination(int destination) {
		this.destination = destination;
	} 
}

package projects.hopBet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class PackAckHopSbet extends Message {
	int destination;
	
	/**
	 * @param destination
	 */
	public PackAckHopSbet(int destination) {
		super();
		this.destination = destination;
	}

	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return new PackAckHopSbet(this.destination);
	}

	public int getDestination() {
		return destination;
	}

	public void setDestination(int destination) {
		this.destination = destination;
	}

	
}

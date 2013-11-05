package projects.etxBet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class PackTeste extends Message {
	int destination;

	/**
	 * @param destination
	 */
	public PackTeste(int destination) {
		this.destination = destination;
	}

	public int getDestination() {
		return destination;
	}

	public void setDestination(int destination) {
		this.destination = destination;
	}
	
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return new PackTeste(destination);
	}

}

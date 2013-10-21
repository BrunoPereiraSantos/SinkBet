package projects.ETX.nodes.messages;

import sinalgo.nodes.messages.Message;
import sinalgo.tools.statistics.UniformDistribution;

public class MarkMessage extends Message {
	public double probRecv;
	public int sourceID;
	public int destinationID;
	
	public MarkMessage(double probRecv, int source, int destination) {
		super();
		this.probRecv = probRecv;
		this.sourceID = source;
		this.destinationID = destination;
	}

	@Override
	public Message clone() {
		double prob = UniformDistribution.nextUniform(0, 1);
		MarkMessage msg = new MarkMessage(prob, this.sourceID, this.destinationID);
		return msg;
	}

	public double getProbRecv() {
		return probRecv;
	}

	public void setProbRecv(double probRecv) {
		this.probRecv = probRecv;
	}
	
	public int getSourceID() {
		return sourceID;
	}

	public void setSourceID(int sourceID) {
		this.sourceID = sourceID;
	}

	public int getDestinationID() {
		return destinationID;
	}

	public void setDestinationID(int destinationID) {
		this.destinationID = destinationID;
	}

}

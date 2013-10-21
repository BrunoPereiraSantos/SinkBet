package projects.ETX.nodes.timers;

import projects.ETX.nodes.messages.PackSbetETX;
import projects.ETX.nodes.nodeImplementations.NodeETX;
import sinalgo.nodes.timers.Timer;

public class SendPackSBetETX extends Timer {
	
	private PackSbetETX pkt;
	
	public SendPackSBetETX() {}
	
	public SendPackSBetETX(PackSbetETX msg) {
		//super();
		this.pkt = msg;
		pkt.setHops(msg.getHops());
		pkt.setsBet(msg.getsBet());
		pkt.setSenderID(msg.getSenderID());
		pkt.setSinkID(msg.getSinkID());
	}
	
	public SendPackSBetETX(int hops, int senderID, int sinkID, double sBet, double ETX) {
		//super();
		//this.pkt = pkt;
		pkt = new PackSbetETX(hops, senderID, sinkID, sBet, ETX);
	}
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		((NodeETX)this.node).triggersMsg(this.pkt);
	}

	public PackSbetETX getPkt() {
		return pkt;
	}

	public void setPkt(PackSbetETX pkt) {
		this.pkt = pkt;
	}
	
	
}

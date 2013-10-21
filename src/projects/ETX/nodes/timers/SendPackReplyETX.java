package projects.ETX.nodes.timers;

import projects.ETX.nodes.messages.PackReplyETX;
import projects.ETX.nodes.nodeImplementations.NodeETX;
import sinalgo.nodes.timers.Timer;

public class SendPackReplyETX extends Timer {
	
	private PackReplyETX pkt;
	
	public SendPackReplyETX() {}
	
	public SendPackReplyETX(PackReplyETX msg) {
		//super();
		this.pkt = msg;
		pkt.setHops(msg.getHops());
		pkt.setPath(msg.getPath());
		pkt.setSenderID(msg.getSenderID());
		pkt.setSinkID(msg.getSinkID());
		pkt.setSendTo(msg.getSendTo());
		pkt.setsBet(msg.getsBet());
		pkt.setFwdID(msg.getFwdID());
	}
	
	public SendPackReplyETX(int hops, int path, int senderID, int sinkID, int sendTo, double ETX, double sBet, int fwdID) {
		//super();
		//this.pkt = pkt;
		pkt = new PackReplyETX(hops, path, senderID, sinkID, sendTo, ETX, sBet, fwdID);
	}
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		((NodeETX)this.node).triggersMsg(this.pkt);
	}

	public PackReplyETX getPkt() {
		return pkt;
	}

	public void setPkt(PackReplyETX pkt) {
		this.pkt = pkt;
	}
	
}

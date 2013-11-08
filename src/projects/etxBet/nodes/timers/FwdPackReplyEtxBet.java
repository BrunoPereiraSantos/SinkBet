package projects.etxBet.nodes.timers;

import java.util.ArrayList;

import projects.etxBet.nodes.messages.PackReplyEtxBet;
import projects.etxBet.nodes.nodeImplementations.NodeEtxBet;
import sinalgo.nodes.timers.Timer;

public class FwdPackReplyEtxBet extends Timer {
	
	private PackReplyEtxBet pkt;
	
	public FwdPackReplyEtxBet() {}
	
	public FwdPackReplyEtxBet(PackReplyEtxBet msg) {
		//super();
		this.pkt = msg;
		pkt.setHops(msg.getHops());
		pkt.setPath(msg.getPath());
		pkt.setSenderID(msg.getSenderID());
		pkt.setSinkID(msg.getSinkID());
		pkt.setSendTo(msg.getSendTo());
		pkt.setSendToNodes(msg.getSendToNodes());
		pkt.setsBet(msg.getsBet());
		pkt.setFwdID(msg.getFwdID());
	}
	
	public FwdPackReplyEtxBet(int hops, int path, int senderID, int sinkID, int sendTo, ArrayList<Integer> sendToNodes, double etx, double sBet, int fwdID) {
		//super();
		//this.pkt = pkt;
		pkt = new PackReplyEtxBet(hops, path, senderID, sinkID, sendTo, sendToNodes, etx, sBet, fwdID);
	}
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		((NodeEtxBet)this.node).fwdReply(this.pkt);
	}

	public PackReplyEtxBet getPkt() {
		return pkt;
	}

	public void setPkt(PackReplyEtxBet pkt) {
		this.pkt = pkt;
	}
	
}

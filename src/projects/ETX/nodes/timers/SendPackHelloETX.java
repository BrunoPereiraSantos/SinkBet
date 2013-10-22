package projects.ETX.nodes.timers;

import projects.ETX.nodes.messages.PackHelloETX;
import projects.ETX.nodes.nodeImplementations.NodeETX;
import sinalgo.nodes.timers.Timer;

public class SendPackHelloETX extends Timer {
	
	private PackHelloETX pkt;
	
	public SendPackHelloETX() {}
	
	public SendPackHelloETX(PackHelloETX msg) {
		//super();
		//this.pkt = pkt;
		pkt.setHops(msg.getHops());
		pkt.setPath(msg.getPath());
		pkt.setSenderID(msg.getSenderID());
		pkt.setSinkID(msg.getSinkID());
		pkt.setETX(msg.getETX());
	}
	
	public SendPackHelloETX(PackHelloETX msg, int senderID) {
		//super();
		//this.pkt = pkt;
		pkt = msg;
		pkt.setSenderID(senderID);
	}
	
	public SendPackHelloETX(int hops, int path, int senderID, int sinkID, double ETX) {
		//super();
		//this.pkt = pkt;
		pkt = new PackHelloETX(hops, path, senderID, sinkID, ETX);
	}
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		//((NodeETX)this.node).triggersMsg(this.pkt);
		((NodeETX)this.node).fwdHelloPack(this.pkt);
	}

	public PackHelloETX getPkt() {
		return pkt;
	}

	public void setPkt(PackHelloETX pkt) {
		this.pkt = pkt;
	}
}

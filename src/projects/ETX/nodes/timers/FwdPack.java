package projects.ETX.nodes.timers;

import projects.ETX.nodes.messages.Pack;
import projects.ETX.nodes.messages.TypeMessageETX;
import projects.ETX.nodes.nodeImplementations.NodeETX;
import sinalgo.nodes.timers.Timer;

public class FwdPack extends Timer {
	
	private Pack hello = new Pack();
	
	public FwdPack() {}
	
	public FwdPack(Pack pkt) {
		//super();
		//this.hello = hello;
		
		hello.setHops(pkt.getHops());
		hello.setPath(pkt.getPath());
		hello.setsBet(pkt.getsBet());
		hello.setType(pkt.getType());
		hello.setSinkID(pkt.getSinkID());
		
		hello.setSenderID(pkt.getSenderID());
	}
	
	public FwdPack(Pack pkt, int senderID) {
		//super();
		//this.hello = hello;
		
		hello.setHops(pkt.getHops());
		hello.setPath(pkt.getPath());
		hello.setsBet(pkt.getsBet());
		hello.setType(pkt.getType());
		hello.setSinkID(pkt.getSinkID());
		
		hello.setSenderID(senderID);
	}

	public FwdPack(Pack pkt, int senderID, TypeMessageETX type) {
		//super();
		//this.hello = hello;
		
		hello.setHops(pkt.getHops());
		hello.setPath(pkt.getPath());
		hello.setsBet(pkt.getsBet());
		hello.setSinkID(pkt.getSinkID());
		hello.setType(type);
		hello.setSenderID(senderID);
	}
	
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		//((NodeEtxBet)this.node).fwdHelloFlooding(this.hello);
		((NodeETX)this.node).triggersMsg(this.hello);
	}

	public Pack getHello() {
		return hello;
	}

	public void setHello(Pack hello) {
		this.hello = hello;
	}
}

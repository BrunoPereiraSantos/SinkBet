package projects.hopBet.nodes.timers;

import projects.hopBet.nodes.messages.PackEventHopSbet;
import projects.hopBet.nodes.nodeImplementations.NodeHopSbet;
import sinalgo.nodes.timers.Timer;

public class FwdPackEventHopSbet extends Timer {

	PackEventHopSbet pkt;
	
	
	
	
	/**
	 * @param pkt
	 */
	public FwdPackEventHopSbet(PackEventHopSbet pkt) {
		super();
		this.pkt = pkt;
	}




	@Override
	public void fire() {
		// TODO Auto-generated method stub
		((NodeHopSbet)this.node).broadcastEvent(this.pkt);
	}


	public PackEventHopSbet getPkt() {
		return pkt;
	}




	public void setPkt(PackEventHopSbet pkt) {
		this.pkt = pkt;
	}
	
	

}

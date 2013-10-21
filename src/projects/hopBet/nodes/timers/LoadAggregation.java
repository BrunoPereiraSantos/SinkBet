package projects.hopBet.nodes.timers;

import projects.hopBet.nodes.messages.PackEventHopSbet;
import projects.hopBet.nodes.nodeImplementations.NodeHopSbet;
import sinalgo.nodes.timers.Timer;

public class LoadAggregation extends Timer {

	PackEventHopSbet pkt;
	
	/**
	 * @param pkt
	 */
	public LoadAggregation(PackEventHopSbet pkt) {
		super();
		this.pkt = pkt;
	}



	@Override
	public void fire() {
		// TODO Auto-generated method stub
		((NodeHopSbet)this.node).fwdEvent(pkt);
	}

}

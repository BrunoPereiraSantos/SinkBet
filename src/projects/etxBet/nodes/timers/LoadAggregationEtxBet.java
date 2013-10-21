package projects.etxBet.nodes.timers;

import projects.etxBet.nodes.messages.PackEventEtxBet;
import projects.etxBet.nodes.nodeImplementations.NodeEtxBet;
import sinalgo.nodes.timers.Timer;

public class LoadAggregationEtxBet extends Timer {

	PackEventEtxBet pkt;
	
	/**
	 * @param pkt
	 */
	public LoadAggregationEtxBet(PackEventEtxBet pkt) {
		super();
		this.pkt = pkt;
	}



	@Override
	public void fire() {
		// TODO Auto-generated method stub
		((NodeEtxBet)this.node).fwdEvent(pkt);
	}

}

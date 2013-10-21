package projects.ETX.nodes.timers;

import projects.ETX.nodes.messages.PackEventETX;
import projects.ETX.nodes.nodeImplementations.NodeETX;
import sinalgo.nodes.timers.Timer;

public class LoadAggregationETX extends Timer {

	PackEventETX pkt;
	
	
	/**
	 * @param pkt
	 */
	public LoadAggregationETX(PackEventETX pkt) {
		super();
		this.pkt = pkt;
	}

	@Override
	public void fire() {
		// TODO Auto-generated method stub
		((NodeETX)this.node).fwdEvent(pkt);
	}

}

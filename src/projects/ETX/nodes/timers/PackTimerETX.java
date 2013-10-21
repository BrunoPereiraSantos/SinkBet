package projects.ETX.nodes.timers;

import projects.ETX.nodes.messages.Pack;
import projects.etxBet.nodes.nodeImplementations.NodeEtxBet;
import sinalgo.nodes.timers.Timer;

public class PackTimerETX extends Timer {

	private Pack m = null;
	
	
	public PackTimerETX(Pack p) {
		this.m = p;
	}


	@Override
	public void fire() {
		((NodeEtxBet) node).broadcast(this.m);
	}

}

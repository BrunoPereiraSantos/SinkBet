package projects.etxBet.nodes.timers;

import projects.etxBet.nodes.nodeImplementations.NodeEtxBet;
import sinalgo.nodes.timers.Timer;

public class StartReplyFloodingEtxBet extends Timer {

	
	public StartReplyFloodingEtxBet() {
	}

	@Override
	public void fire() {
		// TODO Auto-generated method stub
		((NodeEtxBet)this.node).sendReplyFlooding();
	}

}

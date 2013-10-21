package projects.etxBet.nodes.timers;

import projects.etxBet.nodes.nodeImplementations.NodeEtxBet;
import sinalgo.nodes.timers.Timer;

public class SendPackHelloEtxBet extends Timer {
	
	public SendPackHelloEtxBet() {}
	
	
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		((NodeEtxBet)this.node).fwdHelloPack();
	}
	
}

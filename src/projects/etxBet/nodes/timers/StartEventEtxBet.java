package projects.etxBet.nodes.timers;

import projects.etxBet.nodes.nodeImplementations.NodeEtxBet;
import sinalgo.nodes.timers.Timer;
import sinalgo.runtime.Runtime;

public class StartEventEtxBet extends Timer {

	@Override
	public void fire() {
		// TODO Auto-generated method stub
		((NodeEtxBet)this.node).sendEvent();
	}

	@Override
	public String toString() {
		return "StartEventEtxBet [node=" + node.ID + ", getFireTime()="
				+ getFireTime() + ", isNodeTimer()=" + isNodeTimer()
				+ ", getTargetNode()=" + getTargetNode() + ", getClass()="
				+ getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}
	
	

}

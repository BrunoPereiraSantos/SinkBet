package projects.ETX.nodes.timers;

import projects.ETX.nodes.nodeImplementations.NodeETX;
import sinalgo.nodes.timers.Timer;

public class StartEvent extends Timer {

	@Override
	public void fire() {
		// TODO Auto-generated method stub
		((NodeETX)this.node).sendEvent();
	}

}

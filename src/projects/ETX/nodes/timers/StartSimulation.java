package projects.ETX.nodes.timers;

import projects.ETX.nodes.nodeImplementations.NodeETX;
import sinalgo.nodes.timers.Timer;

public class StartSimulation extends Timer {

	@Override
	public void fire() {
		// TODO Auto-generated method stub
		( (NodeETX) this.node).sendHelloFlooding();
	}

}

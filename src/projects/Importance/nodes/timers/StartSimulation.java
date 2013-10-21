package projects.Importance.nodes.timers;

import projects.Importance.nodes.nodeImplementations.ImportanceNode;
import sinalgo.nodes.timers.Timer;

public class StartSimulation extends Timer {

	public StartSimulation(){}
	@Override
	public void fire() {
		((ImportanceNode) this.node).sendHelloFlooding();
	}

}

package projects.Importance.nodes.timers;

import projects.Importance.nodes.nodeImplementations.ImportanceNode;
import sinalgo.nodes.timers.Timer;

public class StartLeafFlooding extends Timer {

	public void fire() {
		((ImportanceNode) this.node).sendLeafFlooding();
	}

}

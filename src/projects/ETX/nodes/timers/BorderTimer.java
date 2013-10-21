package projects.ETX.nodes.timers;

import projects.ETX.nodes.messages.BorderPack;
import projects.ETX.nodes.nodeImplementations.NodeETX;
import sinalgo.nodes.timers.Timer;

public class BorderTimer extends Timer {

	private BorderPack p;
	
	public BorderTimer(BorderPack m){
		this.p = m;
	}
	
	@Override
	public void fire() {
		((NodeETX) node).broadcast(p);

	}

}

package projects.ETX.nodes.timers;

import projects.ETX.nodes.messages.HelloPack;
import projects.ETX.nodes.nodeImplementations.NodeETX;
import sinalgo.nodes.timers.Timer;

public class HelloTimer extends Timer {
	
	private HelloPack p;
	
	public HelloTimer(HelloPack m){
		this.p = m;
	}
	
	@Override
	public void fire() {
		((NodeETX) node).broadcast(p);

	}

}

package projects.wsn.nodes.timers;

import projects.wsn.nodes.messages.Proper;
import projects.wsn.nodes.nodeImplementations.Consenso;
import sinalgo.nodes.timers.Timer;

public class TimerProper extends Timer {
	private Proper m = null;
	
	
	
	/**
	 * @param m
	 */
	public TimerProper(Proper m) {
		super();
		this.m = m;
	}



	@Override
	public void fire() {
		// TODO Auto-generated method stub
		((Consenso) node).broadcast(this.m);
	}

}

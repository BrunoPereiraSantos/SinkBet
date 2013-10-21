package projects.ETX.nodes.timers;

import projects.ETX.nodes.messages.MarkMessage;
import projects.ETX.nodes.nodeImplementations.SimpleNodeETX;
import sinalgo.nodes.timers.Timer;

public class MessageTimer extends Timer {

	private MarkMessage message = null;

	public MessageTimer(MarkMessage message) {
		this.message = message;
	}

	@Override
	public void fire() {
		((SimpleNodeETX) node).broadcast(message);
	}

}

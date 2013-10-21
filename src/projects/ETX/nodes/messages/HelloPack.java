package projects.ETX.nodes.messages;

import sinalgo.nodes.messages.Message;

public class HelloPack extends Message {

	public int hops;
	public int path;
	
	public HelloPack() {
	}
	
	public HelloPack(int hops, int path) {
		this.hops = hops;
		this.path = path;
	}

	@Override
	public Message clone() {
		HelloPack msg = new HelloPack();
		return msg;
	}

}

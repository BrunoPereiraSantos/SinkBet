package projects.ETX.nodes.messages;

import java.util.Vector;

import sinalgo.nodes.messages.Message;

public class BorderPack extends Message {

	public int hops;
	public int path;
	public double sbet;
	public Vector<Integer> sonsPath;
	
	public BorderPack() {
	}
	
	public BorderPack(int hops, int path) {
		this.hops = hops;
		this.path = path;
	}
	
	@Override
	public Message clone() {
		BorderPack msg = new BorderPack();
		return msg;
	}

}

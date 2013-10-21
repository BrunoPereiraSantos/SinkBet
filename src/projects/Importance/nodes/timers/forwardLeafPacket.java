package projects.Importance.nodes.timers;

import projects.Importance.nodes.messages.LeafMsg;
import projects.Importance.nodes.nodeImplementations.ImportanceNode;
import sinalgo.nodes.timers.Timer;

public class forwardLeafPacket extends Timer {
	
	private LeafMsg leaf = new LeafMsg ();
	
	public forwardLeafPacket(){}
	public forwardLeafPacket(LeafMsg leaf){
		this.leaf.setHopsToSink(leaf.getHopsToSink());
		//this.leaf.setPathsToSinkThroughMe(leaf.getPathsToSinkThroughMe());
		this.leaf.setSonsPaths(leaf.sonsPaths);
	}
	
	public void fire() {
		((ImportanceNode) this.node).fwdLeafFlooding();
	}

	public LeafMsg getLeaf() {
		return leaf;
	}

	public void setLeaf(LeafMsg leaf) {
		this.leaf = leaf;
	}
}

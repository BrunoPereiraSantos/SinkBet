package projects.Importance.nodes.timers;

import projects.Importance.nodes.messages.HelloMsg;
import projects.Importance.nodes.nodeImplementations.ImportanceNode;
import sinalgo.nodes.timers.Timer;

public class forwardHelloPacket extends Timer {

	private HelloMsg hello = new HelloMsg();
	public forwardHelloPacket(){}
	
	public forwardHelloPacket(HelloMsg hello){
		//this.hello = hello;
		this.hello.setHopsToSink(hello.getHopsToSink());
		this.hello.setPathsToSinkThroughMe(hello.getPathsToSinkThroughMe());
		this.hello.setSenderID(hello.getSenderID());
	}
	@Override
	public void fire() {
		((ImportanceNode) this.node).fwdHelloFlooding(hello);
	}

	public HelloMsg getHello() {
		return hello;
	}

	public void setHello(HelloMsg hello) {
		this.hello = hello;
	}

}

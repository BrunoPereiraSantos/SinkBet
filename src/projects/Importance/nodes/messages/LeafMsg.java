package projects.Importance.nodes.messages;

import java.util.Hashtable;

import sinalgo.nodes.messages.Message;

public class LeafMsg extends Message {
	
	public int hopsToSink;
	//public HashMap<Node, Integer> sonsPaths = new HashMap<Node, Integer>();
	//public int [] sonsPaths = new int [10000]; //(Tools.getNodeList()).size()
	public Hashtable <Integer, Integer> sonsPaths = new Hashtable<Integer, Integer>();
		
	public LeafMsg() {
		hopsToSink = 0;
	}
	
	public LeafMsg(int hopsToSink) {
		this.hopsToSink = hopsToSink;
	}
	
	//public LeafMsg(int hopsToSink, int pathsToSinkThroughMe, int [] sonsPaths) {
	public LeafMsg(int hopsToSink, Hashtable<Integer, Integer> sonsPaths) {
		this.hopsToSink = hopsToSink;
		this.sonsPaths = sonsPaths;
		///System.arraycopy(sonsPaths, 0, this.sonsPaths, 0, sonsPaths.length);
	}
	
	
	public Message clone() {
		// This message requires a read-only policy
		return this;
	}
	
	public int getHopsToSink() {
		return hopsToSink;
	}

	public void setHopsToSink(int hopsToSink) {
		this.hopsToSink = hopsToSink;
	}

	public Hashtable<Integer, Integer> getSonsPaths() {
		return sonsPaths;
	}

	public void setSonsPaths(Hashtable<Integer, Integer> sonsPaths) {
		this.sonsPaths = sonsPaths;
	}

	public String toString(){
//		String r = new String();
//		for (int i = 0; i < 10; i++)
//			r += sonsPaths[i] + " ";
//		r += "\n";
//		return r;
		return this.sonsPaths.toString();
	}
}

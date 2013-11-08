package projects.hopBet.nodes.messages;

import java.util.ArrayList;

import sinalgo.nodes.messages.Message;

public class PackReplyHopSbet extends Message {

	private int hops; //numero de hops do nodo que o enviou o pacote
	private int path; //numeros de caminho do nodo que o enviou o pacote
	private int senderID; // ID do nodo que enviou o pacote
	private int sinkID; // ID do sink
	private int sendTo; // next hop no nodo que enviou o pacote
	private ArrayList<Integer> sendToNodes; // nodos que devem receber
	private double sBet; //metrica sBet do nodo que enviou o pacote
	
	public PackReplyHopSbet(){}
		
	public PackReplyHopSbet(int hops, int path, int senderID, int sinkID, int sendTo, ArrayList<Integer> sendToNodes , double sBet) {
		this.hops = hops;
		this.path = path;
		this.senderID = senderID;
		this.sinkID = sinkID;
		this.sendTo = sendTo;
		this.sendToNodes = sendToNodes;
		this.sBet = sBet;
	}

	@Override
	public Message clone() {
		return new PackReplyHopSbet(this.hops, this.path, this.senderID, this.sinkID, this.sendTo, this.sendToNodes, this.sBet);
	}
	
	public String toString(){
		String str = "Dados do pacote PackReplyHopSbet ";
		str = str.concat("Path = "+path+"\nSender = "+senderID+"\n");
		str = str.concat("SinkID = "+sinkID+"\n");
		str = str.concat("hops ="+ hops+"\n");
		str = str.concat("sBet ="+ sBet+"\n");
		return str;
	}

	public int getHops() {return hops;}
	public void setHops(int hops) {this.hops = hops;}
	public int getPath() {return path;}
	public void setPath(int path) {this.path = path;}
	public int getSenderID() {return senderID;}
	public void setSenderID(int senderID) {this.senderID = senderID;}
	public int getSinkID() {return sinkID;}
	public void setSinkID(int sinkID) {this.sinkID = sinkID;}
	public int getSendTo() {return sendTo;}
	public void setSendTo(int sendTo) {this.sendTo = sendTo;}
	public double getsBet() {return sBet;}
	public void setsBet(double sBet) {this.sBet = sBet;}
	
	public ArrayList<Integer> getSendToNodes() {
		return sendToNodes;
	}

	public void setSendToNodes(ArrayList<Integer> sendToNodes) {
		this.sendToNodes = sendToNodes;
	}

	protected void finalize() {}
}

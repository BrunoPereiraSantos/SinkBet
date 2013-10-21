package projects.ETX.nodes.messages;

import sinalgo.nodes.messages.Message;

public class Pack extends Message {

	private int hops;
	private int path;
	private int senderID;
	private int sinkID;
	private double sBet;
	private TypeMessageETX type;
	
	public Pack(){}
		
	public Pack(int hops, int path, int senderID, int sinkID, double sBet,
			TypeMessageETX type) {
		this.hops = hops;
		this.path = path;
		this.senderID = senderID;
		this.sinkID = sinkID;
		this.sBet = sBet;
		this.type = type;
	}

	@Override
	public Message clone() {
		return new Pack(this.hops, this.path, this.senderID, this.sinkID, this.sBet, this.type);
	}
	
	public String toString(){
		String str = "Dados do pacote ";
		str = str.concat("Path = "+path+"\nSender = "+senderID+"\n");
		str = str.concat("Type = "+type+"\n");
		str = str.concat("Sbet = "+sBet+"\n");
		str = str.concat("SinkID = "+sinkID+"\n");
		str = str.concat("sonsPath ->");
		
		str = str.concat("\n");
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
	public double getsBet() {return sBet;}
	public void setsBet(double sBet) {this.sBet = sBet;}
	public TypeMessageETX getType() {return type;}
	public void setType(TypeMessageETX type) {this.type = type;}
}

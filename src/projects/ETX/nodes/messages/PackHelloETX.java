package projects.ETX.nodes.messages;

import sinalgo.nodes.messages.Message;

public class PackHelloETX extends Message {

	
	private int hops;	//numero de hops do nodo que o enviou o pacote
	private int path;	//numeros de caminho do nodo que o enviou o pacote
	private int senderID; // ID do nodo que enviou o pacote
	private int sinkID;	// ID do sink
	private double ETX; //ETX do nodo que enviou o pacote
	
	public PackHelloETX(){}
		
	public PackHelloETX(int hops, int path, int senderID, int sinkID, double ETX) {
		this.hops = hops;
		this.path = path;
		this.senderID = senderID;
		this.sinkID = sinkID;
		this.ETX = ETX;
	}

	@Override
	public Message clone() {
		return new PackHelloETX(this.hops, this.path, this.senderID, this.sinkID, this.ETX);
	}
	
	public String toString(){
		String str = "Dados do pacote PackHelloEtxBet ";
		str = str.concat("Path = "+path+"\nSender = "+senderID+"\n");
		str = str.concat("SinkID = "+sinkID+"\n");
		str = str.concat("hops ="+ hops);
		str = str.concat("ETX ="+ ETX);
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
	
	public double getETX() {
		return ETX;
	}

	public void setETX(double eTX) {
		ETX = eTX;
	}

	protected void finalize() {}
	
}

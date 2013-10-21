package projects.ETX.nodes.messages;

import sinalgo.nodes.messages.Message;

public class PackSbetETX extends Message {

	private int hops; //numero de hops do nodo que o enviou o pacote
	private int senderID; // ID do nodo que enviou o pacote
	private int sinkID; // ID do sink
	private double sBet; //metrica sBet do nodo que enviou o pacote
	private double ETX; //ETX do nodo que enviou o pacote
	
	public PackSbetETX() {}

	public PackSbetETX(int hops, int senderID, int sinkID, double sBet, double ETX) {
		super();
		this.hops = hops;
		this.senderID = senderID;
		this.sinkID = sinkID;
		this.sBet = sBet;
		this.ETX = ETX;
	}

	@Override
	public Message clone() {
		return new PackSbetETX(this.hops, this.senderID, this.sinkID, this.sBet, this.ETX);
	}
	
	public String toString(){
		String str = "Dados do PackSbetEtxBet\n";
		str = str.concat("Sender = "+senderID+"\n");
		str = str.concat("Sbet = "+sBet+"\n");
		str = str.concat("SinkID = "+sinkID+"\n");
		str = str.concat("ETX = "+ETX+"\n");
		return str;
	}

	public int getHops() 					{return hops;}
	public void setHops(int hops) 			{this.hops = hops;}
	public int getSenderID() 				{return senderID;}
	public void setSenderID(int senderID) 	{this.senderID = senderID;}
	public int getSinkID() 					{return sinkID;}
	public void setSinkID(int sinkID) 		{this.sinkID = sinkID;}
	public double getsBet() 				{return sBet;}
	public void setsBet(double sBet) 		{this.sBet = sBet;}
	
	public double getETX() {
		return ETX;
	}

	public void setETX(double eTX) {
		ETX = eTX;
	}

	protected void finalize() {}
}

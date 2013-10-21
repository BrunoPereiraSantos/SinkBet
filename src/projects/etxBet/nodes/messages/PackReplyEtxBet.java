package projects.etxBet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class PackReplyEtxBet extends Message {

	/*private int hops; //numero de hops do nodo que o enviou o pacote
	private int path; //numeros de caminho do nodo que o enviou o pacote
	private int senderID; // ID do nodo que enviou o pacote
	private int sinkID; // ID do sink
	private int sendTo; // next hop no nodo que enviou o pacote
	private double sBet; //metrica sBet do nodo que enviou o pacote
	
	public PackReplyEtxBet(){}
		
	public PackReplyEtxBet(int hops, int path, int senderID, int sinkID, int sendTo, double sBet) {
		this.hops = hops;
		this.path = path;
		this.senderID = senderID;
		this.sinkID = sinkID;
		this.sendTo = sendTo;
		this.sBet = sBet;
	}

	@Override
	public Message clone() {
		return new PackReplyEtxBet(this.hops, this.path, this.senderID, this.sinkID, this.sendTo, this.sBet);
	}
	
	public String toString(){
		String str = "Dados do pacote PackReplyEtxBet ";
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

	protected void finalize() {}*/
	private int hops; //numero de hops do nodo que o enviou o pacote
	private int path; //numeros de caminho do nodo que o enviou o pacote
	private int senderID; // ID do nodo que enviou o pacote
	private int sinkID; // ID do sink
	private int sendTo; // next hop no nodo que enviou o pacote
	private double ETX; //ETX do nodo que enviou o pacote
	private double sBet; //metrica sBet do nodo que enviou o pacote
	private int fwdID; //ID do no que encaminhou a mensagem por ultimo
	
	public PackReplyEtxBet(){}
		
	public PackReplyEtxBet(int hops, int path, int senderID, int sinkID, int sendTo, double ETX, double sBet, int fwdID) {
		this.hops = hops;
		this.path = path;
		this.senderID = senderID;
		this.sinkID = sinkID;
		this.sendTo = sendTo;
		this.ETX = ETX;
		this.sBet = sBet;
		this.fwdID = fwdID;
	}

	@Override
	public Message clone() {
		return new PackReplyEtxBet(this.hops, this.path, this.senderID, this.sinkID, this.sendTo, this.ETX, this.sBet, this.fwdID);
	}
	
	public String toString(){
		String str = "Dados do pacote PackReplyEtxBet ";
		str = str.concat("Path = "+path+"\nSender = "+senderID+"\n");
		str = str.concat("SinkID = "+sinkID+"\n");
		str = str.concat("hops ="+ hops);
		str = str.concat("sendTo ="+ sendTo);
		str = str.concat("etx ="+ ETX);
		str = str.concat("sBet ="+ sBet);
		str = str.concat("fwdID ="+ fwdID);
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
	
	public int getSendTo() {
		return sendTo;
	}

	public void setSendTo(int sendTo) {
		this.sendTo = sendTo;
	}
	
	public double getsBet() {
		return sBet;
	}

	public void setsBet(double sBet) {
		this.sBet = sBet;
	}

	public int getFwdID() {
		return fwdID;
	}

	public void setFwdID(int fwdID) {
		this.fwdID = fwdID;
	}

	protected void finalize() {}
}

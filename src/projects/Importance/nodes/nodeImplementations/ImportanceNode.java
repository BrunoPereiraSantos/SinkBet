package projects.Importance.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import projects.Importance.nodes.messages.HelloMsg;
import projects.Importance.nodes.messages.LeafMsg;
import projects.Importance.nodes.timers.StartLeafFlooding;
import projects.Importance.nodes.timers.StartSimulation;
import projects.Importance.nodes.timers.forwardHelloPacket;
import projects.Importance.nodes.timers.forwardLeafPacket;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;
//import sinalgo.tools.topology.RRpoint;

public class ImportanceNode extends Node {

	// public static final int MAX_NODES = 10000;

	private enum Roles {SINK, INTERMEDIARY, BORDER};
	private Roles role = Roles.BORDER;
	
	// atributos de contagem
	private int pathsToSinkThroughMe_;
	private int hopsToSink_;

	// atributos de controle de contagem
	private boolean countedHopsToSink_ = false;
	private boolean sentMyHelloPacket_ = false;
	private boolean sentMyLeafPacket_ = false;

	// sink betweenness calculado de maneira distribuida e centralizada
	public double centrality = 0.0, rcentrality, rstress;
	
	// numero de pacotes que nao foram inseridos no cálculo devido aos erros no atraso
	private int lostPackets = 0;
	
	private static int packetMaxSize;
	private static boolean packetThreshold;
	
	// int sonsPaths_[] = new int [MAX_NODES];
	// <PathsToSinkThroughMe, Quantidade de vezes que um PathsToSinkThroughMe aparece nos nós filhos>
	Hashtable<Integer, Integer> sonsPaths_ = new Hashtable<Integer, Integer>();
	
	// Controle de quem já enviou um Hello
	ArrayList<Integer> listaVizinhos = new ArrayList<Integer>();
	
	forwardHelloPacket fhp = new forwardHelloPacket();
	forwardLeafPacket flp = new forwardLeafPacket();
	StartLeafFlooding slf = new StartLeafFlooding();
	
	/********************* HANDLE **********************/
	
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()) {
			Message msg = inbox.next();
			if (msg instanceof HelloMsg) { 						// Recebeu uma mensagem de hello
				HelloMsg hello = (HelloMsg) msg;
				if (role != Roles.SINK) {
					recvHello (hello);
				} else {
					// Apenas por questão de contagem do número de vizinhos do sink
					listaVizinhos.add(hello.getSenderID()); 	
				}
			}
			if (msg instanceof LeafMsg) {
				recvLeaf((LeafMsg) msg);
			}
		}
	}

	/********************* HELLO **********************/
	public void recvHello(HelloMsg msg) {
		
		int senderID = msg.getSenderID();
		
		if (!listaVizinhos.contains(senderID)) { // Nó ainda não recebeu mensagem deste vizinho
			listaVizinhos.add(senderID);		// Adiciona na lista de vizinhos

			if (!countedHopsToSink_) {			// Controla a recepção do primeiro pacote
				setHopsToSink(msg.getHopsToSink() + 1);
				countedHopsToSink_ = true;
			}

			if (msg.getHopsToSink() + 1 < getHopsToSink()) {// Recebeu este pacote por por rota de tamanho menor, atualizar
				setHopsToSink(msg.getHopsToSink() + 1);
				msg.setHopsToSink(getHopsToSink());
				pathsToSinkThroughMe_ = 0;
				pathsToSinkThroughMe_ += msg.getPathsToSinkThroughMe();
			}

			if (msg.getHopsToSink() + 1 == getHopsToSink()) {// Recebeu este pacote por rota de tamanho igual
				pathsToSinkThroughMe_ += msg.getPathsToSinkThroughMe();
			}

			if (msg.getHopsToSink() > getHopsToSink()) {// Não é nó de borda, pois recebeu pacote de algum nós mais longe do sink que ele
				this.setColor(Color.GREEN);
				role = Roles.INTERMEDIARY;
			}
		}
		//(new StartLeafFlooding()).startRelative(waitingTime(), this);
		
		// A cada novo pacote, define um pacote com os dados atualizados
		fhp.setHello(new HelloMsg(hopsToSink_, pathsToSinkThroughMe_, this.ID));
		
		// A cada vez que esta linha é executada, 
		// o tempo relativo é sobrescrito, ou seja, o tempo é "empurrado" a cada novo pacote
		fhp.startRelative(hopsToSink_, this);
		
		
		slf.startRelative(waitingTime(), this);
	}

	public void sendHelloFlooding() {
		broadcast(new HelloMsg(0, 1, this.ID));
		sentMyHelloPacket_ = true;
	}

	public void fwdHelloFlooding(HelloMsg hello) {
		if (!sentMyHelloPacket_) {
			broadcast(hello);
			sentMyHelloPacket_ = true;
		}
	}
	
	/********************* LEAF **********************/
	
	public void sendLeafFlooding() {
		if (role != Roles.SINK && role != Roles.INTERMEDIARY && !sentMyLeafPacket_) {
			LeafMsg lm = new LeafMsg(hopsToSink_);
			//lm.sonsPaths[pathsToSinkThroughMe_]++;
			lm.sonsPaths.put(pathsToSinkThroughMe_, 1);
			// System.out.println(this.ID + " enviou LeafFlooding: " + lm.toString());
			broadcast(lm);
			sentMyLeafPacket_ = true;
		}
	}

	public void recvLeaf(LeafMsg lm) {
		if (getHopsToSink() < lm.getHopsToSink()) { 						// só pacotes dos filhos
			// System.out.println(this.ID + " LeafFlooding: " + lm.toString());
			/*for (int i = 0; i < MAX_NODES; i++) {
				sonsPaths_[i] += lm.sonsPaths[i]; 							// atualiza minha tabela de filhos
				lm.sonsPaths[i] = sonsPaths_[i]; 							// coloca ela no pacote 
			}*/
			Iterator<Integer> it = lm.sonsPaths.keySet().iterator();											
			while(it.hasNext())  {																				// atualiza minha tabela com os paths do filhos a cada novo pacote
				Integer elemento = it.next();
				if (sonsPaths_.containsKey(elemento)) {
					int paths = sonsPaths_.get(elemento).intValue() + lm.sonsPaths.get(elemento).intValue(); 	// valor deste nó mais o que veio no pacote
					sonsPaths_.remove(elemento);																// remove o valor que eu tinha
					sonsPaths_.put(elemento, paths);															// reinsere atualizado
				}
				else {
					sonsPaths_.put(elemento, lm.sonsPaths.get(elemento));										// como nao existia, apenas insere
				}
			}
			// System.out.println(this.ID + " atualizou sua tabela para: " + lm.toString());
			if (role != Roles.SINK) {								// Marretada leve só para não dar problema quando o sink receber e for encaminhar num tempo 1/hopsToSink (que será 0) -> divisao por 0
				
				flp.startRelative(waitingTime(), this);				// a cada recepção, agenda o timer mais para frente
			}
		}
		// System.out.println("No "+ this.ID + "recebeu de " + sender.ID +"\n");
	}

	public void fwdLeafFlooding() {
		if (!sentMyLeafPacket_) {
/*			for (int i = 1; i < MAX_NODES; i++) { 
				if (sonsPaths_[i] != 0) {
				 // printf("%f += %d * (%d / %d)\n", centrality_, sonsPaths_[i], pathsToSinkThroughMe_, i); 
					centrality_ += sonsPaths_[i] * ((double) pathsToSinkThroughMe_/ (double) i); // calculo do betweenness modificado 
			 	} 
			}*/
			Iterator <Integer> it = sonsPaths_.keySet().iterator();
			while(it.hasNext()) {
				Integer elemento = it.next();
				centrality += sonsPaths_.get(elemento).intValue() * ((double) pathsToSinkThroughMe_/ (double) elemento.intValue());
			}
			// System.out.println(this.ID + " calculou centralidade: " + centrality);
			
			LeafMsg l = new LeafMsg(hopsToSink_, sonsPaths_);						 							// prepara o pacote a ser encaminhado com os campos atualizados
			
			if (l.sonsPaths.containsKey(pathsToSinkThroughMe_)) {
				int paths = l.sonsPaths.get(pathsToSinkThroughMe_).intValue() + 1; 								// atualiza o meu valor
				sonsPaths_.remove(pathsToSinkThroughMe_);														// remove o valor que eu tinha
				sonsPaths_.put(pathsToSinkThroughMe_, paths);													// reinsere atualizado
			}
			else {
				sonsPaths_.put(pathsToSinkThroughMe_, 1);														// como nao existia, apenas insere
			}
			if (packetThreshold) {
				if (sonsPaths_.size() > packetMaxSize) {
					// System.out.println("Resumindo pacote: " + l.toString());
					l.sonsPaths = resumeSonsPaths(l.sonsPaths);
				}
			}
			
			// System.out.println(this.ID + " encaminhou LeafFlooding: " + l.toString());
			broadcast(l);
			sentMyLeafPacket_ = true;
		} else { lostPackets++; }
	}
	
	private Hashtable<Integer, Integer> resumeSonsPaths(Hashtable<Integer, Integer> sonsPathsFromPacket) {
		// enquanto o array nao chegar ao tamanho determinado, faz o primeiro par de elemento do hash virar um so pela media
		while(sonsPathsFromPacket.size() > packetMaxSize) {
			Iterator <Integer> it = sonsPathsFromPacket.keySet().iterator();
			Integer pathA = it.next();																		// primeiro par de indices (paths)
			Integer pathB = it.next();
			Integer freqA = sonsPathsFromPacket.get(pathA);													// primeiro par de frequencias dos paths
			Integer freqB = sonsPathsFromPacket.get(pathB);
			//System.out.println("Fusão (" + pathA.intValue() + ", " + freqA.intValue() + ")" + "(" + pathB.intValue() + ", " + freqB.intValue() + ")" );
			int freqSummarized = (freqA.intValue() + freqB.intValue())/2;									// media das frequencias
			sonsPathsFromPacket.remove(pathA);																// remove um
			sonsPathsFromPacket.put(pathB, freqSummarized);													// substitui o valor do outro pela media
			// System.out.println("Resultado : " + sonsPathsFromPacket.toString());
		}
		return sonsPathsFromPacket;
	}
	
	/********************* INIT **********************/

	public void init() {
		//setRcentrality ((RRpoint.getInstance()).getSinkBetweeness(this.ID));									// Setar meu valor de centralidade calculado no R
		//setRStress((RRpoint.getInstance()).getSinkStress(this.ID));
		if (this.ID == 1) {																						// sink tem ID 1
			role = Roles.SINK;
			(new StartSimulation()).startAbsolute(0.1, this); 													// chama o sendHelloFlooding
			readConfigurationParameters();
			this.setColor(Color.RED);
		} else {
			this.setColor(Color.BLUE);
		}
	}
	
	private void readConfigurationParameters () {
		try {
			packetThreshold = sinalgo.configuration.Configuration.getBooleanParameter("PacketThreshold");
			packetMaxSize = sinalgo.configuration.Configuration.getIntegerParameter("PacketMaxSize");
		} catch (CorruptConfigurationEntryException e) {
			e.printStackTrace();
		}
	}

	/********************* AUXILIARES **********************/
	
	public int getLostPackets ()									{ return lostPackets; } 
	public int getHopsToSink () 									{ return hopsToSink_;}
	public void setHopsToSink (int hopsToSink)						{ this.hopsToSink_ = hopsToSink;}
	public int getPathsToSinkThroughMe () 							{ return pathsToSinkThroughMe_; }
	public void setPathsToSinkThroughMe(int pathsToSinkThroughMe) 	{ this.pathsToSinkThroughMe_ = pathsToSinkThroughMe; }
	public double getRcentrality() 									{ return rcentrality; }
	public double getRStress() 										{ return rstress; }
	public void setRcentrality(double rcentrality)  				{ this.rcentrality = rcentrality; }
	public void setRStress(double rstress)  						{ this.rstress = rstress; }
	public double getCentrality() 									{ return centrality; }
	public int getPacketSize()										{ return sonsPaths_.size(); }
	public boolean isSink()											{ return role == Roles.SINK; }
	public int getMaxPacketSize() 									{ return packetMaxSize; }
	
	public double waitingTime () {
		double waitTime = 0.0;
		/*if (hopsToSink_ < 10)
			waitTime = 90000000 / (Math.exp(hopsToSink_));
		else*/
		waitTime = 1 / (Math.exp(hopsToSink_) * Math.pow(10, -20));
		//waitTime = 1/ (hopsToSink_ * DELAY);
		//if (hopsToSink_ > 1 && hopsToSink_ < 16)
		//	waitTime = 10000000 / (Math.exp(hopsToSink_) * DELAY);						// hopsToSink == 1 leva ao resultado do log ficar igual a infinito
		//else
		/*if (hopsToSink_ > 1)
			waitTime = 10000000 / (Math.log(hopsToSink_) * DELAY);
		else
			waitTime = 10000000 / (Math.exp(hopsToSink_) * DELAY);*/
		/*else
			waitTime = (100 / (Math.log(hopsToSink_ + 1) * DELAY) * 2);*/
		return waitTime;
	}
	
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		//this.drawNodeAsDiskWithText(g, pt, false, Integer.toString(this.hopsToSink_), 1, Color.BLACK);
		this.drawNodeAsDiskWithText(g, pt, false, Integer.toString(this.ID), 3, Color.BLACK);
	}

	@NodePopupMethod(menuText = "Informação")
	public void ShowDebug() {
		Tools.appendToOutput(	"\ncentralidade: " + centrality +
								"\nrcentralidade: " + rcentrality +
								"\nhops: " + hopsToSink_ + "\t" +
								"\npaths: " + pathsToSinkThroughMe_
							);
	}
	
	public String print(){
/*		String r = new String();
		for (int i = 0; i < 10; i++)
			r += sonsPaths_[i] + " ";
		r += "\n";
		return r;*/
		return this.sonsPaths_.toString();
	}
	public Hashtable<Integer, Integer> getPacket () {
		return this.sonsPaths_;
	}
	
	public void preStep() {}
	public void checkRequirements() throws WrongConfigurationException {}
	public void neighborhoodChange() {}
	public void postStep() {}
	
	
	/*
	 * Original
	 public String toString() {
		String s = "Node(" + this.ID + ") [";
		Iterator<Edge> edgeIter = this.outgoingConnections.iterator();
		while (edgeIter.hasNext()) {
			Edge e = edgeIter.next();
			Node n = e.endNode;
			s += n.ID + " ";
		}
		return s + "]";
	}*/
	
	public String toString() {
		String s = "Node(" + this.ID + ") [";
		Iterator<Edge> edgeIter = this.outgoingConnections.iterator();
		while (edgeIter.hasNext()) {
			Edge e = edgeIter.next();
			Node n = e.endNode;
			s += n.ID + " ";
		}
		
		s += "]\n";
		s += sonsPaths_.toString();
		return s;
	}
}
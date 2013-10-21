package projects.ETX.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import projects.ETX.nodes.edges.EdgeWeightETX;
import projects.ETX.nodes.messages.Pack;
import projects.ETX.nodes.messages.PackHelloETX;
import projects.ETX.nodes.messages.PackReplyETX;
import projects.ETX.nodes.messages.PackSbetETX;
import projects.ETX.nodes.timers.SendPackHelloETX;
import projects.ETX.nodes.timers.SendPackReplyETX;
import projects.ETX.nodes.timers.StartBorderFloodingETX;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

public class NodeETX extends Node {

	// Qual o papel do nodo
	private NodeRoleETX role;
	
	// numero de caminhos para o sink
	private int pathsToSink; 
	
	// numero de hops ate o sink
	private int hops; 
	
	// id do prox nodo usando a metrica etx
	private int nextHop; 

	// etx acumulado do caminho ate o nodo
	// Obs: note que o valor do etx acumulado
		// sao sempre calculados com as arestas que
		// apontam para o caminho ate o sink
		// ex: 2 ~[3.0]> 1 e 3 ~[2.0]> 2
		// entao etxPath (etx acumulado) do nodo 3 = 5
	private double etxPath;
	
	// valor do Sink Betweenness
	private double sBet; 
	
	// cada nodo mantem um map com chave(num de caminhos) e valor(quantos nodos
	// descendentes tem 'num de caminhos'
	private Map<Integer, Integer> sonsPathMap;
	
	// Valor do maior sBet entre os vizinhos diretos
	private double neighborMaxSBet; 

	// Flag para indicar se o nodo ja enviou seu pkt hello
	private Boolean firstHello; 
	
	// Flag para indicar se o nodo ja enviou seu pkt border
	private Boolean firstBorder;

	// array com os filhos, isto e, 
	// nodos que utilizam ESTE no como caminho ate o sink
	private ArrayList<Integer> sons; 
	
	// array com os vizinhos diretos do nodo
	private ArrayList<Integer> neighbors; 

	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()) {
			Message msg = inbox.next();

			if (msg instanceof PackHelloETX) {
				PackHelloETX a = (PackHelloETX) msg;
				handlePackHello(a);
			} else if (msg instanceof PackReplyETX) {
				PackReplyETX b = (PackReplyETX) msg;
				handlePackReply(b);
			}/*
			 * else if (msg instanceof PackSbetEtxBet) { PackSbetEtxBet c = (PackSbetEtxBet) msg;
			 * handlePackSbet(c); }
			 */
		}
	}

	public void handlePackHello(PackHelloETX message) {
		if (message.getSinkID() == this.ID) { // no sink nao manipula pacotes hello
			message = null; // drop message
			return;
		}

		double etxToNode = getEtxToNode(message.getSenderID());
		
		// o nodo eh vizinho direto do sink (armazena o nextHop como id do sink)
		if (message.getSenderID() == message.getSinkID()) { 
			setNextHop(message.getSinkID());
			setNeighborMaxSBet(Double.MAX_VALUE);
		}

		// nodo acabou de encontrar um caminho mais curto
		if ((message.getETX() + etxToNode) < getEtxPath() ||
			(getEtxPath() == Double.MAX_VALUE)) { 

			this.setColor(Color.GREEN);

			setFirstHello(false);

			setHops(message.getHops() + 1);

			setEtxPath(message.getETX() + etxToNode);

			setPathsToSink(message.getPath());

			// VERIFICAR se nao e preciso remover esse cara daqui
			setNextHop(message.getSenderID());

			message.setETX(getEtxPath()); // e mesmo necessario fazer isso aqui?
			message.setHops(getHops()); // e mesmo necessario fazer isso aqui?

		}
		
		// existe mais de um caminho deste nodo ate o sink com a mesmo ETX acumulado
		if ((message.getETX() + etxToNode) == getEtxPath()) { 
			this.setColor(Color.MAGENTA);
			setPathsToSink(getPathsToSink() + 1);
			setFirstHello(false);
		}

		// eh a primeira vez que o nodo recebe um hello
		// ele deve encaminhar um pacote com seus dados
		if (!getFirstHello()) {

			// SendPackHelloEtxBet fhp = new SendPackHelloEtxBet(message, this.ID);
			SendPackHelloETX fhp = new SendPackHelloETX(hops, pathsToSink, this.ID,
												  1, etxPath);
			fhp.startRelative(getHops(), this);

			setFirstHello(true);
			
			// Dispara um timer para enviar um pacote de borda
			// para calculo do sbet
			// nodos do tipo border e relay devem enviar tal pacote
			if (!getFirstBorder()) {
				StartBorderFloodingETX sbf = new StartBorderFloodingETX();
				//sbf.startRelative((double) waitingTime(), this);
				sbf.startRelative(getHops()*3+200, this);
				setFirstBorder(true);
			}
		}

		message = null; // drop message

	}

	public void handlePackReply(PackReplyETX message) {
		// o border e fonte nao devem manipular pacotes do tipo Relay
		if ((message.getSinkID() == this.ID) /*|| (getRole() == NodeRole.BORDER)*/)
			return;

		
		double etxToNode = getEtxToNode(message.getFwdID());
		// Uma mensagem foi recebida pelos ancestrais logo devo analisar se e o meu nextHop
		if (message.getETX() + etxToNode < getEtxPath()){	
			if (message.getsBet() > getNeighborMaxSBet()) {
				setNeighborMaxSBet(message.getsBet());
				setNextHop(message.getSenderID());
			}
			message = null;
			return;
		}
		
		// necessaria verificacao de que o no ja recebeu menssagem de um
		// determinado descendente, isso e feito para evitar mensagens
		// duplicadas.
		// Se o nodo ainda nao recebeu menssagem de um descendente ele
		// deve 'processar' (recalcular o sbet) e propagar o pacote
		// desse descendente para que os outros nodos facam o mesmo
		if (!sons.contains(message.getSenderID()) && 
			(message.getETX() > getEtxPath())	  && 
			(message.getSendTo() == this.ID)) { 
			
			sons.add(message.getSenderID());
			
			// se o nodo faz essa operacao ele eh relay
			this.setColor(Color.CYAN);
			setRole(NodeRoleETX.RELAY);	
			
			processBet(message);

			message.setSendTo(getNextHop());
			message.setFwdID(this.ID);
			SendPackReplyETX pktReply = new SendPackReplyETX(message);
			pktReply.startRelative(0.1, this);

			// Verificar se eh preciso encaminhar esse pacote Sbet
			// SendPackSBetEtxBet pktSBet = new SendPackSBetEtxBet(hops, this.ID, 1, sBet,
			// etxPath);
			// pktSBet.startRelative(0.1, this);

		} else {
			message = null;	//drop pacote
		}

	}

	public void processBet(PackReplyETX message) { // faz o cal. do Sbet
		// faz a adicao do par <chave, valor>
		// chave = num de caminhos, valor = numero de nodos
		// descendentes com 'aquela' quantidade de caminho
		if (sonsPathMap.containsKey(message.getPath())) { 
			sonsPathMap.put(message.getPath(),
					sonsPathMap.get(message.getPath()) + 1);
		} else {
			sonsPathMap.put(message.getPath(), 1);
		}

		double tmp = 0.0;

		for (Entry<Integer, Integer> e : sonsPathMap.entrySet()){
			// faz o calculo do Sbet
			tmp += (e.getValue() * ((double) this.pathsToSink / e.getKey()));
		}
		setsBet(tmp);
	}

	public double waitingTime() {// atraso para enviar o pacote 
								// [referencia artigo do Eduardo]
		double waitTime = 0.0;
		waitTime = 1 / (Math.exp(this.hops) * Math.pow(10, -20));
		System.out.println("ID " + this.ID + "  " + waitTime);
		return waitTime;
	}

	public void sendBorderFlooding() { // Dispara o flooding das respostas dos
									// nodos com papel BORDER e RELAY
		if ((getRole() == NodeRoleETX.BORDER) || 
			(getRole() == NodeRoleETX.RELAY)) {
			this.setColor(Color.GRAY);
			// Pack pkt = new Pack(this.hops, this.pathsToSink, this.ID, 1,
			// this.sBet, TypeMessage.BORDER);
			PackReplyETX pkt = new PackReplyETX(hops, pathsToSink, this.ID, 1,
					nextHop, etxPath, sBet, nextHop);
			broadcast(pkt);

			setFirstBorder(true);
		}
	}

	public void fwdHelloFlooding(Pack pkt) {
		broadcast(pkt);
	}
	
	// Dispara um broadcast com o pacote Gernerico | 
	// metodo utilizado pelos timers
	public void triggersMsg(Message pkt) { 
		broadcast(pkt);
	}
	
	// Dispara um broadcast com o pacote PackReplyEtxBet | 
	// metodo utilizado pelos timers
	public void triggersMsg(PackReplyETX pkt) {
		broadcast(pkt);
	}

	// Dispara um broadcast com o pacote PackSbetEtxBet | 
	// metodo utilizado pelos timers
	public void triggersMsg(PackSbetETX pkt) {
		broadcast(pkt);
	}

	@Override
	public void preStep() {
	}

	@Override
	public void init() {
		// this.timeRcv = new Double(Double.POSITIVE_INFINITY);

		setRole(NodeRoleETX.BORDER);
		setPathsToSink(0);
		setHops(0);
		setNextHop(Integer.MAX_VALUE);

		setEtxPath(Double.MAX_VALUE);
		setsBet(0.0);
		// setNeighborMaxSBet(Double.MIN_VALUE);

		setFirstBorder(false);
		setFirstHello(false);

		// this.sonsPath = new int[10];
		setSonsPathMap(new HashMap<Integer, Integer>());
		setSons(new ArrayList<Integer>());
		setNeighbors(new ArrayList<Integer>());

		if (this.ID == 1) {
			this.setColor(Color.BLUE);
			setRole(NodeRoleETX.SINK);
			setFirstHello(true);

			SendPackHelloETX pkt = new SendPackHelloETX(hops, 1, this.ID, this.ID,
					0.0);
			pkt.startRelative(2, this);

			/*
			 * Pack p = new Pack(this.hops, this.pathsToSink, this.ID, this.ID,
			 * this.sBet, TypeMessage.HELLO);
			 * 
			 * PackTimer pTimer = new PackTimer(p); pTimer.startRelative(2,
			 * this);
			 */
		}

	}

	@Override
	public void neighborhoodChange() {
		// TODO Auto-generated method stub

	}

	@Override
	public void postStep() {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub

	}

	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		Integer a = new Integer(hops);
		String str = Integer.toString(this.ID) + "|" + Integer.toString(a)
				+ "|" + pathsToSink;
		super.drawNodeAsDiskWithText(g, pt, highlight, str, 8, Color.YELLOW);
	}

	public double getEtxToNode(int nodeID) {
		Iterator<Edge> it2 = this.outgoingConnections.iterator();
		EdgeWeightETX e;
		while (it2.hasNext()) {
			e = (EdgeWeightETX) it2.next();
			if (e.endNode.ID == nodeID)
				return e.getETX();
		}
		return 0.0;
	}

	public String toString() {

		String str = "Dados do no ";
		str = str.concat("" + this.ID + "\n");
		str = str.concat("role = " + role + "\n");
		str = str.concat("Path = " + pathsToSink + "\n");
		str = str.concat("Hops = " + hops + "\n");
		str = str.concat("extPath = " + etxPath + "\n");
		str = str.concat("Sbet = " + sBet + "\n");
		str = str.concat("nextHop = " + nextHop + "\n");
		str = str.concat("maxSbet = " + neighborMaxSBet + "\n");
		str = str.concat("SonspathMap -> " + sonsPathMap + "\n");
		str = str.concat("myborder -> " + firstBorder + "\n");
		str = str.concat("sons -> " + sons + "\n");
		str = str.concat("\n");

		return str;
	}
	
	public void handlePackSbet(PackSbetETX message) {
		// No fonte nao devem manipular pacotes do tipo Relay
		if ((message.getSinkID() == this.ID)) {
			message = null;
			return;
		}

		// VERIFICAR
		// Uma mensagem foi recebida pelos ancestrais,
		// logo devo analisar se eh meu nextHop
		if (message.getETX() + getEtxToNode(message.getSenderID()) < getEtxPath()) { 
			if (message.getsBet() > getNeighborMaxSBet()) {
				setNeighborMaxSBet(message.getsBet());
				setNextHop(message.getSenderID());
			}
		} else {
			message = null; // drop o pacote
		}
	}
	
	@NodePopupMethod(menuText = "Enviar msg Border")
	public void enviarMsg() {
		/*
		 * if(this.ID == 1){ HelloPack msg = new HelloPack(this.hops,
		 * this.path); HelloTimer ht = new HelloTimer(msg); ht.startRelative(1,
		 * this); }
		 */
		// sendBorder();

		StartBorderFloodingETX sendBorder = new StartBorderFloodingETX();
		sendBorder.startRelative(0.1, this);
	}
	
	/********************************************************************************
	 *							Gets e Sets
	 *********************************************************************************/
	public NodeRoleETX getRole() {return role;}
	public void setRole(NodeRoleETX role) {this.role = role;}
	public Map<Integer, Integer> getSonsPathMap() {return sonsPathMap;}
	public void setSonsPathMap(Map<Integer, Integer> sonsPathMap) {this.sonsPathMap = sonsPathMap;}
	public int getPathsToSink() {return pathsToSink;}
	public void setPathsToSink(int pathsToSink) {this.pathsToSink = pathsToSink;}
	public int getHops() {return hops;}
	public void setHops(int hops) {this.hops = hops;}
	public int getNextHop() {return nextHop;}
	public void setNextHop(int nextHop) {this.nextHop = nextHop;}
	public double getsBet() {return sBet;}
	public void setsBet(double sBet) {this.sBet = sBet;}
	public double getNeighborMaxSBet() {return neighborMaxSBet;}
	public void setNeighborMaxSBet(double neighborMaxSBet) {this.neighborMaxSBet = neighborMaxSBet;}
	public Boolean getFirstHello() {return firstHello;}
	public void setFirstHello(Boolean firstHello) {this.firstHello = firstHello;}
	public Boolean getFirstBorder() {return firstBorder;}
	public void setFirstBorder(Boolean firstBorder) {this.firstBorder = firstBorder;}
	public ArrayList<Integer> getSons() {return sons;}
	public void setSons(ArrayList<Integer> sons) {this.sons = sons;}
	public ArrayList<Integer> getNeighbors() {return neighbors;}
	public void setNeighbors(ArrayList<Integer> neighbors) {this.neighbors = neighbors;}
	public double getEtxPath() {return etxPath;}
	public void setEtxPath(double etxPath) {this.etxPath = etxPath;}
	
}

package projects.ETX.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import projects.ETX.nodes.edges.EdgeWeightETX;
import projects.ETX.nodes.messages.PackEventETX;
import projects.ETX.nodes.messages.PackHelloETX;
import projects.ETX.nodes.messages.PackReplyETX;
import projects.ETX.nodes.timers.LoadAggregationETX;
import projects.ETX.nodes.timers.SendPackHelloETX;
import projects.ETX.nodes.timers.FwdPackReplyETX;
import projects.ETX.nodes.timers.StartBorderFloodingETX;
import projects.ETX.nodes.timers.StartSimulation;
import projects.ETX.nodes.timers.StartEvent;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public class NodeETX extends Node {

	// Qual o papel do nodo
	private NodeRoleETX role;
	
	//ID do no sink
	private int sinkID;
		
		
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
	private boolean sentMyHello;
	
	// Flag para indicar se o nodo ja enviou seu pkt reply
	private boolean sentMyReply;
	
	//Flag para indicar se o nodo esta em periodo de agregacao
	private boolean inAggregation;
	
	//Intervalo para aceitar pacotes para agregacao
	private static double intervalAggr;
		
		
	// array com os filhos, isto e, 
	// nodos que utilizam ESTE no como caminho ate o sink
	private ArrayList<Integer> sons; 
	
	// array com os vizinhos diretos do nodo
	private ArrayList<Integer> neighbors; 
	
	//variavel para indicar quais nodos emitirao eventos
	private static Set<Integer>  setNodesEv = new HashSet<Integer>();
	
	//variavel para gerar numeros aleatorios
	private Random gerador = new Random();
	
	//ESTATISTICAS
	private int countMsgAggr = 0;
	private int count_rcv_ev_sink = 0;
	private static int count_all_pkt_sent = 0;
	private static int count_all_ev_sent = 0;
	private static int NumberNodes = 0; // numero total de nos
	private static int ev = 0;			// % de nodes que vao emitir eventos
	private static int nNodesEv = 0; //numero de nos que vao emitir eventos
	
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
			}else if(msg instanceof PackEventETX) {
				PackEventETX d = (PackEventETX) msg;
				handlePackEvent(d);
			}
		}
	}

	
	/*=============================================================
	 *                 Manipulando o pacote Hello
	 * ============================================================
	 */
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

			setSentMyHello(false);

			setHops(message.getHops() + 1);

			setEtxPath(message.getETX() + etxToNode);

			setPathsToSink(message.getPath());
			
			setSinkID(message.getSinkID());
			
			// VERIFICAR se nao e preciso remover esse cara daqui
			setNextHop(message.getSenderID());

			message.setETX(getEtxPath()); // e mesmo necessario fazer isso aqui?
			message.setHops(getHops()); // e mesmo necessario fazer isso aqui?

		}
		
		// existe mais de um caminho deste nodo ate o sink com a mesmo ETX acumulado
		if ((message.getETX() + etxToNode) == getEtxPath()) { 
			this.setColor(Color.MAGENTA);
			setPathsToSink(getPathsToSink() + 1);
			//sempre preciso criar novas mensagens com os dados atualizados para o ETX
			setSentMyHello(false);
		}

		// eh a primeira vez que o nodo recebe um hello
		// ele deve encaminhar um pacote com seus dados
		if (!isSentMyHello()) {
			
			//sempre preciso criar novas mensagens com os dados atualizados
			SendPackHelloETX fhp = new SendPackHelloETX(hops, pathsToSink, this.ID,
												  1, etxPath);
			fhp.startRelative(getHops(), this);

			setSentMyHello(true);
			
			// Dispara um timer para enviar um pacote de borda
			// para calculo do sbet
			// nodos do tipo border e relay devem enviar tal pacote
			if (!isSentMyReply()) {
				StartBorderFloodingETX sbf = new StartBorderFloodingETX();
				sbf.startRelative((double) waitingTime(), this);
				//sbf.startRelative(getHops()*3+200, this);
				setSentMyReply(true);
			}
		}

		message = null; // drop message

	}

	// Dispara um broadcast com o pacote PackHelloETX | 
	// metodo utilizado pelos timers
	public void fwdHelloPack(PackHelloETX pkt) {
		// Encaminha um pacote com as informacoes atualizadas
		broadcast(pkt); 
		setSentMyHello(true);
		
		//ESTATISTICA
		setCount_all_pkt_sent(getCount_all_pkt_sent() + 1);
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

			setSentMyReply(true);
			
			//ESTATISTICA
			setCount_all_pkt_sent(getCount_all_pkt_sent() + 1);
		}
	}
	
	public double waitingTime() {// atraso para enviar o pacote
		// [referencia artigo do Eduardo]
		double waitTime = 0.0;
		//waitTime = 1 / (Math.exp(this.hops) * Math.pow(10, -20));
		waitTime = 1 / (this.hops * (Math.pow(10, -3.3)));
		System.out.println(waitTime);
		return waitTime;
	}
	
	public void sendHelloFlooding() {
		//Somente o no que inicia o flood (neste caso o no 1) executa essa chamada
		broadcast(new PackHelloETX(hops, 1, this.ID, this.ID, 0.0));
		setSentMyHello(true);
		
		//ESTATISTICA
		setCount_all_pkt_sent(getCount_all_pkt_sent() + 1);
	}
	
	
	/*=============================================================
	 *                 Manipulando o pacote Reply
	 * ============================================================
	 */
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
			FwdPackReplyETX pktReply = new FwdPackReplyETX(message);
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
	
	// Dispara um broadcast com o pacote PackReplyEtxBet | 
	// metodo utilizado pelos timers
	public void fwdReply(PackReplyETX pkt) {
		broadcast(pkt);
		
		//ESTATISTICA
		setCount_all_pkt_sent(getCount_all_pkt_sent() + 1);
	}
	
	/*=============================================================
	 *                 Manipulando o pacote Event
	 * ============================================================
	 */
	
	public void handlePackEvent(PackEventETX message) {
		// TODO Auto-generated method stub
		
		if(this.ID == message.getDestination()){
			this.setColor(Color.PINK);
			message = null;
			
			//ESTATISTICA
			setCount_rcv_ev_sink(getCount_rcv_ev_sink() + 1);
			return;
		}
		
		if((!isInAggregation()) && (message.getnHop() == this.ID)){
			System.out.println(this.ID+" agregando...");
			setInAggregation(true);
			
			message.setnHop(nextHop); // modifica quem e o proximo hop
			LoadAggregationETX la = new LoadAggregationETX(message);
			setCountMsgAggr(getCountMsgAggr() + 1);
			la.startRelative(getIntervalAggr(), this); //10 e o tempo para esperar por mensagens a ser agregadas
		
		}else if(message.getnHop() == this.ID){
			
			setCountMsgAggr(getCountMsgAggr() + 1);
			System.out.println(this.ID+" agregou "+getCountMsgAggr());
			message = null;	// todas as mensgagens agregadas sao descartadas
		}
	}
	
	public void fwdEvent(PackEventETX message){
		this.setColor(Color.WHITE);
		
		setInAggregation(false); //No vai encaminhar o pacote, entao deve voltar ao estado de "nao agregado mensagens"
		
		System.out.println(this.ID+" encaminhou mais 1");
		setCountMsgAggr(0);
		
		broadcast(message);
		//ESTATISTICA
		setCount_all_pkt_sent(getCount_all_pkt_sent() + 1);
	}
	
	public void sendEvent(){
		System.out.println(this.ID+" mandei um evento");
		PackEventETX pktEv = new PackEventETX(this.ID, sinkID, nextHop);
		broadcast(pktEv);
		
		//ESTATISTICA
		setCount_all_pkt_sent(getCount_all_pkt_sent() + 1);
		setCount_all_ev_sent(getCount_all_ev_sent() + 1);	
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

		setSentMyReply(false);
		setSentMyHello(false);

		// this.sonsPath = new int[10];
		setSonsPathMap(new HashMap<Integer, Integer>());
		setSons(new ArrayList<Integer>());
		setNeighbors(new ArrayList<Integer>());

		if (this.ID == 1) {
			this.setColor(Color.BLUE);
			setRole(NodeRoleETX.SINK);
			/*setSentMyHello(true);

			SendPackHelloETX pkt = new SendPackHelloETX(hops, 1, this.ID, this.ID,
					0.0);
			pkt.startRelative(2, this);*/
			(new StartSimulation()).startRelative(2, this);

			/*
			 * Pack p = new Pack(this.hops, this.pathsToSink, this.ID, this.ID,
			 * this.sBet, TypeMessage.HELLO);
			 * 
			 * PackTimer pTimer = new PackTimer(p); pTimer.startRelative(2,
			 * this);
			 */
			readConfigurationParameters();
		}

		/*if(setNodesEv.contains(this.ID)){
			StartEvent se = new StartEvent();
			int time = gerador.nextInt(1000) + 2020;
			System.out.println(this.ID+" emitiriar evento em: "+time);
			se.startRelative(time, this);
			
			
			for(int i = 10; i < 300; i+=10){
				se = new StartEvent();
				//System.out.println(this.ID+" emitiriar evento em: "+(time+i)+"  "+i/10);
				se.startRelative(time+i, this);
				
			}
		}*/
		
	}

	private void readConfigurationParameters () {
		NumberNodes = 0; // numero total de nos
		ev = 0;			// % de nodes que vao emitir eventos
		nNodesEv = 0; //numero de nos que vao emitir eventos
		
		
		try {
			NumberNodes = Configuration.getIntegerParameter("NumberNodes");
			ev = Configuration.getIntegerParameter("EV");
			intervalAggr = Configuration.getDoubleParameter("intervalAggr");
			//System.out.println(NumberNodes);
			//System.out.println(ev);
		} catch (CorruptConfigurationEntryException e) {
			Tools.fatalError("Alguma das variaveis (NumberNodes, EV, interval) nao estao presentes no arquivo de configuracao ");
		}
		
		nNodesEv = (NumberNodes * ev) / 100;
		if(nNodesEv <= 0) nNodesEv = 1;
		//System.out.println(nNodesEv);
		
		while(setNodesEv.size() != nNodesEv){
			setNodesEv.add(new Integer(gerador.nextInt(NumberNodes-1) + 2));
		}
		//System.out.println(setNodesEv);
		
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
		if(this.ID == 1){
			str +=  "|" + count_rcv_ev_sink;
		}
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
		str = str.concat("sons -> " + sons + "\n");
		str = str.concat("\n");

		return str;
	}
	
	@NodePopupMethod(menuText = "Enviar evento")
	public void enviarEvento() {
		StartEvent em = new StartEvent();
		em.startRelative(1, this);
		
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

	public ArrayList<Integer> getSons() {return sons;}
	public void setSons(ArrayList<Integer> sons) {this.sons = sons;}
	public ArrayList<Integer> getNeighbors() {return neighbors;}
	public void setNeighbors(ArrayList<Integer> neighbors) {this.neighbors = neighbors;}
	public double getEtxPath() {return etxPath;}
	public void setEtxPath(double etxPath) {this.etxPath = etxPath;}
	public boolean isSentMyHello() {
		return sentMyHello;
	}


	public void setSentMyHello(boolean sentMyHello) {
		this.sentMyHello = sentMyHello;
	}


	public boolean isSentMyReply() {
		return sentMyReply;
	}


	public void setSentMyReply(boolean sentMyReply) {
		this.sentMyReply = sentMyReply;
	}


	public int getSinkID() {
		return sinkID;
	}


	public void setSinkID(int sinkID) {
		this.sinkID = sinkID;
	}


	public boolean isInAggregation() {
		return inAggregation;
	}


	public void setInAggregation(boolean inAggregation) {
		this.inAggregation = inAggregation;
	}


	public int getCountMsgAggr() {
		return countMsgAggr;
	}


	public void setCountMsgAggr(int countMsgAggr) {
		this.countMsgAggr = countMsgAggr;
	}


	public int getCount_rcv_ev_sink() {
		return count_rcv_ev_sink;
	}


	public void setCount_rcv_ev_sink(int count_rcv_ev_sink) {
		this.count_rcv_ev_sink = count_rcv_ev_sink;
	}


	public static int getCount_all_pkt_sent() {
		return count_all_pkt_sent;
	}


	public static void setCount_all_pkt_sent(int count_all_pkt_sent) {
		NodeETX.count_all_pkt_sent = count_all_pkt_sent;
	}


	public static int getCount_all_ev_sent() {
		return count_all_ev_sent;
	}


	public static void setCount_all_ev_sent(int count_all_ev_sent) {
		NodeETX.count_all_ev_sent = count_all_ev_sent;
	}


	public static int getNumberNodes() {
		return NumberNodes;
	}


	public static void setNumberNodes(int numberNodes) {
		NumberNodes = numberNodes;
	}


	public static int getEv() {
		return ev;
	}


	public static void setEv(int ev) {
		NodeETX.ev = ev;
	}


	public static int getnNodesEv() {
		return nNodesEv;
	}


	public static void setnNodesEv(int nNodesEv) {
		NodeETX.nNodesEv = nNodesEv;
	}


	public static double getIntervalAggr() {
		return intervalAggr;
	}


	public static void setIntervalAggr(double intervalAggr) {
		NodeETX.intervalAggr = intervalAggr;
	}


	public Random getGerador() {
		return gerador;
	}


	public void setGerador(Random gerador) {
		this.gerador = gerador;
	}


	public static Set<Integer> getSetNodesEv() {
		return setNodesEv;
	}


	public static void setSetNodesEv(Set<Integer> setNodesEv) {
		NodeETX.setNodesEv = setNodesEv;
	}
	
	
	
}

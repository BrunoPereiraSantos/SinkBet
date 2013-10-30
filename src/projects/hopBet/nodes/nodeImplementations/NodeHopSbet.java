package projects.hopBet.nodes.nodeImplementations;

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

import projects.etxBet.nodes.edges.EdgeWeightEtxBet;
import projects.hopBet.nodes.edges.EdgeWeightHopSbet;
import projects.hopBet.nodes.messages.PackEventHopSbet;
import projects.hopBet.nodes.messages.PackHelloHopSbet;
import projects.hopBet.nodes.messages.PackReplyHopSbet;
import projects.hopBet.nodes.timers.LoadAggregation;
import projects.hopBet.nodes.timers.SendPackHelloHopSbet;
import projects.hopBet.nodes.timers.FwdPackReplyHopSbet;
import projects.hopBet.nodes.timers.StartReplyFloodingHopSbet;
import projects.hopBet.nodes.timers.StartEvent;
import projects.hopBet.nodes.timers.StartSimulation;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public class NodeHopSbet extends Node {

	// Qual o papel do nodo.
	private NodeRoleHopSbet role;
	
	//private int sonsPath[];
	//cada nodo mantem um map com chave(num de caminhos) e valor(quantos nodos descendentes tem 'num de caminhos'
	private Map<Integer, Integer> sonsPathMap;	
	
	//ID do no sink
	private int sinkID;
	
	//numero de caminhos para o sink
	private int pathsToSink;	
	
	//numero de hops ate o sink
	private int hops;			
	
	//id do prox no usando a metrica numero de hops
	private int nextHop;		
	
	//valor do Sink Betweenness
	private double sBet;		
	
	//Valor do maior sBet entre os vizinhos diretos
	private double neighborMaxSBet;	
	//private double timeRcv;
	
	//Flag para indicar se o nodo ja enviou seu pkt hello
	private boolean sentMyHello;
	
	//Flag para indicar se o nodo ja enviou seu pkt reply
	private boolean sentMyReply;
	
	//Flag para indicar se o nodo esta em periodo de agregacao
	private boolean inAggregation;
	
	//Intervalo para aceitar pacotes para agregacao
	private static double intervalAggr;
	
	//array com os filhos (nodo.hops < hops)
	private ArrayList<Integer> sons;	
	
	//array com os vizinhos diretos do nodo.
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
	private static int countDropPkt = 0;	//contador no numero de pacotes perdidos

	//Disparadores de flood
	SendPackHelloHopSbet fhp = new SendPackHelloHopSbet();
	StartReplyFloodingHopSbet srf = new StartReplyFloodingHopSbet();
	
	
	//modelo de energia
	private static final double cf = 50;
	private static final double y = 0.016;
	//private static final double cr = 236.4;
	private static final double cr = 50;
	private static final double alfa = 2;
	private static final double range = 30;
	
	private static double energySpentByNode = 0;
	private static double energySpentByEvent = 0;
	
	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()) {
			Message msg = inbox.next();
			
			if (msg instanceof PackHelloHopSbet) {
				PackHelloHopSbet a = (PackHelloHopSbet) msg;
				handlePackHello(a);
			}else if (msg instanceof PackReplyHopSbet) {
				PackReplyHopSbet b = (PackReplyHopSbet) msg;
				handlePackReply(b);
			}else if(msg instanceof PackEventHopSbet) {
				PackEventHopSbet d = (PackEventHopSbet) msg;
				handlePackEvent(d);
				
				energySpentByEvent += cr;
			}
			
			energySpentByNode += cr;
		}
	}
	
	/*=============================================================
	 *                 Manipulando o pacote Hello
	 * ============================================================
	 */
	public void handlePackHello(PackHelloHopSbet message) {
		// no sink nao manipula pacotes hello
		if (message.getSinkID() == this.ID){
			message = null;	//drop message
			return;
		}
	
		// o nodo e vizinho direto do sink (armazena o nextHop como id do sink
		if (message.getSenderID() == message.getSinkID()) {	
			setNextHop(message.getSinkID());
			setNeighborMaxSBet(Double.MAX_VALUE);
		}

		// nodo acaba de ser descoberto ou acabou de encontrar um caminho mais curto
		if ((message.getHops() + 1 < getHops()) || (getHops() == 0)) {	
			this.setColor(Color.GREEN);
			setHops(message.getHops() + 1);
			message.setHops(getHops());
			setPathsToSink(message.getPath());
			setSinkID(message.getSinkID());
			setNextHop(message.getSenderID());
		}

		//existe mais de um caminho deste nodo ate o sink com a mesma quantidade de hops
		if ((message.getHops() + 1 == getHops())) {	
			this.setColor(Color.MAGENTA);
			setPathsToSink(getPathsToSink() + message.getPath());
			message.setHops(getHops());
		}

		// eh a primeira vez que o nodo recebe um hello
		// ele deve encaminhar um pacote com seus dados
		// Essas flags ajudam para nao sobrecarregar a memoria com eventos 
		// isto e, mandar mensagens com informa�oes desatualizadas
		if(!isSentMyHello()){
			//FwdPack fhp = new FwdPack(message, this.ID);
			//fhp.setPkt(new PackHelloHopSbet(hops, pathsToSink, this.ID, 1));
			fhp.startRelative(getHops()+1, this);	//Continuara o encaminhamento do pacote hello
			setSentMyHello(true);
			
			// Dispara um timer para enviar um pacote de borda
			// para calculo do sbet
			// nodos do tipo border e relay devem enviar tal pacote
			if(!isSentMyReply()){
				
				srf.startRelative((double) waitingTime(), this);
				//srf.startRelative(getHops()*2, this);
				//srf.startRelative(getHops()*3+200, this);
				setSentMyReply(true);
			}
		}
		
		message = null;	//drop message
	}
	
	public void fwdHelloPack() {
		// Encaminha um pacote com as informacoes atualizadas
		broadcast(new PackHelloHopSbet(hops, pathsToSink, this.ID, sinkID)); 
		setSentMyHello(true);
		
		//ESTATISTICA
		setCount_all_pkt_sent(getCount_all_pkt_sent() + 1);
		
		energySpentByNode += cf + y * Math.pow(range, alfa);
	}
	
	public void sendHelloFlooding() {
		//Somente o no que inicia o flood (neste caso o no 1) executa essa chamada
		broadcast(new PackHelloHopSbet(hops, 1, this.ID, this.ID));
		setSentMyHello(true);
		
		//ESTATISTICA
		setCount_all_pkt_sent(getCount_all_pkt_sent() + 1);
		
		energySpentByNode += cf + y * Math.pow(range, alfa);
	}

	public void sendReplyFlooding(){ //Dispara o flooding das respostas dos nodos com papel BORDER e RELAY
		if ((getRole() == NodeRoleHopSbet.BORDER) || 
			(getRole() == NodeRoleHopSbet.RELAY)) {
			this.setColor(Color.GRAY);
			//Pack pkt = new Pack(this.hops, this.pathsToSink, this.ID, 1, this.sBet, TypeMessage.BORDER);
			
			PackReplyHopSbet pkt = new PackReplyHopSbet(hops, pathsToSink, this.ID, sinkID, nextHop, sBet);
			broadcast(pkt);
			
			setSentMyReply(true);
			
			//ESTATISTICA
			setCount_all_pkt_sent(getCount_all_pkt_sent() + 1);
			
			energySpentByNode += cf + y * Math.pow(range, alfa);
		}
	}
	
	public double waitingTime () {//atraso para enviar o pacote [referencia artigo do Eduardo]
		double waitTime = 0.0;
		//waitTime = 1 / (Math.exp(this.hops) * Math.pow(10, -20));
		waitTime = 1 / (this.hops * (Math.pow(10, -3.3)));
		System.out.println(waitTime);
		return waitTime;
	}
	
	/*=============================================================
	 *                 Manipulando o pacote Reply
	 * ============================================================
	 */
	public void handlePackReply(PackReplyHopSbet message) {
		
		// o sink nao deve manipular pacotes do tipo Relay
		if((message.getSendTo() == message.getSinkID()) && (this.ID == message.getSinkID())){
			return;
		}
		
		
		// o border e fonte nao devem manipular pacotes do tipo Relay
		// necessaria verificacao de que o no ja recebeu menssagem 
		// de um determinado descendente, isso e feito para evitar mensagens duplicadas.
		// Se o nodo ainda nao recebeu menssagem de um descendente 
		// ele deve 'processar' (recalcular o sbet) e propagar o pacote desse descendente
		// para que os outros nodos tambem calculem seu sBet
		if (!sons.contains(message.getSenderID()) && 
			(message.getHops() > getHops())		  && 
			(message.getSendTo() == this.ID)) { 
			
			sons.add(message.getSenderID());

			this.setColor(Color.CYAN);
			setRole(NodeRoleHopSbet.RELAY);
			
			processBet(message);
			
			message.setSendTo(getNextHop());
			
			FwdPackReplyHopSbet fwdReply = new FwdPackReplyHopSbet(message);
			fwdReply.startRelative(0.1, this);
					
		}
		
		// Uma mensagem foi recebida pelos ancestrais logo devo analisar se e o meu nextHop
		if (message.getHops() < getHops()){	
			
			if (message.getsBet() > getNeighborMaxSBet()) {
				//System.out.println(message);
				//System.out.println(this.ID+" Entrei e mudei meu nhop");
				setNeighborMaxSBet(message.getsBet());
				setNextHop(message.getSenderID());
				
			}
			message = null;
		}
		
		
	}
	
	public void processBet(PackReplyHopSbet message) {	// faz o cal. do Sbet
		// faz a adicao do par <chave, valor>
		// chave = num de caminhos, valor = numero de nodos
		// descendentes com 'aquela' quantidade de caminho
		if(sonsPathMap.containsKey(message.getPath())){	
			sonsPathMap.put(message.getPath(), sonsPathMap.get(message.getPath()) + 1);
		}else{
			sonsPathMap.put(message.getPath(), 1);
		}
		
		double tmp = 0.0;
		
		for(Entry<Integer, Integer> e : sonsPathMap.entrySet())	// faz o calculo do Sbet
			tmp = tmp + (e.getValue() * ((double) this.pathsToSink / e.getKey()));
		
		setsBet(tmp);
	}
	
	public void fwdReply(PackReplyHopSbet pkt){//Dispara um broadcast com o pacote PackReplyHopSbet | metodo utilizado pelos timers
		broadcast(pkt);
		
		//ESTATISTICA
		setCount_all_pkt_sent(getCount_all_pkt_sent() + 1);
		
		energySpentByNode += cf + y * Math.pow(range, alfa);
	}
	
	
	/*=============================================================
	 *                 Manipulando o pacote Event
	 * ============================================================
	 */
	public void handlePackEvent(PackEventHopSbet message) {
		// TODO Auto-generated method stub
		
		if(message.getnHop() == this.ID){
			double p = gerador.nextInt(100) + 1;
			System.out.println("No "+this.ID+" gerou um probabilidade p = "+p);
			//a escala esta entre 0 a 10, ou seja, o valor do etx na aresta idica
			// que o noh tem ETX chanses de errar o pacote
			// se etx = 1 entao de cada 10 pacotes eu perco 1
			if(100 - getEtxToMeFromNode(message.getPreviousHop()) < p){
				System.out.println("perdeu um pacote");
				setCountDropPkt(getCountDropPkt() + 1);
				return;
			}else{
				System.out.println("aceitou um pacote");
			}
		}
		
		if((this.ID == message.getDestination()) && (message.getnHop() == this.ID)){	
			this.setColor(Color.PINK);
			message = null;

			//ESTATISTICA
			setCount_rcv_ev_sink(getCount_rcv_ev_sink() + 1);
			return;
		}
		
		//sem agregacao
		if((!isInAggregation()) && (message.getnHop() == this.ID)){
			setInAggregation(true);
			
			message.setnHop(nextHop); // modifica quem e o proximo hop
			setCountMsgAggr(getCountMsgAggr() + 1);
			fwdEvent(message);
		}else if(message.getnHop() == this.ID){
			setCountMsgAggr(getCountMsgAggr() + 1);
			message = null;	// todas as mensgagens agregadas sao descartadas
		}
		
		//com agregacao
		/*if((!isInAggregation()) && (message.getnHop() == this.ID)){
			System.out.println(this.ID+" agregando...");
			setInAggregation(true);
			
			message.setnHop(nextHop); // modifica quem e o proximo hop
			LoadAggregation la = new LoadAggregation(message);
			setCountMsgAggr(getCountMsgAggr() + 1);
			la.startRelative(getIntervalAggr(), this); //getIntervalAggr() e o tempo para esperar por mensagens a ser agregadas
			
		}else if(message.getnHop() == this.ID){
			setCountMsgAggr(getCountMsgAggr() + 1);
			System.out.println(this.ID+" agregou "+getCountMsgAggr());
			message = null;	// todas as mensgagens agregadas sao descartadas
		}*/
	}
	
	public void fwdEvent(PackEventHopSbet message){
		this.setColor(Color.WHITE);
		
		setInAggregation(false); //No vai encaminhar o pacote, entao deve voltar ao estado de "nao agregado mensagens"
		
		System.out.println(this.ID+" encaminhou mais 1");
		setCountMsgAggr(0);
		
		broadcast(message);
		
		//ESTATISTICA
		setCount_all_pkt_sent(getCount_all_pkt_sent() + 1);
		
		
		energySpentByNode += cf + y * Math.pow(range, alfa);
		
		energySpentByEvent += cf + y * Math.pow(range, alfa);
	}
	
	
	public void sendEvent(){
		System.out.println(this.ID+" mandei um evento");
		PackEventHopSbet pktEv = new PackEventHopSbet(this.ID, sinkID, nextHop, this.ID);
		broadcast(pktEv);
		
		//ESTATISTICA
		setCount_all_pkt_sent(getCount_all_pkt_sent() + 1);
		setCount_all_ev_sent(getCount_all_ev_sent() + 1);	
		
		
		energySpentByNode += cf + y * Math.pow(range, alfa);
		
		energySpentByEvent += cf + y * Math.pow(range, alfa);
	}
	
	@Override
	public void preStep() {
		
	}

	@Override
	public void init() {		
		setRole(NodeRoleHopSbet.BORDER);
		setPathsToSink(0);
		setHops(0);
		setsBet(0.0);
		setNextHop(Integer.MAX_VALUE);
		setNeighborMaxSBet(Double.MIN_VALUE);
		setSentMyReply(false);
		setSentMyHello(false);
		setInAggregation(false);
		setCountMsgAggr(0);
		setCount_rcv_ev_sink(0);
		
		setSonsPathMap(new HashMap<Integer, Integer>());
		setSons(new ArrayList<Integer>());
		setNeighbors(new ArrayList<Integer>());

		if (this.ID == 1) {
			this.setColor(Color.BLUE);
			setRole(NodeRoleHopSbet.SINK);
			
			(new StartSimulation()).startRelative(2, this);
			
			/*SendPackHelloHopSbet pkt = new SendPackHelloHopSbet(hops, 1, this.ID, this.ID);
			pkt.startRelative(2, this);*/
			/*Pack p = new Pack(this.hops, this.pathsToSink, this.ID, this.ID, this.sBet, TypeMessage.HELLO);
			
			PackTimer pTimer = new PackTimer(p);
			pTimer.startRelative(2, this);*/
			readConfigurationParameters();
		}
		
		if(setNodesEv.contains(this.ID)){
			StartEvent se = new StartEvent();
			int time = gerador.nextInt(1000) + 2020;
			System.out.println(this.ID+" emitiriar evento em: "+time);
			se.startRelative(time, this);
			
			
			/*for(int i = 10; i < 300; i+=10){
				se = new StartEvent();
				//System.out.println(this.ID+" emitiriar evento em: "+(time+i)+"  "+i/10);
				se.startRelative(time+i, this);
				
			}*/
		}
		
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

	@NodePopupMethod(menuText = "Enviar evento")
	public void enviarEvento() {
		StartEvent em = new StartEvent();
		em.startRelative(1, this);
		
	}


	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		Integer a = new Integer(hops);
		
		String str = Integer.toString(this.ID) + "|" + Integer.toString(a) + "|" + pathsToSink;
		/*str = str.concat("|" + getTotalLostPackets() + " " + getTotalLostPacktetsByMe());
		str = str.concat("|" + getTotalPacktesSent() + " " + getTotalSentByMe());*/
		if(this.ID == 1){
			str +=  "|" + count_rcv_ev_sink;
		}
		super.drawNodeAsDiskWithText(g, pt, highlight, str, 7, Color.YELLOW);
	}

	public String toString() {

		String str = "Dados do no ";
		str = str.concat("" + this.ID + "\n");
		str = str.concat("role = " + role + "\n");
		str = str.concat("Path = " + pathsToSink + "\n");
		str = str.concat("Hops = " + hops + "\n");
		str = str.concat("Sbet = " + sBet + "\n");
		str = str.concat("nextHop = " + nextHop + "\n");
		str = str.concat("maxSbet = " + neighborMaxSBet + "\n");
		str = str.concat("SonspathMap -> "+sonsPathMap+"\n");
		str = str.concat("sons -> "+sons+"\n");
		str = str.concat("\n");
		
		return str;
	}

	/********************************************************************************
	 *							Gets e Sets
	 *********************************************************************************/
	public NodeRoleHopSbet getRole() {return role;}
	public void setRole(NodeRoleHopSbet role) {this.role = role;}
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

	public double getEtxToMeFromNode(int nodeID) {
		Iterator<Edge> it2 = this.outgoingConnections.iterator();
		EdgeWeightHopSbet e;
		while (it2.hasNext()) {
			e = (EdgeWeightHopSbet) it2.next();
			if (e.endNode.ID == nodeID){
				e = (EdgeWeightHopSbet) e.getOppositeEdge();
				return e.getETX();
			}
		}
		return 0.0;
	}

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

	public ArrayList<Integer> getSons() {return sons;}
	public void setSons(ArrayList<Integer> sons) {this.sons = sons;}
	public ArrayList<Integer> getNeighbors() {return neighbors;}
	public void setNeighbors(ArrayList<Integer> neighbors) {this.neighbors = neighbors;}
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

	public static Set<Integer> getSetNodesEv() {
		return setNodesEv;
	}

	public static void setSetNodesEv(Set<Integer> setNodesEv) {
		NodeHopSbet.setNodesEv = setNodesEv;
	}

	public Random getGerador() {
		return gerador;
	}

	public void setGerador(Random gerador) {
		this.gerador = gerador;
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
		NodeHopSbet.count_all_pkt_sent = count_all_pkt_sent;
	}

	public static int getCount_all_ev_sent() {
		return count_all_ev_sent;
	}

	public static void setCount_all_ev_sent(int count_all_ev_sent) {
		NodeHopSbet.count_all_ev_sent = count_all_ev_sent;
	}

	public SendPackHelloHopSbet getFhp() {
		return fhp;
	}

	public void setFhp(SendPackHelloHopSbet fhp) {
		this.fhp = fhp;
	}

	public StartReplyFloodingHopSbet getSrf() {
		return srf;
	}

	public void setSrf(StartReplyFloodingHopSbet srf) {
		this.srf = srf;
	}

	public int getSinkID() {
		return sinkID;
	}

	public void setSinkID(int sinkID) {
		this.sinkID = sinkID;
	}

	public static double getIntervalAggr() {
		return intervalAggr;
	}

	public static void setIntervalAggr(double intervalAggr) {
		NodeHopSbet.intervalAggr = intervalAggr;
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
		NodeHopSbet.ev = ev;
	}

	public static int getnNodesEv() {
		return nNodesEv;
	}

	public static void setnNodesEv(int nNodesEv) {
		NodeHopSbet.nNodesEv = nNodesEv;
	}

	public static int getCountDropPkt() {
		return countDropPkt;
	}

	public static void setCountDropPkt(int countDropPkt) {
		NodeHopSbet.countDropPkt = countDropPkt;
	}

	public static double getEnergySpentByNode() {
		return energySpentByNode;
	}

	public static void setEnergySpentByNode(double energySpentByNode) {
		NodeHopSbet.energySpentByNode = energySpentByNode;
	}

	public static double getEnergySpentByEvent() {
		return energySpentByEvent;
	}

	public static void setEnergySpentByEvent(double energySpentByEvent) {
		NodeHopSbet.energySpentByEvent = energySpentByEvent;
	}

	public static double getCf() {
		return cf;
	}

	public static double getY() {
		return y;
	}

	public static double getCr() {
		return cr;
	}

	public static double getAlfa() {
		return alfa;
	}

	public static double getRange() {
		return range;
	}

	
	
}

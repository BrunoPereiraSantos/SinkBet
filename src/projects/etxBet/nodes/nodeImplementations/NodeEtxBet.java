package projects.etxBet.nodes.nodeImplementations;

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
import projects.etxBet.nodes.messages.PackEventEtxBet;
import projects.etxBet.nodes.messages.PackHelloEtxBet;
import projects.etxBet.nodes.messages.PackReplyEtxBet;
import projects.etxBet.nodes.timers.LoadAggregationEtxBet;
import projects.etxBet.nodes.timers.SendPackHelloEtxBet;
import projects.etxBet.nodes.timers.FwdPackReplyEtxBet;
import projects.etxBet.nodes.timers.StartReplyFloodingEtxBet;
import projects.etxBet.nodes.timers.StartEventEtxBet;
import projects.etxBet.nodes.timers.StartSimulationEtxBet;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public class NodeEtxBet extends Node {

	// Qual o papel do nodo
	private NodeRoleEtxBet role;
	
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
	
	//Flag para indicar se o nodo ja enviou seu pkt border
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
	
	// etx acumulado do caminho ate o nodo
	// Obs: note que o valor do etx acumulado
		// sao sempre calculados com as arestas que
		// apontam para o caminho ate o sink
		// ex: 2 ~[3.0]> 1 e 3 ~[2.0]> 2
		// entao etxPath (etx acumulado) do nodo 3 = 5
	private double etxPath;
	
	//ESTATISTICAS
	private int countMsgAggr = 0;
	private int count_rcv_ev_sink = 0;
	private static int count_all_pkt_sent = 0;
	private static int count_all_ev_sent = 0;
	private static int NumberNodes = 0; // numero total de nos
	private static int ev = 0;			// % de nodes que vao emitir eventos
	private static int nNodesEv = 0; //numero de nos que vao emitir eventos

	//Disparadores de flood
	SendPackHelloEtxBet fhp = new SendPackHelloEtxBet();
	StartReplyFloodingEtxBet srf = new StartReplyFloodingEtxBet();
	
	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()) {
			Message msg = inbox.next();
			
			if (msg instanceof PackHelloEtxBet) {
				PackHelloEtxBet a = (PackHelloEtxBet) msg;
				handlePackHello(a);
			}else if (msg instanceof PackReplyEtxBet) {
				PackReplyEtxBet b = (PackReplyEtxBet) msg;
				handlePackReply(b);
			}else if(msg instanceof PackEventEtxBet) {
				PackEventEtxBet d = (PackEventEtxBet) msg;
				handlePackEvent(d);
			}
		}
	}
	
	/*=============================================================
	 *                 Manipulando o pacote Hello
	 * ============================================================
	 */
	public void handlePackHello(PackHelloEtxBet message) {
		// no sink nao manipula pacotes hello
		if (message.getSinkID() == this.ID){
			message = null;	//drop message
			return;
		}
		
		double etxToNode = getEtxToNode(message.getSenderID());
		
		// o nodo e vizinho direto do sink (armazena o nextHop como id do sink
		if (message.getSenderID() == message.getSinkID()) {	
			setNextHop(message.getSinkID());
			setNeighborMaxSBet(Double.MAX_VALUE);
		}

		// nodo acabou de encontrar um caminho mais curto
		if ((message.getETX() + etxToNode) < getEtxPath() || (getEtxPath() == Double.MAX_VALUE)) {	
			this.setColor(Color.GREEN);
			setHops(message.getHops() + 1);
			message.setHops(getHops());
			setPathsToSink(message.getPath());
			setSinkID(message.getSinkID());
			setNextHop(message.getSenderID());
			setEtxPath(message.getETX() + etxToNode);
			message.setETX(getEtxPath()); // e mesmo necessario fazer isso aqui?
			//setSentMyHello(false);
		}

		// existe mais de um caminho deste nodo ate o sink com a mesmo ETX acumulado
		if ((message.getETX() + etxToNode) == getEtxPath()) { 
			this.setColor(Color.MAGENTA);
			//setPathsToSink(getPathsToSink() + message.getPath());
			setPathsToSink(getPathsToSink() + 1);
			
			//setSentMyHello(false);
		}

		// eh a primeira vez que o nodo recebe um hello
		// ele deve encaminhar um pacote com seus dados
		// Essas flags ajudam para nao sobrecarregar a memoria com eventos 
		// isto e, mandar mensagens com informa�oes desatualizadas
		if(!isSentMyHello()){
			//FwdPack fhp = new FwdPack(message, this.ID);
			//fhp.setPkt(new PackHelloEtxBet(hops, pathsToSink, this.ID, 1));
			/*message.setHops(getHops());
			message.setETX(getEtxPath());
			message.setSenderID(this.ID);
			message.setSinkID(sinkID);
			message.setPath(getPathsToSink());
			fhp.setPkt(message);*/
			fhp.startRelative(getHops(), this);	//Continuara o encaminhamento do pacote hello
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
		// Encaminha um pacote com as informa�oes atualizadas
		broadcast(new PackHelloEtxBet(hops, pathsToSink, this.ID, sinkID, etxPath)); 
		setSentMyHello(true);
		
		//ESTATISTICA
		setCount_all_pkt_sent(getCount_all_pkt_sent() + 1);
	}
	
	public void sendHelloFlooding() {
		
		
		//Somente o no que inicia o flood (neste caso o no 1) executa essa chamada
		PackHelloEtxBet pkt = new PackHelloEtxBet(hops, 1, this.ID, this.ID, 0.0);
		System.out.println(pkt);
		broadcast(pkt);
		setSentMyHello(true);
		
		//ESTATISTICA
		setCount_all_pkt_sent(getCount_all_pkt_sent() + 1);
	}

	public void sendReplyFlooding(){ //Dispara o flooding das respostas dos nodos com papel BORDER e RELAY
		if ((getRole() == NodeRoleEtxBet.BORDER) || 
			(getRole() == NodeRoleEtxBet.RELAY)) {
			this.setColor(Color.GRAY);
			//Pack pkt = new Pack(this.hops, this.pathsToSink, this.ID, 1, this.sBet, TypeMessage.BORDER);
			
			PackReplyEtxBet pkt = new PackReplyEtxBet(hops, pathsToSink, this.ID, sinkID, nextHop, etxPath, sBet, this.ID);
			broadcast(pkt);
			
			setSentMyReply(true);
			
			//ESTATISTICA
			setCount_all_pkt_sent(getCount_all_pkt_sent() + 1);
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
	public void handlePackReply(PackReplyEtxBet message) {
		
		// o sink nao deve manipular pacotes do tipo Relay
		if((message.getSendTo() == message.getSinkID()) && (this.ID == message.getSinkID())){
			return;
		}
		
		double etxToNode = getEtxToNode(message.getFwdID());
		
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
			setRole(NodeRoleEtxBet.RELAY);
			
			processBet(message);
			
			message.setSendTo(getNextHop());
			message.setFwdID(this.ID);
			
			FwdPackReplyEtxBet fwdReply = new FwdPackReplyEtxBet(message);
			fwdReply.startRelative(0.1, this);
					
		}
		
		// Uma mensagem foi recebida pelos ancestrais logo devo analisar se e o meu nextHop
		if (message.getETX() + etxToNode < getEtxPath()){	
			if (message.getsBet() > getNeighborMaxSBet()) {
				setNeighborMaxSBet(message.getsBet());
				setNextHop(message.getSenderID());
			}
			message = null;
		}
		
		
	}
	
	public void processBet(PackReplyEtxBet message) {	// faz o cal. do Sbet
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
	
	public void fwdReply(PackReplyEtxBet pkt){//Dispara um broadcast com o pacote PackReplyEtxBet | metodo utilizado pelos timers
		broadcast(pkt);
		
		//ESTATISTICA
		setCount_all_pkt_sent(getCount_all_pkt_sent() + 1);
	}
	
	
	/*=============================================================
	 *                 Manipulando o pacote Event
	 * ============================================================
	 */
	public void handlePackEvent(PackEventEtxBet message) {
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
			LoadAggregationEtxBet la = new LoadAggregationEtxBet(message);
			setCountMsgAggr(getCountMsgAggr() + 1);
			la.startRelative(getIntervalAggr(), this); //10 e o tempo para esperar por mensagens a ser agregadas
		}else if(message.getnHop() == this.ID){
			setCountMsgAggr(getCountMsgAggr() + 1);
			System.out.println(this.ID+" agregou "+getCountMsgAggr());
			message = null;	// todas as mensgagens agregadas sao descartadas
		}
	}
	
	public void fwdEvent(PackEventEtxBet message){
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
		PackEventEtxBet pktEv = new PackEventEtxBet(this.ID, sinkID, nextHop);
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
		setRole(NodeRoleEtxBet.BORDER);
		setPathsToSink(0);
		setHops(0);
		setsBet(0.0);
		setNextHop(Integer.MAX_VALUE);
		setNeighborMaxSBet(Double.MIN_VALUE);
		setEtxPath(Double.MAX_VALUE);
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
			setRole(NodeRoleEtxBet.SINK);
			
			(new StartSimulationEtxBet()).startRelative(2, this);
			
			/*SendPackHelloEtxBet pkt = new SendPackHelloEtxBet(hops, 1, this.ID, this.ID);
			pkt.startRelative(2, this);*/
			/*Pack p = new Pack(this.hops, this.pathsToSink, this.ID, this.ID, this.sBet, TypeMessage.HELLO);
			
			PackTimer pTimer = new PackTimer(p);
			pTimer.startRelative(2, this);*/
			readConfigurationParameters();
		}
		
		if(setNodesEv.contains(this.ID)){
			StartEventEtxBet se = new StartEventEtxBet();
			int time = gerador.nextInt(1000) + 2020;
			System.out.println(this.ID+" emitiriar evento em: "+time);
			se.startRelative(time, this);
			
			
			for(int i = 10; i < 300; i+=10){
				se = new StartEventEtxBet();
				//System.out.println(this.ID+" emitiriar evento em: "+(time+i)+"  "+i/10);
				se.startRelative(time+i, this);
				
			}
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
		StartEventEtxBet em = new StartEventEtxBet();
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
		str = str.concat("etx "+etxPath+"\n");
		str = str.concat("\n");
		
		return str;
	}

	/********************************************************************************
	 *							Gets e Sets
	 *********************************************************************************/
	public NodeRoleEtxBet getRole() {return role;}
	public void setRole(NodeRoleEtxBet role) {this.role = role;}
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
		NodeEtxBet.setNodesEv = setNodesEv;
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
		NodeEtxBet.count_all_pkt_sent = count_all_pkt_sent;
	}

	public static int getCount_all_ev_sent() {
		return count_all_ev_sent;
	}

	public static void setCount_all_ev_sent(int count_all_ev_sent) {
		NodeEtxBet.count_all_ev_sent = count_all_ev_sent;
	}

	public SendPackHelloEtxBet getFhp() {
		return fhp;
	}

	public void setFhp(SendPackHelloEtxBet fhp) {
		this.fhp = fhp;
	}

	public StartReplyFloodingEtxBet getSrf() {
		return srf;
	}

	public void setSrf(StartReplyFloodingEtxBet srf) {
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
		NodeEtxBet.intervalAggr = intervalAggr;
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
		NodeEtxBet.ev = ev;
	}

	public static int getnNodesEv() {
		return nNodesEv;
	}

	public static void setnNodesEv(int nNodesEv) {
		NodeEtxBet.nNodesEv = nNodesEv;
	}

	public double getEtxPath() {
		return etxPath;
	}

	public void setEtxPath(double etxPath) {
		this.etxPath = etxPath;
	}

	public double getEtxToNode(int nodeID) {
		Iterator<Edge> it2 = this.outgoingConnections.iterator();
		EdgeWeightEtxBet e;
		while (it2.hasNext()) {
			e = (EdgeWeightEtxBet) it2.next();
			if (e.endNode.ID == nodeID)
				return e.getETX();
		}
		return 0.0;
	}
	
}

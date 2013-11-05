package projects.etxBet.nodes.nodeImplementations;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import projects.etxBet.nodes.edges.EdgeWeightEtxBet;
import projects.etxBet.nodes.messages.PackAckEtxBet;
import projects.etxBet.nodes.messages.PackEventEtxBet;
import projects.etxBet.nodes.messages.PackHelloEtxBet;
import projects.etxBet.nodes.messages.PackReplyEtxBet;
import projects.etxBet.nodes.messages.PackTeste;
import projects.etxBet.nodes.timers.EncaminhaEvento;
import projects.etxBet.nodes.timers.IniciaEvento;
import projects.etxBet.nodes.timers.LoadAggregationEtxBet;
import projects.etxBet.nodes.timers.ReFwdEventExtBet;
import projects.etxBet.nodes.timers.ResendEventEtxBet;
import projects.etxBet.nodes.timers.SendPackHelloEtxBet;
import projects.etxBet.nodes.timers.FwdPackReplyEtxBet;
import projects.etxBet.nodes.timers.StartReplyFloodingEtxBet;
import projects.etxBet.nodes.timers.StartEventEtxBet;
import projects.etxBet.nodes.timers.StartSimulationEtxBet;
import projects.etxBet.nodes.timers.TimerTeste;
import projects.hopBet.nodes.timers.LoadAggregation;
import projects.hopBet.nodes.timers.StartEventHopSbet;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.messages.NackBox;
import sinalgo.runtime.Global;
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
	
	//Flag para indicar que o nodo recebeu um Ack
	private boolean rcvAck;
	
	//Flag para indicar se o nodo esta em periodo de agregacao
	private boolean inAggregation;
	
	//Flag para informar se o nodo emitira evento
	private boolean sendEvent;
	
	//Intervalo para aceitar pacotes para agregacao
	private static double intervalAggr;
	
	//array com os filhos (nodo.hops < hops)
	private ArrayList<Integer> sons;	
	
	//array com os vizinhos diretos do nodo.
	private ArrayList<Integer> neighbors;	
	
	//variavel para indicar quais nodos emitirao eventos
	private static Set<Integer>  setNodesEv = new HashSet<Integer>();
	
	//variavel para indicar qual o tempo de inicio do primerio evento deste no
	private int timeEvent;
	
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
	private int countMsgAggr = 0;		// numero de mensagens que foram agregadas em cada nodo
	private int broadcastCount = 0;		// numero de mensagens broadcast por no
	private static int count_all_msg_aggr = 0;		// numero de mensagens que foram agregadas
	private static int count_rcv_ev_sink = 0;	//quantidade de mensagens eventos recebidas pelo sink
	private static int count_all_msg_sent = 0; //numero de mensagens enviadas
	private static int count_all_broadcast = 0; //numero de broadcasts
	private static int count_all_overhead = 0; //numero de broadcasts overhead
	private static int count_all_ev_sent = 0;  //numero de eventos
	private static int NumberNodes = 0; // numero total de nos
	private static int ev = 0;			// % de nodes que vao emitir eventos
	private static int nNodesEv = 0; //numero de nos que vao emitir eventos
	private static int countDropPkt = 0;	//contador no numero de pacotes perdidos

	//Disparadores de flood
	SendPackHelloEtxBet fhp = new SendPackHelloEtxBet();
	StartReplyFloodingEtxBet srf = new StartReplyFloodingEtxBet();
	
	//modelo de energia
	private static final double cf = 50;
	private static final double y = 0.016;
	//private static final double cr = 236.4;
	private static final double cr = 50;
	private static final double alfa = 2;
	private static final double range = 30;
	
	private static double energySpentTotal = 0;
	private static double energySpentByEvent = 0;
	
	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()) {
			Message msg = inbox.next();
			if (msg instanceof PackHelloEtxBet) {
				PackHelloEtxBet a = (PackHelloEtxBet) msg;
				handlePackHello(a);
				energySpentTotal += cr;
			}else if (msg instanceof PackReplyEtxBet) {
				PackReplyEtxBet b = (PackReplyEtxBet) msg;
				handlePackReply(b);
				energySpentTotal += cr;	
			} else if(msg instanceof PackEventEtxBet) {
				PackEventEtxBet c = (PackEventEtxBet) msg;
				manipuladorEventos(c);
				energySpentByEvent += cr;	
				energySpentTotal += cr;
			}
			/*else if(msg instanceof PackEventEtxBet) {
				PackEventEtxBet c = (PackEventEtxBet) msg;
				handlePackEvent(c);
				energySpentByEvent += cr;	
				energySpentTotal += cr;
			}
			else if(msg instanceof PackAckEtxBet) {
				PackAckEtxBet d = (PackAckEtxBet) msg;
				handlePackAck(d);
				energySpentTotal += cr;
				energySpentByEvent += cr;	
			}*/
			// o custo foi colocado em cada if
			// pois o tratamento de perda de pacotes foi colocado
			// no handleEvent, logo nem toda mensagem que passa por aqui
			// deve ser contabilizada como mensagem recebida
		}
	}
	
	public void manipuladorEventos(PackEventEtxBet message){
		//mensagem chegou ate o sink, incrementa a quantidade de msg recebidas pelo sink
		if((this.ID == message.getDestination()) && (message.getnHop() == this.ID)){
			this.setColor(Color.PINK);
			message = null;

			//ESTATISTICA
			setCount_rcv_ev_sink(getCount_rcv_ev_sink() + 1);
			return;
		}
		
		if(message.getnHop() == this.ID){
			message.setnHop(nextHop);
			message.setPreviousHop(this.ID);
			EncaminhaEvento ee = new EncaminhaEvento(message);
			ee.startRelative(1, this);
		}
		
	}
	
	/*public void encaminhaEvento(PackEventEtxBet message){
		//send(message, Tools.getNodeByID(nextHop));
		broadcastEvent(message);
	}*/
	
	public void enviaEvent(){
		broadcastEvent(new PackEventEtxBet(this.ID, sinkID, nextHop, this.ID));
	}
	
	public void broadcastEvent(PackEventEtxBet message){
		send(message, Tools.getNodeByID(nextHop));
		
		Iterator<Edge> it = this.outgoingConnections.iterator();
		NodeEtxBet n;
		EdgeWeightEtxBet e;
		while(it.hasNext()){
			e = (EdgeWeightEtxBet) it.next();
			if(e.endNode.ID != nextHop){
				//neste caso sempre os vizinhos escutam.
				//Nao necessito enviar para todos os vizinhos
				//pq vou receber um ack para cada perda
				energySpentByEvent += cr;	
				energySpentTotal += cr;
			}
		}
	}
	
	public void handleNAckMessages(NackBox nackBox) {
		
		while(nackBox.hasNext()) {
			Message msg = nackBox.next();
			if(msg instanceof PackTeste) {
				//handle this type of message
				System.out.println(this.ID+" Entrei!!!!");
				
			}
			
			if(msg instanceof PackEventEtxBet){
				System.out.println(this.ID+" contei!!!!");
				countDropPkt++;
				PackEventEtxBet p = (PackEventEtxBet) msg;
				EncaminhaEvento ee = new EncaminhaEvento(p);
				ee.startRelative(2, this);
				
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
			setPathsToSink(getPathsToSink() + message.getPath());
			//setPathsToSink(getPathsToSink() + 1);
			fhp.updateTimer(2.0);
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
			fhp.startRelative(getHops()+1, this);	//Continuara o encaminhamento do pacote hello
			setSentMyHello(true);
			
			// Dispara um timer para enviar um pacote de borda
			// para calculo do sbet
			// nodos do tipo border e relay devem enviar tal pacote
			if(!isSentMyReply()){
				
				srf.startAbsolute((double) waitingTime(), this);
				//srf.startRelative(getHops()*2, this);
				//srf.startRelative(getHops()*3+200, this);
				setSentMyReply(true);
			}
		}
		
		/*System.out.println("======inicio===== node "+this.ID);
		TimerCollection tc = getTimers();
		System.out.println("size  "+tc.size());
		Iterator<Timer> it = tc.iterator();
		
		while(it.hasNext()){
			Timer tm = it.next();
			if ( tm instanceof SendPackHelloEtxBet) {
				SendPackHelloEtxBet a = (SendPackHelloEtxBet) tm;
				System.out.println(tm.getFireTime());
			}
		}
		System.out.println("======fim=====\n\n\n");*/
		
		message = null;	//drop message
	}
	
	public void fwdHelloPack() {
		// Encaminha um pacote com as informacoes atualizadas
		broadcast(new PackHelloEtxBet(hops, pathsToSink, this.ID, sinkID, etxPath)); 
		setSentMyHello(true);
		
		//ESTATISTICA
		setCount_all_broadcast(getCount_all_broadcast() + 1);
		setCount_all_msg_sent(getCount_all_msg_sent() + 1);
		setCount_all_overhead(getCount_all_overhead() + 1);
		energySpentTotal += cf + y * Math.pow(range, alfa);
	}
	
	public void sendHelloFlooding() {
		
		//Somente o no que inicia o flood (neste caso o no 1) executa essa chamada
		PackHelloEtxBet pkt = new PackHelloEtxBet(hops, 1, this.ID, this.ID, 0.0);
		//System.out.println(pkt);
		broadcast(pkt);
		setSentMyHello(true);
		
		//ESTATISTICA
		setCount_all_broadcast(getCount_all_broadcast() + 1);
		setCount_all_msg_sent(getCount_all_msg_sent() + 1);
		setCount_all_overhead(getCount_all_overhead() + 1);
		energySpentTotal += cf + y * Math.pow(range, alfa);
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
			setCount_all_broadcast(getCount_all_broadcast() + 1);
			setCount_all_msg_sent(getCount_all_msg_sent() + 1);
			setCount_all_overhead(getCount_all_overhead() + 1);
			energySpentTotal += cf + y * Math.pow(range, alfa);
		}
	}
	
	public double waitingTime () {//atraso para enviar o pacote [referencia artigo do Eduardo]
		double waitTime = 0.0;
		//waitTime = 1 / (Math.exp(this.hops) * Math.pow(10, -20));
		//waitTime = 1 / (this.hops * (Math.pow(5, -3.3)));
		waitTime = Math.pow(5, 3.3) / this.hops;
		//System.out.println(waitTime);
		return waitTime+100; //o flood somente inicia apos o tempo 100
	}
	
	/*=============================================================
	 *                 Manipulando o pacote Reply
	 * ============================================================
	 */
	public void handlePackReply(PackReplyEtxBet message) {
		
		// o sink nao deve manipular pacotes do tipo Relay
		if(this.ID == message.getSinkID()){
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
			fwdReply.startRelative(1, this);
					
		}
		
		// Uma mensagem foi recebida pelos ancestrais logo devo analisar se e o meu nextHop
		if (message.getETX() + etxToNode < getEtxPath()){	
			if (message.getsBet() > getNeighborMaxSBet()) {
				//System.out.println("Antes\n"+this);
				setNeighborMaxSBet(message.getsBet());
				setNextHop(message.getSenderID());
				//System.out.println("Depois\n"+this+"\n\n");
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
		setCount_all_broadcast(getCount_all_broadcast() + 1);
		setCount_all_overhead(getCount_all_overhead() + 1);
		energySpentTotal += cf + y * Math.pow(range, alfa);
	}
	
	
	/*=============================================================
	 *                 Manipulando o pacote Event
	 * ============================================================
	 */
	public void handlePackEvent(PackEventEtxBet message) {
		// TODO Auto-generated method stub
		
		if(message.getnHop() == this.ID){
			double p = gerador.nextInt(100) + 1;
			System.out.println("No "+this.ID+" gerou um probabilidade p = "+p);
			//a escala esta entre 0 a 10, ou seja, o valor do etx na aresta idica
			// que o noh tem ETX chanses de errar o pacote
			// se etx = 1 entao de cada 10 pacotes eu perco 1
			if(100 - getEtxToMeFromNode(message.getPreviousHop()) < p){
				System.out.println("perdeu um pacote");
				return;
			}else{
				System.out.println("aceitou um pacote");
				
				//envia um Ack para o nodo que envidou a mensagem
				sendAck(message.getPreviousHop());
				
				//o tratamento de mensagens recebidas
				//do tipo evendo foi colocado no handleEvent
				//energySpentByEvent += cr;	
				//energySpentTotal += cr;	
			}
		}
		
		//mensagem chegou ate o sink, incrementa a quantidade de msg recebidas pelo sink
		if((this.ID == message.getDestination()) && (message.getnHop() == this.ID)){
			this.setColor(Color.PINK);
			message = null;

			//ESTATISTICA
			setCount_rcv_ev_sink(getCount_rcv_ev_sink() + 1);
			return;
		}
	
		if(message.getnHop() == this.ID){
			message.setnHop(nextHop); // modifica quem e o proximo hop
			message.setPreviousHop(this.ID); //modifica qm foi o ultimo a enviar
			
			LoadAggregationEtxBet la = new LoadAggregationEtxBet(message);
			//setCountMsgAggr(getCountMsgAggr() + 1);
			la.startRelative(1, this);
		}
		
		//sem agregacao
		/*if((!isInAggregation()) && (message.getnHop() == this.ID)){
			
			setInAggregation(true);
			
			message.setnHop(nextHop); // modifica quem e o proximo hop
			//setCountMsgAggr(getCountMsgAggr() + 1);
			fwdEvent(message);
		}else if(message.getnHop() == this.ID){
			
			//setCountMsgAggr(getCountMsgAggr() + 1);
			message = null;	// todas as mensgagens agregadas sao descartadas
		}*/
		
		//com agregacao
		/*if((!isInAggregation()) && (message.getnHop() == this.ID)){
			System.out.println(this.ID+" agregando...");
			setInAggregation(true);
			
			message.setnHop(nextHop); // modifica quem e o proximo hop
			message.setPreviousHop(this.ID); //modifica qm foi o ultimo a enviar
			
			LoadAggregationEtxBet la = new LoadAggregationEtxBet(message);
			
			//setCountMsgAggr(getCountMsgAggr() + 1);
			la.startRelative(getIntervalAggr(), this); //getIntervalAggr() e o tempo para esperar por mensagens a ser agregadas
		}else if(message.getnHop() == this.ID){
			
			//ESTATISTICA
			setCountMsgAggr(getCountMsgAggr() + 1);
			setCount_all_msg_aggr(getCount_all_msg_aggr() + 1);
			System.out.println(this.ID+" agregou "+getCountMsgAggr());
			
			message = null;	// todas as mensgagens agregadas sao "descartadas"
		}*/
	}
	
	public void fwdEvent(PackEventEtxBet message){
		this.setColor(Color.WHITE);
		System.out.println(this.ID+" encaminhando pacote");
		//No vai encaminhar o pacote, entao deve voltar ao estado de "nao agregado mensagens"
		setInAggregation(false); 
		
		// comentar essa linha se quiser saber quantas mensagens cada nodo agregou
		//setCountMsgAggr(0); 
		
		setRcvAck(false);
		
		broadcast(message);
		
		ReFwdEventExtBet rfe = new ReFwdEventExtBet(message);
		rfe.startRelative(4, this);
		
		
		//ESTATISTICA
		broadcastCount++;
		setCount_all_broadcast(getCount_all_broadcast() + 1);
		
		energySpentTotal += cf + y * Math.pow(range, alfa);
		
		energySpentByEvent += cf + y * Math.pow(range, alfa);
	}
	
	public void reFwdEvent(PackEventEtxBet message){
		if(!isRcvAck()){
			System.out.println(this.ID+" reenviando pacote");
			broadcast(message);
			
			ReFwdEventExtBet rfe = new ReFwdEventExtBet(message);
			rfe.startRelative(4, this);
			
			//ESTATISTICA
			broadcastCount++;
			setCount_all_broadcast(getCount_all_broadcast() + 1);
			setCountDropPkt(getCountDropPkt() + 1);
			
			energySpentTotal += cf + y * Math.pow(range, alfa);
			
			energySpentByEvent += cf + y * Math.pow(range, alfa);
		}else{
			message = null;
			return;
		}
	}
	
	public void sendEvent(){
		System.out.println(this.ID+" mandei um evento");
		PackEventEtxBet pktEv = new PackEventEtxBet(this.ID, sinkID, nextHop, this.ID);
		broadcast(pktEv);
		
		ResendEventEtxBet re = new ResendEventEtxBet();
		re.startRelative(4, this);
		
		
		//ESTATISTICA
		broadcastCount++;
		setCount_all_broadcast(getCount_all_broadcast() + 1);
		
		energySpentTotal += cf + y * Math.pow(range, alfa);
		
		energySpentByEvent += cf + y * Math.pow(range, alfa);
	}
	
	public void resendEvent(){
		if(!isRcvAck()){
			sendEvent();
			setCountDropPkt(getCountDropPkt() + 1);
		}
	}
	
	public void startEvent(){
		setRcvAck(false);
		sendEvent();

		//ESTATISTICA
		setCount_all_ev_sent(getCount_all_ev_sent() + 1);	
	}
	
	public void sendAck(int destination){
		broadcast(new PackAckEtxBet(destination));
		
		//ESTATISTICA
		broadcastCount++;
		setCount_all_broadcast(getCount_all_broadcast() + 1);
		
		energySpentTotal += cf + y * Math.pow(range, alfa);
		
		energySpentByEvent += cf + y * Math.pow(range, alfa);
	}
	
	/*=============================================================
	 *                 Manipulando o pacote Ack
	 * ============================================================
	 */
	
	public void handlePackAck(PackAckEtxBet message){
		if(this.ID == 1){
			return;
		}
		
		if(message.getDestination() == this.ID){
			setRcvAck(true);
		}
	}
	 
	
	@Override
	public void preStep() {
		if(Global.currentTime == 2){
			/*if(isSendEvent()){
				StartEventEtxBet se = new StartEventEtxBet();
				int time = gerador.nextInt(1000) + 1000;
				System.out.println(this.ID+" emitiriar evento em: "+time);
				se.startRelative(time, this);
			}*/
	
			if(setNodesEv.contains(this.ID)){
				/*StartEventEtxBet se = new StartEventEtxBet();
				timeEvent += 1000;
				System.out.println(this.ID+" emitiriar evento em: "+timeEvent);
				se.startRelative(timeEvent, this);*/
				IniciaEvento ee = new IniciaEvento();
				timeEvent += 1000;
				System.out.println(this.ID+" emitiriar evento em: "+timeEvent);
				ee.startRelative(timeEvent, this);
				
	//			for(int i = 10; i < 300; i+=10){
	//				se = new StartEvent();
	//				//System.out.println(this.ID+" emitiriar evento em: "+(time+i)+"  "+i/10);
	//				se.startRelative(time+i, this);
	//				
	//			}
			}
		}
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
		setRcvAck(false);
		setSentMyReply(false);
		setSentMyHello(false);
		setInAggregation(false);
		setCountMsgAggr(0);
		setCount_rcv_ev_sink(0);
		setSendEvent(false);
		
		setSonsPathMap(new HashMap<Integer, Integer>());
		setSons(new ArrayList<Integer>());
		setNeighbors(new ArrayList<Integer>());

		if (this.ID == 1) {
			this.setColor(Color.BLUE);
			setRole(NodeRoleEtxBet.SINK);
			
			(new StartSimulationEtxBet()).startRelative(3, this);
			
			/*SendPackHelloEtxBet pkt = new SendPackHelloEtxBet(hops, 1, this.ID, this.ID);
			pkt.startRelative(2, this);*/
			/*Pack p = new Pack(this.hops, this.pathsToSink, this.ID, this.ID, this.sBet, TypeMessage.HELLO);
			
			PackTimer pTimer = new PackTimer(p);
			pTimer.startRelative(2, this);*/
			readConfigurationParameters();
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
		
		/*nNodesEv = (NumberNodes * ev) / 100;
		if(nNodesEv <= 0) nNodesEv = 1;
		//System.out.println(nNodesEv);
		
		while(setNodesEv.size() != nNodesEv){
			setNodesEv.add(new Integer(gerador.nextInt(NumberNodes-1) + 2));
		}*/
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
	
	@NodePopupMethod(menuText = "Enviar teste")
	public void enviarTest() {
		/*TimerTeste tt = new TimerTeste();
		tt.startRelative(1, this);*/
		EncaminhaEvento ee = new EncaminhaEvento(new PackEventEtxBet(this.ID, sinkID, nextHop, this.ID));
		ee.startRelative(1, this);
	}

	public void sendTeste(){
		PackTeste pkt = new PackTeste(nextHop);
		send(pkt, Tools.getNodeByID(nextHop));
	}

//	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
//		Integer a = new Integer(hops);
//		
//		String str = Integer.toString(this.ID) + "|" + Integer.toString(a) + "|" + pathsToSink;
//		/*str = str.concat("|" + getTotalLostPackets() + " " + getTotalLostPacktetsByMe());
//		str = str.concat("|" + getTotalPacktesSent() + " " + getTotalSentByMe());*/
//		if(this.ID == 1){
//			str +=  "|" + count_rcv_ev_sink;
//		}
//		super.drawNodeAsDiskWithText(g, pt, highlight, str, 7, Color.YELLOW);
//	}

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
		str = str.concat("etxPath "+etxPath+"\n");
		str = str.concat("PktAggr "+countMsgAggr+"\n");
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

	public static int getCount_rcv_ev_sink() {
		return count_rcv_ev_sink;
	}

	public static void setCount_rcv_ev_sink(int count_rcv_ev_sink) {
		NodeEtxBet.count_rcv_ev_sink = count_rcv_ev_sink;
	}

	public static int getCount_all_broadcast() {
		return count_all_broadcast;
	}

	public static void setCount_all_broadcast(int count_all_broadcast) {
		NodeEtxBet.count_all_broadcast = count_all_broadcast;
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

	public double getEtxToMeFromNode(int nodeID) {
		Iterator<Edge> it2 = this.outgoingConnections.iterator();
		EdgeWeightEtxBet e;
		while (it2.hasNext()) {
			e = (EdgeWeightEtxBet) it2.next();
			if (e.endNode.ID == nodeID){
				e = (EdgeWeightEtxBet) e.getOppositeEdge();
				return e.getETX();
			}
		}
		return 0.0;
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

	public static int getCountDropPkt() {
		return countDropPkt;
	}

	public static void setCountDropPkt(int countDropPkt) {
		NodeEtxBet.countDropPkt = countDropPkt;
	}

	public static double getEnergySpentTotal() {
		return energySpentTotal;
	}

	public static void setEnergySpentTotal(double energySpentTotal) {
		NodeEtxBet.energySpentTotal = energySpentTotal;
	}

	public static double getEnergySpentByEvent() {
		return energySpentByEvent;
	}

	public static void setEnergySpentByEvent(double energySpentByEvent) {
		NodeEtxBet.energySpentByEvent = energySpentByEvent;
	}

	public static int getCount_all_msg_sent() {
		return count_all_msg_sent;
	}

	public static void setCount_all_msg_sent(int count_all_msg_sent) {
		NodeEtxBet.count_all_msg_sent = count_all_msg_sent;
	}

	public boolean isRcvAck() {
		return rcvAck;
	}

	public void setRcvAck(boolean rcvAck) {
		this.rcvAck = rcvAck;
	}

	public static int getCount_all_msg_aggr() {
		return count_all_msg_aggr;
	}

	public static void setCount_all_msg_aggr(int count_all_msg_aggr) {
		NodeEtxBet.count_all_msg_aggr = count_all_msg_aggr;
	}

	public static int getCount_all_overhead() {
		return count_all_overhead;
	}

	public static void setCount_all_overhead(int count_all_overhead) {
		NodeEtxBet.count_all_overhead = count_all_overhead;
	}

	public boolean isSendEvent() {
		return sendEvent;
	}

	public void setSendEvent(boolean sendEvent) {
		this.sendEvent = sendEvent;
	}

	public int getTimeEvent() {
		return timeEvent;
	}

	public void setTimeEvent(int timeEvent) {
		this.timeEvent = timeEvent;
	}

	public int getBroadcastCount() {
		return broadcastCount;
	}

	public void setBroadcastCount(int broadcastCount) {
		this.broadcastCount = broadcastCount;
	}
	
	
	
	
	
}

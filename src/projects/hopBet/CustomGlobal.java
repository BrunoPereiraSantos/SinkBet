/*
 Copyright (c) 2007, Distributed Computing Group (DCG)
                    ETH Zurich
                    Switzerland
                    dcg.ethz.ch

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

 - Neither the name 'Sinalgo' nor the names of its contributors may be
   used to endorse or promote products derived from this software
   without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package projects.hopBet;


import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.Math;

import javax.swing.JOptionPane;

import projects.defaultProject.models.reliabilityModels.LossyDelivery;
import projects.hopBet.nodes.edges.EdgeWeightHopSbet;
import projects.hopBet.nodes.nodeImplementations.NodeHopSbet;
import projects.hopBet.nodes.nodeImplementations.NodeRoleHopSbet;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.runtime.Global;
import sinalgo.runtime.Runtime;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

/**
 * This class holds customized global state and methods for the framework. 
 * The only mandatory method to overwrite is 
 * <code>hasTerminated</code>
 * <br>
 * Optional methods to override are
 * <ul>
 * <li><code>customPaint</code></li>
 * <li><code>handleEmptyEventQueue</code></li>
 * <li><code>onExit</code></li>
 * <li><code>preRun</code></li>
 * <li><code>preRound</code></li>
 * <li><code>postRound</code></li>
 * <li><code>checkProjectRequirements</code></li>
 * </ul>
 * @see sinalgo.runtime.AbstractCustomGlobal for more details.
 * <br>
 * In addition, this class also provides the possibility to extend the framework with
 * custom methods that can be called either through the menu or via a button that is
 * added to the GUI. 
 */
public class CustomGlobal extends AbstractCustomGlobal{
	
	
	
	private Logging myLogHopSbet;
	private int NumberNodes = 0, idTopology = 0, rMax = 0;
	
	
	public void preRun() {
		
		try {
			NumberNodes = Configuration.getIntegerParameter("NumberNodes");
			idTopology = Configuration.getIntegerParameter("idTopology");
			rMax = Configuration.getIntegerParameter("UDG/rMax");
			//System.out.println(NumberNodes);
			//System.out.println(ev);
		} catch (CorruptConfigurationEntryException e) {
			Tools.fatalError("Alguma das variaveis (NumberNodes, idTopology) nao estao presentes no arquivo de configuracao ");
		}
		
		// colocar true como segundo parametro (append) quando for rodar mais simulacoes
		// usa o arquivo que está no config.xml
		myLogHopSbet = Logging.getLogger("logHopSbet_"+NumberNodes+".txt", true);	// false caso for ler estes valores pelo CTDistribuido
	}
	
	public void onExit() {
		int numberOfNodes = Tools.getNodeList().size();
		DecimalFormat format = new DecimalFormat();  
	    format.setMaximumFractionDigits(3);  
	    format.setMinimumFractionDigits(3);
		String str = "";
		
		/*str += "N=" + numberOfNodes;
		str += " rcvSink="+NodeHopSbet.getCount_rcv_ev_sink();
		str += " allMsg_HeR="+NodeHopSbet.getCount_all_msg_sent();
		str += " allPkt="+NodeHopSbet.getCount_all_broadcast();
		str += " AllpktEv=" + NodeHopSbet.getCount_all_ev_sent();
		//str += " PktAggr=" + NodeHopSbet.getCount_all_msg_aggr();
		str += " PktAggr=" + (NodeHopSbet.getCount_all_ev_sent() - NodeHopSbet.getCount_rcv_ev_sink());
		str += " intervalAggr=" + NodeHopSbet.getIntervalAggr();
		str += " ev=" + NodeHopSbet.getEv();
		str += " nNodesEv=" + NodeHopSbet.getnNodesEv();
		str += " NumberNodes=" + NodeHopSbet.getNumberNodes();
		str += " PktDrop=" + NodeHopSbet.getCountDropPkt();
		str += " Energy=" + format.format(NodeHopSbet.getEnergySpentTotal());
		str += " EnergyEvent=" + format.format(NodeHopSbet.getEnergySpentByEvent());
		str += " id=" + idTopology;*/
		
		Iterator<Node> it = Tools.getNodeList().iterator();
		NodeHopSbet n;
		int qntBroadcastEv = 0;
		while(it.hasNext()){
			n = (NodeHopSbet) it.next();
			qntBroadcastEv += n.getCount_all_broadcast_event();
		}
		
		
		str += idTopology;
		str += " "+numberOfNodes;
		//str += " " + NodeHopSbet.getNumberNodes();
		str += " "+NodeHopSbet.getCount_rcv_ev_sink();
		str += " "+NodeHopSbet.getCount_all_msg_sent();
		str += " "+NodeHopSbet.getCount_all_broadcast();
		str += " "+NodeHopSbet.getCount_all_overhead();
		str += " "+qntBroadcastEv;
		str += " " + NodeHopSbet.getCount_all_ev_sent();
		str += " " + NodeHopSbet.getCount_all_msg_aggr();
		//str += " PktAggr=" + NodeHopSbet.getCount_all_msg_aggr();
		//str += " " + (NodeHopSbet.getCount_all_ev_sent() - NodeHopSbet.getCount_rcv_ev_sink());
		str += " " + NodeHopSbet.getIntervalAggr();
		str += " " + NodeHopSbet.getEv();
		//str += " " + NodeHopSbet.getnNodesEv();
		str += " " + NodeHopSbet.getCountDropPkt();
		str += " " + format.format(NodeHopSbet.getEnergySpentTotal());
		str += " " + format.format(NodeHopSbet.getEnergySpentByEvent());
		
		
		myLogHopSbet.logln(str);
		
		
//		ArrayList<Integer> listPackSent = new ArrayList<Integer>();
//		ArrayList<Integer> listPackLost = new ArrayList<Integer>();
//		
//		for(int i = 1; i <= numberOfNodes; i++){
//			str = "";
//			n = (NodeHopSbet) Tools.getNodeByID(i);
//			ArrayList<Integer> a = new ArrayList<Integer>(n.getSonsPathMap().values());
//			str += "ID=" + i + "; ";
//			str += "Hops=" + n.getHops() + "; ";
//			if(i != 1) str += "nHop=" + n.getNextHop() + "; "; else str += "nHop=" + 0 + "; ";
//			str += "lostPackts=" + n.getTotalLostPacktetsByMe() + "; ";
//			listPackLost.add(n.getTotalLostPacktetsByMe());
//			str += "sentPackts=" + n.getTotalSentByMe() + "; ";
//			listPackSent.add(n.getTotalSentByMe());
//			str += "sBet=" + n.getsBet() + "; ";
//			str += "a.size()=" + a.size() + "; ";
//			str += "sons.size()=" + n.getSons().size() + "; \n";
//			myLogHopSbet.logln(str);
//			if(n.getRole() == NodeRoleHopSbet.BORDER)
//				numberOfBordes++;			
//		}
//		
//		myLogHopSbet.logln("numberOfBordes=" + numberOfBordes);
//		myLogHopSbet.logln("numberOfRelay=" + (numberOfNodes - numberOfBordes - 1) + "\n\n");
//		
//		myLogHopSbet.logln("TotalPacktesSent=" + NodeHopSbet.getTotalPacktesSent());
//		myLogHopSbet.logln("media de pkt send=" + m.mean(listPackSent));
//		myLogHopSbet.logln("sd de pkt send=" + m.sd(listPackSent));
//		myLogHopSbet.logln("var de pkt send=" + m.var(listPackSent)+ "\n\n");
//
//		myLogHopSbet.logln("TotalLostPackets=" + NodeHopSbet.getTotalLostPackets());
//		myLogHopSbet.logln("media de pkt lost=" + m.mean(listPackLost));
//		myLogHopSbet.logln("sd de pkt lost=" + m.sd(listPackLost));
//		myLogHopSbet.logln("var de pkt lost=" + m.var(listPackLost)+ "\n\n");
			
//		for (int i = 1; i <= numberOfNodes; i++) {
//			NodeHopSbet n = (NodeHopSbet) Tools.getNodeByID(i);
//			if (n.getHops() >= 0) {
//				List<Integer> a = new ArrayList<Integer>(n.getSonsPathMap().values());
//				if(a.size() > 0){
//					methods m = new methods();
//					
//					myLogHopSbet.logln(Integer.toString(Tools.getNodeList().size()) + "\t" +
//								Integer.toString(i) + " \t" +
//								Integer.toString(n.getHops()) + "\t" +
//								Integer.toString(a.size()) + "\t" +
//								Integer.toString(Collections.min(a)) + "\t" +
//								Double.toString(m.median(a)) + "\t" +
//								Double.toString(m.mean(a)) + "\t" +
//								Integer.toString(Collections.max(a)) + "\t" +
//								/*Double.toString(m.sd(a))*/
//								Integer.toString(NodeHopSbet.getTotalLostPackets()) + "\t" +
//								Integer.toString(n.getTotalSentByMe()) + "\t" +
//								Integer.toString(NodeHopSbet.getTotalPacktesSent()));
//				}else{
//					myLogHopSbet.logln(Integer.toString(NodeHopSbet.getTotalLostPackets()) + "\t" +
//										Integer.toString(n.getTotalSentByMe()) + "\t" +
//										Integer.toString(NodeHopSbet.getTotalPacktesSent()));
//				}
//			}
//		}
	}
	
	
	public void postRound(){
		if(Global.currentTime == 1){
			
			try {  
		        Process p = java.lang.Runtime.getRuntime().exec("pwd");
		        BufferedReader stdInput = new BufferedReader(new  InputStreamReader(p.getInputStream()));
		        String s, s1;
		        
		        if((s = stdInput.readLine()) != null){
		        	s += "/topology/";
		    		s += idTopology;
		    		s += "_"+Configuration.dimX+"X"+Configuration.dimY;
		    		s += "_"+NumberNodes;
		    		s += "_"+rMax;
	    			s1 = s;
		    		
		    		s += "_conection.txt";
		    		s1 += "_NodesEvents.txt";
		    		File arquivo = new File(s);
		        	File arquivo1 = new File(s1);
		        	
		        	if (!arquivo.exists()) {
		        		System.out.println("Erro arquivo nao existe"); 
	        		}
		        	
		        	if (!arquivo1.exists()) {
		        		System.out.println("Erro arquivo nao existe"); 
	        		}
		        	
		        	//faz a leitura do arquivo
		        	FileReader fr = new FileReader(arquivo);
		        	 
		        	BufferedReader br = new BufferedReader(fr);
		        	
		        	//equanto houver mais linhas
		        	while (br.ready()) {
		        		//lê a proxima linha
		        		
		        		String linha = br.readLine();
			        	String[] vet = linha.split(" ");
			        	//System.out.println(vet[0]+" "+vet[1]+"   "+vet[2]);
			        	int id_u, id_v;
			        	id_u = Integer.parseInt(vet[0]);
			        	id_v = Integer.parseInt(vet[1]);
			        	double etx;
			        	etx = Double.parseDouble(vet[2]);
			        	
			        	System.out.println(id_u+" "+id_v+"    "+etx);
		        		
		        		Node u, v;
		        		u = Tools.getNodeByID(id_u);
		        		v = Tools.getNodeByID(id_v);
		        		
		        		Iterator<Edge> it = u.outgoingConnections.iterator();
		        		
		        		while(it.hasNext()){
		        			EdgeWeightHopSbet e = (EdgeWeightHopSbet) it.next();
		        			if(e.startNode.equals(u) && e.endNode.equals(v)){
		        				e.setETX(etx);
		        			}
		        		}
		        		
		        		
		        		//faz algo com a linha
		        		//System.out.println(id_u+" "+id_v+"   "+etx);
		        		
		        	}
		        	
		        	
		        	br.close();
		        	fr.close();
		        	
		        	//faz a leitura do arquivo
		        	fr = new FileReader(arquivo1);
		        	 
		        	br = new BufferedReader(fr);
		        	
		        	//equanto houver mais linhas
		        	while (br.ready()) {
		        		String linha = br.readLine();
		        		String[] vet = linha.split(" ");
		        		
		        		int id = Integer.parseInt(vet[0]);
		        		int time = Integer.parseInt(vet[1]);
		        		//System.out.println(id);
		        		//NodeHopSbet n = (NodeHopSbet) Tools.getNodeByID(id);
		        		//n.setSendEvent(true);
		        		NodeHopSbet.getSetNodesEv().add(new Integer(id));
		        		NodeHopSbet n = (NodeHopSbet) Tools.getNodeByID(id);
		        		n.setTimeEvent(time);
		        	}
		        	
		        	br.close();
		        	fr.close();
		        }
		       
		    } catch(Exception e) {  
		        System.out.println(e.toString());  
		        e.printStackTrace();  
		    }  
		}
	}
	
	/* (non-Javadoc)
	 * @see runtime.AbstractCustomGlobal#hasTerminated()
	 */
	public boolean hasTerminated() {
		return false;
	}

	/**
	 * An example of a method that will be available through the menu of the GUI.
	 */
	@AbstractCustomGlobal.GlobalMethod(menuText="Echo")
	public void echo() {
		// Query the user for an input
		String answer = JOptionPane.showInputDialog(null, "This is an example.\nType in any text to echo.");
		// Show an information message 
		JOptionPane.showMessageDialog(null, "You typed '" + answer + "'", "Example Echo", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * An example to add a button to the user interface. In this sample, the button is labeled
	 * with a text 'GO'. Alternatively, you can specify an icon that is shown on the button. See
	 * AbstractCustomGlobal.CustomButton for more details.   
	 */
	/*@AbstractCustomGlobal.CustomButton(buttonText="GO", toolTipText="A sample button")
	public void sampleButton() {
		JOptionPane.showMessageDialog(null, "You Pressed the 'GO' button.");
	}*/
	
	/**
	 * remove the markings from all nodes
	 */
	@AbstractCustomGlobal.CustomButton(buttonText="unmark", toolTipText="unmarks all nodes")
	public void unMark() {
		
		for(Node n : Tools.getNodeList()) {
			n.setColor(Color.BLACK);
		}
		Tools.repaintGUI();
	}
	
	
	
	/**
	 * Dummy button to create a tree.  
	 */
	@AbstractCustomGlobal.CustomButton(buttonText="ETX", toolTipText="Insert ETX")
	public void Button() {
		//int numNodes = Integer.parseInt(Tools.showQueryDialog("Number of nodes:"));
		//int fanOut = Integer.parseInt(Tools.showQueryDialog("Max fanout:"));
		//buildTree(fanOut, numLeaves);
		insertETX(); 
	}
	
	public void insertETX(){
		Iterator<Node> it = Runtime.nodes.iterator();
		NodeHopSbet n;
		/*Iterator<Node> it = Runtime.nodes.iterator();
		Node n;
		Random generator = new Random(1);
		while(it.hasNext()){
			n = it.next();
			System.out.println(n);
			Iterator<Edge> it2 = n.outgoingConnections.iterator();
			EdgeWeightHopSbet e;
			while(it2.hasNext()){
				e = (EdgeWeightHopSbet) it2.next();
				//e.setETX(UniformDistribution.nextUniform(0, 1));
				//e.setETX(generator.nextDouble());
				e.setETX(1+generator.nextInt(9));

			}
		}*/
		
		while(it.hasNext()){
			n = (NodeHopSbet) it.next();
			//System.out.println(n);
			Iterator<Edge> it2 = n.outgoingConnections.iterator();
			EdgeWeightHopSbet e;
			while(it2.hasNext()){
				e = (EdgeWeightHopSbet) it2.next();
				if(n.ID == 1){
					if(e.endNode.ID == 2)
						e.setETX(2);
				}
				
				if(n.ID == 2){
					if(e.endNode.ID == 1)
						e.setETX(1);
					if(e.endNode.ID == 3)
						e.setETX(1);
					if(e.endNode.ID == 4)
						e.setETX(1);
				}

				if(n.ID == 3){
					if(e.endNode.ID == 2)
						e.setETX(2);
					if(e.endNode.ID == 5)
						e.setETX(1);
					if(e.endNode.ID == 6)
						e.setETX(1);
				}

				if(n.ID == 4){
					if(e.endNode.ID == 2)
						e.setETX(2);
					if(e.endNode.ID == 5)
						e.setETX(1);
				}

				if(n.ID == 5){
					if(e.endNode.ID == 3)
						e.setETX(2);
					if(e.endNode.ID == 4)
						e.setETX(2);
					if(e.endNode.ID == 7)
						e.setETX(1);
					if(e.endNode.ID == 8)
						e.setETX(1);
				}

				if(n.ID == 6){
					if(e.endNode.ID == 3)
						e.setETX(9);
					if(e.endNode.ID == 7)
						e.setETX(1);
				}

				if(n.ID == 7){
					if(e.endNode.ID == 5)
						e.setETX(2);
					if(e.endNode.ID == 6)
						e.setETX(1);
					if(e.endNode.ID == 8)
						e.setETX(2);
				}

				if(n.ID == 8){
					if(e.endNode.ID == 5)
						e.setETX(4);
					if(e.endNode.ID == 7)
						e.setETX(2);
					if(e.endNode.ID == 9)
						e.setETX(2);
				}

				if(n.ID == 9){
					if(e.endNode.ID == 8)
						e.setETX(1);
				}
				
				//e.setETX(1+generator.nextInt(9));
				System.out.println("ID "+ e.getID()+"      " +n.ID+" ~["+e.getETX()+"]> "+e.endNode.ID);

			}
		}
	}
	
	/**
	 * Dummy button to create a tree.  
	 */
	@AbstractCustomGlobal.CustomButton(buttonText="Show ETX", toolTipText="Show ETX")
	public void Button2() {
		//int numNodes = Integer.parseInt(Tools.showQueryDialog("Number of nodes:"));
		//int fanOut = Integer.parseInt(Tools.showQueryDialog("Max fanout:"));
		//buildTree(fanOut, numLeaves);
		showETX(); 
	}
	
	public void showETX(){
		Iterator<Node> it = Runtime.nodes.iterator();
		Node n;
		while(it.hasNext()){
			n = it.next();
			Iterator<Edge> it2 = n.outgoingConnections.iterator();
			EdgeWeightHopSbet e;
			while(it2.hasNext()){
				e = (EdgeWeightHopSbet) it2.next();
				System.out.println("start["+e.startNode.ID+"] -> end["+e.endNode.ID+"] ETX = " + e.getETX());
			}
		}
		
		/*Vector<Integer> vec = new Vector<Integer>(5);
		Integer b = new Integer(0);
		Integer c = new Integer(2);
		for(int i = 0; i < vec.capacity(); i++)
			vec.add(i, b);
		
		//vec.insertElementAt(b, 10);
		Iterator<Integer> it5 = vec.iterator();
		it5 = vec.iterator();
		while(it5.hasNext()){
			Integer a = it5.next();
			System.out.println(""+a);
		}
		
		vec.set(2, c);
		System.out.println("");
		it5 = vec.iterator();
		while(it5.hasNext()){
			Integer a = it5.next();
			System.out.println(""+a);
		}*/
	}
	
	
	
	/**
	 * Dummy button to create a tree.  
	 */
	@AbstractCustomGlobal.CustomButton(buttonText="Graphics", toolTipText="Show Graphics")
	public void Button3() {
		//int numNodes = Integer.parseInt(Tools.showQueryDialog("Number of nodes:"));
		//int fanOut = Integer.parseInt(Tools.showQueryDialog("Max fanout:"));
		//buildTree(fanOut, numLeaves);
		printGraphicsINGuI(); 
	}
	
	public void printGraphicsINGuI(){
		Vector<NodeHopSbet> myNodes = new Vector<NodeHopSbet>();
		
		NodeHopSbet n = new NodeHopSbet();
		n.setPosition(300, 500, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new NodeHopSbet();
		n.setPosition(350, 500, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		
		n = new NodeHopSbet();
		n.setPosition(400, 450, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new NodeHopSbet();
		n.setPosition(400, 550, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new NodeHopSbet();
		n.setPosition(450, 500, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new NodeHopSbet();
		n.setPosition(450, 400, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new NodeHopSbet();
		n.setPosition(500, 450, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		
		n = new NodeHopSbet();
		n.setPosition(500, 500, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new NodeHopSbet();
		n.setPosition(550, 550, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		// Repaint the GUI as we have added some nodes
		Tools.repaintGUI();
		
	}

	/**
	 * Dummy button to create a tree.  
	 */
	@AbstractCustomGlobal.CustomButton(buttonText="NodeValues", toolTipText="Show NodeValues")
	public void Button4() {
		
		Iterator<Node> it = Runtime.nodes.iterator();
		NodeHopSbet n = null;
		
		while(it.hasNext()){
			n = (NodeHopSbet) it.next();
			System.out.println(n);
			System.out.println("\n\n");
		}
		
	}
	
	/**
	 * Dummy button to create a tree.  
	 */
	@AbstractCustomGlobal.CustomButton(buttonText="TypeNodes", toolTipText="Show TypeNodes")
	public void Button5() {
		
		Iterator<Node> it = Runtime.nodes.iterator();
		NodeHopSbet n = null;
		
		while(it.hasNext()){
			n = (NodeHopSbet) it.next();
			if(n.getRole().equals(NodeRoleHopSbet.BORDER))
				n.setColor(Color.ORANGE);
			if(n.getRole().equals(NodeRoleHopSbet.RELAY))
				n.setColor(Color.PINK);
		}
		
		// Repaint the GUI as we have added some nodes
		Tools.repaintGUI();
	}
	
	
	
	/**
	 * Dummy button to create a tree.  
	 */
	@AbstractCustomGlobal.CustomButton(buttonText="show outgoingE", toolTipText="Show OGE")
	public void Button7() {
		//int numNodes = Integer.parseInt(Tools.showQueryDialog("Number of nodes:"));
		//int fanOut = Integer.parseInt(Tools.showQueryDialog("Max fanout:"));
		//buildTree(fanOut, numLeaves);
		showOGE(); 
	}

	private void showOGE() {
		Iterator<Node> it = Runtime.nodes.iterator();
		NodeHopSbet n;
		
		while(it.hasNext()){
			n = (NodeHopSbet) it.next();
			System.out.println(n);
			System.out.println("\n\n");
		}
		
	}
	
	/**
	 * Dummy button to create a tree.  
	 */
	@AbstractCustomGlobal.CustomButton(buttonText="Reliability", toolTipText="Set Reliability")
	public void Button8() {
		Iterator<Node> it = Runtime.nodes.iterator();
		NodeHopSbet n;
		
		while(it.hasNext()){
			n = (NodeHopSbet) it.next();
			n.setReliabilityModel(new LossyDelivery());
		}
	}
}

class methods {
    public int sum (ArrayList<Integer> a){
        if (a.size() > 0) {
        	int sum = 0;

            for (Integer i : a) {
                sum += i;
            }
            return sum;
        }
        return 0;
    }
    public double mean (ArrayList<Integer> a){
    	double sum = sum(a);
        double mean = 0.0;

        if (sum > 0) {
            mean = (double)(sum / a.size());
        }
        return mean;
    }
    public double median (ArrayList<Integer> a){
    	Collections.sort(a);
    	int middle = a.size()/2;

        if (a.size() % 2 == 1) {
            return (double) a.get(middle);
        } else {
           return (double) ((double)(a.get(middle-1) + a.get(middle))) / (double)2.0;
        }
    }
    public double sd (ArrayList<Integer> a){
        double sum = 0.0;
        double mean = (double) mean(a);

        for (Integer i : a)
            sum += Math.pow((i - mean), 2);
        return Math.sqrt(  ((double) sum / (double)( a.size() - 1 )) ); // sample
    }
    
    public double var (ArrayList<Integer> a){
        double sum = 0.0;
        double mean = mean(a);

        for (Integer i : a)
            sum += Math.pow((i - mean), 2);
        return (double) sum / (double)( a.size() - 1 ); // sample
    }
}

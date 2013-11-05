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
package projects.Distribution;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;

import projects.Distribution.nodes.edges.EdgeETX;
import projects.Distribution.nodes.nodeImplementations.SimpleNode;
import projects.hopBet.nodes.edges.EdgeWeightHopSbet;
import projects.hopBet.nodes.nodeImplementations.NodeHopSbet;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
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
	
	private Logging Topology;
	private Logging conectionEtx;
	private Logging NodesEvents;
	private int NumberNodes = 0, idTopology = 0, rMax = 0;
	
	private int ev = 0;	// % de nodes que vao emitir eventos
	//variavel para gerar numeros aleatorios
	private Random gerador = new Random();
	//variavel para indicar quais nodos emitirao eventos
	private static Set<Integer>  setNodesEv = new HashSet<Integer>();
	
	public void preRun() {
		
		try {
			NumberNodes = Configuration.getIntegerParameter("NumberNodes");
			idTopology = Configuration.getIntegerParameter("idTopology");
			rMax = Configuration.getIntegerParameter("UDG/rMax");
			ev = Configuration.getIntegerParameter("EV");
			//System.out.println(NumberNodes);
			//System.out.println(ev);
		} catch (CorruptConfigurationEntryException e) {
			Tools.fatalError("Alguma das variaveis (NumberNodes, idTopology) nao estao presentes no arquivo de configuracao ");
		}
		
		String str = "";
		str += idTopology;
		str += "_"+Configuration.dimX+"X"+Configuration.dimY;
		str += "_"+NumberNodes;
		str += "_"+rMax;
		
		Topology = Logging.getLogger(str+"_topology.txt", true);
		
		conectionEtx = Logging.getLogger(str+"_conection.txt", true);
		
		
		NodesEvents = Logging.getLogger(str+"_NodesEvents.txt", true);
		
		int nNodesEv = (NumberNodes * ev) / 100;
		if(nNodesEv <= 0) nNodesEv = 1;
		//System.out.println(nNodesEv);
		
		while(setNodesEv.size() != nNodesEv){
			setNodesEv.add(new Integer(gerador.nextInt(NumberNodes-1) + 2));
		}
		Iterator<Integer> it = setNodesEv.iterator();
		while(it.hasNext()){
			Integer idNode = (Integer) it.next();
			NodesEvents.logln(idNode.toString()+" "+gerador.nextInt(1000));
		}
		
	}
	
	
	public void postRound(){
		if(Global.currentTime == 5){
			
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
		        			EdgeETX e = (EdgeETX) it.next();
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
		        		System.out.println(id+" "+time);
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

		/*Iterator<Node> it = Tools.getNodeList().iterator();
		String str;
		it = Tools.getNodeList().iterator();
		while(it.hasNext()){
			Node n = it.next();
			str = "";
			Iterator<Edge> itEdg = n.outgoingConnections.iterator();
			
			while(itEdg.hasNext()){
				EdgeETX e = (EdgeETX)itEdg.next();
				str += n.ID;
				str += " -["+e.getETX()+"]-> ";
				str += e.endNode.ID+"\n";
			}	
			//System.out.println(str);
		}*/
		return false;
	}

	public void onExit() {
		Iterator<Node> it = Tools.getNodeList().iterator();
		String str, aux;
		while(it.hasNext()){
			Node n = it.next();
			str = "";
			aux = "";
			
			aux = n.getPosition().toString();
			aux = aux.replace("[", "");
			aux = aux.replace("]", "");
			aux = aux.replace(", ", " ");
			str += aux;
			Topology.logln(str);
		}
		
		
		it = Tools.getNodeList().iterator();
		while(it.hasNext()){
			Node n = it.next();
			str = "";
			Iterator<Edge> itEdg = n.outgoingConnections.iterator();
			
			while(itEdg.hasNext()){
				EdgeETX e = (EdgeETX)itEdg.next();
				str += n.ID;
				str += " "+e.endNode.ID;
				str += " "+e.getETX()+"\n";
				
			}	
			//System.out.println(str);
			conectionEtx.log(str);
			
		}
		
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
	@AbstractCustomGlobal.CustomButton(buttonText="GO", toolTipText="A sample button")
	public void sampleButton() {
		JOptionPane.showMessageDialog(null, "You Pressed the 'GO' button.");
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
		Vector<SimpleNode> myNodes = new Vector<SimpleNode>();
		
		SimpleNode n = new SimpleNode();
		n.setPosition(300, 500, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new SimpleNode();
		n.setPosition(350, 500, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		
		n = new SimpleNode();
		n.setPosition(400, 450, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new SimpleNode();
		n.setPosition(400, 550, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new SimpleNode();
		n.setPosition(450, 500, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new SimpleNode();
		n.setPosition(450, 400, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new SimpleNode();
		n.setPosition(500, 450, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		
		n = new SimpleNode();
		n.setPosition(500, 500, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new SimpleNode();
		n.setPosition(550, 550, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		// Repaint the GUI as we have added some nodes
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
		SimpleNode n;
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
			n = (SimpleNode) it.next();
			//System.out.println(n);
			Iterator<Edge> it2 = n.outgoingConnections.iterator();
			EdgeETX e;
			while(it2.hasNext()){
				e = (EdgeETX) it2.next();
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
	
}

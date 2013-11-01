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
import java.util.Iterator;

import javax.swing.JOptionPane;

import projects.Distribution.nodes.edges.EdgeETX;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.nodes.edges.Edge;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.runtime.Global;
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
	private int NumberNodes = 0, idTopology = 0;
	
	public void preRun() {
		
		try {
			NumberNodes = Configuration.getIntegerParameter("NumberNodes");
			idTopology = Configuration.getIntegerParameter("idTopology");
			//System.out.println(NumberNodes);
			//System.out.println(ev);
		} catch (CorruptConfigurationEntryException e) {
			Tools.fatalError("Alguma das variaveis (NumberNodes, idTopology) nao estao presentes no arquivo de configuracao ");
		}
		
		String str = "";
		str += idTopology;
		str += "_"+Configuration.dimX+"X"+Configuration.dimY;
		str += "_"+NumberNodes;
		
		Topology = Logging.getLogger(str+"_topology.txt", true);
		
		conectionEtx = Logging.getLogger(str+"_conection.txt", true);
		
	}
	
	
	public void postRound(){
		if(Global.currentTime == 3){
			
			try {  
		        Process p = Runtime.getRuntime().exec("pwd");
		        BufferedReader stdInput = new BufferedReader(new  InputStreamReader(p.getInputStream()));
		        String s;
		        
		        if((s = stdInput.readLine()) != null){
		        	s += "/topology/";
		    		s += idTopology;
		    		s += "_"+Configuration.dimX+"X"+Configuration.dimY;
		    		s += "_"+NumberNodes;
		    		s += "_conection.txt";
		        	File arquivo = new File(s);
		        	
		        	if (!arquivo.exists()) {
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

		Iterator<Node> it = Tools.getNodeList().iterator();
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
		}
		return true;
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
}

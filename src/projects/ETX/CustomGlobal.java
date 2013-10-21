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
package projects.ETX;


import java.awt.Color;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JOptionPane;
import projects.ETX.nodes.edges.EdgeWeightETX;
import projects.ETX.nodes.nodeImplementations.NodeETX;
import projects.ETX.nodes.nodeImplementations.NodeRoleETX;
import projects.ETX.nodes.timers.StartBorderFloodingETX;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.runtime.Global;
import sinalgo.runtime.Runtime;
import sinalgo.tools.Tools;

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
		Node n;
		/*Random generator = new Random(1);
		while(it.hasNext()){
			n = it.next();
			//System.out.println(n);
			Iterator<Edge> it2 = n.outgoingConnections.iterator();
			EdgeWeightEtxBet e;
			while(it2.hasNext()){
				e = (EdgeWeightEtxBet) it2.next();
				//e.setETX(UniformDistribution.nextUniform(0, 1));
				//e.setETX(generator.nextDouble());
				e.setETX(1+generator.nextInt(9));
				System.out.println("ID "+n.ID+" ~["+e.getETX()+"]> "+e.endNode.ID);

			}
		}*/
		
		while(it.hasNext()){
			n = it.next();
			//System.out.println(n);
			Iterator<Edge> it2 = n.outgoingConnections.iterator();
			EdgeWeightETX e;
			while(it2.hasNext()){
				e = (EdgeWeightETX) it2.next();
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
			EdgeWeightETX e;
			while(it2.hasNext()){
				e = (EdgeWeightETX) it2.next();
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
		Global.currentTime = 1.0;
		insertETX();
	}
	
	public void printGraphicsINGuI(){
		Vector<NodeETX> myNodes = new Vector<NodeETX>();
		
		NodeETX n = new NodeETX();
		n.setPosition(300, 500, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new NodeETX();
		n.setPosition(350, 500, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		
		n = new NodeETX();
		n.setPosition(400, 450, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new NodeETX();
		n.setPosition(400, 550, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new NodeETX();
		n.setPosition(450, 500, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new NodeETX();
		n.setPosition(450, 400, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new NodeETX();
		n.setPosition(500, 450, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		
		n = new NodeETX();
		n.setPosition(500, 500, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new NodeETX();
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
		NodeETX n = null;
		
		while(it.hasNext()){
			n = (NodeETX) it.next();
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
		NodeETX n = null;
		
		while(it.hasNext()){
			n = (NodeETX) it.next();
			if(n.getRole().equals(NodeRoleETX.BORDER))
				n.setColor(Color.ORANGE);
			if(n.getRole().equals(NodeRoleETX.RELAY))
				n.setColor(Color.PINK);
		}
		
		// Repaint the GUI as we have added some nodes
		Tools.repaintGUI();
	}
	
	/**
	 * Dummy button to create a tree.  
	 */
	@AbstractCustomGlobal.CustomButton(buttonText="SendBorder", toolTipText="SendBorder")
	public void Button6() {
		Iterator<Node> it = Runtime.nodes.iterator();
		NodeETX n;
		while(it.hasNext()){
			n = (NodeETX) it.next();
			if(n.getRole() == NodeRoleETX.BORDER ){
				StartBorderFloodingETX sbf = new StartBorderFloodingETX();
				sbf.startRelative(0.1, n);
			}
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
		NodeETX n;
		
		while(it.hasNext()){
			n = (NodeETX) it.next();
			System.out.println(n);
			System.out.println("\n\n");
		}
		
	}
	
}

package projects.ETX.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;

import projects.ETX.nodes.edges.EdgeWeightETX;
import projects.ETX.nodes.messages.MarkMessage;
import projects.ETX.nodes.timers.MessageTimer;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

public class SimpleNodeETX extends Node {
	
	@Override
	public void handleMessages(Inbox inbox) {
		while(inbox.hasNext()) {
			Message m = inbox.next();
			if(m instanceof MarkMessage){
				MarkMessage msg = (MarkMessage) m;
				Iterator<Edge> it = this.outgoingConnections.iterator();
				EdgeWeightETX e;
				while (it.hasNext()) {
					e = (EdgeWeightETX) it.next();
					if((msg.getProbRecv() >= e.getETX()) && ((msg.getDestinationID() == e.endNode.ID))){
						this.setColor(Color.RED);
						System.out.println(this.ID+" ETX = "+e.getETX()+" msgETX "+msg.getProbRecv());
					}
				}
			}
		}
	}

	
	@NodePopupMethod(menuText = "Enviar msg")
	public void construirRoteamento() {
		Iterator<Edge> it = this.outgoingConnections.iterator();
		Edge e = it.next();
		int dest;
		
		if(e.endNode.ID == this.ID){
			dest = e.startNode.ID;
		}else{
			dest = e.endNode.ID;
		}
		
		MarkMessage msg = new MarkMessage((double)1.0, this.ID, dest);
		MessageTimer timer = new MessageTimer(msg);
		timer.startRelative(1, this);
	}
	
	@Override
	public void preStep() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		
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
	
	public void draw(Graphics g, PositionTransformation pt, boolean highlight){
		super.drawNodeAsDiskWithText(g, pt, highlight, Integer.toString(this.ID), 30, Color.YELLOW);
	}

}

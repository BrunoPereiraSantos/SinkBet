package projects.wsn.nodes.nodeImplementations;

import java.util.*;

import projects.wsn.nodes.messages.Proper;
import projects.wsn.nodes.messages.WsnMsg;
import projects.wsn.nodes.timers.TimerProper;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public class Consenso extends Node {
//	Cada nó escolhe um valor v ∈ [0, max] e difunde este valor para todos 
//	os demais nós na rede. Quando um nó tiver recebido o valor de 
//	todos os demais nós, escolhe o valor mais votado (ou no caso de 
//	empate, um valor default) como o valor de consenso d.
	
	// <a, b> a = id no nó, b = valor de v
	private Map<Integer, Integer> properSet;
	private Map<Integer, Integer> tmp;
	private int countMsg;
	
	
	
	@Override
	public void handleMessages(Inbox inbox) {
		// TODO Auto-generated method stub
		while (inbox.hasNext()) {
			Message message = inbox.next();
			if (message instanceof Proper) {
				Proper a = (Proper) message;
				countMsg++;
				
				
				tmp.putAll(a.getValues());
				if(countMsg == Tools.getNodeList().size() - 1){
					countMsg = 0;
					//handleProper(a);

					System.out.println("Id "+this.ID+" escolheu ->"+properSet+" Recebeu ->"+tmp);
				}
				
			}
		}
	}
	
	private void handleProper(Proper a) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preStep() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		Random generator = new Random();
		properSet = new HashMap<Integer, Integer>();
		properSet.put(this.ID, generator.nextInt(10));
		
		tmp = new HashMap<Integer, Integer>();
		
		countMsg = 0;
		
		Proper m = new Proper(properSet, this.ID);
		TimerProper tp = new TimerProper(m);
		tp.startRelative(2, this);
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
}

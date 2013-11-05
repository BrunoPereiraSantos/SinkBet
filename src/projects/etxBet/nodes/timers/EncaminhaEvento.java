package projects.etxBet.nodes.timers;

import projects.etxBet.nodes.messages.PackEventEtxBet;
import projects.etxBet.nodes.nodeImplementations.NodeEtxBet;
import sinalgo.nodes.timers.Timer;

public class EncaminhaEvento extends Timer {
	PackEventEtxBet pkt;
	
	
	
	/**
	 * @param pkt
	 */
	public EncaminhaEvento(PackEventEtxBet pkt) {
		super();
		this.pkt = pkt;
	}



	@Override
	public void fire() {
		// TODO Auto-generated method stub
		((NodeEtxBet) this.node).encaminhaEvento(this.pkt);
	}



	public PackEventEtxBet getPkt() {
		return pkt;
	}



	public void setPkt(PackEventEtxBet pkt) {
		this.pkt = pkt;
	}

	
}

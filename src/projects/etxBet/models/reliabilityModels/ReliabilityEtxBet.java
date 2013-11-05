package projects.etxBet.models.reliabilityModels;

import projects.etxBet.nodes.messages.PackEventEtxBet;
import projects.etxBet.nodes.messages.PackTeste;
import projects.etxBet.nodes.nodeImplementations.NodeEtxBet;
import sinalgo.models.ReliabilityModel;
import sinalgo.nodes.messages.Packet;

public class ReliabilityEtxBet extends ReliabilityModel {

	@Override
	public boolean reachesDestination(Packet p) {
		// TODO Auto-generated method stub
		if(p.message instanceof PackEventEtxBet){
			NodeEtxBet nDest = (NodeEtxBet) p.destination;
			p.denyDelivery();
			
			return false;
		}
		
		return true;
	}

}

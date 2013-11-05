package projects.hopBet.models.reliabilityModels;

import java.util.Random;

import projects.etxBet.nodes.nodeImplementations.NodeEtxBet;
import projects.hopBet.nodes.messages.PackEventHopSbet;
import projects.hopBet.nodes.nodeImplementations.NodeHopSbet;
import sinalgo.models.ReliabilityModel;
import sinalgo.nodes.messages.Packet;

public class ReliabilityHopSbet extends ReliabilityModel {
	
	//variavel para gerar numeros aleatorios
	private Random gerador = new Random();
	
	@Override
	public boolean reachesDestination(Packet p) {
		// TODO Auto-generated method stub
		if(p.message instanceof PackEventHopSbet){
			NodeHopSbet nOrigin = (NodeHopSbet) p.origin;
			NodeHopSbet nDest = (NodeHopSbet) p.destination;
			
			double prob = gerador.nextInt(100) + 1;
			System.out.println("NO "+nDest.ID+ " gerou um probabilidade p = "+prob);
			System.out.println("100 - etx = "+(100 - nOrigin.getEtxToNode(nDest.ID)));
			
			if((100 - nOrigin.getEtxToNode(nDest.ID)) < prob){
				System.out.println("NO "+nDest.ID+ " perdeu um pacote");
				return false;
			}else{
				System.out.println("NO "+nDest.ID+ " aceitou um pacote");
				return true;
			}
		}
			
		
		return true;
	}

}

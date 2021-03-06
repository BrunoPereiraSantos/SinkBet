package projects.etxBet.models.reliabilityModels;

import java.util.Random;

import projects.etxBet.nodes.messages.PackEventEtxBet;
import projects.etxBet.nodes.messages.PackTeste;
import projects.etxBet.nodes.nodeImplementations.NodeEtxBet;
import sinalgo.models.ReliabilityModel;
import sinalgo.nodes.messages.Packet;

public class ReliabilityEtxBet extends ReliabilityModel {

	//variavel para gerar numeros aleatorios
	private Random gerador = new Random();
	
	@Override
	public boolean reachesDestination(Packet p) {
		// TODO Auto-generated method stub
		if(p.message instanceof PackEventEtxBet){
			NodeEtxBet nOrigin = (NodeEtxBet) p.origin;
			NodeEtxBet nDest = (NodeEtxBet) p.destination;
			
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
			
			/*
			 * if(100 - getEtxToMeFromNode(message.getPreviousHop()) < p){
				System.out.println("perdeu um pacote");
				return;
			}else{
			 */
			
			/*p.denyDelivery();
			
			return false;*/
		}
		
		return true;
	}

}

package projects.Importance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import projects.Importance.nodes.nodeImplementations.ImportanceNode;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

public class CustomGlobal extends AbstractCustomGlobal{
	
	private Logging myLog;
	
	public boolean hasTerminated() {
		return false;
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
		
		Vector<ImportanceNode> myNodes = new Vector<ImportanceNode>();
		
		ImportanceNode n = new ImportanceNode();
		n.setPosition(300, 500, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new ImportanceNode();
		n.setPosition(350, 500, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		
		n = new ImportanceNode();
		n.setPosition(400, 450, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new ImportanceNode();
		n.setPosition(400, 550, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new ImportanceNode();
		n.setPosition(450, 500, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new ImportanceNode();
		n.setPosition(450, 400, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new ImportanceNode();
		n.setPosition(500, 450, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		
		n = new ImportanceNode();
		n.setPosition(500, 500, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		
		n = new ImportanceNode();
		n.setPosition(550, 550, 0);
		n.finishInitializationWithDefaultModels(true);
		myNodes.add(n);
		// Repaint the GUI as we have added some nodes
		Tools.repaintGUI();
		
	}
	
	public void preRun() {
		// colocar true como segundo parametro (append) quando for rodar mais simulacoes
		// usa o arquivo que está no config.xml
		myLog = Logging.getLogger("logImportance.txt", true);	// false caso for ler estes valores pelo CTDistribuido
	}
	
	public void onExit() {
		int numberOfNodes = Tools.getNodeList().size();
		int density = 0;
		int simulationID = 0;
		try {
			density = sinalgo.configuration.Configuration.getIntegerParameter("Density");
			simulationID = sinalgo.configuration.Configuration.getIntegerParameter("SimulationID");
		} catch (CorruptConfigurationEntryException e) {
			e.printStackTrace();
		}
		myLog.logln("N� \t ID \t Hops \t a.size() \t min \t mediana \t media \t max");
		for (int i = 1; i <= numberOfNodes; i++) {
			// Para gerar o valor de centralidade dos nós lido pelo CT
			// *Lembrar* de de colocar false na chamada do Logging.getLogger no preRun() 
/*			ImportanceNode n = (ImportanceNode) Tools.getNodeByID(i);
			myLog.logln(Integer.toString(i) + "\t" +
						Double.toString(n.getCentrality()));*/
			
			// Ver histograma dos paths (nao sei se esta funcionando)
/*			int simulationID = 0;
			try {
				simulationID = sinalgo.configuration.Configuration.getIntegerParameter("SimulationID");
			} catch (CorruptConfigurationEntryException e) {
				e.printStackTrace();
			}
			ImportanceNode n = (ImportanceNode) Tools.getNodeByID(i);
			if (n.isSink()) {
				Hashtable<Integer, Integer> pacote = new Hashtable<Integer, Integer>();
				pacote = n.getPacket();
				Iterator <Integer> it = pacote.keySet().iterator();
				while (it.hasNext()) {
					Integer path = it.next();
					Integer freq = pacote.get(path);
					myLog.logln(Integer.toString(simulationID) + "\t" +
								Integer.toString(Tools.getNodeList().size()) + "\t" +
								Integer.toString(path) + "\t" +
								Integer.toString(freq));
				}
			}
*/			
/*			// Para fazer o estudo do Importance, erro, tamanho do pacote, pacotes perdidos, etc 
			ImportanceNode n = (ImportanceNode) Tools.getNodeByID(i);
			// Erro relativo em %
			double delta = Math.abs(n.getCentrality() - n.getRcentrality());
			double error;
			if (delta > 0.0) {
				if (n.getRcentrality() > 0.0)
					error = (delta/n.getRcentrality()) * 100;
				else
					error = 0;
			} else {			// delta == 0.0
				error = 0.0;
			}
			
			// Nao precisa de verificar o Stress, uma vez já foi visto que ele tem erro 0
			double deltaStress = Math.abs(n.getRStress() - n.getHopsToSink());
			double errorStress;
			if (deltaStress > 0.0) {
				if (n.getRStress() > 0.0)
					errorStress = (deltaStress/n.getRStress()) * 100;
				else
					errorStress = 0;
			} else {			// delta == 0.0
				errorStress = 0.0;
			}

			myLog.logln(Integer.toString(i) + "\t" +
						Integer.toString(numberOfNodes) + "\t" +
						Integer.toString(density) + "\t" +
						Integer.toString(n.getHopsToSink()) + "\t" +
						Integer.toString(n.getPathsToSinkThroughMe()) + "\t" +
						Double.toString(n.getCentrality()) + "\t" +
						Double.toString(n.getRcentrality()) + "\t" +
						Double.toString(error) + "\t" +
						Integer.toString(n.getPacketSize()) + "\t" +
						Integer.toString(n.getLostPackets()) + "\t" +
						Integer.toString(n.getMaxPacketSize())
						);*/
			
			// Para verificar os valores de sonsPaths de cada nó
			/*ImportanceNode n = (ImportanceNode) Tools.getNodeByID(i);
			if (n.getHopsToSink() >= 2) {
				String s = "";
				for (Iterator iter = n.getPacket().values().iterator(); iter.hasNext();) {
					   Integer t = (Integer) iter.next();
					   s += t.toString() + " ";
				}
				myLog.logln(Integer.toString(i) + "\t" +
							Integer.toString(n.getHopsToSink()) + "\t" +
							s);	
			}*/
			// Para imprimir um resumo do sonsPaths de cada nó como um dataset.R com
			ImportanceNode n = (ImportanceNode) Tools.getNodeByID(i);
			if (n.getHopsToSink() >= 2) {
				
				List<Integer> a = new ArrayList<Integer>(n.getPacket().values());
				if (a.size() > 0) {																// tem alguma coisa no pacote
					methods m = new methods();

					myLog.logln(Integer.toString(Tools.getNodeList().size()) + "\t" +
								//Integer.toString(density) + "\t" +
								//.toString(simulationID) + "\t" +
								Integer.toString(i) + "\t" +
								Integer.toString(n.getHopsToSink()) + "\t" +
								Integer.toString(a.size()) + "\t" +
								Integer.toString(Collections.min(a)) + "\t" +
								Double.toString(m.median(a)) + "\t" +
								Double.toString(m.mean(a)) + "\t" +
								Integer.toString(Collections.max(a)) + "\t" /*+
								Double.toString(m.sd(a))*/);
				}
			}
			
		}
	}
}

class methods {
    public int sum (List<Integer> a){
        if (a.size() > 0) {
            int sum = 0;

            for (Integer i : a) {
                sum += i;
            }
            return sum;
        }
        return 0;
    }
    public double mean (List<Integer> a){
        int sum = sum(a);
        double mean = 0;

        if (sum > 0) {
            mean = sum / (a.size() * 1.0);
        }
        return mean;
    }
    public double median (List<Integer> a){
        int middle = a.size()/2;

        if (a.size() % 2 == 1) {
            return a.get(middle);
        } else {
           return (a.get(middle-1) + a.get(middle)) / 2.0;
        }
    }
    public double sd (List<Integer> a){
        int sum = 0;
        double mean = mean(a);

        for (Integer i : a)
            sum += Math.pow((i - mean), 2);
        return Math.sqrt( sum / ( a.size() - 1 ) ); // sample
    }
}

package projects.etxBet.models.distributionModels;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.models.DistributionModel;
import sinalgo.nodes.Position;
import sinalgo.tools.Tools;

public class ReadTopology extends DistributionModel {
	public static int index = 1;
	public int idTopology, dimX, dimY, NumberNodes, rMax;
	
	@Override
	public Position getNextPosition() {
		// TODO Auto-generated method stub
		if(index == 1){
			try {
				NumberNodes = Configuration.getIntegerParameter("NumberNodes");
				idTopology = Configuration.getIntegerParameter("idTopology");
				rMax = Configuration.getIntegerParameter("UDG/rMax");
				//System.out.println(NumberNodes);
				//System.out.println(ev);
			} catch (CorruptConfigurationEntryException e) {
				Tools.fatalError("Alguma das variaveis (NumberNodes, dimX, dimY, idTopology) nao estao presentes no arquivo de configuracao ");
			}
		}
		
		try {  
	        Process p = Runtime.getRuntime().exec("pwd");
	        BufferedReader stdInput = new BufferedReader(new  InputStreamReader(p.getInputStream()));
	        String s;
	        if((s = stdInput.readLine()) != null){
	        	s += "/topology/";
	    		s += idTopology;
	    		s += "_"+Configuration.dimX+"X"+Configuration.dimY;
	    		s += "_"+NumberNodes;
	    		s += "_"+rMax;
	    		s += "_topology.txt";
	        	File arquivo = new File(s);
	        	
	        	if (!arquivo.exists()) {
	        		System.out.println("Erro arquivo nao existe"); 
        		}
	        	
	        	//faz a leitura do arquivo
	        	FileReader fr = new FileReader(arquivo);
	        	 
	        	BufferedReader br = new BufferedReader(fr);
	        	
	        	double posX, posY, posZ;
	        	int cont = 1;
	        	//equanto houver mais linhas
	        	while ((br.ready())&&(cont < index)) {
	        		//lê a proxima linha
	        		br.readLine();
	        		cont++;
//	        		String linha = br.readLine();
//	        		String[] vet = linha.split(" ");
//	        		
//	        		posX = Double.parseDouble(vet[0]);
//	        		posY = Double.parseDouble(vet[1]);
//	        		posZ = Double.parseDouble(vet[2]);
//	        		
//	        		//faz algo com a linha
//	        		System.out.println(vet[0]+" "+vet[1]+" "+vet[2]);
//	        		return new Position(posX, posY, posZ);
	        	}
	        	
	        	String linha = br.readLine();
	        	String[] vet = linha.split(" ");
        		
	        	posX = Double.parseDouble(vet[0]);
        		posY = Double.parseDouble(vet[1]);
        		posZ = Double.parseDouble(vet[2]);
        		
        		//faz algo com a linha
        		//System.out.println(vet[0]+" "+vet[1]+" "+vet[2]+"    "+index);
        		index++;
	        	br.close();
	        	fr.close();
	        	
	        	
	        	return new Position(posX, posY, posZ);
	        }
	       
	    } catch(Exception e) {  
	        System.out.println(e.toString());  
	        e.printStackTrace();  
	    }  
		return null;
	}

}

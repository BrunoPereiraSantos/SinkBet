package projects.Distribution.models.distributionModels;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import sinalgo.models.DistributionModel;
import sinalgo.nodes.Position;

public class ReadTopology extends DistributionModel {

	public static int index = 1;
	
	@Override
	public Position getNextPosition() {
		// TODO Auto-generated method stub
		try {  
	        Process p = Runtime.getRuntime().exec("pwd");
	        BufferedReader stdInput = new BufferedReader(new  InputStreamReader(p.getInputStream()));
	        String s;
	        if((s = stdInput.readLine()) != null){
	        	s += "/topologias/";
	        	s += "Topology2.txt";
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

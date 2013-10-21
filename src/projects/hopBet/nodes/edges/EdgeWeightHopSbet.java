package projects.hopBet.nodes.edges;

import sinalgo.nodes.edges.Edge;

public class EdgeWeightHopSbet extends Edge {
	public double ETX;

	public double getETX() {
		return ETX;
	}

	public void setETX(double eTX) {
		ETX = eTX;
	}
	
	public String toString(){
		String str = "ETX link = ";
		str = str.concat(ETX+"");
		return str;
	}
	
}

package projects.hopBet.nodes.edges;

import sinalgo.nodes.edges.Edge;
import sinalgo.tools.statistics.UniformDistribution;

public class EdgeWeightHopSbet extends Edge {
	public double ETX;

	
	public EdgeWeightHopSbet() {
		super();
		UniformDistribution cte = new UniformDistribution(1, 100);
		setETX((int)cte.nextSample());
	}

	public double getETX() {
		return ETX;
	}

	public void setETX(double eTX) {
		ETX = eTX;
	}
	
	public String toString(){
		String str = "ETX link = ";
		str += ETX;
		return str;
	}
	
}

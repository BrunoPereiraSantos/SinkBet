package projects.etxBet.nodes.edges;

import sinalgo.nodes.edges.Edge;
import sinalgo.tools.statistics.UniformDistribution;

public class EdgeWeightEtxBet extends Edge {
	private double ETX;

	public EdgeWeightEtxBet() {
		super();
		UniformDistribution cte = new UniformDistribution(1, 10);
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

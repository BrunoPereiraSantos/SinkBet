package projects.ETX.nodes.edges;

import sinalgo.nodes.edges.Edge;
import sinalgo.tools.statistics.UniformDistribution;

public class EdgeWeightETX extends Edge {
	private double ETX;

	
	public EdgeWeightETX() {
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
		str = str.concat(ETX+"");
		return str;
	}
	
}

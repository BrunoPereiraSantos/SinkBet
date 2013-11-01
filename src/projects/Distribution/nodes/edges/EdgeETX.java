package projects.Distribution.nodes.edges;

import sinalgo.nodes.edges.Edge;
import sinalgo.tools.statistics.UniformDistribution;

public class EdgeETX extends Edge {
	private double ETX;

	/**
	 * @param eTX
	 */
	public EdgeETX() {
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

	@Override
	public String toString() {
		return "EdgeETX [ETX=" + ETX + "]";
	}
	
	
	
	
}

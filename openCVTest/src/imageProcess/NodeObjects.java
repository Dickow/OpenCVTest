package imageProcess;

public class NodeObjects {
	private String type; 
	private double x; 
	private double y; 
	
	public NodeObjects(double x, double y, String type){
		this.type = type; 
		this.x = x; 
		this.y = y; 
	}

	public String getType() {
		return type;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	
	public String toString(){
		return "x = " + x + " y = " + y;
	}
	
}

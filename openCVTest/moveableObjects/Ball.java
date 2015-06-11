package moveableObjects;

public class Ball extends Coordinate{
	
	private double radius;
	
	public Ball(double x, double y){
		super(x,y);
		
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}


}

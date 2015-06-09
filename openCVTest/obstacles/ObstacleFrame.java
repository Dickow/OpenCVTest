package obstacles;

import moveableObjects.Coordinate;

public class ObstacleFrame {
	
	Coordinate[] frameCorners = new Coordinate[4];
	
	public ObstacleFrame(){
		frameCorners[0] = new Coordinate(10,10);
		frameCorners[1] = new Coordinate(630,10);
		frameCorners[2] = new Coordinate(630, 470);
		frameCorners[3] = new Coordinate(10,470);
	}
	
	public Coordinate topLeft(){
		return frameCorners[0];
	}
	public Coordinate topRight(){
		return frameCorners[1];
	}
	public Coordinate lowRight(){
		return frameCorners[2];
	}
	public Coordinate lowLeft(){
		return frameCorners[3];
	}
	
	public void setTopLeft(Coordinate topLeft){
		frameCorners[0] = topLeft;
	}
	public void setTopRight(Coordinate topRight){
		frameCorners[1] = topRight; 
	}
	public void setLowRight(Coordinate lowRight){
		frameCorners[2] = lowRight;
	}
	public void setLowLeft(Coordinate lowLeft){
		frameCorners[3] = lowLeft;
	}
}

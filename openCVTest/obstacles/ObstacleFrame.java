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
}

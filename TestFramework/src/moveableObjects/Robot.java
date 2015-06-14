package moveableObjects;


import utilities.Vector;

public class Robot {

	private Coordinate frontCord;
	private Coordinate backCord;
	private Coordinate middleCord;
	private MoveState state;
	public final int robotRadius = 15;

	public Robot(Coordinate front, Coordinate back) {
		this.frontCord = front;
		this.backCord = back;
		double xMiddle = ((frontCord.getX() + backCord.getX()) / 2);
		double yMiddle = ((frontCord.getY() + backCord.getY()) / 2);
		this.middleCord = new Coordinate(xMiddle, yMiddle);
		
	}
	public Robot(){
		
	}
	
	public MoveState getState(){
		return this.state;
	}
	
	public void setState(MoveState state){
		this.state = state; 
	}
	
	public Coordinate getFrontCord() {
		return frontCord;
	}

	public void setFrontCord(Coordinate frontCord) {
		this.frontCord = frontCord;
	}

	public Coordinate getBackCord() {
		return backCord;
	}

	public void setBackCord(Coordinate backCord) {
		this.backCord = backCord;
	}

	public Coordinate getMiddleCord() {
		return middleCord;
	}

	public void setMiddleCord(Coordinate middleCord) {
		this.middleCord = middleCord;
	}
	
	public void updateMiddleCord() {
		middleCord = new Coordinate((frontCord.getX() + backCord.getX()) / 2,
				(frontCord.getY() + backCord.getY()) / 2);
	}
	
	public Coordinate toCoordinate(){
		return new Coordinate(frontCord.getX(), frontCord.getY());
	}
	
	public void rotateRobotRight(double rotationAngle) {
		// code for rotation found on
		// http://stackoverflow.com/questions/14842090/rotate-line-around-center-point-given-two-vertices
		double radianAngle = Math.toRadians(rotationAngle);
		double newX = (((frontCord.getX() - middleCord.getX())
				* Math.cos(radianAngle) + (frontCord.getY() - middleCord
				.getY()) * Math.sin(radianAngle)) + middleCord.getX());
		
		double newY = ((-(frontCord.getX() - middleCord.getX())
				* Math.sin(radianAngle) + (frontCord.getY() - middleCord
				.getY()) * Math.cos(radianAngle)) + middleCord.getY());
		
		frontCord.setX(newX);
		frontCord.setY(newY);
		
		newX = (((backCord.getX() - middleCord.getX())
				* Math.cos(radianAngle) + (backCord.getY() - middleCord
				.getY()) * Math.sin(radianAngle)) + middleCord.getX());
		
		newY = ((-(backCord.getX() - middleCord.getX())
				* Math.sin(radianAngle) + (backCord.getY() - middleCord
				.getY()) * Math.cos(radianAngle)) + middleCord.getY());
		
		backCord.setX(newX);
		backCord.setY(newY);
	}
	
	public void rotateRobotLeft(double rotationAngle) {
		// code for rotation found on
		// http://stackoverflow.com/questions/14842090/rotate-line-around-center-point-given-two-vertices
		double radianAngle = Math.toRadians(rotationAngle);
		double newX = (((frontCord.getX() - middleCord.getX())
				* Math.cos(radianAngle) + (frontCord.getY() - middleCord
				.getY()) * Math.sin(radianAngle)) + middleCord.getX());
		
		double newY = ((-(frontCord.getX() - middleCord.getX())
				* Math.sin(radianAngle) + (frontCord.getY() - middleCord
				.getY()) * Math.cos(radianAngle)) + middleCord.getY());
		
		frontCord.setX(newX);
		frontCord.setY(newY);
		
		newX = (((backCord.getX() - middleCord.getX())
				* Math.cos(radianAngle) + (backCord.getY() - middleCord
				.getY()) * Math.sin(radianAngle)) + middleCord.getX());
		
		newY = ((-(backCord.getX() - middleCord.getX())
				* Math.sin(radianAngle) + (backCord.getY() - middleCord
				.getY()) * Math.cos(radianAngle)) + middleCord.getY());
		
		backCord.setX(newX);
		backCord.setY(newY);
	}
	
	public void forward(double distance, Coordinate dest) {
		
		// formula for calculation found at 
		// http://math.stackexchange.com/questions/333350/moving-point-along-the-vector	
		
		double lengthToDest = calcDifference(frontCord.getX(), frontCord.getY(), dest.getX(), dest.getY());
		
		Vector frontVector = new Vector(frontCord.getX(),frontCord.getY());
		Vector backVector = new Vector(backCord.getX(), backCord.getY());
		Vector destination = new Vector(dest.getX(), dest.getY());
		Vector directionVector = destination.sub(frontVector);
		Vector newRobotVect = frontVector.add(directionVector.scale((distance)/(0.00000000001+lengthToDest)));
	
		frontCord.setX(newRobotVect.dX);
		frontCord.setY(newRobotVect.dY);

		newRobotVect = backVector.add(directionVector.scale((distance)/(0.00000000001+lengthToDest)));
		backCord.setX(newRobotVect.dX);
		backCord.setY(newRobotVect.dY);
		
		// calculate the new middle coordinate
		double xMiddle = ((frontCord.getX() + backCord.getX()) / 2);
		double yMiddle = ((frontCord.getY() + backCord.getY()) / 2);
		middleCord.setX(xMiddle);
		middleCord.setY(yMiddle);
		
		
	}
	
	private double calcDifference(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
	}
	
	public String toString(){
		return "front coordinates: x = " + frontCord.getX()+ " y = " + frontCord.getY() +" \n "
				+ "back coordinates: x = " + backCord.getX() + " y = " + backCord.getY(); 
	}

}

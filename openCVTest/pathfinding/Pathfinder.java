package pathfinding;

import geometry.Vector;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import javafx.scene.shape.Circle;
import moveableObjects.Ball;
import moveableObjects.Coordinate;
import moveableObjects.MoveState;
import moveableObjects.Robot;
import obstacles.Goal;
import obstacles.MiddleCross;
import obstacles.ObstacleFrame;
import robotCommunication.BTConnector;

public class Pathfinder {

	private RobotState state = RobotState.NOBALL;
	private ObstacleFrame frames;
	private MiddleCross cross;
	public double rotationAngle = 0, lengthToDest = 0;
	public Coordinate dest;
	private Coordinate[] safePoints = new Coordinate[4];
	private int currentSafePoint = -1;
	private Rectangle crossHorizontalPart;
	private Rectangle crossVerticalPart;
	private DestState destState = DestState.NODEST;
	private BTConnector robotController = new BTConnector();
	private int calibrationStep = 0;
	private double calibrationLength, xCalibrate, yCalibrate;

	public void findPath(Robot robot, ArrayList<Ball> balls, Goal goalA,
			Goal goalB, Goal goalADelivery, ObstacleFrame frames,
			MiddleCross cross) {

		if (!calibrateRobot(robot)) {
			// we are not yet done calibrating, so just return
			return;
		}

		// try to set it all the time
		robotController.calibration = calibrationLength;
		rotationAngle = 0;
		lengthToDest = 0;

		// calculate and set the frame and obstacles of the course prior to
		// routing
		this.frames = frames;
		this.cross = cross;
		setSafePoints();
		// makes sure the cross is seen as an obstacle
		setObstacles(cross, robot.robotRadius);
		projectRobot(robot);
		robot.updateMiddleCord();

		switch (destState) {
		case NODEST:
			// try to see if we can reach a ball
			if (state == RobotState.NOBALL) {
				// we try to open the arms, then we are ready to catch a new
				// ball
				if (currentSafePoint == -1) {
					System.out.println("open arms");
					robotController.openRobotArms();
				}
				dest = balls.get(findClosestBall(balls, robot));

			} else if (state == RobotState.HASBALL) {
				// calculate the goal delivery point coordinate
				dest = new Coordinate((goalA.getX() + (cross.getLeftCross()
						.getX() - robot.robotRadius)) / 2, goalA.getY());
			} else if (state == RobotState.GRABBALL) {
				// grab the ball here, but in the test we already got it
				state = RobotState.HASBALL;
				return;
			} else if (state == RobotState.SCOREBALL) {
				// drive a little closer to the goal
				dest = new Coordinate(goalA.getX() + 10, goalA.getY());
				// System.out.println("4");
			} else if (state == RobotState.SCORED) {
				// we try to score now
				System.out.println("We scored!!!");
				robotController.deliverBall();
				state = RobotState.AWAYFROMGOAL;
				return;
			} // move backwards from the goal
			else if (state == RobotState.AWAYFROMGOAL) {
				dest = new Coordinate((goalA.getX() + cross.getLeftCross()
						.getX()) / 4, goalA.getY());
			} else {

				System.out.println("Error in system");
			}

			lengthToDest = calcDifference(robot.getFrontCord().getX(), robot
					.getFrontCord().getY(), dest.getX(), dest.getY());

			if (avoidObstacle(robot, dest, lengthToDest)) {
				if ((findSafePoint(robot.toCoordinate()) != currentSafePoint)
						&& currentSafePoint == -1) {
					dest = safePoints[findSafePoint(robot.toCoordinate())];
					currentSafePoint = findSafePoint(robot.toCoordinate());
				} else {
					currentSafePoint = nextSafePoint();
					dest = safePoints[currentSafePoint];
				}
			} else {
				currentSafePoint = -1;
			}
			destState = DestState.HASDEST;

		case HASDEST:
			System.out.println(state);
			rotationAngle = findRotationAngle(robot.getFrontCord(),
					robot.getMiddleCord(), dest);
			lengthToDest = calcDifference(robot.getFrontCord().getX(), robot
					.getFrontCord().getY(), dest.getX(), dest.getY());

			if (state == RobotState.AWAYFROMGOAL) {
				// we do not need to rotate, we just need to drive backwards
				rotationAngle = findRotationAngle(robot.getBackCord(),
						robot.getMiddleCord(), dest);
				lengthToDest = calcDifference(robot.getBackCord().getX(), robot
						.getBackCord().getY(), dest.getX(), dest.getY());
			}

			// rotate right
			if (rotationAngle > 1 && !withinRobot(dest, robot)
					&& rotationAngle <= 180) {
				System.out.println("rotating right " + rotationAngle);
				robotController.rotateRobotRight(Math.abs(rotationAngle));
				robot.setState(MoveState.ROTATING);
			}
			// rotate left
			else if (rotationAngle < -1 && !withinRobot(dest, robot)
					&& rotationAngle >= -180) {
				System.out.println("rotating left " + rotationAngle);
				robotController.rotateRobotLeft(Math.abs(rotationAngle));
				robot.setState(MoveState.ROTATING);
			}
			// move forward
			else if (lengthToDest > 4 && !withinRobot(dest, robot)) {
				if (state == RobotState.AWAYFROMGOAL) {
					robotController.robotBackwards(lengthToDest);
					robot.setState(MoveState.MOVING);
					System.out.println("moving backwards");
				} else {
					robotController.robotForward(lengthToDest);
					robot.setState(MoveState.MOVING);
					System.out.println("moving forward");
				}
			} else {
				// arrived at dest routine
				System.out.println("destination reached");
				destState = DestState.NODEST;
				destReached();
			}

			break;

		default:
			break;

		}
	}

	private boolean withinRobot(Coordinate dest, Robot robot) {
		Circle robotArea = new Circle(robot.getMiddleCord().getX(), robot
				.getMiddleCord().getY(), robot.robotRadius * 2);

		return robotArea.contains(dest.getX(), dest.getY()) ? true : false;
	}

	private int findClosestBall(ArrayList<Ball> balls, Robot robot) {
		// init with improbable values
		int indexOfClosestBall = -1;
		int shortestDistance = 9999;

		// look through the rest of the array and see if another ball is closer
		for (int i = 0; i < balls.size(); i++) {
			int distance = (int) calcDifference(robot.getFrontCord().getX(),
					robot.getFrontCord().getY(), balls.get(i).getX(), balls
							.get(i).getY());
			if (distance < shortestDistance) {
				indexOfClosestBall = i;
				shortestDistance = distance;
			}
		}
		return indexOfClosestBall;
	}

	private double calcDifference(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
	}

	private double findRotationAngle(Coordinate front, Coordinate middle,
			Coordinate dest) {

		Vector robotVect = new Vector(front.getX() - middle.getX(),
				front.getY() - middle.getY());

		Vector destVect = new Vector((dest.getX() - middle.getX()),
				(dest.getY() - middle.getY()));

		double angRadians = Math.atan2(robotVect.dX * (destVect.dY)
				- (robotVect.dY) * destVect.dX, robotVect.dX * destVect.dX
				+ (robotVect.dY) * (destVect.dY));

		double angDegrees = Math.toDegrees(angRadians);
		// invert the degrees to match the statements in the code
		return angDegrees;

	}

	private void destReached() {
		switch (state) {
		case GRABBALL:
			state = RobotState.HASBALL;
			break;
		case HASBALL:
			if (currentSafePoint == -1) {
				state = RobotState.SCOREBALL;
			}
			break;
		case NOBALL:
			if (currentSafePoint == -1) {
				// lets try closing the robot arms
				robotController.closeRobotArms();
				state = RobotState.GRABBALL;
			}
			break;
		case SCOREBALL:
			state = RobotState.SCORED;
			break;
		case AWAYFROMGOAL:
			state = RobotState.NOBALL;
		default:
			break;

		}
	}

	public RobotState getState() {
		return this.state;
	}

	public void setState(RobotState state) {
		this.state = state;
	}

	public Coordinate getDest() {
		return this.dest;
	}

	/**
	 * Method to avoid obstacles on the course by a given coordinate list
	 * 
	 * @param crossCoordinates
	 */
	private boolean avoidObstacle(Robot robot, Coordinate destCord,
			double distance) {

		Line2D line = new Line2D.Double(robot.getFrontCord().getX(), robot
				.getFrontCord().getY(), destCord.getX(), destCord.getY());

		// If we hit an obstacle rotate and move robot away
		if (crossHorizontalPart.intersectsLine(line)
				|| crossVerticalPart.intersectsLine(line)) {
			return true;
		}
		return false;
	}

	private void setObstacles(MiddleCross cross, int radius) {
		setcrossHorizontalPart(crossHorizontalPart, cross, radius);
		setcrossVerticalPart(crossVerticalPart, cross, radius);
	}

	public Rectangle getcrossHorizontalPart() {
		return crossHorizontalPart;
	}

	public void setcrossHorizontalPart(Rectangle crossHorizontalPart,
			MiddleCross cross, int radius) {
		int crossX = (int) cross.getLeftCross().getX();
		int crossY = (int) cross.getLeftCross().getY() - 10;
		int crossWidth = (int) (((frames.topRight().getX() - frames.topLeft()
				.getX()) / 18) * 2) + 2 * radius;
		int crossHeight = 10 + 2 * radius;
		this.crossHorizontalPart = new Rectangle(crossX, crossY, crossWidth,
				crossHeight);
	}

	public Rectangle getcrossVerticalPart() {
		return crossVerticalPart;
	}

	public void setcrossVerticalPart(Rectangle crossVerticalPart,
			MiddleCross cross, int radius) {
		int crossX = (int) cross.getTopCross().getX() - 10;
		int crossY = (int) cross.getTopCross().getY();
		int crossWidth = 10 + 2 * radius;
		int crossHeight = (int) (((frames.lowRight().getY() - frames.topRight()
				.getY()) / 12) * 2) + 2 * radius;
		this.crossVerticalPart = new Rectangle(crossX, crossY, crossWidth,
				crossHeight);
	}

	private void setSafePoints() {

		for (int i = 0; i < safePoints.length; i++) {
			switch (i) {
			case 0:
				safePoints[0] = new Coordinate((frames.topLeft().getX() + cross
						.getCenterOfCross().getX()) / 2, (frames.topLeft()
						.getY() + cross.getTopCross().getY()) / 2);
				break;
			case 1:
				safePoints[1] = new Coordinate(
						(frames.topRight().getX() + cross.getCenterOfCross()
								.getX()) / 2, (frames.topRight().getY() + cross
								.getTopCross().getY()) / 2);
				break;
			case 2:
				safePoints[2] = new Coordinate(
						(frames.lowRight().getX() + cross.getCenterOfCross()
								.getX()) / 2, (frames.lowRight().getY() + cross
								.getBottomCross().getY()) / 2);
				break;
			case 3:
				safePoints[3] = new Coordinate((frames.lowLeft().getX() + cross
						.getCenterOfCross().getX()) / 2, (frames.lowLeft()
						.getY() + cross.getBottomCross().getY()) / 2);
				break;
			default:
				System.out.println("WTF happend");
				break;

			}
		}
	}

	private int findSafePoint(Coordinate dest) {

		int retIndex = -1;
		double shortestDistance = 99999;

		for (int i = 0; i < safePoints.length; i++) {
			if (retIndex == -1) {
				retIndex = i;
				shortestDistance = calcDifference(safePoints[0].getX(),
						safePoints[0].getY(), dest.getX(), dest.getY());
			}
			double tmpDistance = calcDifference(safePoints[i].getX(),
					safePoints[i].getY(), dest.getX(), dest.getY());
			if (tmpDistance < shortestDistance && tmpDistance > 20) {
				shortestDistance = tmpDistance;
				retIndex = i;
			}

		}
		return retIndex;
	}

	private int nextSafePoint() {

		switch (currentSafePoint) {
		case 0:

			return 1;

		case 1:

			return 2;
		case 2:

			return 3;

		case 3:

			return 0;

		default:
			return 0;
		}
	}

	private void projectRobot(Robot robot) {
		double heightOfRobot = 19;
		double heightOfCamera = 208.5;
		Coordinate centerOfCamera = new Coordinate(cross.getCenterOfCross()
				.getX(), cross.getCenterOfCross().getY());
		System.out.println("robot before project " + robot.toString());
		double newX = ((robot.getFrontCord().getX() - centerOfCamera.getX()) * (heightOfRobot / heightOfCamera))
				+ centerOfCamera.getX();

		double newY = ((robot.getFrontCord().getY() - centerOfCamera.getY()) * (heightOfRobot / heightOfCamera))
				+ centerOfCamera.getY();
		
		robot.getFrontCord().setX(newX);
		robot.getFrontCord().setY(newY);
		
		// calculate the back of the robot
		
		newX = ((robot.getBackCord().getX() - centerOfCamera.getX()) * (heightOfRobot / heightOfCamera))
				+ centerOfCamera.getX();

		newY = ((robot.getBackCord().getY() - centerOfCamera.getY()) * (heightOfRobot / heightOfCamera))
				+ centerOfCamera.getY();
		
		robot.getBackCord().setX(newX);
		robot.getBackCord().setY(newY);
		
		System.out.println("robot after project " + robot.toString());
		
		
		
		
	}

	private boolean calibrateRobot(Robot robot) {
		// this is the first time we enter calibration
		if (calibrationStep == 0) {
			// save our x and y values so we can see how far we move
			xCalibrate = robot.getFrontCord().getX();
			yCalibrate = robot.getFrontCord().getY();

			robotController.robotCalibrate();
			calibrationStep++;
			return false;
		} else if (calibrationStep == 1) {
			calibrationLength = calcDifference(xCalibrate, yCalibrate, robot
					.getFrontCord().getX(), robot.getFrontCord().getY());
			calibrationStep++;
		}
		return true;
	}
}

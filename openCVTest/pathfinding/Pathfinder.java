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
import robotCommunication.BTConnector2;

public class Pathfinder {

	private RobotState state = RobotState.NOBALL;
	private ObstacleFrame frames;
	private MiddleCross cross;
	public double rotationAngle = 0, lengthToDest = 0;
	public Coordinate dest;
	private Coordinate[] safePoints = new Coordinate[8];
	private int currentSafePoint = -1;
	private Rectangle crossHorizontalPart;
	private Rectangle crossVerticalPart;
	private DestState destState = DestState.NODEST;
	private BTConnector2 robotController = new BTConnector2();
	private int calibrationStep = 0;
	private double calibrationLength, xCalibrate, yCalibrate, ballsAfterGrab;

	public void findPath(Robot robot, ArrayList<Ball> balls, Goal goalA,
			Goal goalB, Goal goalADelivery, ObstacleFrame frames,
			MiddleCross cross) {

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

		// try to make the balls by the walls accessible for the robot

		switch (destState) {
		case NODEST:
			// try to see if we can reach a ball
			if (state == RobotState.NOBALL && balls.size() > 0) {
				// we try to open the arms, then we are ready to catch a new
				// ball
				if (currentSafePoint == -1) {
					robotController.openRobotArms();
					ballsAfterGrab = balls.size();
				}
				dest = balls.get(findClosestBall(balls, robot));

			} else if (state == RobotState.HASBALL) {
				// calculate the goal delivery point coordinate
				if (balls.size() > ballsAfterGrab) {
					System.out.println("too many balls left on the map");
					state = RobotState.NOBALL;
					destState = DestState.NODEST;
					return;
				} else {
					dest = new Coordinate(
							(goalA.getX() + (cross.getLeftCross().getX())) / 1.5,
							goalA.getY());

				}

			} else if (state == RobotState.GRABBALL) {
				// grab the ball here, but in the test we already got it
				robotController.closeRobotArms();
				ballsAfterGrab--;
				// decrement the balls we should find on the map
				state = RobotState.HASBALL;

				return;

			} else if (state == RobotState.SCOREBALL) {
				// drive a little closer to the goal
				
				dest = new Coordinate((goalA.getX() + (3 * robot.robotRadius)),
						goalA.getY());

			} else if (state == RobotState.SCORED) {
				// we try to score now
				System.out.println("We scored!!!");
				robotController.deliverBall();
				state = RobotState.AWAYFROMGOAL;
				destState = DestState.NODEST;
				return;

			} // move backwards from the goal
			else if (state == RobotState.AWAYFROMGOAL) {
				System.out.println("go away from goal");
				state = RobotState.NOBALL;
				destState = DestState.NODEST;
				return;
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
					System.out.println("new safepoint");
				} else {
					currentSafePoint = nextSafePoint();
					dest = safePoints[currentSafePoint];
					System.out.println("next safepoint");
				}
			} else {
				currentSafePoint = -1;
			}
			destState = DestState.HASDEST;

		case HASDEST:

			rotationAngle = findRotationAngle(robot.getFrontCord(),
					robot.getMiddleCord(), dest);
			lengthToDest = calcDifference(robot.getFrontCord().getX(), robot
					.getFrontCord().getY(), dest.getX(), dest.getY());

			// if we have the ball we must get to the goal scoring coordinate
			if (state == RobotState.HASBALL) {
				lengthToDest = calcDifference(robot.getMiddleCord().getX(),
						robot.getMiddleCord().getY(), dest.getX(), dest.getY());
			}

			// move forward
			if (lengthToDest > 4 && !withinRobot(dest, robot)) {

				robotController.robotForward(lengthToDest, rotationAngle);
				robot.setState(MoveState.MOVING);

			} else {
				// arrived at dest routine
				destState = DestState.NODEST;
				robot.setState(MoveState.ROTATING);
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

	public void stopAllCommands() {
		robotController.stopRobot();
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
		int crossX = (int) cross.getLeftCross().getX() - radius;
		int crossY = (int) cross.getLeftCross().getY() - 10;

		int crossWidth = (int) (((frames.topRight().getX() - frames.topLeft()
				.getX()) / 18) * 2) + (4 * radius) + radius / 2; // TODO changed
																	// to + 10
																	// for
																	// bigger
																	// cross
		int crossHeight = 10 + (2 * radius) + radius / 2; // TODO changed to +
															// 10 for
		// bigger cross

		this.crossHorizontalPart = new Rectangle(crossX, crossY, crossWidth,
				crossHeight);
	}

	public Rectangle getcrossVerticalPart() {
		return crossVerticalPart;
	}

	public void setcrossVerticalPart(Rectangle crossVerticalPart,
			MiddleCross cross, int radius) {
		int crossX = (int) cross.getTopCross().getX() - 10;
		int crossY = (int) cross.getTopCross().getY() - radius;

		int crossWidth = 10 + (2 * radius);
		int crossHeight = (int) (((frames.lowRight().getY() - frames.topRight()
				.getY()) / 12) * 2) + (4 * radius);

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
			// safepoint in straight line with cross
			case 4:
				safePoints[4] = new Coordinate(safePoints[3].getX(),
						((safePoints[3].getY() + safePoints[0].getY()) / 2));
				break;
			case 5:
				safePoints[5] = new Coordinate(
						((safePoints[1].getX() + safePoints[0].getX()) / 2),
						safePoints[1].getY());
				break;
			case 6:
				safePoints[6] = new Coordinate(safePoints[1].getX(),
						((safePoints[2].getY() + safePoints[1].getY()) / 2));
				break;
			case 7:
				safePoints[7] = new Coordinate(
						((safePoints[2].getX() + safePoints[3].getX()) / 2),
						safePoints[2].getY());
				break;
			default:
				System.out.println("WTF happened");
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

			return 5;

		case 1:

			return 6;
		case 2:

			return 7;

		case 3:

			return 4;

		case 4:

			return 0;
		case 5:

			return 1;
		case 6:

			return 2;
		case 7:

			return 3;

		default:
			return 0;
		}
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

	/**
	 * Method to make the balls accessible to the robot, instead of making it
	 * drive into the wall because it thinks it cannot reach the ball
	 * 
	 * @param balls
	 */
	private void adjustBallsAtWalls(ArrayList<Ball> balls) {
		// TODO adjust this value if necessary
		double maxDifFromWall = 5;

		double topYAvg = (frames.topLeft().getY() + frames.topRight().getY()) / 2;
		double rightXAvg = (frames.topRight().getX() + frames.lowRight().getX()) / 2;
		double bottomYAvg = (frames.lowLeft().getY() + frames.lowRight().getY()) / 2;
		double leftXAvg = (frames.topLeft().getX() + frames.lowLeft().getX()) / 2;

		for (Ball ball : balls) {

			// check for top wall
			if ((ball.getY() - 2 * ball.getRadius()) + topYAvg < maxDifFromWall) {
				ball.setY(ball.getY() + 2 * ball.getRadius());
			}

			// check for right wall
			else if (rightXAvg - (ball.getX() + 2 * ball.getRadius()) < maxDifFromWall) {
				ball.setX(ball.getX() - 2 * ball.getRadius());
			}

			// check for bottom wall
			else if ((ball.getY() + 2 * ball.getRadius()) - bottomYAvg < maxDifFromWall) {
				ball.setY(ball.getY() - 2 * ball.getRadius());
			}
			// check for left wall
			else if ((ball.getX() - 2 * ball.getRadius()) + leftXAvg < maxDifFromWall) {
				ball.setX(ball.getX() + 2 * ball.getRadius());
			}
		}

	}
}

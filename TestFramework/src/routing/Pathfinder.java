package routing;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import moveableObjects.Ball;
import moveableObjects.Coordinate;
import moveableObjects.MoveState;
import moveableObjects.Robot;
import obstacles.Goal;
import obstacles.MiddleCross;
import obstacles.ObstacleFrame;
import utilities.Vector;

public class Pathfinder {

	private RobotState state = RobotState.NOBALL;
	private Ball destBall;
	private ObstacleFrame frames;
	public double rotationAngle = 0, lengthToDest = 0;

	private Rectangle crossHorizontalPart;
	private Rectangle crossVerticalPart;

	public void findPath(Robot robot, ArrayList<Ball> balls, Goal goalA,
			Goal goalB, ObstacleFrame frames, MiddleCross cross) {
		rotationAngle = 0;
		lengthToDest = 0;
		this.frames = frames;

		// we have no ball right now so find one
		if (state == RobotState.NOBALL) {
			// find the closest ball first
			destBall = null;
			int indexOfClosestBall = findClosestBall(balls, robot);

			// this functions returns the angle to the destination based on the
			// current state
			rotationAngle = Math.abs(findRotationAngle(robot, goalA,
					balls.get(indexOfClosestBall)));

			if (rotationAngle > 1) {
				robot.setState(MoveState.ROTATING);
			} else {
				lengthToDest = calcDifference(robot.getFrontCord().getX(),
						robot.getFrontCord().getY(),
						balls.get(indexOfClosestBall).getX(),
						balls.get(indexOfClosestBall).getY());
				avoidObstacle(robot, balls.get(indexOfClosestBall),
						lengthToDest, cross); // :TODO
				if (lengthToDest > 1) {
					robot.setState(MoveState.MOVING);
					destBall = balls.get(indexOfClosestBall);
				} else {
					state = RobotState.GRABBALL;
					destBall = balls.get(indexOfClosestBall);
				}
			}
		}
		// we already have a ball so find a way to the goal
		else if (state == RobotState.HASBALL) {
			// this functions returns the angle to the destination based on the
			// current state
			rotationAngle = Math.abs(findRotationAngle(robot, goalA, null));
			// calculate the length to the destination
			lengthToDest = calcDifference(robot.getFrontCord().getX(), robot
					.getFrontCord().getY(), goalA.getX(), goalA.getY());
			avoidObstacle(robot, goalA, lengthToDest, cross); // :TODO
			// we have the angle from before
			if (rotationAngle > 1) {
				// rotate the robot
				robot.setState(MoveState.ROTATING);
			} else if (lengthToDest > 1) {
				// move towards the goal
				robot.setState(MoveState.MOVING);
			} else {
				// we are the goal throw it in
				state = RobotState.NOBALL;
			}

			// find the distance if no rotation were necessary
		}
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

	private double findRotationAngle(Robot robot, Goal goalA, Ball ball) {

		Vector robotVect = new Vector(robot.getFrontCord().getX()
				- robot.getMiddleCord().getX(), robot.getFrontCord().getY()
				- robot.getMiddleCord().getY());

		Vector goalVect = new Vector((goalA.getX() - robot.getMiddleCord()
				.getX()), (goalA.getY() - robot.getMiddleCord().getY()));

		if (state == RobotState.NOBALL) {
			if (ball != null) {
				Vector ballVect = new Vector((ball.getX() - robot
						.getMiddleCord().getX()), (ball.getY() - robot
						.getMiddleCord().getY()));
				double angRadians = Math.atan2(robotVect.dX * (ballVect.dY)
						- (robotVect.dY) * ballVect.dX, robotVect.dX
						* ballVect.dX + (robotVect.dY) * (ballVect.dY));

				double angDegrees = Math.toDegrees(angRadians);

				System.out.println("rotation angle to ball = " + angDegrees);
				return angDegrees;
			}
			return -1;

		} else {

			double angRadians = Math.atan2(robotVect.dX * (goalVect.dY)
					- (robotVect.dY) * goalVect.dX, robotVect.dX * goalVect.dX
					+ (robotVect.dY) * (goalVect.dY));

			double angDegrees = Math.toDegrees(angRadians);

			System.out.println("rotation angle to goal = " + angDegrees);
			return angDegrees;
		}
	}

	public RobotState getState() {
		return this.state;
	}

	public void setState(RobotState state) {
		this.state = state;
	}

	public Ball getDest() {
		return this.destBall;
	}

	/**
	 * Method to avoid obstacles on the course by a given coordinate list
	 * 
	 * @param crossCoordinates
	 */
	private void avoidObstacle(Robot robot, Coordinate destCord, double distance, MiddleCross cross) {

		Line2D line = new Line2D.Double(robot.getFrontCord().getX(), robot
				.getFrontCord().getY(), destCord.getX(), destCord.getY());

		setcrossHorizontalPart(crossHorizontalPart, cross);
		setcrossVerticalPart(crossVerticalPart, cross);


			// If we hit an obstacle rotate and move robot away
			if (crossHorizontalPart.intersectsLine(line) || crossVerticalPart.intersectsLine(line)) {

				System.out.println("Error in coordinates");
		}
	}
	
	public Rectangle getcrossHorizontalPart() {
		return crossHorizontalPart;
	}

	public void setcrossHorizontalPart(Rectangle crossHorizontalPart, MiddleCross cross) {
		int crossX = (int) cross.getLeftCross().getX();
		int crossY = (int) cross.getLeftCross().getY() - 10;
		int crossWidth = (int) ((frames.topRight().getX() - frames.topLeft().getX()) / 18) * 2;
		int crossHeight = 10;
		this.crossHorizontalPart = new Rectangle(crossX, crossY, crossWidth, crossHeight);
	}

	public Rectangle getcrossVerticalPart() {
		return crossVerticalPart;
	}

	public void setcrossVerticalPart(Rectangle crossVerticalPart, MiddleCross cross) {
		int crossX = (int) cross.getLeftCross().getX() - 10;
		int crossY = (int) cross.getLeftCross().getY();
		int crossWidth = 10;
		int crossHeight = (int) ((frames.lowRight().getY() - frames.topRight().getY()) / 12) * 2;
		this.crossVerticalPart = new Rectangle(crossX, crossY, crossWidth, crossHeight);
	}

}

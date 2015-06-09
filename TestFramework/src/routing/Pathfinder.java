package routing;

import java.awt.Point;
import java.util.ArrayList;

import obstacles.Goal;
import obstacles.ObstacleFrame;
import utilities.Vector;
import MoveableObjects.Ball;
import MoveableObjects.Coordinate;
import MoveableObjects.MoveState;
import MoveableObjects.Robot;

public class Pathfinder {

	private RobotState state = RobotState.NOBALL;
	private Ball destBall;
	private Coordinate tempCord;
	public double rotationAngle = 0, lengthToDest = 0;

	public void findPath(Robot robot, ArrayList<Ball> balls, Goal goalA,
			Goal goalB, ObstacleFrame frames) {
		rotationAngle = 0;
		lengthToDest = 0;
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
	 * @param obstacleCoordinates
	 */
	private void avoidObstacle(Robot robot, ArrayList<Point> obstacleCoordinates, Coordinate destCord, double distance) {
		
		double lengthToDest = calcDifference(robot.getMiddleCord().getX(), robot.getMiddleCord().getY(), destCord.getX(), destCord.getY());

		// Get the vector to nearest object
		Vector robotVector = new Vector(robot.getMiddleCord().getX(),robot.getMiddleCord().getY());
		Vector destination = new Vector(destCord.getX(), destCord.getY());
		Vector directionVector = destination.sub(robotVector);
		
		
		// Run through every coordinate to see if the vector runs through
		for (int i = 0; i < obstacleCoordinates.size(); i++) {


			for (int j = 0; j < robotVector.length(); j++) {
				Vector newRobotVect = robotVector.add(directionVector.scale((double)distance/lengthToDest));
				tempCord.setX(newRobotVect.dX);
				tempCord.setY(newRobotVect.dY);

				// If we hit an obstacle rotate and move robot away
				if (tempCord.getX() == obstacleCoordinates.get(i).x
						&& tempCord.getY() == obstacleCoordinates.get(i).y) {
					//robot.rotateRobot(90);
					//robot.forward(5, Coordinate);//TODO
					System.out.println("Error in coordinates");
				}
			}
		}
	}

}

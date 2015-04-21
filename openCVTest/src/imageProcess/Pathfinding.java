package imageProcess;

import geometry.Vector;

import java.util.ArrayList;

import robotCommunication.BTConnector;

public class Pathfinding {
	// TODO
	private BTConnector robot = new BTConnector();
	// index of robot points in the objects
	private int robotFrontIndex;
	private int robotBackIndex;
	private int testInt = 0;

	// coordinate of the middle of the robot
	private NodeObjects robotMiddle;
	private NodeObjects robotFront;
	private NodeObjects robotBack;

	public void run(ArrayList<NodeObjects> objects) {
		robotFrontIndex = findFront(objects);
		robotBackIndex = findBack(objects);
		// robot coordinates
		robotFront = objects.get(robotFrontIndex);
		robotBack = objects.get(robotBackIndex);
		robotMiddle = calcMiddleRobotCoord();

		int min_index = findClosestBall(objects);
		instructRobot(objects.get(min_index));

	}

	/**
	 * @param objects
	 * @return
	 */
	private int findClosestBall(ArrayList<NodeObjects> objects) {
		double tmpLength;
		double min_length = 1000;
		int min_index = -1;

		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).getType().equals("ball")) {
				if (min_index == -1) {
					min_index = i;
				} else if ((tmpLength = calcLength(objects.get(i),
						objects.get(robotFrontIndex))) < min_length) {
					min_length = tmpLength;
					min_index = i;
				}
			}
		}
		return min_index;
	}

	private double findRotationAngle(NodeObjects robotFront,
			NodeObjects robotMiddle, NodeObjects dest) {
		//
		// // define the lenghts needed to calculate the angle
		// double frontToMiddle, middleToBall, frontToBall;
		// frontToMiddle = calcLength(robotFront, robotMiddle);
		// middleToBall = calcLength(robotMiddle, dest);
		// frontToBall = calcLength(robotFront, dest);
		// /*
		// * frontToMiddle = c middleToBall = b frontToBall = a
		// */
		// double a = frontToBall;
		// double b = middleToBall;
		// double c = frontToMiddle;
		//
		// // the angles in the triangle
		// double A, B, C;
		//
		// // TODO figure out which one of the angles is the right one
		// A = Math.acos((b * b + c * c - a * a) / (2.0 * b * c)) * 180 /
		// Math.PI;
		// B = Math.asin((Math.sin(Math.toRadians(A)) * c) / b);
		// C = 180 - (A + B);
		//
		// return A;

		Vector vector1 = new Vector(robotFront.getX() - dest.getX(),
				robotFront.getY() - dest.getY());
		Vector vector2 = new Vector(robotMiddle.getX() - dest.getX(),
				robotMiddle.getY() - dest.getY());

		double ang = Math
				.toDegrees(Math.acos((vector1.dX * vector2.dX + vector1.dY
						* vector2.dY)
						/ (Math.sqrt(Math.pow(vector1.dX, 2)
								+ Math.pow(vector1.dY, 2)) * Math.sqrt(Math
								.pow(vector2.dX, 2) + Math.pow(vector2.dY, 2)))));
		return 180 - ang;

	}

	/**
	 * find the appropriate instruction to give the robot, considering all the
	 * coordinates and the closest ball that we have to drive to.
	 * 
	 * @param dest
	 * @return
	 */
	private void instructRobot(NodeObjects dest) {
		// find the length to the objects
		double rotationAngle = findRotationAngle(robotFront, robotMiddle, dest);
		System.out.println("rotation Angle = " + rotationAngle);
		// if the rotation angle is very small we need not rotate the robot
		double lengthToDest = calcLength(robotFront, dest);

		if (testInt == 0) {
			// TODO
			robot.rotateRobotLeft(rotationAngle);
			testInt++;
		}

	}

	int findFront(ArrayList<NodeObjects> objects) {
		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).getType().equals("FrontRobot")) {
				return i;
			}

		}
		return -1;
	}

	int findBack(ArrayList<NodeObjects> objects) {
		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).getType().equals("BackRobot")) {
				return i;
			}

		}
		return -1;
	}

	/**
	 * Calculate the length from second to first
	 * 
	 * @param first
	 * @param second
	 * @return
	 */
	private double calcLength(NodeObjects first, NodeObjects second) {
		return Math.sqrt(Math.pow((first.getX() - second.getX()), 2)
				+ Math.pow((first.getY() - second.getY()), 2));
	}

	NodeObjects calcMiddleRobotCoord() {

		double x = robotFront.getX() - robotBack.getX();
		double y = robotFront.getY() - robotBack.getY();

		return new NodeObjects(x, y, "MiddleRobot");
	}

}

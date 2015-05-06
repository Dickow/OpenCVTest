package imageProcess;

import geometry.Vector;

import java.util.ArrayList;

import robotCommunication.BTConnector;

public class PathFinding2 {
	private NodeObjects robotFront, robotMiddle, robotBack, dest;
	// private BTConnector robot = new BTConnector(); TODO
	private double xCalibrate, yCalibrate;
	private int calibrationStep = 0;
	private double calibrationLength;

	public void findPath(ArrayList<NodeObjects> objects) {

		// find all the robot parts

		// find front and back
		try {
			robotBack = objects.get(findBack(objects));
			robotFront = objects.get(findFront(objects));
			// calculate the middle robot
			robotMiddle = calcMiddleRobotCoord();

			// find the closest ball to the robot

			dest = objects.get(findClosestBall(objects));
		} catch (Exception e) {
			return;
		}

		// calculate the rotation angle to the ball
		double rotationAngle = findRotationAngle(robotFront, robotMiddle, dest);

		// calculate the distance to the ball
		double distanceToDest = calcLength(robotFront, dest);

		// we should have calibrated the robot, so we know how much we
		// should
		// rotate our motors to move a certain distance
		if (!calibrateRobot()) {
			// we are not yet done calibrating, so just return
			return;
		}
		// see if we need to rotate the robot
		if (rotationAngle > 1) {
			/*
			 * we need to rotate the robot but we also need to know which way we
			 * should be rotating
			 */
			rotateRobot(rotationAngle);
		}
		// see if we are far away from the destination
		else if (distanceToDest > 20) {
			// we need to drive closer to the robot
			/*
			 * robot.robotForward(distanceToDest / calibrationLength - 30 TODO
			 * 30 is just a random value need to test this );
			 */
		}
		// if we do not need to rotate and we are close, then we must be
		// able to
		// collect the ball
		else {
			// grab the ball and get the job done
		}

	}

	private double findRotationAngle(NodeObjects robotFront,
			NodeObjects robotMiddle, NodeObjects dest) {

		Vector vector1 = new Vector(robotFront.getX() - robotMiddle.getX(),
				robotFront.getY() - robotMiddle.getY());

		Vector vector2 = new Vector((dest.getX() - robotMiddle.getX()),
				(dest.getY() - robotMiddle.getY()));

		// double angDegrees = Math
		// .toDegrees(Math.acos((((vector1.dX) * (vector2.dX)) + ((vector1.dY) *
		// (vector2.dY)))
		// / (Math.sqrt(Math.pow(vector1.dX, 2)
		// + Math.pow(vector1.dY, 2)) * Math.sqrt(Math
		// .pow(vector2.dX, 2) + Math.pow(vector2.dY, 2)))));

		// double angDegrees = Math.acos((((vector1.dX) * (vector2.dX)) +
		// ((vector1.dY) * (vector2.dY)))
		// / (Math.sqrt(Math.pow(vector1.dX, 2) + Math.pow(vector1.dY, 2)) *
		// Math.sqrt(Math.pow(vector2.dX, 2) + Math.pow(vector2.dY, 2))));
		// angDegrees = Math.toDegrees(angDegrees);
		//
		double angRadians = Math.atan2(vector1.dX * (vector2.dY) - (vector1.dY)
				* vector2.dX, vector1.dX * vector2.dX + (vector1.dY)
				* (vector2.dY));

		double angDegrees = Math.toDegrees(angRadians);

		System.out.println("rotation angle = " + angDegrees);
		return angDegrees;

	}

	private int findClosestBall(ArrayList<NodeObjects> objects) {
		// init with improbable values
		int indexOfClosestBall = -1;
		int shortestDistance = 9999;

		// find the first ball in the array
		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).getType().equals("ball")) {
				indexOfClosestBall = i;
				shortestDistance = (int) calcLength(robotFront, objects.get(i));
				break;
			}
		}
		// look through the rest of the array and see if another ball is closer
		for (int i = indexOfClosestBall + 1; i < objects.size(); i++) {
			int distance = (int) calcLength(robotFront, objects.get(i));
			if (distance < shortestDistance
					&& objects.get(i).getType().equals("ball")) {
				indexOfClosestBall = i;
				shortestDistance = distance;
			}
		}
		return indexOfClosestBall;
	}

	private NodeObjects calcMiddleRobotCoord() {

		double x = (robotFront.getX() + robotBack.getX()) / 2;
		double y = (robotFront.getY() + robotBack.getY()) / 2;

		return new NodeObjects(x, y, "MiddleRobot");
	}

	private int findFront(ArrayList<NodeObjects> objects) {
		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).getType().equals("robotFront")) {
				System.out.println(objects.get(i).toString());
				return i;
			}

		}
		return -1;
	}

	private int findBack(ArrayList<NodeObjects> objects) {
		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).getType().equals("robotBack")) {
				System.out.println(objects.get(i).toString());
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

	private void rotateRobot(double rotationAngle) {

//		if (robotFront.getY() < dest.getY()) {
//			if (dest.getX() - robotFront.getX() > 0) {
////				robot.rotateRobotRight(rotationAngle);
//				System.out.println("rotating right " + rotationAngle);
//			} else if (dest.getX() - robotFront.getX() < 0) {
////				robot.rotateRobotLeft(rotationAngle);
//				System.out.println("rotating left " + rotationAngle);
//			}
//		} else if (robotFront.getY() > dest.getY()) {
//			if (dest.getX() - robotFront.getX() > 0) {
////				robot.rotateRobotLeft(rotationAngle);
//				System.out.println("rotating left " + rotationAngle);
//			} else if (dest.getX() - robotFront.getX() < 0) {
////				robot.rotateRobotRight(rotationAngle);
//				System.out.println("rotating right " + rotationAngle);
//			}
//		} else if (robotFront.getY() == dest.getY()) {
//			if (dest.getX() - robotFront.getX() > 0) {
//				if (robotFront.getY() < robotMiddle.getY()) {
////					robot.rotateRobotRight(rotationAngle);
//					System.out.println("rotating right " + rotationAngle);
//				} else {
////					robot.rotateRobotLeft(rotationAngle);
//					System.out.println("rotating left " + rotationAngle);
//				}
//			} else {
//				if (robotFront.getY() < robotMiddle.getY()) {
////					robot.rotateRobotLeft(rotationAngle);
//					System.out.println("rotating left " + rotationAngle);
//				} else {
////					robot.rotateRobotRight(rotationAngle);
//					System.out.println("rotating right " + rotationAngle);
//				}
//			}
//		} else if (robotFront.getY() == dest.getY()
//				&& robotFront.getY() == robotMiddle.getY()) {
////			robot.rotateRobotLeft(rotationAngle);
//			System.out.println("rotating left " + rotationAngle);
//		}

		 Vector vector1 = new Vector(robotFront.getX() - robotMiddle.getX(),
		 robotFront.getY() - robotMiddle.getY());
		 Vector vector2 = new Vector(dest.getX() - robotMiddle.getX(),
		 dest.getY() - robotMiddle.getY());
		
		 double delta = vector1.dX * -vector2.dY - -vector1.dY * vector2.dX;
		 System.out.println("delta = " + delta);
		
		 if (delta > 0) {
		 // the new direction is to the left of our current facing
		 // robot.rotateRobotLeft(rotationAngle); TODO
		 System.out.println("rotating left" + rotationAngle);
		 } else if (delta < 0) {
		 // the new direction is to the right of our current facing
		 // robot.rotateRobotRight(rotationAngle); TODO
		 System.out.println("rotating right " + rotationAngle);
		 } else if (delta == 0) {
		 // we are directly opposite of the direction, just pick a direction
		 // to turn
		 // robot.rotateRobotRight(rotationAngle); TODO
		 System.out.println("rotating 180 degrees");
		 }

	}

	private double calcDifference(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
	}

	private boolean calibrateRobot() {
		// this is the first time we enter calibration
		if (calibrationStep == 0) {
			// save our x and y values so we can see how far we move
			xCalibrate = robotFront.getX();
			yCalibrate = robotBack.getY();

			// robot.robotCalibrate(); TODO
			calibrationStep++;
			return false;
		} else if (calibrationStep == 1) {
			calibrationLength = calcDifference(xCalibrate, yCalibrate,
					robotFront.getX(), robotFront.getY());
			calibrationStep++;
		}
		return true;
	}
}

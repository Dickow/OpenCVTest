package imageProcess;

import geometry.Vector;

import java.awt.Image;
import java.util.ArrayList;

import org.opencv.core.Mat;

import robotCommunication.BTConnector;

public class Pathfinding {
	// TODO
	private BTConnector robot = new BTConnector();
	// index of robot points in the objects
	private int robotFrontIndex;
	private int robotBackIndex;
	private boolean gotBall = false;
	private int calibrating = 0;
	private double distanceCalibration = 0;
	private double xCalibration, yCalibration;
	// coordinates of the middle of the robot
	private NodeObjects robotMiddle;
	private NodeObjects robotFront;
	private NodeObjects robotBack;

	public void run(ArrayList<NodeObjects> objects) {
		robotFrontIndex = findFront(objects);
		robotBackIndex = findBack(objects);
		// robot coordinates
		try {
			robotFront = objects.get(robotFrontIndex);
			robotBack = objects.get(robotBackIndex);
		} catch (ArrayIndexOutOfBoundsException e) {
			return;
		}
		robotMiddle = calcMiddleRobotCoord();

		System.out.println(robotFront.toString());
		System.out.println(robotBack.toString());
		
		int min_index = findClosestBall(objects);
		if (min_index == -1) {
			return;
		}
		try {
			instructRobot(objects.get(min_index));
		} catch (ArrayIndexOutOfBoundsException e) {
			return;
		}
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
				if ((tmpLength = calcLength(objects.get(i),
						objects.get(robotFrontIndex))) < min_length) {
					min_length = tmpLength;
					min_index = i;
				}
			}
		}
		/* remove this when done testing */
		try {
			System.out.println(objects.get(min_index).toString());
		} catch (ArrayIndexOutOfBoundsException e) {
			return -1;
		}
		return min_index;
	}

	private double findRotationAngle(NodeObjects robotFront,
			NodeObjects robotMiddle, NodeObjects dest) {

		Vector vector1 = new Vector(robotFront.getX() - robotMiddle.getX(),
				robotFront.getY() - robotMiddle.getY());
		Vector vector2 = new Vector(dest.getX() - robotMiddle.getX(),
				dest.getY() - robotMiddle.getY());

		double ang = Math
				.toDegrees(Math.acos((vector1.dX * vector2.dX + vector1.dY
						* vector2.dY)
						/ (Math.sqrt(Math.pow(vector1.dX, 2)
								+ Math.pow(vector1.dY, 2)) * Math.sqrt(Math
								.pow(vector2.dX, 2) + Math.pow(vector2.dY, 2)))));
		System.out.println("rotation angle = " + ang);
		return ang;

	}

	/**
	 * find the appropriate instruction to give the robot, considering all the
	 * coordinates and the closest ball that we have to drive to. contains all
	 * the routing information
	 * 
	 * @param dest
	 * @return
	 */
	private void instructRobot(NodeObjects dest) {
		// find the length to the objects
		double rotationAngle = findRotationAngle(robotFront, robotMiddle, dest);
		// if the rotation angle is very small we need not rotate the robot
		double lengthToDest = calcLength(robotFront, dest);
		double frontToBackDistance = calcLength(robotFront, robotBack);
		
		if (calibratingDone()) {
			if (!gotBall) {
				// sequence to get to ball
				if (rotationAngle > 2) {
					// insert function to rotate robot
					rotateRobot(rotationAngle, dest);

				} else if (lengthToDest > 20) {
					// calculate how many degrees the motor should be rotated
					// use a relative distance, which is the distance
					// between front and back of robot divided by 2
					robot.robotForward((lengthToDest - frontToBackDistance / 2)
							/ distanceCalibration);
				}
				/* we are just assuming we catched the ball could be optimized */
				else if (lengthToDest <= frontToBackDistance / 2) {
					robot.openRobotArms();
					gotBall = true;
				}
			} else {
				System.out.println("else sentence !=!!=");
				robot.closeRobotArms();
				gotBall = false; 
			}
		}

	}

	int findFront(ArrayList<NodeObjects> objects) {
		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).getType().equals("robotFront")) {
				return i;
			}

		}
		return -1;
	}

	int findBack(ArrayList<NodeObjects> objects) {
		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).getType().equals("robotBack")) {
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

	private double calcDifference(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
	}

	NodeObjects calcMiddleRobotCoord() {

		double x = robotFront.getX() - robotBack.getX();
		double y = robotFront.getY() - robotBack.getY();

		return new NodeObjects(x, y, "MiddleRobot");
	}

	/**
	 * calibrate the robot to find out how much the motors should be rotated.
	 * return false if it's not done, true if it is done
	 * 
	 * @return boolean
	 */
	private boolean calibratingDone() {
		if (calibrating == 0) {
			xCalibration = robotFront.getX();
			yCalibration = robotFront.getY();
			robot.robotCalibrate();
			calibrating++;
			return false;
		} else if (calibrating == 1) {
			distanceCalibration = calcDifference(xCalibration, yCalibration,
					robotFront.getX(), robotFront.getY());
			calibrating++;
			return true;
		} else {
			return true;
		}
	}

	private void rotateRobot(double rotationAngle, NodeObjects dest) {

		if (robotFront.getY() < dest.getY()) {
			if (dest.getX() - robotFront.getX() > 0) {
				robot.rotateRobotRight(rotationAngle);
			} else if (dest.getX() - robotFront.getX() < 0) {
				robot.rotateRobotLeft(rotationAngle);
			}
		} else if (robotFront.getY() > dest.getY()) {
			if (dest.getX() - robotFront.getX() > 0) {
				robot.rotateRobotLeft(rotationAngle);
			} else if (dest.getX() - robotFront.getX() < 0) {
				robot.rotateRobotRight(rotationAngle);
			}
		} else if (robotFront.getY() == dest.getY()) {
			if (dest.getX() - robotFront.getX() > 0) {
				if (robotFront.getY() < robotMiddle.getY()) {
					robot.rotateRobotRight(rotationAngle);
				} else {
					robot.rotateRobotLeft(rotationAngle);
				}
			} else {
				if (robotFront.getY() < robotMiddle.getY()) {
					robot.rotateRobotLeft(rotationAngle);
				} else {
					robot.rotateRobotRight(rotationAngle);
				}
			}
		} else if (robotFront.getY() == dest.getY()
				&& robotFront.getY() == robotMiddle.getY()) {
			robot.rotateRobotLeft(rotationAngle);
		}

	}

}

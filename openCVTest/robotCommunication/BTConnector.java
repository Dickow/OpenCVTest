package robotCommunication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;

public class BTConnector {
	private final int TURNLEFT = 1, TURNRIGHT = 2, FORWARD = 3, BACKWARDS = 4,
			STOP = 5, OPEN = 6, CLOSE = 7, DELIVER = 8, CALIBRATE = 9,
			FINISHED = 10;
	private NXTConnector conn;
	private DataOutputStream dos;
	private DataInputStream din;

	/**
	 * create a new BTConnector, it creates a connection to the nxt device use
	 * the public functions to communicate with the robot.
	 */
	public BTConnector() {
		conn = new NXTConnector();

		conn.addLogListener(new NXTCommLogListener() {

			public void logEvent(String message) {
				System.out.println("BTSend Log.listener: " + message);

			}

			public void logEvent(Throwable throwable) {
				System.out.println("BTSend Log.listener - stack trace: ");
				throwable.printStackTrace();

			}

		});

		// Connect to any NXT over Bluetooth
		boolean connected = conn.connectTo("btspp://");

		if (!connected) {
			System.err.println("Failed to connect to any NXT");
			System.exit(1);
		}
		dos = new DataOutputStream(conn.getOutputStream());
		din = new DataInputStream(conn.getInputStream());

	}

	/**
	 * rotate the robot right
	 * 
	 * @param angle
	 */
	public void rotateRobotRight(double angle) {
		try {
			dos.writeInt(TURNRIGHT);
			dos.writeInt((int) angle);
			dos.flush();
			waitForRobot();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * rotate the robot to the left
	 * 
	 * @param angle
	 */
	public void rotateRobotLeft(double angle) {

		try {
			dos.writeInt(TURNLEFT);
			dos.writeInt((int) angle);
			dos.flush();
			waitForRobot();
			System.out.println("written to robot");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * tell the robot to go forward for a given distance
	 * 
	 * @param distance
	 */
	public void robotForward(double distance) {


		try {
			dos.writeInt(FORWARD);
			dos.writeDouble(distance);
			dos.flush();

			waitForRobot();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * tell the robot to go backwards for a given distance
	 * 
	 * @param distance
	 */
	public void robotBackwards(double distance) {

		// we want to move in smaller steps to avoid missing the destination
		if (distance > 100) {
			distance /= 4;
		} else if (distance > 50) {
			distance /= 2;
		} else {
			// it seems like we needed a little extra distance to acctually
			// reach the target
			distance += 5;
		}
		try {
			dos.writeInt(BACKWARDS);
			dos.writeDouble(distance);
			dos.flush();
			waitForRobot();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * tell the robot to open it's arms
	 */
	public void openRobotArms() {
		try {
			dos.writeInt(OPEN);
			dos.flush();
			waitForRobot();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * tell the robot to close it's arms
	 */
	public void closeRobotArms() {
		try {
			dos.writeInt(CLOSE);
			dos.flush();
			waitForRobot();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * tell the robot to open the arms while going forward this is used to
	 * deliver the ball to the goal.
	 */
	public void deliverBall() {
		try {
			dos.writeInt(DELIVER);
			dos.flush();
			waitForRobot();
		} catch (IOException e) {

		}
	}

	public void robotCalibrate() {
		try {
			dos.writeInt(CALIBRATE);
			dos.flush();
			waitForRobot();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void waitForRobot() {
		try {
			while (din.readInt() != FINISHED) {

			}
		} catch (IOException e) {
			System.out.println("Why did we end up here????!?!?!?!??!");
		}
	}
}

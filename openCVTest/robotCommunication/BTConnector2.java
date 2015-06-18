package robotCommunication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;

public class BTConnector2 {
	private final int TURNLEFT = 1, TURNRIGHT = 2, FORWARD = 3, BACKWARDS = 4,
			STOP = 5, OPEN = 6, CLOSE = 7, DELIVER = 8, CALIBRATE = 9,
			FINISHED = 10;
	private NXTConnector conn;
	private DataOutputStream dos;
	private DataInputStream din;
	private final double KP = 0.8;
	private final int SPEED = 50;
	public double calibration;

	public BTConnector2() {
		conn = new NXTConnector();
		conn.addLogListener(new NXTCommLogListener() {

			@Override
			public void logEvent(String message) {
				System.out.println("BTSend Log.listener: " + message);

			}

			@Override
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
	 * calibrate the robot
	 */
	public void robotCalibrate() {
		try {
			dos.writeInt(CALIBRATE);
			dos.flush();
			waitForRobot();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * rotate the robot
	 * 
	 * @param angle
	 */
	public void rotateRobotLeft(double angle) {

		double pGain = KP * angle;
		double motorASpeed;
		double motorBSpeed;

		motorASpeed = SPEED + pGain;
		motorBSpeed = SPEED + pGain;

		try {
			dos.writeInt(TURNLEFT);
			dos.writeDouble(motorASpeed);
			dos.writeDouble(motorBSpeed);
			dos.flush();
			waitForRobot();
			System.out.println("rotate " + angle);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * rotate the robot
	 * 
	 * @param angle
	 */
	public void rotateRobotRight(double angle) {

		double pGain = KP * angle;
		double motorASpeed;
		double motorBSpeed;

		motorASpeed = SPEED + pGain;
		motorBSpeed = SPEED + pGain;

		try {
			dos.writeInt(TURNRIGHT);
			dos.writeDouble(motorASpeed);
			dos.writeDouble(motorBSpeed);
			dos.flush();
			waitForRobot();
			System.out.println("rotate " + angle);
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
	public void robotForward(double distance, double angle) {

		double pGain = KP * angle;
		double motorASpeed;
		double motorBSpeed;

		motorASpeed = SPEED + (pGain);
		motorBSpeed = SPEED - (pGain);

		try {
			dos.writeInt(FORWARD);
			dos.writeDouble(-motorASpeed);
			dos.writeDouble(-motorBSpeed);
			dos.flush();

			waitForRobot();
			System.out.println("forward " + distance);
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
			System.out.println("open arms");
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
			System.out.println("close arms");
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

			System.out.println("deliver");
		} catch (IOException e) {

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

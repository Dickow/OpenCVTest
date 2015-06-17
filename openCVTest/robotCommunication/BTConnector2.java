package robotCommunication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;

public class BTConnector2 {
	private final int FINISHED = 10;
	private NXTConnector conn;
	private DataOutputStream dos;
	private DataInputStream din;
	private final double KP = 0.4;
	private final int SPEED = 50;

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
	 * rotate the robot
	 * 
	 * @param angle
	 */
	public void rotateRobot(double angle) {

		double pGain = KP * angle;
		double motorASpeed;
		double motorBSpeed;
		double motorCAngle;

		if (angle < 0) {
			motorASpeed = SPEED + pGain;
			motorBSpeed = -(SPEED - pGain);
			motorCAngle = 0;
		} else {
			motorASpeed = -(SPEED - pGain);
			motorBSpeed = SPEED + pGain;
			motorCAngle = 0;
		}

		try {
			dos.writeDouble(motorASpeed);
			dos.writeDouble(motorBSpeed);
			dos.writeDouble(motorCAngle);
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
		double motorCAngle = 0;
		
		
		if (distance > 100) {
			distance /= 5;
		} else if (distance > 50) {
			distance /= 4;
		} else if (distance > 20) {
			distance /= 3;
		}else{
			distance /= 2; 
		}
		
		if (angle < 0) {
			motorASpeed = KP * distance * SPEED + (pGain);
			motorBSpeed = KP * distance * SPEED - (pGain);
		} else {
			motorASpeed = KP * distance * SPEED + (pGain);
			motorBSpeed = KP * distance * SPEED - (pGain);
		}

		try {
			dos.writeDouble(-motorASpeed);
			dos.writeDouble(-motorBSpeed);
			dos.writeDouble(motorCAngle);
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

		double motorASpeed = 0;
		double motorBSpeed = 0;
		double motorCAngle = 50;

		try {
			dos.writeDouble(-motorASpeed);
			dos.writeDouble(-motorBSpeed);
			dos.writeDouble(motorCAngle);
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

		double motorASpeed = 0;
		double motorBSpeed = 0;
		double motorCAngle = -50;
		try {
			dos.writeDouble(-motorASpeed);
			dos.writeDouble(-motorBSpeed);
			dos.writeDouble(motorCAngle);
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

		double motorASpeed = KP * 30;
		double motorBSpeed = KP * 30;
		double motorCAngle = 80;

		try {
			dos.writeDouble(-motorASpeed);
			dos.writeDouble(-motorBSpeed);
			dos.writeDouble(motorCAngle);
			dos.flush();
			waitForRobot();
			dos.writeDouble(0);
			dos.writeDouble(0);
			dos.writeDouble(-(motorCAngle));
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

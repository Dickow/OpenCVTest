package robotCommunication;

import java.io.DataOutputStream;
import java.io.IOException;

import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;


public class BTConnector {
	private final int TURNLEFT = 0,TURNRIGHT = 1,FORWARD = 2,BACKWARDS = 3,STOP = 4,OPEN = 5,CLOSE = 6, DELIVER = 7;
	private NXTConnector conn;
	private DataOutputStream dos;
	
	/**
	 * create a new BTConnector, it creates a connection to the nxt device
	 * use the public functions to communicate with the robot.
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
		
		
		
	}
	
	public void rotateRobotRight(double angle){
		
	}
	
	public void rotateRobotLeft(double angle){
		
		try {
			dos.writeInt(TURNLEFT);
			dos.writeInt((int)angle);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void robotForward(double distance){
		
	}
	
	public void robotBackwards(double distance){
		
	}
	
	public void openRobotArms(){
		
	}
	
	public void closeRobotArms(){
		
	}
	
	public void deliverBall(){
		
	}
}

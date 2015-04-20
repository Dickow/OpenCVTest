package robotCommunication;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import lejos.nxt.LCD;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.Motor;


public class BTReciever {
	private static final int QUIT = -1, TURNLEFT = 0, TURNRIGHT = 1,
			FORWARD = 2, BACKWARDS = 3, STOP = 4, OPEN = 5, CLOSE = 6,
			DELIVER = 7;

	public static void main(String[] args) throws Exception {
		String connected = "Connected";
		String waiting = "Waiting...";
		String closing = "Closing...";
		String excecuting = "Excecuting";
		String test = "Test";

		while (true) {

			LCD.clear();
			LCD.drawString(test, 0, 0);
			LCD.refresh();

			BTConnection btc = Bluetooth.waitForConnection();

			LCD.clear();
			LCD.drawString(connected, 0, 0);
			LCD.refresh();

			DataInputStream dis = btc.openDataInputStream();
			DataOutputStream dos = btc.openDataOutputStream();
			while (true) {

				LCD.clear();
				LCD.drawString(waiting, 0, 0);
				LCD.refresh();

				int n = dis.readInt();

				LCD.clear();
				LCD.drawString(excecuting, 0, 0);
				LCD.refresh();

				switch (n) {
				case QUIT:
					dis.close();
					dos.close();
					Thread.sleep(100); // wait for data to drain
					LCD.clear();
					LCD.drawString(closing, 0, 0);
					LCD.refresh();
					btc.close();
					LCD.clear();
					break;
				case TURNLEFT:
					int angle = dis.readInt();
//					double angleTime = 93.26;
//					double time = angle/angleTime;
					Motor.A.rotate(angle, true);;
					Motor.B.rotate(-angle, true);;
					
					break;

				case TURNRIGHT:

					break;

				case FORWARD:

					break;

				case BACKWARDS:

					break;

				case STOP:

					break;

				case OPEN:

					break;

				case CLOSE:

					break;

				case DELIVER:

					break;
				}
			}

			// Turns robot left
			//
			// if (command == 'a'){
			// if(angle >= 0)
			// turnLeft(angle)
			//
			// }
		}

	}
	// }

	// public void turnLeft(int angle){
	// double angleTime = 93.26;
	// double time = angle / angleTime;
	// motor.A.forward();
	// motor.B.backward();
	// wait(time);
	// }
}

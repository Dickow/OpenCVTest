package imageProcess;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.util.ArrayList;

import moveableObjects.Ball;
import moveableObjects.Robot;
import obstacles.Goal;
import obstacles.MiddleCross;
import obstacles.ObstacleFrame;

public class WindowApplet extends Applet implements Runnable {
	private boolean newInput;
	private static final long serialVersionUID = -695085763165967408L;
	private ArrayList<Ball> balls;
	private Robot robot;
	private Goal goalA, goalB;
	private ObstacleFrame frames;
	private MiddleCross cross;

	@Override
	public void run() {
		// keep polling for information from the image processing
		newInput = false;

		while (true) {

			while (!newInput()) {
				// keep polling the new input
			}
			takeNewInput();
			repaint();
			sleep();
		}

	}

	public synchronized boolean newInput() {
		return this.newInput;
	}

	public synchronized void setNewInput() {
		this.newInput = true;
	}

	public synchronized void takeNewInput() {
		this.newInput = false;
	}

	public void updateAllComponents(ArrayList<Ball> balls, Robot robot,
			Goal goalA, Goal goalB, ObstacleFrame frames, MiddleCross cross) {
		this.balls = balls; 
		this.robot = robot; 
		this.goalA = goalA; 
		this.goalB = goalB; 
		this.frames = frames; 
		this.cross = cross; 
	}

	public void init() {
		System.out.println("windows opened");
		setSize(640, 480);
		setBackground(Color.BLACK);
		setFocusable(true);
		Frame frame = (Frame) this.getParent().getParent();
		frame.setTitle("Robot test framework");
	}

	public void paint(Graphics g) {
		// paint the frame
		g.setColor(Color.RED);

		g.drawLine((int) frames.topLeft().getX(),
				(int) frames.topLeft().getY(), (int) frames.topRight().getX(),
				(int) frames.topRight().getY());
		g.drawLine((int) frames.topLeft().getX(),
				(int) frames.topLeft().getY(), (int) frames.lowLeft().getX(),
				(int) frames.lowLeft().getY());
		g.drawLine((int) frames.topRight().getX(), (int) frames.topRight()
				.getY(), (int) frames.lowRight().getX(), (int) frames
				.lowRight().getY());
		g.drawLine((int) frames.lowLeft().getX(),
				(int) frames.lowLeft().getY(), (int) frames.lowRight().getX(),
				(int) frames.lowRight().getY());

		// paint the cross
		g.fillRect(
				(int) cross.getLeftCross().getX(),
				(int) cross.getLeftCross().getY() - 10,
				(int) ((frames.topRight().getX() - frames.topLeft().getX()) / 18) * 2,
				10);

		g.fillRect(
				(int) cross.getTopCross().getX() - 10,
				(int) cross.getTopCross().getY(),
				10,
				(int) ((frames.lowRight().getY() - frames.topRight().getY()) / 12) * 2);

		// paint the goals
		g.setColor(Color.BLUE);
		g.drawLine((int) goalB.getX(), (int) goalB.getY() - 35,
				(int) goalB.getX(), (int) goalB.getY() + 40);
		g.drawLine((int) goalA.getX(), (int) goalA.getY() - 14,
				(int) goalA.getX(), (int) goalA.getY() + 16);

		// paint the robot
		g.setColor(Color.GREEN);
		g.drawLine((int) robot.getBackCord().getX(), (int) robot.getBackCord()
				.getY(), (int) robot.getFrontCord().getX(), (int) robot
				.getFrontCord().getY());
		g.setColor(Color.RED);
		g.fillRect((int) robot.getFrontCord().getX(), (int) robot
				.getFrontCord().getY(), 5, 5);
		g.setColor(Color.BLUE);
		g.fillRect((int) robot.getBackCord().getX(), (int) robot.getBackCord()
				.getY(), 5, 5);

		// paint all the remaining balls
		g.setColor(Color.CYAN);
		try {
			for (Ball ball : balls) {
				g.drawOval((int) ball.getX() - 5, (int) ball.getY() - 5, 10, 10);
			}
		} catch (Exception e) {
			// do nothing
		}
	}

	private void sleep() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}

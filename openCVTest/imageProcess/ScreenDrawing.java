package imageProcess;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;

import moveableObjects.Ball;
import moveableObjects.Robot;
import obstacles.Goal;
import obstacles.MiddleCross;
import obstacles.ObstacleFrame;

public class ScreenDrawing extends Canvas {

	private static final long serialVersionUID = 2536246323158620153L;
	private ArrayList<Ball> balls = new ArrayList<Ball>();
	private Robot robot = new Robot();
	private Goal goalA, goalB;
	private ObstacleFrame frames;
	private MiddleCross cross;
	private Image backGroundImg;

	public void paint(Graphics g) {
		try {
			g.drawImage(backGroundImg, 0, 0, 640, 480, getParent());
			// paint the frame
//			g.setColor(Color.RED);
//
//			g.drawLine((int) frames.topLeft().getX(), (int) frames.topLeft()
//					.getY(), (int) frames.topRight().getX(), (int) frames
//					.topRight().getY());
//			g.drawLine((int) frames.topLeft().getX(), (int) frames.topLeft()
//					.getY(), (int) frames.lowLeft().getX(), (int) frames
//					.lowLeft().getY());
//			g.drawLine((int) frames.topRight().getX(), (int) frames.topRight()
//					.getY(), (int) frames.lowRight().getX(), (int) frames
//					.lowRight().getY());
//			g.drawLine((int) frames.lowLeft().getX(), (int) frames.lowLeft()
//					.getY(), (int) frames.lowRight().getX(), (int) frames
//					.lowRight().getY());
//
//			// paint the cross
//			g.fillRect((int) cross.getLeftCross().getX(), (int) cross
//					.getLeftCross().getY() - 10, (int) ((frames.topRight()
//					.getX() - frames.topLeft().getX()) / 18) * 2, 10);
//
//			g.fillRect(
//					(int) cross.getTopCross().getX() - 10,
//					(int) cross.getTopCross().getY(),
//					10,
//					(int) ((frames.lowRight().getY() - frames.topRight().getY()) / 12) * 2);
//
//			// paint the goals
			g.setColor(Color.BLUE);
			g.drawLine((int) goalB.getX(), (int) goalB.getY() - 35,
					(int) goalB.getX(), (int) goalB.getY() + 40);
			g.drawLine((int) goalA.getX(), (int) goalA.getY() - 14,
					(int) goalA.getX(), (int) goalA.getY() + 16);

//			// paint the robot
//			g.setColor(Color.GREEN);
//			g.drawLine((int) robot.getBackCord().getX(), (int) robot
//					.getBackCord().getY(), (int) robot.getFrontCord().getX(),
//					(int) robot.getFrontCord().getY());
//			g.setColor(Color.RED);
//			g.fillRect((int) robot.getFrontCord().getX(), (int) robot
//					.getFrontCord().getY(), 5, 5);
//			g.setColor(Color.BLUE);
//			g.fillRect((int) robot.getBackCord().getX(), (int) robot
//					.getBackCord().getY(), 5, 5);
//
//			// paint all the remaining balls
//			g.setColor(Color.CYAN);
		} catch (Exception e) {

		}
//		try {
//			for (Ball ball : balls) {
//				g.drawOval((int) ball.getX() - 5, (int) ball.getY() - 5, 10, 10);
//			}
//		} catch (Exception e) {
//			// do nothing
//		}
	}

	public void setBalls(ArrayList<Ball> balls) {
		this.balls = balls;
	}

	public void setRobot(Robot robot) {
		this.robot = robot;
	}

	public void setGoalA(Goal goalA) {
		this.goalA = goalA;
	}

	public void setGoalB(Goal goalB) {
		this.goalB = goalB;
	}

	public void setFrames(ObstacleFrame frames) {
		this.frames = frames;
	}

	public void setCross(MiddleCross cross) {
		this.cross = cross;
	}

	public void setBackGroundImg(Image backGroundImg) {
		this.backGroundImg = backGroundImg;
	}

}

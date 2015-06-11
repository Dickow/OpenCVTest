package screen;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;

import moveableObjects.Ball;
import moveableObjects.Coordinate;
import moveableObjects.MoveState;
import moveableObjects.Robot;
import obstacles.Goal;
import obstacles.MiddleCross;
import obstacles.ObstacleFrame;
import routing.Pathfinder;
import routing.RobotState;

public class TestMain extends Applet implements Runnable {

	private static final long serialVersionUID = -5731219325204852230L;
	private Goal goalA, goalB, goalADelivery;
	private Coordinate safePoint;
	private ObstacleFrame frames;
	private ArrayList<Ball> balls = new ArrayList<Ball>();
	private Pathfinder router;
	private RotationState state;
	private MiddleCross cross;
	private Robot robot;

	public void init() {
		setSize(640, 480);
		setBackground(Color.BLACK);
		setFocusable(true);
		Frame frame = (Frame) this.getParent().getParent();
		frame.setTitle("Robot test framework");
	}

	private void createObjects() {
		// create the robot
		robot = new Robot(new Coordinate(100, 100), new Coordinate(100, 130));

		// create the balls placed on the map
		Random rand = new Random();
		for (int i = 0; i < 5; i++) {

			int randomX = rand.nextInt((625 - 15) + 1) + 15;
			int randomY = rand.nextInt((465 - 15) + 1) + 15;
			balls.add(new Ball(randomX, randomY));
		}
		balls.add(new Ball(400, 250));

	}

	private void createObstacles() {

		// setup the frames
		frames = new ObstacleFrame();

		// setup the goals
		goalA = new Goal(
				((frames.topLeft().getX() + frames.lowLeft().getX()) / 2),
				(frames.topLeft().getY() + frames.lowLeft().getY()) / 2);
		goalB = new Goal(
				((frames.topRight().getX() + frames.lowRight().getX()) / 2),
				(frames.topRight().getY() + frames.lowRight().getY()) / 2);

		goalADelivery = new Goal(goalA.getX() + 30, goalA.getY());

		cross = new MiddleCross((frames.topRight().getX() + frames.topLeft()
				.getX()) / 2, (frames.lowLeft().getY() + frames.topLeft()
				.getY()) / 2);

		cross.setLeftCross(new Coordinate(cross.getCenterOfCross().getX()
				- ((frames.topRight().getX() - frames.topLeft().getX()) / 18),
				cross.getCenterOfCross().getY()));

		cross.setRightCross(new Coordinate(cross.getCenterOfCross().getX()
				+ ((frames.topRight().getX() - frames.topLeft().getX()) / 18),
				cross.getCenterOfCross().getY()));

		cross.setTopCross(new Coordinate(cross.getCenterOfCross().getX(), cross
				.getCenterOfCross().getY()
				- ((frames.lowRight().getY() - frames.topRight().getY()) / 12)));

		cross.setBottomCross(new Coordinate(
				cross.getCenterOfCross().getX(),
				cross.getCenterOfCross().getY()
						+ ((frames.lowRight().getY() - frames.topRight().getY()) / 12)));

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

	public void updateComponents() {

		if (robot.getState() == MoveState.ROTATING) {

			state = RotationState.ZERO;
			while (state != RotationState.TEN) {

				double rotationAngle = router.rotationAngle / 10;
				robot.rotateRobot(rotationAngle);
				nextState();
				repaint();
				// set slower rotation
				sleep();
			}
			robot.setState(MoveState.MOVING);
		} else if (robot.getState() == MoveState.MOVING) {
			state = RotationState.ZERO;
			while (state != RotationState.TEN) {
				double distance = router.lengthToDest / 10;

				// there are 2 types of destinations, balls or goals

				// go for the ball
				if (router.getState() == RobotState.NOBALL) {
					robot.forward(distance, new Coordinate(router.getDest()
							.getX(), router.getDest().getY()));

					nextState();
					repaint();
					sleep();
				} else if (router.getState() == RobotState.SAFETY) {
					safePoint = new Coordinate(router.testDest().getX(),
							router.testDest().getY());
					//robot.forward(distance, cord);

					if (router.getPrevState() == RobotState.TO_DELIVER) {
						router.setState(RobotState.TO_DELIVER);
					} else {
						router.setState(RobotState.NOBALL);
					}
					nextState();
					repaint();
					sleep();
					break;

				} else if (router.getState() == RobotState.GRABBALL) {
					balls.remove(router.getDest());
					router.setState(RobotState.HASBALL);
					repaint();
					sleep();
					break;
				}
				// go for the goal
				else if (router.getState() == RobotState.HASBALL
						|| router.getState() == RobotState.TO_DELIVER
						|| router.getState() == RobotState.AT_DELIVER
						|| router.getState() == RobotState.SAFETY) {

					robot.forward(distance, router.dest);
					nextState();
					repaint();
					sleep();

				}

			}

		} else {

		}

	}

	@Override
	public void run() {
		while (true) {

			router.findPath(robot,balls, goalA, goalB, goalADelivery, frames, cross);

			updateComponents();

		}

	}

	public void start() {
		createObjects();
		createObstacles();
		router = new Pathfinder();
		state = RotationState.ZERO;
		Thread thread = new Thread(this);
		thread.start();
	}

	private void sleep() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void nextState() {
		switch (state) {
		case ZERO:
			state = RotationState.ONE;
			break;
		case ONE:
			state = RotationState.TWO;
			break;
		case TWO:
			state = RotationState.THREE;
			break;
		case THREE:
			state = RotationState.FOUR;
			break;
		case FOUR:
			state = RotationState.FIVE;
			break;
		case FIVE:
			state = RotationState.SIX;
			break;
		case SIX:
			state = RotationState.SEVEN;
			break;
		case SEVEN:
			state = RotationState.EIGHT;
			break;
		case EIGHT:
			state = RotationState.NINE;
			break;
		case NINE:
			state = RotationState.TEN;
			break;
		case TEN:
			state = RotationState.ZERO;
			break;

		default:
			break;
		}

	}

}

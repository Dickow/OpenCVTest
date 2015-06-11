package imageProcess;

import imageCapture.ImageCapturer;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MediaTracker;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class WindowApplet extends Applet implements Runnable {
	private static final long serialVersionUID = -695085763165967408L;

	private ScreenDrawing drawer;
	private Image backGroundImg;
	private JSlider lowerHSlider, upperHSlider, lowerSSlider, upperSSlider,
			lowerVSlider, upperVSlider;
	private JSlider lowerHSlider2, upperHSlider2, lowerSSlider2, upperSSlider2,
			lowerVSlider2, upperVSlider2;
	private JLabel lowerH, upperH, lowerS, upperS, lowerV, upperV;
	private JLabel lowerH2, upperH2, lowerS2, upperS2, lowerV2, upperV2;
	private ImageProcessing processing;

	@Override
	public void run() {

		// start the processing thread
		// start the img capture thread
		 ImageCapturer capturer = new ImageCapturer();
		 Thread capThread = new Thread(capturer);
		 capThread.start();

		while (true) {

			// process a new image
			processing.process();

			// get the newly process objects in the image
			drawer.setBalls(processing.getBalls());
			drawer.setCross(processing.getCross());
			drawer.setFrames(processing.getFrames());
			drawer.setGoalA(processing.getGoalA());
			drawer.setGoalB(processing.getGoalB());
			drawer.setRobot(processing.getRobot());
			backGroundImg = processing.getBackgroundImage();
			drawer.setBackGroundImg(backGroundImg);
			drawer.repaint();
			
			// sleep for a bit
			sleep();
		}

	}

	public void start() {
		Thread thread = new Thread(this);
		thread.start();
	}

	public void init() {
		System.out.println("window opened");
		setLayout(new FlowLayout());
		//backGroundImg = getImage(getCodeBase(), "test.jpg");
		setFocusable(true);
		setSize(640, 640);
		Frame frame = (Frame) this.getParent().getParent();
		frame.setTitle("Robot Screen");
		processing = new ImageProcessing();
		backGroundImg = processing.getBackgroundImage();
		drawer = new ScreenDrawing();
		drawer.setPreferredSize(new Dimension(640,480));
		
		// add the canvas
		add(drawer);
		
		// create all the sliders 
		SlideListener slideListener = new SlideListener();
		// set the Labels
		lowerH = new JLabel("lower H Back : " + processing.iLowH);
		upperH = new JLabel("upper H Back : " + processing.iHighH);

		lowerS = new JLabel("lower S Back: " + processing.iLowS);
		upperS = new JLabel("upper S Back: " + processing.iHighS);

		lowerV = new JLabel("lower V Back: " + processing.iLowV);
		upperV = new JLabel("upper V Back: " + processing.iHighV);

		// set the Sliders
		lowerHSlider = new JSlider(0, 255, processing.iLowH);
		lowerHSlider.addChangeListener(slideListener);
		upperHSlider = new JSlider(0, 255, processing.iHighH);
		upperHSlider.addChangeListener(slideListener);

		lowerSSlider = new JSlider(0, 255, processing.iLowS);
		lowerSSlider.addChangeListener(slideListener);
		upperSSlider = new JSlider(0, 255, processing.iHighS);
		upperSSlider.addChangeListener(slideListener);

		lowerVSlider = new JSlider(0, 255, processing.iLowV);
		lowerVSlider.addChangeListener(slideListener);
		upperVSlider = new JSlider(0, 255, processing.iHighV);
		upperVSlider.addChangeListener(slideListener);
		
		// add all the components to the panel

		add(lowerH);
		add(lowerHSlider);
		add(upperH);
		add(upperHSlider);

		add(lowerS);
		add(lowerSSlider);
		add(upperS);
		add(upperSSlider);

		add(lowerV);
		add(lowerVSlider);
		add(upperV);
		add(upperVSlider);
		
		// set the Labels of the blue color find
		lowerH2 = new JLabel("lower H Front: " + processing.iLowHFront);
		upperH2 = new JLabel("upper H Front: " + processing.iHighHFront);

		lowerS2 = new JLabel("lower S Front: " + processing.iLowSFront);
		upperS2 = new JLabel("upper S Front: " + processing.iHighSFront);

		lowerV2 = new JLabel("lower V Front: " + processing.iLowVFront);
		upperV2 = new JLabel("upper V Front: " + processing.iHighVFront);

		// set the Sliders
		lowerHSlider2 = new JSlider(0, 255, processing.iLowHFront);
		lowerHSlider2.addChangeListener(slideListener);
		upperHSlider2 = new JSlider(0, 255, processing.iHighHFront);
		upperHSlider2.addChangeListener(slideListener);

		lowerSSlider2 = new JSlider(0, 255, processing.iLowSFront);
		lowerSSlider2.addChangeListener(slideListener);
		upperSSlider2 = new JSlider(0, 255, processing.iHighSFront);
		upperSSlider2.addChangeListener(slideListener);

		lowerVSlider2 = new JSlider(0, 255, processing.iLowVFront);
		lowerVSlider2.addChangeListener(slideListener);
		upperVSlider2 = new JSlider(0, 255, processing.iHighVFront);
		upperVSlider2.addChangeListener(slideListener);

		// add all the components to the panel

		add(lowerH2);
		add(lowerHSlider2);
		add(upperH2);
		add(upperHSlider2);

		add(lowerS2);
		add(lowerSSlider2);
		add(upperS2);
		add(upperSSlider2);

		add(lowerV2);
		add(lowerVSlider2);
		add(upperV2);
		add(upperVSlider2);

	}
	
	private void sleep() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private class SlideListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() == lowerHSlider) {
				lowerH.setText("lower H Back: " + lowerHSlider.getValue());
				processing.iLowH = lowerHSlider.getValue();
			} else if (e.getSource() == upperHSlider) {
				upperH.setText("upper H Back: " + upperHSlider.getValue());
				processing.iHighH = upperHSlider.getValue();
			} else if (e.getSource() == lowerSSlider) {
				lowerS.setText("lower S Back: " + lowerSSlider.getValue());
				processing.iLowS = lowerSSlider.getValue();
			} else if (e.getSource() == upperSSlider) {
				upperS.setText("upper S Back: " + upperSSlider.getValue());
				processing.iHighS = upperSSlider.getValue();
			} else if (e.getSource() == lowerVSlider) {
				lowerV.setText("lower V Back: " + lowerVSlider.getValue());
				processing.iLowV = lowerVSlider.getValue();
			} else if (e.getSource() == upperVSlider) {
				upperV.setText("upper V Back: " + upperVSlider.getValue());
				processing.iHighV = upperVSlider.getValue();
			}
			// second part of the sliders
			else if (e.getSource() == lowerHSlider2) {
				lowerH2.setText("lower H Front: " + lowerHSlider2.getValue());
				processing.iLowHFront = lowerHSlider2.getValue();
			} else if (e.getSource() == upperHSlider2) {
				upperH2.setText("upper H Front: " + upperHSlider2.getValue());
				processing.iHighHFront = upperHSlider2.getValue();
			} else if (e.getSource() == lowerSSlider2) {
				lowerS2.setText("lower S Front: " + lowerSSlider2.getValue());
				processing.iLowSFront = lowerSSlider2.getValue();
			} else if (e.getSource() == upperSSlider2) {
				upperS2.setText("upper S Front: " + upperSSlider2.getValue());
				processing.iHighSFront = upperSSlider2.getValue();
			} else if (e.getSource() == lowerVSlider2) {
				lowerV2.setText("lower V Front: " + lowerVSlider2.getValue());
				processing.iLowVFront = lowerVSlider2.getValue();
			} else if (e.getSource() == upperVSlider2) {
				upperV2.setText("upper V Front: " + upperVSlider2.getValue());
				processing.iHighVFront = upperVSlider2.getValue();
			}

		}

	}
}

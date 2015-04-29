package imageProcess;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class TrackbarWindow {

	private JFrame frame;
	private JPanel trackPanel, imgPanel;
	private JSlider lowerHSlider, upperHSlider, lowerSSlider, upperSSlider,
			lowerVSlider, upperVSlider;
	private JSlider lowerHSlider2, upperHSlider2, lowerSSlider2, upperSSlider2,
			lowerVSlider2, upperVSlider2;
	private JLabel lowerH, upperH, lowerS, upperS, lowerV, upperV, imgLabel,
			imgLabel2;
	private JLabel lowerH2, upperH2, lowerS2, upperS2, lowerV2, upperV2;
	private ContourTest imageProcess;
	private SlideListener slideListener = new SlideListener();
	private boolean imageIsSet = false;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TrackbarWindow window = new TrackbarWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * 
	 * @throws IOException
	 */
	public TrackbarWindow() throws IOException {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @throws IOException
	 */
	private void initialize() throws IOException {
		frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setBounds(100, 100, 640, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		trackPanel = new JPanel(new GridLayout(2, 2));
		imgPanel = new JPanel(new FlowLayout());
		frame.getContentPane().add(trackPanel, BorderLayout.SOUTH);
		frame.getContentPane().add(imgPanel, BorderLayout.CENTER);

		imageProcess = new ContourTest();
		new Thread(imageProcess).start();

		// set the Labels
		lowerH = new JLabel("lower H : " + imageProcess.iLowH);
		upperH = new JLabel("upper H : " + imageProcess.iHighH);

		lowerS = new JLabel("lower S : " + imageProcess.iLowS);
		upperS = new JLabel("upper S : " + imageProcess.iHighS);

		lowerV = new JLabel("lower V : " + imageProcess.iLowV);
		upperV = new JLabel("upper V : " + imageProcess.iHighV);

		// set the Sliders
		lowerHSlider = new JSlider(0, 255, imageProcess.iLowH);
		lowerHSlider.addChangeListener(slideListener);
		upperHSlider = new JSlider(0, 255, imageProcess.iHighH);
		upperHSlider.addChangeListener(slideListener);

		lowerSSlider = new JSlider(0, 255, imageProcess.iLowS);
		lowerSSlider.addChangeListener(slideListener);
		upperSSlider = new JSlider(0, 255, imageProcess.iHighS);
		upperSSlider.addChangeListener(slideListener);

		lowerVSlider = new JSlider(0, 255, imageProcess.iLowV);
		lowerVSlider.addChangeListener(slideListener);
		upperVSlider = new JSlider(0, 255, imageProcess.iHighV);
		upperVSlider.addChangeListener(slideListener);

		// add all the components to the panel

		trackPanel.add(lowerH);
		trackPanel.add(lowerHSlider);
		trackPanel.add(upperH);
		trackPanel.add(upperHSlider);

		trackPanel.add(lowerS);
		trackPanel.add(lowerSSlider);
		trackPanel.add(upperS);
		trackPanel.add(upperSSlider);

		trackPanel.add(lowerV);
		trackPanel.add(lowerVSlider);
		trackPanel.add(upperV);
		trackPanel.add(upperVSlider);

		// set the Labels of the blue color find
		lowerH2 = new JLabel("lower H : " + imageProcess.iLowH2);
		upperH2 = new JLabel("upper H : " + imageProcess.iHighH2);

		lowerS2 = new JLabel("lower S : " + imageProcess.iLowS2);
		upperS2 = new JLabel("upper S : " + imageProcess.iHighS2);

		lowerV2 = new JLabel("lower V : " + imageProcess.iLowV2);
		upperV2 = new JLabel("upper V : " + imageProcess.iHighV2);

		// set the Sliders
		lowerHSlider2 = new JSlider(0, 255, imageProcess.iLowH2);
		lowerHSlider2.addChangeListener(slideListener);
		upperHSlider2 = new JSlider(0, 255, imageProcess.iHighH2);
		upperHSlider2.addChangeListener(slideListener);

		lowerSSlider2 = new JSlider(0, 255, imageProcess.iLowS2);
		lowerSSlider2.addChangeListener(slideListener);
		upperSSlider2 = new JSlider(0, 255, imageProcess.iHighS2);
		upperSSlider2.addChangeListener(slideListener);

		lowerVSlider2 = new JSlider(0, 255, imageProcess.iLowV2);
		lowerVSlider2.addChangeListener(slideListener);
		upperVSlider2 = new JSlider(0, 255, imageProcess.iHighV2);
		upperVSlider2.addChangeListener(slideListener);

		// add all the components to the panel

		trackPanel.add(lowerH2);
		trackPanel.add(lowerHSlider2);
		trackPanel.add(upperH2);
		trackPanel.add(upperHSlider2);

		trackPanel.add(lowerS2);
		trackPanel.add(lowerSSlider2);
		trackPanel.add(upperS2);
		trackPanel.add(upperSSlider2);

		trackPanel.add(lowerV2);
		trackPanel.add(lowerVSlider2);
		trackPanel.add(upperV2);
		trackPanel.add(upperVSlider2);

		setupTimer();

	}

	private void setupTimer() {
		// setup a timer to update frame continuously
		int delay = 100; // milliseconds
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// add an image to the top panel
				if (imageProcess.outImg != null) {
					imgLabel.removeAll();
					imgLabel.setIcon(new ImageIcon(imageProcess.outImg));
					imgLabel2.removeAll();
					imgLabel2.setIcon(new ImageIcon(imageProcess.outImg2));
					//System.out.println("input image");
				} else {
					try {
						if (!imageIsSet) {
							imgLabel = new JLabel(new ImageIcon(
									ImageIO.read(new File("cameraTest.jpg"))));
							imgLabel2 = new JLabel(new ImageIcon(
									ImageIO.read(new File("cameraTest.jpg"))));
							imgPanel.add(imgLabel);
							imgPanel.add(imgLabel2);
							System.out.println("new image");
							imageIsSet = true;
						}
					} catch (IOException e) {
						System.out
								.println("could not read standard image fatal error");
					}
				}

			}
		};
		new Timer(delay, taskPerformer).start();
	}

	private class SlideListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() == lowerHSlider) {
				lowerH.setText("lower H : " + lowerHSlider.getValue());
				imageProcess.iLowH = lowerHSlider.getValue();
				// imgLabel.removeAll();
				// imgLabel.setIcon(new ImageIcon(imageProcess.outImg));
			} else if (e.getSource() == upperHSlider) {
				upperH.setText("upper H : " + upperHSlider.getValue());
				imageProcess.iHighH = upperHSlider.getValue();
				// imgLabel.removeAll();
				// imgLabel.setIcon(new ImageIcon(imageProcess.outImg));
			} else if (e.getSource() == lowerSSlider) {
				lowerS.setText("lower S : " + lowerSSlider.getValue());
				imageProcess.iLowS = lowerSSlider.getValue();
				// imgLabel.removeAll();
				// imgLabel.setIcon(new ImageIcon(imageProcess.outImg));
			} else if (e.getSource() == upperSSlider) {
				upperS.setText("upper S : " + upperSSlider.getValue());
				imageProcess.iHighS = upperSSlider.getValue();
				// imgLabel.removeAll();
				// imgLabel.setIcon(new ImageIcon(imageProcess.outImg));
			} else if (e.getSource() == lowerVSlider) {
				lowerV.setText("lower V : " + lowerVSlider.getValue());
				imageProcess.iLowV = lowerVSlider.getValue();
				// imgLabel.removeAll();
				// imgLabel.setIcon(new ImageIcon(imageProcess.outImg));
			} else if (e.getSource() == upperVSlider) {
				upperV.setText("upper V : " + upperVSlider.getValue());
				imageProcess.iHighV = upperVSlider.getValue();
				// imgLabel.removeAll();
				// imgLabel.setIcon(new ImageIcon(imageProcess.outImg));
			}
			// second part of the sliders
			if (e.getSource() == lowerHSlider2) {
				lowerH2.setText("lower H : " + lowerHSlider2.getValue());
				imageProcess.iLowH2 = lowerHSlider2.getValue();
				// imgLabel.removeAll();
				// imgLabel.setIcon(new ImageIcon(imageProcess.outImg));
			} else if (e.getSource() == upperHSlider2) {
				upperH2.setText("upper H : " + upperHSlider2.getValue());
				imageProcess.iHighH2 = upperHSlider2.getValue();
				// imgLabel.removeAll();
				// imgLabel.setIcon(new ImageIcon(imageProcess.outImg));
			} else if (e.getSource() == lowerSSlider2) {
				lowerS2.setText("lower S : " + lowerSSlider2.getValue());
				imageProcess.iLowS2 = lowerSSlider2.getValue();
				// imgLabel.removeAll();
				// imgLabel.setIcon(new ImageIcon(imageProcess.outImg));
			} else if (e.getSource() == upperSSlider2) {
				upperS2.setText("upper S : " + upperSSlider2.getValue());
				imageProcess.iHighS2 = upperSSlider2.getValue();
				// imgLabel.removeAll();
				// imgLabel.setIcon(new ImageIcon(imageProcess.outImg));
			} else if (e.getSource() == lowerVSlider2) {
				lowerV2.setText("lower V : " + lowerVSlider2.getValue());
				imageProcess.iLowV2 = lowerVSlider2.getValue();
				// imgLabel.removeAll();
				// imgLabel.setIcon(new ImageIcon(imageProcess.outImg));
			} else if (e.getSource() == upperVSlider2) {
				upperV2.setText("upper V : " + upperVSlider2.getValue());
				imageProcess.iHighV2 = upperVSlider2.getValue();
				// imgLabel.removeAll();
				// imgLabel.setIcon(new ImageIcon(imageProcess.outImg));
			}

		}

	}

}

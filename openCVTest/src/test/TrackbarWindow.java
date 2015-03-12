package test;

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
	private JLabel lowerH, upperH, lowerS, upperS, lowerV, upperV, imgLabel;
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

		// setup a timer to update frame continuously
		int delay = 100; // milliseconds
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// add an image to the top panel
				if (imageProcess.outImg != null) {
					imgLabel.removeAll();
					imgLabel.setIcon((new ImageIcon(imageProcess.outImg)));
					System.out.println("input image");
				} else {
					try {
						if (!imageIsSet) {
							imgLabel = new JLabel(new ImageIcon(
									ImageIO.read(new File("cameraInput.jpg"))));
							imgPanel.add(imgLabel);
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

		}

	}

}

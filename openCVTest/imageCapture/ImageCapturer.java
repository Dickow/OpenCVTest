package imageCapture;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

public class ImageCapturer implements Runnable {
	private VideoCapture videoCapture;

	@Override
	public void run() {

		// Load the library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		// get a picture from the webcam and save it VideoCapture
		videoCapture = new VideoCapture(0);
		if (!videoCapture.isOpened()) {
			System.out.println("could not find video ");
		} else {
			System.out.println(" webcam was found: " + videoCapture.toString());
		}
		Mat img = new Mat();
		while (true) {

			videoCapture.read(img);
			ImageObject.getInstance().setImg(img);
			Highgui.imwrite("image.jpg", img);
			try {
				Thread.sleep(100);
			} catch (Exception e) {

			}

		}

	}
}

package startPackage;

import imageCapture.ImageCapturer;
import imageProcess.ImageProcessing;

public class StartMain {

	public static void main(String[] args) {
		// start the new image processing thread
		ImageProcessing processing = new ImageProcessing();
		Thread thread = new Thread(processing);
		thread.start(); 
		
		// start the image capturing process
		ImageCapturer capturer = new ImageCapturer();
		Thread thread2  = new Thread(capturer);
		thread2.start();
	}

}

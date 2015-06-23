package imageCapture;

import org.opencv.core.Mat;

/**
 * This class holds the image that the camera captures, this class were used to
 * avoid race conditions. It is based on the singleton pattern.
 * 
 * @author Group 1
 *
 */
public class ImageObject {

	// variables
	private static ImageObject instance = null;
	private Mat matImg = new Mat();
	private boolean imgSet = false;

	protected ImageObject() {

	}

	public static ImageObject getInstance() {
		if (instance == null) {
			instance = new ImageObject();
		}
		return instance;
	}

	public synchronized Mat getImg() {

		if (imgSet == true) {
			imgSet = false;
			return this.matImg;
		} else {
			return null;
		}
	}

	public synchronized void setImg(Mat newImg) {
		imgSet = true;
		this.matImg = newImg;
	}

}

package application;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;



public class camtest {
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat frame = new Mat();
        VideoCapture cam = new VideoCapture();
        cam.open(0, Videoio.CAP_DSHOW); // try other backends if needed
        
        if (cam.isOpened()) {
            cam.read(frame);
            System.out.println("Frame size: " + frame.width() + "x" + frame.height());
        } else {
            System.out.println("Cannot open camera!");
        }
        
        cam.release();
    }
}


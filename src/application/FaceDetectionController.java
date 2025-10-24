package application;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.FaceDetectorYN;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import utils.Utils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FaceDetectionController {
    // FXML buttons
    @FXML
    private Button cameraButton;
    // the FXML area for showing the current frame
    @FXML
    private ImageView originalFrame;
    // checkboxes for enabling/disabling a classifier
    @FXML
    private CheckBox haarClassifier;
    @FXML
    private CheckBox lbpClassifier;
    @FXML
    private CheckBox yunetClassifier;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that performs the video capture
    private VideoCapture capture;
    // a flag to change the button behavior
    private boolean cameraActive;

    // face cascade classifier
    private CascadeClassifier faceCascade;
    private int absoluteFaceSize;

    // YuNet face detector
    private FaceDetectorYN yunetDetector;

    /**
     * Init the controller, at start time
     */
    protected void init() {
        this.capture = new VideoCapture();
        this.faceCascade = new CascadeClassifier();
        this.absoluteFaceSize = 0;

        // Initialize YuNet face detector
        String modelPath = "resources/yunet/face_detection_yunet_2023mar_int8bq.onnx";  // adjust if needed
        String configPath = ""; // leave empty for ONNX models
		Size inputSize = new Size(320, 320);
		float scoreThreshold = 0.9f;
		float nmsThreshold = 0.3f;
		int topK = 500;
		int backendId = 0; // default

        yunetDetector = FaceDetectorYN.create(modelPath, configPath, inputSize, scoreThreshold, nmsThreshold, topK, backendId);

        // set a fixed width for the frame
        originalFrame.setFitWidth(600);
        // preserve image ratio
        originalFrame.setPreserveRatio(true);

        // Initially disable camera button until classifier is selected
        cameraButton.setDisable(true);
    }

    /**
     * The action triggered by pushing the button on the GUI
     */
    @FXML
    protected void startCamera() {
        if (!this.cameraActive) {
            // disable setting checkboxes while camera is on
            this.haarClassifier.setDisable(true);
            this.lbpClassifier.setDisable(true);
            this.yunetClassifier.setDisable(true);

            // open the default camera
            this.capture.open(0);

            if (this.capture.isOpened()) {
                this.cameraActive = true;
                System.out.println("Camera opened successfully!");

                // Grab frames periodically
                Runnable frameGrabber = () -> {
                    Mat frame = grabFrame();
                    Image imageToShow = Utils.mat2Image(frame);
                    updateImageView(originalFrame, imageToShow);
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // Update button label
                this.cameraButton.setText("Stop Camera");
            } else {
                System.err.println("Failed to open the camera connection...");
            }
        } else {
            // Stop the camera
            this.cameraActive = false;
            this.cameraButton.setText("Start Camera");

            // Enable checkboxes again
            this.haarClassifier.setDisable(false);
            this.lbpClassifier.setDisable(false);
            this.yunetClassifier.setDisable(false);

            // Stop acquisition
            this.stopAcquisition();
        }
    }

    /**
     * Get a frame from the opened video stream (if any)
     * 
     * @return the {@link Image} to show
     */
    private Mat grabFrame() {
        Mat frame = new Mat();

        if (this.capture.isOpened()) {
            try {
                this.capture.read(frame);
                System.out.println("Grabbed frame: " + frame.width() + "x" + frame.height());
                if (!frame.empty()) {
                    detectAndDisplay(frame);
                }
            } catch (Exception e) {
                System.err.println("Exception during the image elaboration: " + e);
            }
        }
        return frame;
    }

    /**
     * Method for face detection and tracking
     * 
     * @param frame it looks for faces in this frame
     */
    private void detectAndDisplay(Mat frame) {
        if (yunetClassifier.isSelected()) {
			System.out.println("Using YuNet!");
            detectWithYuNet(frame);
        } else if (haarClassifier.isSelected() || lbpClassifier.isSelected()) {
            detectWithCascade(frame);
        }
        // else no detector selected, just show original frame
    }

    /**
     * Face detection using Haar or LBP cascades
     */
    private void detectWithCascade(Mat frame) {
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayFrame, grayFrame);

        if (this.absoluteFaceSize == 0) {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0) {
                this.absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        this.faceCascade.detectMultiScale(
            grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, 
            new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

        Rect[] facesArray = faces.toArray();
        for (Rect face : facesArray) {
            Imgproc.rectangle(frame, face.tl(), face.br(), new Scalar(0, 255, 0), 3);
        }
    }

    /**
     * Face detection using YuNet
     */
	private void detectWithYuNet(Mat frame) {
    yunetDetector.setInputSize(new Size(frame.cols(), frame.rows()));

    Mat detections = new Mat();
    yunetDetector.detect(frame, detections);

    System.out.println("YuNet detections: " + detections.rows());

    for (int i = 0; i < detections.rows(); i++) {
	// only grabbed 1 column worth of data (from column 0), which is why you only saw a partial/empty result. YuNet outputs 6 values per detection row
    // double[] det = detections.get(i, 0);  // this may only return 1 element
    
    // ✅ Correct way:
    float[] detF = new float[detections.cols()];
    detections.get(i, 0, detF);  // read all 15 values as float
    
    double x = detF[0];
    double y = detF[1];
    double w = detF[2];
    double h = detF[3];
    double score = detF[14];

    if (score >= 0.6) {
        // Green bounding box
        Imgproc.rectangle(frame,
            new org.opencv.core.Point(x, y),
            new org.opencv.core.Point(x + w, y + h),
            new Scalar(0, 255, 0), 2);

        // Draw landmarks
        for (int j = 4; j < 14; j += 2) {
            Imgproc.circle(frame,
                new org.opencv.core.Point(detF[j], detF[j+1]),
                2, new Scalar(0, 0, 255), -1);
        }

        // Label with score
        Imgproc.putText(frame, String.format("Conf: %.2f", score),
            new org.opencv.core.Point(x, y - 5),
            Imgproc.FONT_HERSHEY_SIMPLEX, 0.6,
            new Scalar(0, 255, 0), 2);
    }
}


    detections.release();
}


    /**
     * The action triggered by selecting the Haar Classifier checkbox. It loads
     * the trained set to be used for frontal face detection.
     */
    @FXML
    protected void haarSelected(Event event) {
        if (this.lbpClassifier.isSelected())
            this.lbpClassifier.setSelected(false);
        if (this.yunetClassifier.isSelected())
            this.yunetClassifier.setSelected(false);

        this.checkboxSelection("resources/haarcascades/haarcascade_frontalface_alt.xml");
        cameraButton.setDisable(false);
    }

    /**
     * The action triggered by selecting the LBP Classifier checkbox. It loads
     * the trained set to be used for frontal face detection.
     */
    @FXML
    protected void lbpSelected(Event event) {
        if (this.haarClassifier.isSelected())
            this.haarClassifier.setSelected(false);
        if (this.yunetClassifier.isSelected())
            this.yunetClassifier.setSelected(false);

        this.checkboxSelection("resources/lbpcascades/lbpcascade_frontalface.xml");
        cameraButton.setDisable(false);
    }

    /**
     * The action triggered by selecting the YuNet Classifier checkbox. It
     * enables YuNet detection mode.
     */
    @FXML
    protected void yunetSelected(Event event) {
        if (this.haarClassifier.isSelected())
            this.haarClassifier.setSelected(false);
        if (this.lbpClassifier.isSelected())
            this.lbpClassifier.setSelected(false);

        cameraButton.setDisable(false);
        // faceCascade not used in YuNet mode
    }

    /**
     * Method for loading a classifier trained set from disk
     * 
     * @param classifierPath the path on disk where a classifier trained set is located
     */
    private void checkboxSelection(String classifierPath) {
        this.faceCascade.load(classifierPath);
    }

    /**
     * Stop the acquisition from the camera and release all the resources
     */
    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.capture.isOpened()) {
            this.capture.release();
        }
    }

    /**
     * Update the {@link ImageView} in the JavaFX main thread
     * 
     * @param view  the {@link ImageView} to update
     * @param image the {@link Image} to show
     */
    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    /**
     * On application close, stop the acquisition from the camera
     */
    protected void setClosed() {
        this.stopAcquisition();
    }
}

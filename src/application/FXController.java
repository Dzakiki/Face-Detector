package application;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;


import utils.Utils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FXController {

    @FXML
    private Button button;
    @FXML
    private ImageView currentFrame;
    @FXML
    private CheckBox grayscale;

    private ScheduledExecutorService timer;
    private VideoCapture capture = new VideoCapture();
    private boolean cameraActive = false;
    private static final int cameraId = 0;

    
    @FXML
    protected void startCamera(ActionEvent event) {
        if (!this.cameraActive) {
            // Try opening camera with DirectShow backend
            this.capture.open(cameraId, Videoio.CAP_DSHOW);

            // Check if the camera opened
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                System.out.println("Camera opened successfully!");
                

                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {
                        Mat frame = grabFrame();
                        if (!frame.empty()) {
                            Image imageToShow = Utils.mat2Image(frame);
                            updateImageView(currentFrame, imageToShow);
                        } else {
                            System.err.println("Empty frame grabbed!");
                        }
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                this.button.setText("Stop Camera");
            } else {
                System.err.println("Impossible to open the camera connection...");
            }
        } else {
            // Stop the camera
            this.cameraActive = false;
            this.button.setText("Start Camera");
            stopAcquisition();
        }
    }


    private Mat grabFrame()
    {
        Mat frame = new Mat();

        if (capture.isOpened())
        {
            capture.read(frame);
            if (!frame.empty())
            {
            	System.out.println("Grabbed frame: " + frame.width() + "x" + frame.height());
                if (grayscale.isSelected())
                    {
                    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                    }
                // Convert BGR → RGB for proper JavaFX display
            //    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
            }else {
            	System.err.println("Empty Frame!");
            }
        }
        return frame;
    }


    private void stopAcquisition() {
        if (timer != null && !timer.isShutdown()) {
            try {
                timer.shutdown();
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Exception in stopping frame capture: " + e);
            }
        }

        if (capture.isOpened()) {
            capture.release();
        }
    }

    private void updateImageView(ImageView view, Image image) {
        Platform.runLater(() -> view.setImage(image));
    }

    protected void setClosed() {
        stopAcquisition();
    }
}

package freforall;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.OpenCVFrameGrabber;

import java.util.logging.Logger;
import static com.googlecode.javacv.cpp.opencv_core.*;

import java.util.*;
import java.util.logging.*;

/**
 * Experiment in using OpenCV's camera capture mechanisms to get frames. (see
 * for a more elaborate example that uses mpeg4 to encode the frames).
 */
public final class CameraCapture {

    private final static Logger log
            = Logger.getLogger(CameraCapture.class.getName());

    private final CanvasFrame canvas;
    private final OpenCVFrameGrabber grabber;

    private final static int MY_CAMERA = -0;

    public CameraCapture() throws FrameGrabber.Exception {
        this.canvas = new CanvasFrame("Camera");

        // request closing of the application when the image window is closed
        canvas.setDefaultCloseOperation(CanvasFrame.EXIT_ON_CLOSE);

        // 0-default camera, 1 - next...so on
        log.info("Getting grabber for camera " + MY_CAMERA);
        this.grabber = new RetryingOpenCVFrameGrabber(MY_CAMERA);
        grabber.start();
    }

    public void run(final FeatureDetection detector) {
        while (true) {
            final IplImage image;
            try {
                image = grabber.grab();
            } catch (FrameGrabber.Exception ex) {
                log.log(Level.SEVERE, "Couldn't grab: " + ex);
                continue;
            }
            detector.detect(image);
            canvas.showImage(image);
        }
    }


    public final static FeatureDetection getDetector() {
     final Collection< FeatureDetection.FeatureDetectorDescription> descs =
                new ArrayList<>();

        // A bunch of detectors.
        // XXX Consider reading this from a config file instead.
        descs.add(new FeatureDetection.FeatureDetectorDescription("frontalface", "haarcascade_frontalface_alt.xml"));
        descs.add(new FeatureDetection.FeatureDetectorDescription("fullbody", "haarcascade_fullbody.xml"));
        descs.add(new FeatureDetection.FeatureDetectorDescription("profile", "haarcascade_profileface.xml"));
        descs.add(new FeatureDetection.FeatureDetectorDescription("upperbody", "haarcascade_upperbody.xml"));
        descs.add(new FeatureDetection.FeatureDetectorDescription("lowerbody", "haarcascade_lowerbody.xml"));
        descs.add(new FeatureDetection.FeatureDetectorDescription("headshoulder", "HS.xml"));
        descs.add(new FeatureDetection.FeatureDetectorDescription("wallclock", "classifier_WallClock.xml"));

        return  new FeatureDetection(descs);
    }

    public final static void main(final String[] argv)
            throws FrameGrabber.Exception {
        final CameraCapture capture = new CameraCapture();
        capture.run(getDetector());
    }
}

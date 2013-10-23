package freforall;

import java.util.logging.*;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;

/**
 * An implementation of OpenCVFrameGrabber that in the face of failing grab
 * or start invocations, just tries and tries again until success.
 *
 * This is very much a hack and not something that should be considered good
 * and defensive programming.
 *
 */
public final class RetryingOpenCVFrameGrabber extends OpenCVFrameGrabber {
    private static final Logger log =
            Logger.getLogger(RetryingOpenCVFrameGrabber.class.getName());

    public RetryingOpenCVFrameGrabber(int deviceNumber) {
        super(deviceNumber);
    }

    @Override
    public void start() throws Exception {
        // Busy-wait
        while (true) {
            try {
                super.start();
                return;
            } catch (FrameGrabber.Exception e) {
                log.log(Level.WARNING, "Failed starting grabber {0}", this);
            }
        }
    }

    @Override
    public opencv_core.IplImage grab() throws Exception {
        while (true) {
            try {
                return super.grab();
            } catch (FrameGrabber.Exception e) {
                log.log(Level.WARNING,"Couldn''t grab frame, retrying "
                        + " in grabber {0}", this);
            }
        }
    }
}

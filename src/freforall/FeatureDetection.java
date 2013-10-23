package freforall;

import java.util.logging.*;
import com.googlecode.javacv.cpp.opencv_core;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;

import com.googlecode.javacpp.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;



/**
 * Detect features based on haar cascades from OpenCV.
 */
public final class FeatureDetection {

    private final static Logger log = Logger.getLogger(FeatureDetection.class.getName());

    private final static String CASCADE_CONFIG_FILE
            = "haarcascade_frontalface_alt_tree.xml";

    private final CascadeClassifier faceDetector;

    @Deprecated
    final String classifierDir = "/Users/rmz/Desktop/git/webrtc-peer/src/main/classifiers/";

    public final static class FeatureDetectorDescription {

        private final String name;
        private final String xmlfile;

        public FeatureDetectorDescription(final String name, final String xmlfile) {
            this.name = name;
            this.xmlfile = xmlfile;
        }
    }

    private final Map<FeatureDetectorDescription, CvHaarClassifierCascade> classifiers =
            new HashMap<>();

    public FeatureDetection(final Collection<FeatureDetectorDescription> descriptions) {
        // checkNotNull(descriptions);

        // XXX If the next line is removed (as it should be), then
        //     cvLoad method below blows up (as it shouldn't). WTF?
        faceDetector = new CascadeClassifier(classifierDir + CASCADE_CONFIG_FILE);

        for (final FeatureDetectorDescription desc : descriptions) {
            final Pointer filePointer = cvLoad(classifierDir + desc.xmlfile);
            // Preconditions.checkNotNull(filePointer, "No pointer to file " + desc.xmlfile);
            final CvHaarClassifierCascade haarCascade = new CvHaarClassifierCascade(filePointer);
            classifiers.put(desc, haarCascade);
        }
    }

    private final static class Label {

        private final CvRect rect;
        private final String name;

        private final static CvFont font = new CvFont(CV_FONT_HERSHEY_SIMPLEX, 0.5, 1);

        public Label(final CvRect rect, final String name) {
            this.rect = rect; // checkNotNull(rect);
            this.name = name; // checkNotNull(name);
        }

        public void imprint(final IplImage image) {
            // Adding some branding for the classifiers.

            cvRectangle(
                    image,
                    cvPoint(rect.x(), rect.y()),
                    cvPoint(rect.width() + rect.x(), rect.height() + rect.y()),
                    CvScalar.RED,
                    2,
                    CV_AA,
                    0);

            CvPoint labelLocation = cvPoint(rect.x() + (rect.width() / 2) - 50,
                    rect.y() + (rect.height() / 15) - 10);
            cvPutText(image, name, labelLocation, font, CvScalar.BLUE);
        }
    }


    /**
     * Will detect a feature and paint a red rectangle around it, thus
     * modifying the image.
     * @param image
     */
    public void detect(final IplImage image) {

        final Collection<Label> labels = new ArrayList<>();

        final Collection<Map.Entry<FeatureDetectorDescription, CvHaarClassifierCascade>> entrySet =
                classifiers.entrySet();

        // Loop through all the detctors
        for (final Map.Entry<FeatureDetectorDescription, CvHaarClassifierCascade>  entry: entrySet) {

            final CvHaarClassifierCascade haarCascade = entry.getValue();
            final String name = entry.getKey().name;

            final CvMemStorage storage = CvMemStorage.create();
            final CvSeq sign = cvHaarDetectObjects(
                    image,
                    haarCascade,
                    storage,
                    1.5,
                    3,
                    CV_HAAR_DO_CANNY_PRUNING);

            cvClearMemStorage(storage);

            // Loop through all the hits found by this detector.
            int total_Faces = sign.total();
            for (int i = 0; i < total_Faces; i++) {
                final CvRect r = new CvRect(cvGetSeqElem(sign, i));

                // Remember this hit so that we can paint it later.
                labels.add(new Label(r, name));
            }
        }

        // Paint all the hits.
        for (final Label l: labels) {
            l.imprint(image);
        }
    }


    /// Here is an example that detects Lena's. Make an unit test for that.
    //  (that will be fun, making the thing run on osx too :-)
    // http://opencvlover.blogspot.no/2012/11/face-detection-in-javacv-using-haar.html
    // http://www.mon-club-elec.fr/mes_docs/my_javacv_javadoc/com/googlecode/javacv/cpp/opencv_objdetect.CascadeClassifier.html
    // XXX From http://stackoverflow.com/questions/14407644/javacv-detectmultiscale-with-lbp-haarCascade-does-not-work-on-physical-device
    private  CvRect runDetectors(final opencv_core.IplImage image) {

        final CvRect detectedFaces = new CvRect(null);

        synchronized (faceDetector) {  // XXX Bad choice, shouldn't be bottleneck.
            faceDetector.detectMultiScale(
                    image,
                    detectedFaces, // A vector of detected faces?
                    1.1, // Scalefactor
                    2, // minNeigbours, 3??
                    0, // CV_HAAR_FIND_BIGGEST_OBJECT | CV_HAAR_DO_ROUGH_SEARCH, // flags
                    new CvSize(), // minsize
                    new CvSize(image.width(), image.height())); // maxsizw

            // detectedFaces.
            log.info(String.format("Detected %s faces", detectedFaces.limit()));
            // Draw a bounding box around each face.
            //for (CvRect rect : faceDetections.toArray()) {
            final CvRect rect = detectedFaces;

            while (rect.position() < rect.limit()) {
                opencv_core.cvRectangle(image,
                        new CvPoint(rect.x(), rect.y()),
                        new CvPoint(rect.x() + rect.width(),
                        rect.y() + rect.height()),
                        CvScalar.RED,
                        1, 1, 1);
                rect.position(rect.position() + 1); // ??
            }

            return detectedFaces;
        }
    }
}

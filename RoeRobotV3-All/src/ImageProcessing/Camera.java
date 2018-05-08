package ImageProcessing;

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import static org.opencv.videoio.Videoio.CAP_PROP_FRAME_HEIGHT;
import static org.opencv.videoio.Videoio.CAP_PROP_FRAME_WIDTH;

/**
 * A class handling handle camera activity, open camera, take picture
 *
 * @author Kristian Andre Lilleindset
 * @version 13-02-2018
 */

public class Camera 
{
    // Which connected camera to open
    private final int camToOpen = 0;
    
    // Picture frame
    private final Mat frame;
    
    // Timestamp of last image
    private long timestamp;
    
    // Camera
    private final VideoCapture cam;
    
    // Cameras field of view (angle)
    private final int FOV = 78;
    
    
    public Camera() 
    {
        boolean found = false;
        // Open a camerasource
        this.cam = new VideoCapture(camToOpen);
        System.out.println("Camera øpnat");
        
        // create a Mat frame variable
        this.frame = new Mat();
        
        // set the desired width and height of the pictures taken
        this.cam.set(CAP_PROP_FRAME_WIDTH, 1920);
        this.cam.set(CAP_PROP_FRAME_HEIGHT, 1080);
    }
   

    
    /**
     * Take a picture and return the Mat frame.
     * 
     * @param cameraHeight distance between lens and surface 
     * @return RoeImage with picture and properties
     */
    public RoeImage takePicture(float cameraHeight) 
    {
        System.out.println("skal ta bilde");
        // return variable
        RoeImage result = new RoeImage(cameraHeight, this.FOV);
        
        // take picture 
        this.cam.read(this.frame);
        System.out.println("bilde tatt");
        // update timestamp for image capturing
        this.timestamp = System.currentTimeMillis();
        
        // add picture to result
        result.SetImage(this.frame);
        
        // add timestamp of captured image
        result.setTimeStamp(this.timestamp);
        
        OpenCVWindow orgImage = new OpenCVWindow(1,1,512,512);
       Mat origImage = frame.clone();
       //orgImage.showImage(origImage);
            
       Image image = Mat2BufferedImage(frame);
       displayImage(image);
        
        
        
        
        // the image captured with properties
        return result;
    }
    
    
    public static BufferedImage Mat2BufferedImage(Mat m)
    {
// source: http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
// Fastest code
// The output can be assigned either to a BufferedImage or to an Image

int type = BufferedImage.TYPE_BYTE_GRAY;
if ( m.channels() > 1 ) 
{
    type = BufferedImage.TYPE_3BYTE_BGR;
}
int bufferSize = m.channels()*m.cols()*m.rows();
byte [] b = new byte[bufferSize];
m.get(0,0,b); // get all the pixels
BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
System.arraycopy(b, 0, targetPixels, 0, b.length);  
return image;
}
    
public static void displayImage(Image img2)
{   
//BufferedImage img=ImageIO.read(new File("/HelloOpenCV/lena.png"));
ImageIcon icon=new ImageIcon(img2);
JFrame frame=new JFrame();
frame.setLayout(new FlowLayout());        
frame.setSize(img2.getWidth(null)+50, img2.getHeight(null)+50);     
JLabel lbl=new JLabel();
lbl.setIcon(icon);
frame.add(lbl);
frame.setVisible(true);
frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
}


}
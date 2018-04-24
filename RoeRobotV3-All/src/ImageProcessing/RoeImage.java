
package ImageProcessing;

import roerobotyngve.Coordinate;
import java.util.ArrayList;
import java.util.Iterator;
import org.opencv.core.Mat;


/**
 * Class represents image with the following properties:
 * - timestamp
 * - index of image in tray
 * - 
 *
 * @author KristianAndreLilleindset
 * @version 17-04-2018
 */
public class RoeImage 
{
    // image as Mat file
    private Mat image;
    
    // timestamp of image 
    private long timestamp;
    
    // frame index in tray
    private int pictureIndex;
    
    // variable holding height of camera above surface in mm  
    private final float captureHeight;
    
    //variable holding the Field Of View from the camera (angle)
    private final int FOV;
    
    // list holding position of roe detected in the image
    private final ArrayList<Coordinate> roePositionPixels;
    
    // list holding position of roe detecten in image in millimeters
    private final ArrayList<Coordinate> roePositionMillimeters;
    
    /**
     * 
     * @param captureHeight distance between lens and surface captured in mm
     * @param fieldOfView field of view of camera (angle)
     */
    public RoeImage(float captureHeight, int fieldOfView)
    {
        // save the camera to surface distance 
        this.captureHeight = captureHeight;
        
        // save the cameras FOV
        this.FOV = fieldOfView;
        
        // create list of roe positions
        roePositionPixels = new ArrayList();
        roePositionMillimeters = new ArrayList();
    }
    
    
    /**
     * Set image
     * 
     * @param image added to RoeImage
     */
    public void SetImage(Mat image)
    {
        this.image = image;
    }
    
    
    /**
     * Get image
     * 
     * @return Mat frame image
     */
    public Mat getImage()
    {
        return image;
    }
    
    
    /**
     * Set timestamp of the image captured
     * 
     * @param timestamp
     */
    public void setTimeStamp(long timestamp)
    {
        this.timestamp = timestamp;
    }
    
    
    /**
     * Get timestamp of image
     * 
     * @return timestamp of image
     */
    public long getTimestamp()
    {
        return this.timestamp;
    }
    
    
    /**
     * Add position of roe in pixels.
     * 
     * @param coordinate of roe in pixels 
     */
    public void addRoePositionPixel(Coordinate coordinate)
    {
        this.roePositionPixels.add(coordinate);
        System.out.println("pixel: " + coordinate.getxCoord() + " - " + coordinate.getyCoord());
    }
            
    
    /**
     * Get iterator from list of roe postitions in pixels.
     * 
     * @return iterator from list of roe positions in pixels. 
     */
    public Iterator getRoePositionPixelIterator()
    {
        return this.roePositionPixels.iterator();
    }
    
    
    /**
     * Add position of roe in millimeter.
     * 
     * @param coordinate of roe in millimeter
     */
    public void addRoePositionMillimeter(Coordinate coordinate)
    {
        this.roePositionMillimeters.add(coordinate);
        System.out.println("millimeter: " + coordinate.getxCoord() + " - " + coordinate.getyCoord());
    }
            
    
    /**
     * Get iterator from list of roe postitions in millimeter.
     * 
     * @return iterator from list of roe positions in millimeter. 
     */
    public Iterator getRoePositionMillimeterIterator()
    {
        return this.roePositionMillimeters.iterator();
    }
    
    
    /**
     * Set picture index in coordinate system
     * 
     * @param pictureIndex of picture
     */
    public void setPictureIndex(int pictureIndex)
    {
        this.pictureIndex = pictureIndex;
    }
    
    
    /**
     * Get index of picture in coordinatesystem
     * 
     * @return pictureIndex in coordinatesystem
     */
    public int getPictureIndex()
    {
        return this.pictureIndex;
    }
    
    /**
     * Get the cameras field of view.
     * 
     * @return returns field of view of camera (angle)
     */
    public int getFieldOfView()
    {
        return this.FOV;
    }
    
    /**
     * Get the distance between camera and surface in mm
     * 
     * @return distance between camera and surface in mm
     */
    public float getDistance()
    {
        return this.captureHeight;
    }
}

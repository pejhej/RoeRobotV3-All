/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessing;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

/**
 *
 * @author KristianAndreLilleindset
 */
public class testing implements ImageProcessingListener
{
    public static void main(String[] args) 
    { 
       // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.load("/home/odroid/NetBeansProjects/RoeRobotV3-All/RoeRobotV3-All/lib/opencv-package-xu4/libopencv_java310.so");
        testing tester = new testing();
        tester.test();
        
        
    }
    
    
    public void test()
    {
        ImageProcessing ip = new ImageProcessing();
        Mat img = Imgcodecs.imread("RoeRobotV3-All/lib/1.jpg");
        RoeImage im = new RoeImage();
        im.SetImage(img);
        
        ip.addListener(this);
        ip.addImageToProcessingQueue(im);
        ip.run();
        
    }   

    @Override
    public void notifyImageProcessed(RoeImage processedImage) {
       System.out.println("funka");
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessing;


import com.pi4j.platform.PlatformAlreadyAssignedException;
import roerobotv3.all.MegaMasterClass;

/**
 *
 * @author PerEspen
 */
public class ImageTest
{
      public static void main(String[] args){
        
        //Load the open cv
        System.load("/home/odroid/NetBeansProjects/RoeRobotV3-All/RoeRobotV3-All/lib/opencv-package-xu4/libopencv_java310.so");
        
       ImageTest test = new ImageTest();
               test.test();

    }
      
      
      public void test()
      {
          Camera cam = new Camera();
          cam.takePicture(80,0);
      }
    
}
